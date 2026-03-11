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
import java.util.ArrayList;
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

            GameData gameData = readGameFromResultSet(resultSet);
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
        String sql = """
            INSERT INTO games (game_id, white_username, black_username, game_name, game_state_json)
            VALUES (?, ?, ?, ?, ?)
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int gameId = game.gameID();
            String whiteUsername = game.whiteUsername();
            String blackUsername = game.blackUsername();
            String gameName = game.gameName();
            ChessGame chessGame = game.game();
            String gameStateJson = gson.toJson(chessGame, ChessGame.class);

            int gameIdParameterIndex = 1;
            int whiteUsernameParameterIndex = 2;
            int blackUsernameParameterIndex = 3;
            int gameNameParameterIndex = 4;
            int gameStateJsonParameterIndex = 5;

            preparedStatement.setInt(gameIdParameterIndex, gameId);
            preparedStatement.setString(whiteUsernameParameterIndex, whiteUsername);
            preparedStatement.setString(blackUsernameParameterIndex, blackUsername);
            preparedStatement.setString(gameNameParameterIndex, gameName);
            preparedStatement.setString(gameStateJsonParameterIndex, gameStateJson);

            int rowsUpdated = preparedStatement.executeUpdate();
            boolean isMissingInsertedRow = rowsUpdated != 1;
            if (isMissingInsertedRow) {
                String message = "failed to insert game into database";
                throw new DataAccessException(message);
            }

            return game;
        } catch (Exception exception) {
            String message = "failed to create game in database";
            throw new DataAccessException(message, exception);
        }
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        String sql = """
            SELECT game_id, white_username, black_username, game_name, game_state_json
            FROM games
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();
            Collection<GameData> games = new ArrayList<>();

            while (resultSet.next()) {
                GameData gameData = readGameFromResultSet(resultSet);
                games.add(gameData);
            }

            return games;
        } catch (Exception exception) {
            String message = "failed to list games from database";
            throw new DataAccessException(message, exception);
        }
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
        String sql = """
            UPDATE games
            SET white_username = ?, black_username = ?, game_name = ?, game_state_json = ?
            WHERE game_id = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int gameId = game.gameID();
            String whiteUsername = game.whiteUsername();
            String blackUsername = game.blackUsername();
            String gameName = game.gameName();
            ChessGame chessGame = game.game();
            String gameStateJson = gson.toJson(chessGame, ChessGame.class);

            int whiteUsernameParameterIndex = 1;
            int blackUsernameParameterIndex = 2;
            int gameNameParameterIndex = 3;
            int gameStateJsonParameterIndex = 4;
            int gameIdParameterIndex = 5;

            preparedStatement.setString(whiteUsernameParameterIndex, whiteUsername);
            preparedStatement.setString(blackUsernameParameterIndex, blackUsername);
            preparedStatement.setString(gameNameParameterIndex, gameName);
            preparedStatement.setString(gameStateJsonParameterIndex, gameStateJson);
            preparedStatement.setInt(gameIdParameterIndex, gameId);

            int rowsUpdated = preparedStatement.executeUpdate();
            boolean isMissingUpdatedRow = rowsUpdated != 1;
            if (isMissingUpdatedRow) {
                String message = "failed to update game in database";
                throw new DataAccessException(message);
            }
        } catch (Exception exception) {
            String message = "failed to update game in database";
            throw new DataAccessException(message, exception);
        }
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

    private GameData readGameFromResultSet(ResultSet resultSet) throws DataAccessException {
        try {
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

            return new GameData(
                    storedGameId,
                    whiteUsername,
                    blackUsername,
                    gameName,
                    deserializedGame
            );
        } catch (Exception exception) {
            String message = "failed to read game from result set";
            throw new DataAccessException(message, exception);
        }
    }
}
