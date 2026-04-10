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

  
    public static Session connect(Object endpoint, String host, int port) {
        try {
            String webSocketUrl = "ws://" + host + ":" + port + "/ws";
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            return container.connectToServer(endpoint, URI.create(webSocketUrl));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("unable to connect websocket client", e);
        } catch (DeploymentException e) {
            throw new RuntimeException("unable to connect websocket client", e);
        } catch (IOException e) {
            throw new RuntimeException("unable to connect websocket client", e);
        }
    }

 
    public static void sendText(Session session, String payload) {
        try {
            session.getBasicRemote().sendText(payload);
        } catch (IOException e) {
            throw new RuntimeException("unable to send websocket command", e);
        }
    }
}
