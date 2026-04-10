package client;

import chess.ChessMove;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import websocket.commands.ConnectCommand;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.ResignCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import jakarta.websocket.ClientEndpoint;
import jakarta.websocket.OnMessage;
import jakarta.websocket.Session;

@ClientEndpoint
public class WebSocketFacade {

    private final Gson gson = new Gson();
    private final GameHandler gameHandler;
    private final Session session;


    public WebSocketFacade(String host, int port, GameHandler gameHandler) {
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host is required");
        if (gameHandler == null) throw new IllegalArgumentException("gameHandler is required");

        this.gameHandler = gameHandler;
        this.session = WebSocketConnection.connect(this, host, port);
    }


    @OnMessage
    public void onMessage(String messageJson) {
        try {
            ServerMessage message = gson.fromJson(messageJson, ServerMessage.class);
            if (message == null || message.getServerMessageType() == null) {
                gameHandler.onError(new ErrorMessage("error reading websocket message: missing serverMessageType"));
                return;
            }

            switch (message.getServerMessageType()) {
                case LOAD_GAME -> gameHandler.onLoadGame(gson.fromJson(messageJson, LoadGameMessage.class));
                case ERROR -> gameHandler.onError(gson.fromJson(messageJson, ErrorMessage.class));
                case NOTIFICATION -> gameHandler.onNotification(gson.fromJson(messageJson, NotificationMessage.class));
            }
        } catch (JsonParseException e) {
            gameHandler.onError(new ErrorMessage("error reading websocket message: " + e.getMessage()));
        }
    }


    public void sendConnect(String authToken, Integer gameID) {
        sendCommand(new ConnectCommand(authToken, gameID));
    }


    public void sendMakeMove(String authToken, Integer gameID, ChessMove move) {
        sendCommand(new MakeMoveCommand(authToken, gameID, move));
    }


    public void sendLeave(String authToken, Integer gameID) {
        sendCommand(new LeaveCommand(authToken, gameID));
    }


    public void sendResign(String authToken, Integer gameID) {
        sendCommand(new ResignCommand(authToken, gameID));
    }


    private void sendCommand(UserGameCommand command) {
        if (session == null || !session.isOpen()) throw new RuntimeException("websocket is not connected");
        String commandJson = gson.toJson(command);
        WebSocketConnection.sendText(session, commandJson);
    }
}
