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
        var result = facade.register("user", "pass", "user@test.com");
        assertEquals("user", result.username());
        assertNotNull(result.authToken());
        assertFalse(result.authToken().isBlank());
    }

    // duplicate username should fail
    @Test
    public void registerNegativeDuplicateReturnsForbidden() {
        facade.register("user", "pass", "user@test.com");

        ServerFacade.ServerFacadeException ex = assertThrows(
                ServerFacade.ServerFacadeException.class,
                () -> facade.register("user", "pass", "user@test.com"));
        assertEquals(403, ex.statusCode());
    }

    @Test
    public void loginPositiveReturnsAuthAndUsername() {
        facade.register("user", "pass", "user@test.com");

        ServerFacade.LoginResult result = facade.login("user", "pass");
        assertEquals("user", result.username());
        assertNotNull(result.authToken());
    }


    @Test
    public void loginNegativeBadPasswordReturnsUnauthorized() {
        facade.register("user", "pass", "user@test.com");
        var exception = assertThrows(
                ServerFacade.ServerFacadeException.class,
                () -> facade.login("user", "wrong-pass")
        );

        assertEquals(401, exception.statusCode());
    }

    @Test
    public void clearPositiveRemovesExistingData() {
        facade.register("user", "pass", "user@test.com");
        facade.clear();

        assertDoesNotThrow(() ->
            facade.register("user", "pass", "user@test.com")
        );
    }

    @Test
    public void clearNegativeThrowsServerFacadeException() {
        ServerFacade badFacade = new ServerFacade("localhost", 1);

        var exception = assertThrows(
            ServerFacade.ServerFacadeException.class,
            badFacade::clear
        );

        assertEquals(0, exception.statusCode());
    }

}
