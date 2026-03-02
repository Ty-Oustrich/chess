package service;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class UserServiceTest {

    @Test
    public void loginPositiveReturnsResultAndStoresAuthData() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "chess-user";
        String password = "secret123";
        dataAccess.createUser(new UserData(username, password, "user@example.com"));
        UserService userService = new UserService(dataAccess);

        LoginResult loginResult = userService.login(new LoginRequest(username, password));

        assertEquals(username, loginResult.username());
        assertNotNull(loginResult.authToken());

        AuthData storedAuthData = dataAccess.getAuth(loginResult.authToken());
        assertNotNull(storedAuthData);
        assertEquals(username, storedAuthData.username());
    }

    @Test
    public void loginUnknownUserThrowsExpectedException() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        try {
            userService.login(new LoginRequest("missing-user", "secret123"));
            fail("Expected DataAccessException to be thrown");
        } catch (DataAccessException e) {
            assertEquals("Error: username was wrong", e.getMessage());
        }
    }

    @Test
    public void loginWrongPasswordThrowsExpectedException() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "chess-user";
        dataAccess.createUser(new UserData(username, "correct-password", "user@example.com"));
        UserService userService = new UserService(dataAccess);

        try {
            userService.login(new LoginRequest(username, "wrong-password"));
            fail("Expected DataAccessException to be thrown");
        } catch (DataAccessException e) {
            assertEquals("Error: password was wrong", e.getMessage());
        }
    }
}
