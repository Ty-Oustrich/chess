package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;


public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> authTokens = new HashMap<>();

    public UserData getUser(String username){
        return users.get(username);
    }
    
    public AuthData createAuth(AuthData auth) {
        authTokens.put(auth.authToken(), auth);
        return auth;
    }

    public void clear() {
        users.clear();
        authTokens.clear();
    }

    public UserData createUser(UserData user) {
        users.put(user.username(), user);
        return user;
    }

    public GameData createGame(GameData game) { return null; }
    public GameData getGame(int gameID) { return null; }
    public Collection<GameData> listGames() { return null; }
    public void updateGame(GameData game) {}
    public AuthData getAuth(String authToken) { return authTokens.get(authToken); }
    public void deleteAuth(String authToken) { authTokens.remove(authToken); }
}

//hashmaps and collections here