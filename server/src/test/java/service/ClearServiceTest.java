package service;

import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;  // Assertion method
public class ClearServiceTest {

    @Test
    public void clearRemovesAllData()throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        ClearService clearService = new ClearService(dataAccess);

        dataAccess.createUser(new UserData("ty", "pass123", "ty@test.com"));
        dataAccess.createAuth(new AuthData("token-abc", "ty"));
        dataAccess.createGame(new GameData(1, null, null, "game1", null));
        
        clearService.clear();

        assertNull(dataAccess.getUser("ty"));
        assertNull(dataAccess.getAuth("token-abc"));
        assertNull(dataAccess.getGame(1));
    }
}

