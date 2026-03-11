package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.AuthData;
import model.GameData;
import model.UserData;
import util.GsonFactory;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;

public class MySqlDataAccess implements DataAccess {
    private final Gson gson = GsonFactory.create();

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = """
            SELECT game_id, white_username, black_username, game_name, game_state_json
            FROM games
            WHERE game_id = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int gameIdParameterIndex = 1;
            preparedStatement.setInt(gameIdParameterIndex, gameID);

            ResultSet resultSet = preparedStatement.executeQuery();
            boolean isMissingRow = !resultSet.next();
            if (isMissingRow) {
                return null;
            }

            int storedGameId = resultSet.getInt("game_id");
            String whiteUsername = resultSet.getString("white_username");
            String blackUsername = resultSet.getString("black_username");
            String gameName = resultSet.getString("game_name");
            String gameStateJson = resultSet.getString("game_state_json");

            ChessGame deserializedGame;
            try {
                deserializedGame = gson.fromJson(gameStateJson, ChessGame.class);
            } catch (JsonSyntaxException exception) {
                String message = "corrupt game_state_json for game_id=" + storedGameId;
                throw new DataAccessException(message, exception);
            }

            GameData gameData = new GameData(
                    storedGameId,
                    whiteUsername,
                    blackUsername,
                    gameName,
                    deserializedGame
            );

            return gameData;
        } catch (Exception exception) {
            String message = "failed to get game from database";
            throw new DataAccessException(message, exception);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String message = "clear is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public UserData createUser(UserData user) throws DataAccessException {
        String message = "createUser is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        String message = "getUser is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public GameData createGame(GameData game) throws DataAccessException {
        String message = "createGame is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String message = "listGames is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String message = "updateGame is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        String message = "createAuth is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String message = "getAuth is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String message = "deleteAuth is not implemented for MySqlDataAccess";
        throw new DataAccessException(message);
    }
}
