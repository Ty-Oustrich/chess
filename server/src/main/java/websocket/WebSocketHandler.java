package websocket;


import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsConfig;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsContext;
import io.javalin.websocket.WsErrorContext;
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
        wsConfig.onError(this::handleErrorEvent);
    }

    private void handleConnectEvent(WsContext context) {
        //needed?
    }

    private void handleCloseEvent(WsCloseContext context) {
        System.out.println("ws close: status=" + context.status() + " reason=" + context.reason());
        Integer gameID = context.attribute("gameID");
        if (gameID != null) {
            connectionManager.removeSession(gameID, context);
        }
    }

    private void handleErrorEvent(WsErrorContext context) {
        Throwable error = context.error();
        String errorMessage = (error == null) ? "unknown websocket error" : error.getMessage();
        System.out.println("ws error: " + errorMessage);
        if (error != null) {
            error.printStackTrace();
        }
    }


    private void handleMessage(WsMessageContext context) {
        try {
            JsonObject commandJson = gson.fromJson(context.message(), JsonObject.class);
            UserGameCommand command = parseBaseCommand(commandJson);

            switch (command.getCommandType()) {
                case CONNECT -> handleConnect(context, command);
                case MAKE_MOVE -> handleMakeMove(context, parseMoveCommand(commandJson, command));
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


    private UserGameCommand parseBaseCommand(JsonObject commandJson) throws DataAccessException {
        if (commandJson == null || !commandJson.has("commandType")) {
            throw new DataAccessException("error invalid websocket command");
        }

        String rawType = commandJson.get("commandType").getAsString();
        UserGameCommand.CommandType commandType = UserGameCommand.CommandType.valueOf(rawType);

        String authToken = commandJson.has("authToken") ? commandJson.get("authToken").getAsString() : null;
        Integer gameID = commandJson.has("gameID") ? commandJson.get("gameID").getAsInt() : null;
        return new UserGameCommand(commandType, authToken, gameID);
    }

    private MakeMoveCommand parseMoveCommand(JsonObject commandJson, UserGameCommand baseCommand) throws DataAccessException {
        // move is only present on MAKE_MOVE
        if (!commandJson.has("move")) {
            throw new DataAccessException("error invalid move command");
        }

        ChessMove move = gson.fromJson(commandJson.get("move"), ChessMove.class);
        if (move == null) {
            throw new DataAccessException("error invalid move command");
        }

        return new MakeMoveCommand(baseCommand.getAuthToken(), baseCommand.getGameID(), move);
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

        ChessGame.TeamColor playerColor = getPlayerColorForMove(context, username, gameData);
        if (playerColor == null) return;
        if (isMoveRejectedByGameState(context, game, playerColor)) return;

        ChessMove move = command.getMove();
        if (!tryApplyMove(context, game, move)) return;

        GameData updatedGameData = rebuildGameData(gameData, game);
        dataAccess.updateGame(updatedGameData);

        broadcastMoveMessages(gameData.gameID(), context, username, move, game);
        handlePostMoveGameState(gameData.gameID(), game, playerColor, updatedGameData, username);
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

    private ChessGame.TeamColor getPlayerColorForMove(WsContext context, String username, GameData gameData) {
        boolean isWhite = Objects.equals(username, gameData.whiteUsername());
        boolean isBlack = Objects.equals(username, gameData.blackUsername());
        if (!isWhite && !isBlack) {
            sendError(context, "error observers cannot make moves");
            return null;
        }
        return isWhite ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;
    }

    private boolean isMoveRejectedByGameState(WsContext context, ChessGame game, ChessGame.TeamColor playerColor) {
        if (game.isOver()) {
            sendError(context, "error game is already over");
            return true;
        }

        if (game.getTeamTurn() != playerColor) {
            sendError(context, "error not your turn");
            return true;
        }
        return false;
    }

    private boolean tryApplyMove(WsContext context, ChessGame game, ChessMove move) {
        try {
            game.makeMove(move);
            return true;
        } catch (InvalidMoveException exception) {
            sendError(context, "error: " + exception.getMessage());
            return false;
        }
    }

    private GameData rebuildGameData(GameData gameData, ChessGame game) {
        return new GameData(
                gameData.gameID(),
                gameData.whiteUsername(),
                gameData.blackUsername(),
                gameData.gameName(),
                game
        );
    }

    private void broadcastMoveMessages(Integer gameID, WsContext sender, String username, ChessMove move, ChessGame game) {
        connectionManager.broadcastToGame(gameID, new LoadGameMessage(game));

        String from = posToAlgebraic(move.getStartPosition());
        String to = posToAlgebraic(move.getEndPosition());
        String moveMessage = username + " moved " + from + " to " + to;
        connectionManager.broadcastToGameExcept(gameID, sender, new NotificationMessage(moveMessage));
    }

    private void handlePostMoveGameState(Integer gameID, ChessGame game, ChessGame.TeamColor playerColor,
                                         GameData updatedGameData, String username) throws DataAccessException {
        ChessGame.TeamColor opponent = (playerColor == ChessGame.TeamColor.WHITE)
                ? ChessGame.TeamColor.BLACK : ChessGame.TeamColor.WHITE;

        if (game.isInCheckmate(opponent)) {
            game.setOver(true);
            dataAccess.updateGame(updatedGameData);
            connectionManager.broadcastToGame(gameID, new NotificationMessage(username + " wins by checkmate!"));
            return;
        }

        if (game.isInStalemate(opponent)) {
            game.setOver(true);
            dataAccess.updateGame(updatedGameData);
            connectionManager.broadcastToGame(gameID, new NotificationMessage("Stalemate... the game ends in a draw."));
            return;
        }

        if (game.isInCheck(opponent))
            connectionManager.broadcastToGame(gameID, new NotificationMessage("Check!"));
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
