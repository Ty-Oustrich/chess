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
        Set<WsContext> gameSessions = sessionsByGame.computeIfAbsent(gameID, ignored -> ConcurrentHashMap.newKeySet());
        gameSessions.add(session);
    }





    public void broadcastToGame(Integer gameID, ServerMessage message) {
        Set<WsContext> gameSessions = sessionsByGame.get(gameID);
        if (gameSessions == null || gameSessions.isEmpty()) {
            return;
        }

        Set<WsContext> staleSessions = ConcurrentHashMap.newKeySet();
        for (WsContext session : gameSessions) {
            if (!sendToSession(session, message)) {
                staleSessions.add(session);
            }
        }

        for (WsContext staleSession : staleSessions) {
            removeSession(gameID, staleSession);
        }
    }


    public void broadcastToGameExcept(Integer gameID, WsContext excludedSession, ServerMessage message) {
        Set<WsContext> gameSessions = sessionsByGame.get(gameID);
        if (gameSessions == null || gameSessions.isEmpty()) {
            return;
        }

        Set<WsContext> staleSessions = ConcurrentHashMap.newKeySet();
        String excludedSessionId = excludedSession == null ? null : excludedSession.sessionId();
        for (WsContext session : gameSessions) {
            if (Objects.equals(session.sessionId(), excludedSessionId)) {
                continue;
            }
            if (!sendToSession(session, message)) {
                staleSessions.add(session);
            }
        }

        for (WsContext staleSession : staleSessions) {
            removeSession(gameID, staleSession);
        }
    }


    
    public boolean sendToSession(WsContext session, ServerMessage message) {
        String messageJson = gson.toJson(message);
        try {
            session.send(messageJson);
            return true;
        } catch (Exception sendException) {
            return false;
        }
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
