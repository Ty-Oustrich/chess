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

import java.net.http.WebSocket;
import java.util.Queue;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentLinkedQueue;

public class WebSocketFacade implements WebSocket.Listener {

    private final Gson gson = new Gson();
    private GameHandler gameHandler;
    private WebSocket webSocket;

    private final Queue<String> incomingMessages = new ConcurrentLinkedQueue<>();
    private final StringBuilder messageBuffer = new StringBuilder();

    public WebSocketFacade(String host, int port, GameHandler gameHandler) {
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host is required");
        }
        this.gameHandler = gameHandler;
        this.webSocket = WebSocketConnection.connect(this, host, port);
    }

    public void setGameHandler(GameHandler gameHandler) {
        if (gameHandler == null) {
            throw new IllegalArgumentException("gameHandler is required");
        }
        this.gameHandler = gameHandler;
    }

    @Override
    public CompletionStage<?> onText(WebSocket ws, CharSequence data, boolean last) {
        messageBuffer.append(data);
        ws.request(1);

        if (last) {
            incomingMessages.add(messageBuffer.toString());
            messageBuffer.setLength(0);
        }

        return null;
    }

    @Override
    public CompletionStage<?> onClose(WebSocket ws, int statusCode, String reason) {
        String closeText = "websocket closed (" + statusCode + "): " + reason;
        incomingMessages.add(gson.toJson(new ErrorMessage(closeText)));
        return null;
    }

    @Override
    public void onError(WebSocket ws, Throwable error) {
        String errorMessage = error == null ? "unknown websocket error" : error.getMessage();
        incomingMessages.add(gson.toJson(new ErrorMessage("websocket error: " + errorMessage)));
    }

    public void processQueuedMessages() {
        GameHandler currentGameHandler = gameHandler;
        if (currentGameHandler == null) {
            return;
        }

        String messageJson = incomingMessages.poll();
        while (messageJson != null) {
            handleMessage(messageJson, currentGameHandler);
            messageJson = incomingMessages.poll();
        }
    }

    private void handleMessage(String messageJson, GameHandler currentGameHandler) {
        ServerMessage.ServerMessageType messageType = parseMessageType(messageJson, currentGameHandler);
        if (messageType == null) {
            return;
        }
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
        if (webSocket == null) {
            throw new RuntimeException("websocket is not connected");
        }
        WebSocketConnection.sendText(webSocket, gson.toJson(command));
    }
}
