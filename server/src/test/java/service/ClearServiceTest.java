package service;

import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

public class ClearServiceTest {

    @Test
    public void clearRemovesAllData() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);
        String username = "ty";
        String password = "pass123";
        String email = "ty@test.com";
        String authToken = "token-abc";
        int gameId = 1;
        String gameName = "game1";
        UserData userToCreate = new UserData(username, password, email);
        AuthData authToCreate = new AuthData(authToken, username);
        GameData gameToCreate = new GameData(gameId, null, null, gameName, null);

        dataAccess.createUser(userToCreate);
        dataAccess.createAuth(authToCreate);
        dataAccess.createGame(gameToCreate);
        clearService.clear();

        assertNull(dataAccess.getUser(username));
        assertNull(dataAccess.getAuth(authToken));
        assertNull(dataAccess.getGame(gameId));
    }

    @Test
    public void clearWhenAlreadyEmptyDoesNotThrow() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);

        clearService.clear();
        clearService.clear();
    }
}

