package client;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.client.WebSocketClient;

import java.net.URI;

public final class WebSocketConnection {

    private WebSocketConnection() {
    }

    public static Session connect(WebSocketFacade facade, String host, int port) {
        if (facade == null) throw new IllegalArgumentException("facade is required");
        if (host == null || host.isBlank()) throw new IllegalArgumentException("host is required");
        if (port <= 0) throw new IllegalArgumentException("port must be positive");

        try {
            String url = "ws://" + host + ":" + port + "/ws";
            WebSocketClient client = new WebSocketClient();
            client.start();
            Session session = client.connect(facade, URI.create(url)).get();
            session.setIdleTimeout(java.time.Duration.ZERO);
            return session;
        } catch (Exception exception) {
            throw new RuntimeException("unable to connect websocket client", exception);
        }
    }

    public static void sendText(Session session, String payload) {
        if (session == null || payload == null) {
            throw new IllegalArgumentException("session and payload are required");
        }

        try {
            session.getRemote().sendString(payload);
        } catch (Exception exception) {
            throw new RuntimeException("unable to send websocket command", exception);
        }
    }
}
