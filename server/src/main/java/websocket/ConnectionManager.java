package websocket;


import websocket.messages.ServerMessage;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;
import com.google.gson.Gson;
import io.javalin.websocket.WsContext;
import util.GsonFactory;


public class ConnectionManager {

    private final Map<Integer, Set<WsContext>> sessionsByGame = new ConcurrentHashMap<>();
    private final Gson gson = GsonFactory.create();


    public void addSession(Integer gameID, WsContext session) {
        Set<WsContext> gameSessions = sessionsByGame.get(gameID);
        if (gameSessions == null) {
            gameSessions = ConcurrentHashMap.newKeySet();
            sessionsByGame.put(gameID, gameSessions);
        }
        gameSessions.add(session);
    }




//can exclude the sender later? idk whats needed yet
    public void broadcastToGame(Integer gameID, ServerMessage message) {
        Set<WsContext> gameSessions = sessionsByGame.get(gameID);
        if (gameSessions == null || gameSessions.isEmpty()) {
            return;
        }

        for (WsContext session : gameSessions) {
            sendToSession(session, message);
        }
    }

    public void broadcastToGameExcept(Integer gameID, WsContext excludedSession, ServerMessage message) {
        Set<WsContext> gameSessions = sessionsByGame.get(gameID);
        if (gameSessions == null || gameSessions.isEmpty()) {
            return;
        }

        String excludedSessionId = excludedSession == null ? null : excludedSession.sessionId();
        for (WsContext session : gameSessions) {
            if (Objects.equals(session.sessionId(), excludedSessionId)) {
                continue;
            }
            sendToSession(session, message);
        }
    }


    public void sendToSession(WsContext session, ServerMessage message) {
        String messageJson = gson.toJson(message);
        session.send(messageJson);
    }

    public void removeSession(Integer gameID, WsContext session) {
        Set<WsContext> gameSessions = sessionsByGame.get(gameID);
        if (gameSessions == null) {
            return;
        }

        gameSessions.remove(session);
        if (gameSessions.isEmpty()) {
            sessionsByGame.remove(gameID);
        }
    }
}
