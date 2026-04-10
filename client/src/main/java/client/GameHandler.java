package client;

import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ErrorMessage;


public interface GameHandler {
    void onLoadGame(LoadGameMessage message);

    void onError(ErrorMessage message);

    void onNotification(NotificationMessage message);
}
