package client;

import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;

public final class WebSocketConnection {

    private WebSocketConnection() {
    }

    public static Session connect(WebSocketFacade facade, String host, int port) {
        if (facade == null) throw new IllegalArgumentException("facade is required");
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host is required");
        if (port <= 0) throw new IllegalArgumentException("port must be positive");

        try {
            String webSocketUrl = buildWebSocketUrl(host, port);
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            return container.connectToServer(facade, URI.create(webSocketUrl));
        } catch (DeploymentException | IOException | RuntimeException exception) {
            throw new RuntimeException("unable to connect websocket client", exception);
        }
    }

    private static String buildWebSocketUrl(String host, int port) {
        return "ws://" + host + ":" + port + "/ws";
    }

    public static void sendText(Session session, String payload) {
        if (session == null || payload == null) {
            throw new IllegalArgumentException("session and payload are required");
        }

        try {
            session.getBasicRemote().sendText(payload);
        } catch (IOException exception) {
            throw new RuntimeException("unable to send websocket command", exception);
        }
    }
}
