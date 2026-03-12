package dataaccess;

import java.sql.Connection;
import java.sql.Statement;

public class MySqlTestHelper {

    public static void createTablesIfNotExist() throws DataAccessException {
        DatabaseManager.createDatabase();
        String createUsers = """
            CREATE TABLE IF NOT EXISTS users (
                username VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL,
                email VARCHAR(255) NOT NULL
            )
            """;
        String createAuth = """
            CREATE TABLE IF NOT EXISTS auth (
                auth_token VARCHAR(255) PRIMARY KEY,
                username VARCHAR(255) NOT NULL
            )
            """;
        String createGames = """
            CREATE TABLE IF NOT EXISTS games (
                game_id INT PRIMARY KEY,
                white_username VARCHAR(255),
                black_username VARCHAR(255),
                game_name VARCHAR(255) NOT NULL,
                game_state_json TEXT NOT NULL
            )
            """;
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(createUsers);
            stmt.executeUpdate(createAuth);
            stmt.executeUpdate(createGames);
        } catch (Exception e) {
            String message = "could not create test tables";
            throw new DataAccessException(message, e);
        }
    }
}
