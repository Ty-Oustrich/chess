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

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@WebSocket
public class WebSocketFacade {

    private final Gson gson = new Gson();
    private GameHandler gameHandler;
    private Session session;
    private final Queue<String> incomingMessages = new ConcurrentLinkedQueue<>();


    public WebSocketFacade(String host, int port, GameHandler gameHandler) {
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host is required");

        this.gameHandler = gameHandler;
        this.session = WebSocketConnection.connect(this, host, port);
    }

    public void setGameHandler(GameHandler gameHandler) {
        if (gameHandler == null) throw new IllegalArgumentException("gameHandler is required");
        this.gameHandler = gameHandler;
    }

    @OnWebSocketMessage
    public void onMessage(String messageJson) {
        incomingMessages.add(messageJson);
    }

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        String closeText = "websocket closed (" + statusCode + "): " + reason;
        incomingMessages.add(gson.toJson(new ErrorMessage(closeText)));
    }

    @OnWebSocketError
    public void onError(Throwable exception) {
        String errorMessage = exception == null ? "unknown websocket error" : exception.getMessage();
        incomingMessages.add(gson.toJson(new ErrorMessage("websocket error: " + errorMessage)));
    }

    public void processQueuedMessages() {
        GameHandler currentGameHandler = gameHandler;
        if (currentGameHandler == null) return;

        String messageJson = incomingMessages.poll();
        while (messageJson != null) {
            handleMessage(messageJson, currentGameHandler);
            messageJson = incomingMessages.poll();
        }
    }

    private void handleMessage(String messageJson, GameHandler currentGameHandler) {
        ServerMessage.ServerMessageType messageType = parseMessageType(messageJson, currentGameHandler);
        if (messageType == null) return;

        dispatchMessage(messageType, messageJson, currentGameHandler);
    }

    private ServerMessage.ServerMessageType parseMessageType(String messageJson, GameHandler currentGameHandler) {
        try {
            ServerMessage baseMessage = gson.fromJson(messageJson, ServerMessage.class);
            if (baseMessage == null || baseMessage.getServerMessageType() == null) {
                currentGameHandler.onError(new ErrorMessage("error reading websocket message: missing serverMessageType"));
                return null;
            }
            return baseMessage.getServerMessageType();
        } catch (JsonParseException exception) {
            currentGameHandler.onError(new ErrorMessage("error reading websocket message: " + exception.getMessage()));
            return null;
        }
    }

    private void dispatchMessage(ServerMessage.ServerMessageType messageType, String messageJson, GameHandler currentGameHandler) {
        switch (messageType) {
            case LOAD_GAME -> currentGameHandler.onLoadGame(gson.fromJson(messageJson, LoadGameMessage.class));
            case ERROR -> currentGameHandler.onError(gson.fromJson(messageJson, ErrorMessage.class));
            case NOTIFICATION -> currentGameHandler.onNotification(gson.fromJson(messageJson, NotificationMessage.class));
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
        if (session == null || !session.isOpen()) {
            throw new RuntimeException("websocket is not connected");
        }
        String commandJson = gson.toJson(command);
        WebSocketConnection.sendText(session, commandJson);
    }
}
