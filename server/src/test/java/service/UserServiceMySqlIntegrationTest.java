package service;

import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import dataaccess.MySqlTestHelper;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserServiceMySqlIntegrationTest {

    private static MySqlDataAccess dataAccess;
    private UserService userService;

    @BeforeAll
    public static void setupDatabase() throws DataAccessException {
        MySqlTestHelper.createTablesIfNotExist();
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess.clear();
        userService = new UserService(dataAccess);
    }

    @Test
    public void registerThenLoginPositiveReturnsResultAndAuthStored() throws Exception {
        String username = "mysql-user";
        String password = "secret123";
        String email = "mysql@test.com";
        RegisterRequest request = new RegisterRequest(username, password, email);

        RegisterResult registerResult = userService.register(request);

        assertEquals(username, registerResult.username());
        assertNotNull(registerResult.authToken());
        UserData storedUser = dataAccess.getUser(username);
        assertNotNull(storedUser);
        assertEquals(email, storedUser.email());
        AuthData storedAuth = dataAccess.getAuth(registerResult.authToken());
        assertNotNull(storedAuth);

        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResult loginResult = userService.login(loginRequest);
        assertEquals(username, loginResult.username());
        assertNotNull(loginResult.authToken());
    }

    @Test
    public void loginNegativeWrongPasswordThrowsUnauthorized() throws Exception {
        String username = "mysql-user";
        String correctPassword = "correct";
        String wrongPassword = "wrong";
        String hashedPassword = BCrypt.hashpw(correctPassword, BCrypt.gensalt());
        String email = "u@test.com";
        UserData userToCreate = new UserData(username, hashedPassword, email);
        dataAccess.createUser(userToCreate);
        LoginRequest wrongPasswordRequest = new LoginRequest(username, wrongPassword);

        assertThrows(UnauthorizedException.class, () -> userService.login(wrongPasswordRequest));
    }

    @Test
    public void logoutPositiveRemovesAuthFromDatabase() throws Exception {
        String username = "logout-user";
        String password = "pass";
        String email = "l@test.com";
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        UserData userToCreate = new UserData(username, hashed, email);
        dataAccess.createUser(userToCreate);
        LoginRequest loginRequest = new LoginRequest(username, password);
        LoginResult loginResult = userService.login(loginRequest);
        String token = loginResult.authToken();
        assertNotNull(dataAccess.getAuth(token));

        userService.logout(token);

        assertNull(dataAccess.getAuth(token));
    }

    @Test
    public void logoutNegativeInvalidTokenThrowsUnauthorized() {
        String invalidToken = "invalid-token";

        assertThrows(UnauthorizedException.class, () -> userService.logout(invalidToken));
    }
}
