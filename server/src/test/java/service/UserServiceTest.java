package service;

import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

public class UserServiceTest {

    @Test
    public void loginPositiveReturnsResultAndStoresAuthData() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "chess-user";
        String password = "secret123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        dataAccess.createUser(new UserData(username, hashedPassword, "user@example.com"));
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
        String hashedPassword = BCrypt.hashpw("correct-password", BCrypt.gensalt());
        dataAccess.createUser(new UserData(username, hashedPassword, "user@example.com"));
        UserService userService = new UserService(dataAccess);

        try {
            userService.login(new LoginRequest(username, "wrong-password"));
            fail("Expected UnauthorizedException to be thrown");
        } catch (UnauthorizedException e) {
            assertNotNull(e);
        }
    }

    @Test
    public void loginMissingFieldsThrowBadRequestException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "chess-user";
        String hashedPassword = BCrypt.hashpw("correct-password", BCrypt.gensalt());
        dataAccess.createUser(new UserData(username, hashedPassword, "user@example.com"));
        UserService userService = new UserService(dataAccess);

        LoginRequest missingUsernameRequest = new LoginRequest(null, "correct-password");
        LoginRequest missingPasswordRequest = new LoginRequest(username, null);
        LoginRequest blankUsernameRequest = new LoginRequest("   ", "correct-password");
        LoginRequest blankPasswordRequest = new LoginRequest(username, "  ");

        assertThrows(BadRequestException.class, () -> userService.login(missingUsernameRequest));
        assertThrows(BadRequestException.class, () -> userService.login(missingPasswordRequest));
        assertThrows(BadRequestException.class, () -> userService.login(blankUsernameRequest));
        assertThrows(BadRequestException.class, () -> userService.login(blankPasswordRequest));
    }

    @Test
    public void loginNullRequestThrowsBadRequestException() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        assertThrows(BadRequestException.class, () -> userService.login(null));
    }

    @Test
    public void registerPositiveReturnsResultAndStoresAuthData() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        RegisterResult registerResult = userService.register(
                new RegisterRequest("new-user", "secret123", "new-user@example.com"));

        assertEquals("new-user", registerResult.username());
        assertNotNull(registerResult.authToken());
        UserData storedUserData = dataAccess.getUser("new-user");
        assertNotNull(storedUserData);
        assertEquals("new-user@example.com", storedUserData.email());
        AuthData storedAuthData = dataAccess.getAuth(registerResult.authToken());
        assertNotNull(storedAuthData);
        assertEquals("new-user", storedAuthData.username());
    }

    @Test
    public void registerDuplicateUsernameThrowsExpectedException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String hashedPassword = BCrypt.hashpw("secret123", BCrypt.gensalt());
        dataAccess.createUser(new UserData("chess-user", hashedPassword, "user@example.com"));
        UserService userService = new UserService(dataAccess);

        assertThrows(AlreadyTakenException.class,
                () -> userService.register(new RegisterRequest("chess-user", "new-secret", "new@example.com")));
    }

    @Test
    public void registerMissingRequiredFieldThrowsExpectedException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);

        assertThrows(BadRequestException.class,
                () -> userService.register(new RegisterRequest("chess-user", "", "user@example.com")));
    }

    @Test
    public void logoutPositiveDeletesExistingAuthToken() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "chess-user";
        String password = "secret123";
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());
        dataAccess.createUser(new UserData(username, hashedPassword, "user@example.com"));
        UserService userService = new UserService(dataAccess);

        LoginResult loginResult = userService.login(new LoginRequest(username, password));
        String authToken = loginResult.authToken();
        AuthData storedAuthDataBeforeLogout = dataAccess.getAuth(authToken);
        assertNotNull(storedAuthDataBeforeLogout);

        userService.logout(authToken);

        AuthData storedAuthDataAfterLogout = dataAccess.getAuth(authToken);
        assertNull(storedAuthDataAfterLogout);
    }

    @Test
    public void logoutMissingOrInvalidTokenThrowsUnauthorizedException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        UserService userService = new UserService(dataAccess);
        String missingToken = "missing-auth-token";
        String blankToken = "   ";

        assertThrows(UnauthorizedException.class, () -> userService.logout(null));
        assertThrows(UnauthorizedException.class, () -> userService.logout(blankToken));
        assertThrows(UnauthorizedException.class, () -> userService.logout(missingToken));
    }
}
