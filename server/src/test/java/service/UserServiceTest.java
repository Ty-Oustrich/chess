package service;

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
    public void loginUnknownUserThrowsExpectedException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        try {
            userService.login(new LoginRequest("missing-user", "secret123"));
            fail("Expected UnauthorizedException to be thrown");
        } catch (UnauthorizedException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void loginWrongPasswordThrowsExpectedException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "chess-user";
        dataAccess.createUser(new UserData(username, "correct-password", "user@example.com"));
        UserService userService = new UserService(dataAccess);

        try {
            userService.login(new LoginRequest(username, "wrong-password"));
            fail("Expected UnauthorizedException to be thrown");
        } catch (UnauthorizedException e) {
            assertNotNull(e);
        }
    }
}
