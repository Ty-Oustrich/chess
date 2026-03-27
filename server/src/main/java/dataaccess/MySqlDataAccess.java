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

    public MySqlDataAccess() {
        try {
            configureDatabase();
        } catch (DataAccessException e) {
            throw new RuntimeException("Could not initialize database", e);
        }
    }

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS auth (
                auth_token VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
            )
            """,
            """
            CREATE TABLE IF NOT EXISTS games (
                game_id INT PRIMARY KEY,
                white_username VARCHAR(255),
                black_username VARCHAR(255),
                game_name VARCHAR(255) NOT NULL,
                game_state_json TEXT NOT NULL
            )
            """
        };
        try (var conn = DatabaseManager.getConnection();
             var stmt = conn.createStatement()) {
            for (String sql : createStatements) {
                stmt.executeUpdate(sql);
            }
        } catch (Exception e) {
            throw new DataAccessException("Failed to create tables", e);
        }
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        String sql = """
            SELECT game_id, white_username, black_username, game_name, game_state_json
            FROM games
            WHERE game_id = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, gameID);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            return readGameFromResultSet(resultSet);
        } catch (Exception exception) {
            throw new DataAccessException("getGame failed for id=" + gameID, exception);
        }
    }

    @Override
    public void clear() throws DataAccessException {
        String[] tables = {"auth", "games", "users"};
        try (var connection = DatabaseManager.getConnection()) {
            for (String table : tables) {
                try (PreparedStatement stmt = connection.prepareStatement("DELETE FROM " + table)) {
                    stmt.executeUpdate();
                }
            }
        } catch (Exception exception) {
            throw new DataAccessException("clear failed", exception);
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

            preparedStatement.setString(1, user.username());
            preparedStatement.setString(2, user.password());
            preparedStatement.setString(3, user.email());

            if (preparedStatement.executeUpdate() != 1) {
                throw new DataAccessException("failed to insert user into database");
            }

            return user;
        } catch (Exception exception) {
            throw new DataAccessException("createUser failed for username=" + user.username(), exception);
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

            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            return new UserData(
                    resultSet.getString("username"),
                    resultSet.getString("password"),
                    resultSet.getString("email")
            );
        } catch (Exception exception) {
            throw new DataAccessException("getUser failed for username=" + username, exception);
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

            preparedStatement.setInt(1, game.gameID());
            preparedStatement.setString(2, game.whiteUsername());
            preparedStatement.setString(3, game.blackUsername());
            preparedStatement.setString(4, game.gameName());
            preparedStatement.setString(5, gson.toJson(game.game(), ChessGame.class));

            if (preparedStatement.executeUpdate() != 1) {
                throw new DataAccessException("createGame insert returned unexpected row count");
            }

            return game;
        } catch (Exception exception) {
            throw new DataAccessException("createGame failed for id=" + game.gameID(), exception);
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
                games.add(readGameFromResultSet(resultSet));
            }

            return games;
        } catch (Exception exception) {
            throw new DataAccessException("listGames failed", exception);
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

            preparedStatement.setString(1, game.whiteUsername());
            preparedStatement.setString(2, game.blackUsername());
            preparedStatement.setString(3, game.gameName());
            preparedStatement.setString(4, gson.toJson(game.game(), ChessGame.class));
            preparedStatement.setInt(5, game.gameID());

            if (preparedStatement.executeUpdate() != 1) {
                throw new DataAccessException("no game row found for id=" + game.gameID());
            }
        } catch (Exception exception) {
            throw new DataAccessException("updateGame failed for id" + game.gameID(), exception);
        }
    }

    @Override
    public AuthData createAuth(AuthData auth) throws DataAccessException {
        String sql = """
            INSERT INTO auth (auth_token, username)
            VALUES (?, ?)
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, auth.authToken());
            preparedStatement.setString(2, auth.username());

            if (preparedStatement.executeUpdate() != 1) {
                throw new DataAccessException("createAuth insert returned unexpected row count");
            }

            return auth;
        } catch (Exception exception) {
            throw new DataAccessException("createAuth failed", exception);
        }
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        String sql = """
            SELECT auth_token, username
            FROM auth
            WHERE auth_token = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, authToken);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                return null;
            }

            return new AuthData(
                    resultSet.getString("auth_token"),
                    resultSet.getString("username")
            );
        } catch (Exception exception) {
            throw new DataAccessException("getAuth failed", exception);
        }
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
        String sql = """
            DELETE FROM auth
            WHERE auth_token = ?
            """;

        try (var connection = DatabaseManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, authToken);
            preparedStatement.executeUpdate();
        } catch (Exception exception) {
            throw new DataAccessException("deleteAuth failed", exception);
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
