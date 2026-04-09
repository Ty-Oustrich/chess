package websocket;


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


    // websocket callbacks for connect, message, and close events.
    public void register(WsConfig ws){
        ws.onConnect(ctx -> this.handleConnectEvent(ctx));
        ws.onMessage(msgCtx -> this.handleMessage(msgCtx));
        ws.onClose(ctx -> this.handleCloseEvent(ctx));
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

        connectionManager.broadcastToGame(gameData.gameID(),
                new NotificationMessage(authData.username() + " made a move"));
    }


    private void handleLeave(WsContext context, UserGameCommand command) throws DataAccessException {
        AuthData authData = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());

        connectionManager.removeSession(gameData.gameID(), context);
        connectionManager.broadcastToGame(gameData.gameID(),
                new NotificationMessage(authData.username() + " left the game."));
    }


    private void handleResign(WsContext context, UserGameCommand command) throws DataAccessException {
        AuthData authData = requireAuth(command.getAuthToken());
        GameData gameData = requireGame(command.getGameID());

        connectionManager.broadcastToGame(gameData.gameID(),
            new NotificationMessage(authData.username() + " resigned from the game :-{"));
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
