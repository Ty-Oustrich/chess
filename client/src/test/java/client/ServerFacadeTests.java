package client;

import org.junit.jupiter.api.*;
import server.Server;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    public void clearDatabase() {
        facade.clear();
    }

    @Test
    public void registerPositive() {
        var result = facade.register("user", "p@s123", "reg@test.com");
        assertEquals("user", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    // duplicate username should fail
    @Test
    public void registerNegativeDuplicateReturnsForbidden() {
        facade.register("duplicate-user", "p123", "dup@test.com");

        ServerFacade.ServerFacadeException ex = assertThrows(
                ServerFacade.ServerFacadeException.class,
                () -> facade.register("duplicate-user", "p123", "dup@test.com"));
        assertEquals(403, ex.statusCode());
    }

    @Test
    public void loginPositiveReturnsAuthAndUsername() {
        facade.register("login-user", "my-password", "login@test.com");

        ServerFacade.LoginResult result = facade.login("login-user", "my-password");
        assertEquals("login-user", result.username());
        assertNotNull(result.authToken());
    }


    @Test
    public void loginNegativeBadPasswordReturnsUnauthorized() {
        facade.register("bad-creds-user", "correct-password", "bad-creds@test.com");
        var exception = assertThrows(
                ServerFacade.ServerFacadeException.class,
                () -> facade.login("bad-creds-user", "wrong-password")
        );

        assertEquals(401, exception.statusCode());
    }

}
