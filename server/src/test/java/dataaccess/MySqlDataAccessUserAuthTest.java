package dataaccess;

import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class MySqlDataAccessUserAuthTest {

    private static MySqlDataAccess dataAccess;

    @BeforeAll
    public static void setupDatabase() throws DataAccessException {
        MySqlTestHelper.createTablesIfNotExist();
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    public void clearData() throws DataAccessException {
        dataAccess.clear();
    }

    @Test
    public void createUserPositiveStoresAndGetReturnsSameData() throws DataAccessException {
        String username = "testuser";
        String password = "hashedpass";
        String email = "test@example.com";
        UserData toCreate = new UserData(username, password, email);

        UserData created = dataAccess.createUser(toCreate);

        assertNotNull(created);
        assertEquals(username, created.username());
        assertEquals(email, created.email());

        UserData fetched = dataAccess.getUser(username);
        assertNotNull(fetched);
        assertEquals(created.username(), fetched.username());
        assertEquals(created.email(), fetched.email());
        assertEquals(created.password(), fetched.password());
    }

    @Test
    public void getUserNegativeReturnsNullWhenUserMissing() throws DataAccessException {
        String missingUsername = "nonexistent_user_xyz";

        UserData result = dataAccess.getUser(missingUsername);

        assertNull(result);
    }

    @Test
    public void createAuthPositiveStoresAndGetReturnsSameData() throws DataAccessException {
        String token = "auth-token-123";
        String username = "authuser";
        AuthData toCreate = new AuthData(token, username);

        AuthData created = dataAccess.createAuth(toCreate);

        assertNotNull(created);
        assertEquals(token, created.authToken());
        assertEquals(username, created.username());

        AuthData fetched = dataAccess.getAuth(token);
        assertNotNull(fetched);
        assertEquals(created.authToken(), fetched.authToken());
        assertEquals(created.username(), fetched.username());
    }

    @Test
    public void getAuthNegativeReturnsNullWhenAuthMissing() throws DataAccessException {
        String missingToken = "missing-token-xyz";

        AuthData result = dataAccess.getAuth(missingToken);

        assertNull(result);
    }

    @Test
    public void deleteAuthPositiveThenGetReturnsNull() throws DataAccessException {
        String token = "token-to-delete";
        String username = "deleteuser";
        dataAccess.createAuth(new AuthData(token, username));
        AuthData beforeDelete = dataAccess.getAuth(token);
        assertNotNull(beforeDelete);

        dataAccess.deleteAuth(token);

        AuthData afterDelete = dataAccess.getAuth(token);
        assertNull(afterDelete);
    }
}
