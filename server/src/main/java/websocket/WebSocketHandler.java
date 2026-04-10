package websocket;


import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsMessageContext;
import model.AuthData;
import model.GameData;
import util.GsonFactory;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import java.util.Objects;


public class WebSocketHandler {
    private final DataAccess dataAccess;
    private final ConnectionManager connectionManager;
    private final Gson gson;


    public WebSocketHandler(DataAccess dataAccess, ConnectionManager connectionManager) {
        this.dataAccess = dataAccess;
        this.connectionManager = connectionManager;
        this.gson = GsonFactory.create();
    }



    public void register(WsConfig wsConfig) {
        wsConfig.onConnect(this::handleConnectEvent);
        wsConfig.onMessage(this::handleMessage);
        wsConfig.onClose(this::handleCloseEvent);
    }

    private void handleConnectEvent(WsContext context) {
        //implement later
    }

    private void handleCloseEvent(WsContext context) {
        Integer gameID = context.attribute("gameID");
        if (gameID != null) {
            connectionManager.removeSession(gameID, context);
        }
    }


    private void handleMessage(WsMessageContext context) {
        try {
            String commandJson = context.message();
            //deserialize to usergamecommand
            UserGameCommand command = gson.fromJson(commandJson, UserGameCommand.class);

            if (command == null || command.getCommandType() == null) {
                sendError(context, "error invalid websocket command-context or field");
                return;
            }

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(context, command);
                case MAKE_MOVE -> {
                    MakeMoveCommand makeMoveCommand = gson.fromJson(commandJson, MakeMoveCommand.class);
                    handleMakeMove(context, makeMoveCommand);
                }
                case LEAVE -> handleLeave(context, command);
                case RESIGN -> handleResign(context, command);
                default -> sendError(context, "error unsupported command");
            }
        } catch (DataAccessException exception) {
            sendError(context, exception.getMessage());
        } catch (Exception exception) {
            sendError(context, "error unable to process websocket message");
        }
    }

    private void handleConnect(WsContext context, UserGameCommand command) throws DataAccessException {
        AuthData authData = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());

        context.attribute("gameID", gameData.gameID());
        connectionManager.addSession(gameData.gameID(), context);
        connectionManager.sendToSession(context, new LoadGameMessage(gameData.game()));

        String name = authData.username();
        String joinMessage;
        if (Objects.equals(name, gameData.whiteUsername())) {
            joinMessage = name + " joined as WHITE";
        } else if (Objects.equals(name, gameData.blackUsername())) {
            joinMessage = name + " joined as BLACK";
        } else {
            joinMessage = name + " is observing";
        }
        connectionManager.broadcastToGameExcept(gameData.gameID(), context, new NotificationMessage(joinMessage));
    }

    private void handleMakeMove(WsContext context, MakeMoveCommand command) throws DataAccessException {
        AuthData authData = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());
        ChessGame game = gameData.game();
        String username = authData.username();

        // make sure this person is a player, not just watching
        boolean isWhite = Objects.equals(username, gameData.whiteUsername());
        boolean isBlack = Objects.equals(username, gameData.blackUsername());
        if (!isWhite && !isBlack) {
            sendError(context, "error observers cannot make moves");
            return;
        }

        if (game.isOver()) {
            sendError(context, "error game is already over");
            return;
        }

        // check whose turn it is
        ChessGame.TeamColor playerColor = isWhite ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
        if (game.getTeamTurn() != playerColor) {
            sendError(context, "error not your turn");
            return;
        }

        ChessMove move = command.getMove();
        try {
            game.makeMove(move);
        } catch (InvalidMoveException e) {
            sendError(context, "error: " + e.getMessage());
            return;
        }

        dataAccess.updateGame(gameData);

        // push the new board state
        connectionManager.broadcastToGame(gameData.gameID(), new LoadGameMessage(game));

       
        String from = posToAlgebraic(move.getStartPosition());
        String to = posToAlgebraic(move.getEndPosition());
        connectionManager.broadcastToGameExcept(gameData.gameID(), context,
                new NotificationMessage(username + " moved " + from + " to " + to));

        // isInCheck or game over check
        ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (game.isInCheckmate(opponent)) {
            game.setOver(true);
            dataAccess.updateGame(gameData);
            connectionManager.broadcastToGame(gameData.gameID(),
                    new NotificationMessage(username + " wins by checkmate!"));
        } else if (game.isInStalemate(opponent)) {
            game.setOver(true);
            dataAccess.updateGame(gameData);
            connectionManager.broadcastToGame(gameData.gameID(),
                    new NotificationMessage("Stalemate... the game ends in a draw."));
        } else if (game.isInCheck(opponent)) {
            connectionManager.broadcastToGame(gameData.gameID(), new NotificationMessage("Check!"));
        }
    }

  
    private String posToAlgebraic(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        return "" + col + pos.getRow();
    }


    private void handleLeave(WsContext context, UserGameCommand command) throws DataAccessException {
        AuthData authData = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());
        String username = authData.username();

        // clear spot in game record for players
        if (Objects.equals(username, gameData.whiteUsername())) {
            GameData updated = new GameData(gameData.gameID(), null, gameData.blackUsername(),
                    gameData.gameName(), gameData.game());
            dataAccess.updateGame(updated);
        } else if (Objects.equals(username, gameData.blackUsername())) {
            GameData updated = new GameData(gameData.gameID(), gameData.whiteUsername(), null,
                    gameData.gameName(), gameData.game());
            dataAccess.updateGame(updated);
        }

        connectionManager.removeSession(gameData.gameID(), context);
        connectionManager.broadcastToGame(gameData.gameID(),
                new NotificationMessage(username + " left the game"));
    }


    private void handleResign(WsContext context, UserGameCommand command) throws DataAccessException {
        AuthData authData = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());
        String username = authData.username();

        // only actual players can resign
        boolean isPlayer = Objects.equals(username, gameData.whiteUsername())
                || Objects.equals(username, gameData.blackUsername());
        if (!isPlayer) {
            sendError(context, "error: observers cannot resign");
            return;
        }

        if (gameData.game().isOver()) {
            sendError(context, "error: game is already over");
            return;
        }

        gameData.game().setOver(true);
        dataAccess.updateGame(gameData);


        connectionManager.broadcastToGame(gameData.gameID(),
                new NotificationMessage(username + " resigned from the game :-{"));
    }


    private AuthData requireAuth(String token) throws DataAccessException{
        if(token == null || token.isBlank()){
            throw new DataAccessException("error unathaurized");
        }

        AuthData authData = dataAccess.getAuth(token);
        if(authData == null){
            throw new DataAccessException("error unathaurized");
        }

        return authData;
    }


    private GameData requireGame(Integer gameID) throws DataAccessException {
        if (gameID == null) {
            throw new DataAccessException("error: bad request");
        }

        GameData gameData = dataAccess.getGame(gameID);
        if (gameData == null) {
            throw new DataAccessException("error: bad request");
        }

        return gameData;
    }


    private void sendError(WsContext context, String errorMessage) {
        connectionManager.sendToSession(context, new ErrorMessage(errorMessage));
    }
}
