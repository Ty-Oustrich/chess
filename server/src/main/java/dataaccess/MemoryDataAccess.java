package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;


public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> authTokens = new HashMap<>();
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public UserData getUser(String username) throws DataAccessException {
        String requestedUsername = username;
        UserData storedUser = users.get(requestedUsername);
        return storedUser;
    }
    
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        AuthData authToStore = auth;
        String authToken = authToStore.authToken();
        authTokens.put(authToken, authToStore);
        return authToStore;
    }

    public AuthData getAuth(String authToken) throws DataAccessException {
        String requestedAuthToken = authToken;
        AuthData storedAuth = authTokens.get(requestedAuthToken);
        return storedAuth;
    }

    public void deleteAuth(String authToken) throws DataAccessException {
        String tokenToDelete = authToken;
        authTokens.remove(tokenToDelete);
    }

    public GameData createGame(GameData game) throws DataAccessException {
        GameData gameToStore = game;
        int gameId = gameToStore.gameID();
        games.put(gameId, gameToStore);
        return gameToStore;
    }

    public void clear() throws DataAccessException{
        users.clear();
        authTokens.clear();
        games.clear();
    }

    public UserData createUser(UserData user) throws DataAccessException {
        UserData userToStore = user;
        String username = userToStore.username();
        users.put(username, userToStore);
        return userToStore;
    }

    public GameData getGame(int gameID) throws DataAccessException {
        int requestedGameId = gameID;
        GameData storedGame = games.get(requestedGameId);
        return storedGame;
    }

    public Collection<GameData> listGames() throws DataAccessException {
        Collection<GameData> storedGames = games.values();
        Collection<GameData> gamesCopy = new ArrayList<>(storedGames);
        return gamesCopy;
    }

    public void updateGame(GameData game) throws DataAccessException {
        GameData gameToUpdate = game;
        int gameId = gameToUpdate.gameID();
        boolean hasExistingGame = games.containsKey(gameId);
        if (!hasExistingGame) {
            String message = "failed to update game";
            throw new DataAccessException(message);
        }
        games.put(gameId, gameToUpdate);
    }

}

