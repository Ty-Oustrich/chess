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
        String deleteAuthSql = """
            DELETE FROM auth
            """;

        String deleteGamesSql = """
            DELETE FROM games
            """;

        String deleteUsersSql = """
            DELETE FROM users
            """;

        try (var connection = DatabaseManager.getConnection()) {
            try (PreparedStatement deleteAuthStatement = connection.prepareStatement(deleteAuthSql)) {
                deleteAuthStatement.executeUpdate();
            }

            try (PreparedStatement deleteGamesStatement = connection.prepareStatement(deleteGamesSql)) {
                deleteGamesStatement.executeUpdate();
            }

            try (PreparedStatement deleteUsersStatement = connection.prepareStatement(deleteUsersSql)) {
                deleteUsersStatement.executeUpdate();
            }
        } catch (Exception exception) {
            String message = "failed to clear database tables";
            throw new DataAccessException(message, exception);
        }
    }

    @Override
    public UserData createUser(UserData user) throws DataAccessException {
        String sql = """
            INSERT INTO users (username, password, email)
            VALUES (?, ?, ?)
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            String username = user.username();
            String password = user.password();
            String email = user.email();
            int usernameParameterIndex = 1;
            int passwordParameterIndex = 2;
            int emailParameterIndex = 3;

            preparedStatement.setString(usernameParameterIndex, username);
            preparedStatement.setString(passwordParameterIndex, password);
            preparedStatement.setString(emailParameterIndex, email);

            int rowsUpdated = preparedStatement.executeUpdate();
            boolean isMissingInsertedRow = rowsUpdated != 1;
            if (isMissingInsertedRow) {
                String message = "failed to insert user into database";
                throw new DataAccessException(message);
            }

            return user;
        } catch (Exception exception) {
            String message = "failed to create user in database";
            throw new DataAccessException(message, exception);
        }
    }


    @Override
    public UserData getUser(String username) throws DataAccessException {
        String sql = """
            SELECT username, password, email
            FROM users
            WHERE username = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int usernameParameterIndex = 1;
            preparedStatement.setString(usernameParameterIndex, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            boolean isMissingRow = !resultSet.next();
            if (isMissingRow) {
                return null;
            }

            String storedUsername = resultSet.getString("username");
            String storedPassword = resultSet.getString("password");
            String storedEmail = resultSet.getString("email");

            UserData user = new UserData(storedUsername, storedPassword, storedEmail);
            return user;
        } catch (Exception exception) {
            String message = "failed to get user from database";
            throw new DataAccessException(message, exception);
        }
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

    /**
     * Inserts a new auth row that links an auth token to a username.
     */
    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        String sql = """
            INSERT INTO auth (auth_token, username)
            VALUES (?, ?)
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            String authToken = auth.authToken();
            String username = auth.username();

            int authTokenParameterIndex = 1;
            int usernameParameterIndex = 2;

            preparedStatement.setString(authTokenParameterIndex, authToken);
            preparedStatement.setString(usernameParameterIndex, username);

            int rowsUpdated = preparedStatement.executeUpdate();
            boolean isMissingInsertedRow = rowsUpdated != 1;
            if (isMissingInsertedRow) {
                String message = "failed to insert auth into database";
                throw new DataAccessException(message);
            }

            return auth;
        } catch (Exception exception) {
            String message = "failed to create auth in database";
            throw new DataAccessException(message, exception);
        }
    }

    /**
     * Looks up an auth row by auth token and returns the corresponding AuthData.
     * Returns null when there is no matching row.
     */
    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = """
            SELECT auth_token, username
            FROM auth
            WHERE auth_token = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int authTokenParameterIndex = 1;
            preparedStatement.setString(authTokenParameterIndex, authToken);

            ResultSet resultSet = preparedStatement.executeQuery();
            boolean isMissingRow = !resultSet.next();
            if (isMissingRow) {
                return null;
            }

            String storedAuthToken = resultSet.getString("auth_token");
            String storedUsername = resultSet.getString("username");

            AuthData authData = new AuthData(storedAuthToken, storedUsername);
            return authData;
        } catch (Exception exception) {
            String message = "failed to get auth from database";
            throw new DataAccessException(message, exception);
        }
    }

    /**
     * Deletes an auth row for the provided auth token.
     * Missing rows are treated as a no-op.
     */
    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = """
            DELETE FROM auth
            WHERE auth_token = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int authTokenParameterIndex = 1;
            preparedStatement.setString(authTokenParameterIndex, authToken);

            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            String message = "failed to delete auth from database";
            throw new DataAccessException(message, exception);
        }
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
