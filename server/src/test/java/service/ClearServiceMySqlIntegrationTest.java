package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import dataaccess.MySqlTestHelper;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearServiceMySqlIntegrationTest {

    private static MySqlDataAccess dataAccess;
    private ClearService clearService;

    @BeforeAll
    public static void setupDatabase() throws DataAccessException {
        MySqlTestHelper.createTablesIfNotExist();
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess.clear();
        clearService = new ClearService(dataAccess);
    }

    @Test
    public void clearPositiveRemovesAllUserAuthAndGameData() throws DataAccessException {
        String username = "clear-user";
        String password = "pass";
        String email = "c@test.com";
        String authToken = "clear-token";
        int gameId = 1;
        String gameName = "ClearGame";
        UserData userToCreate = new UserData(username, password, email);
        AuthData authToCreate = new AuthData(authToken, username);
        GameData gameToCreate = new GameData(gameId, null, null, gameName, new ChessGame());
        dataAccess.createUser(userToCreate);
        dataAccess.createAuth(authToCreate);
        dataAccess.createGame(gameToCreate);

        clearService.clear();

        assertNull(dataAccess.getUser(username));
        assertNull(dataAccess.getAuth(authToken));
        assertNull(dataAccess.getGame(gameId));
    }

    @Test
    public void clearNegativeWhenAlreadyEmptyDoesNotThrow() throws DataAccessException {
        clearService.clear();
        clearService.clear();
    }
}
