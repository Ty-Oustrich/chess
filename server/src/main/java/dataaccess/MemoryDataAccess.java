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
    public GameData createGame(GameData game) throws DataAccessException{
        int thisgame = game.gameID();
        games.put(thisgame, game); //implement already exists check later on.
        return game;
    }

    public void clear() throws DataAccessException{
        users.clear();
        authTokens.clear();
        games.clear();
    }

    public UserData createUser(UserData user) throws DataAccessException{
        users.put(user.username(), user);
        return user;
    }

    public GameData getGame(int gameID) throws DataAccessException { 
        return games.get(gameID); 
    }

    public Collection<GameData> listGames()throws DataAccessException {
        return games.values(); //this should be printable?
    }


    public void updateGame(GameData game) throws DataAccessException{
        int gameId = game.gameID();
        games.put(gameId, game);
    }

}

//hashmaps and collections here