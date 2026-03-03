package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.Collection;
import java.util.HashMap;


public class MemoryDataAccess implements DataAccess {
    final private HashMap<String, UserData> users = new HashMap<>();
    final private HashMap<String, AuthData> authTokens = new HashMap<>();
    final private HashMap<Integer, GameData> games = new HashMap<>();

    public UserData getUser(String username)throws DataAccessException{
        return users.get(username);
    }
    
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        authTokens.put(auth.authToken(), auth);
        return auth;
    }
    public AuthData getAuth(String authToken) throws DataAccessException{
         return authTokens.get(authToken); 
        }

    public void deleteAuth(String authToken)  throws DataAccessException{
        authTokens.remove(authToken); 
    }


    public void clear() {
        users.clear();
        authTokens.clear();
        games.clear();
    }

    public UserData createUser(UserData user) throws DataAccessException{
        users.put(user.username(), user);
        return user;
    }

    public GameData createGame(GameData game) { return null; }
    public GameData getGame(int gameID) { return null; }
    public Collection<GameData> listGames() { return null; }
    public void updateGame(GameData game) {}

}

//hashmaps and collections here