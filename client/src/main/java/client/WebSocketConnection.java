package client;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

public final class WebSocketConnection {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private WebSocketConnection() {}

    public static WebSocket connect(WebSocket.Listener listener, String host, int port) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is required");
        }
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("host is required");
        }
        if (port <= 0) {
            throw new IllegalArgumentException("port must be positive");
        }

        try {
            URI uri = URI.create("ws://" + host + ":" + port + "/ws");
            return HTTP_CLIENT.newWebSocketBuilder().buildAsync(uri, listener).get();
        } catch (Exception exception) {
            throw new RuntimeException("unable to connect websocket client", exception);
        }
    }

    public static void sendText(WebSocket webSocket, String payload) {
        if (webSocket == null || payload == null) {
            throw new IllegalArgumentException("webSocket and payload are required");
        }

        try {
            webSocket.sendText(payload, true).get();
        } catch (Exception exception) {
            throw new RuntimeException("unable to send websocket command", exception);
        }
    }
}
