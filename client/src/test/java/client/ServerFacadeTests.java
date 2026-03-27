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

    @Test
    public void logoutPositiveInvalidatesToken() {
        var reg = facade.register("user", "pass", "user@test.com");
        assertDoesNotThrow(() -> facade.logout(reg.authToken()));
    }

    // second logout should be rejected
    @Test
    public void logoutNegativeSecondLogoutReturnsUnauthorized() {
        var reg = facade.register("user", "pass", "user@test.com");
        facade.logout(reg.authToken());

        var ex = assertThrows(
            ServerFacade.ServerFacadeException.class,
            () -> facade.logout(reg.authToken())
        );
        assertEquals(401, ex.statusCode());
    }

    @Test
    public void createGamePositiveReturnsGameId() {
        var token = createRegisteredUserAndReturnAuthToken("creator");

        var result = facade.createGame(token, "test-game");

        assertNotNull(result);
        assertNotNull(result.gameID());
        assertTrue(result.gameID() > 0);
    }

    @Test
    public void createGameNegativeBadAuthReturnsUnauthorized() {
        createRegisteredUserAndReturnAuthToken("creator");

        var ex = assertThrows(
            ServerFacade.ServerFacadeException.class,
            () -> facade.createGame("invalid-token", "test-game")
        );
        assertEquals(401, ex.statusCode());
    }

    @Test
    public void listGamesPositiveReturnsCreatedGame() {
        var auth = createRegisteredUserAndReturnAuthToken("lister");
        var created = facade.createGame(auth, "alpha");

        var games = facade.listGames(auth);

        assertNotNull(games);
        assertNotNull(games.games());
        assertEquals(1, games.games().size());
        var first = games.games().get(0);
        assertEquals(created.gameID(), first.gameID());
        assertEquals("alpha", first.gameName());
    }

    @Test
    public void listGamesNegativeBadAuthReturnsUnauthorized() {
        createRegisteredUserAndReturnAuthToken("lister");

        var exception = assertThrows(
            ServerFacade.ServerFacadeException.class,
            () -> facade.listGames("invalid-token")
        );
        assertEquals(401, exception.statusCode());
    }

    @Test
    public void joinGamePositiveAssignsWhitePlayer() {
        var auth = createRegisteredUserAndReturnAuthToken("joiner");
        var newGame = facade.createGame(auth, "joinable");

        assertDoesNotThrow(() -> facade.joinGame(auth, "WHITE", newGame.gameID()));

        var list = facade.listGames(auth);
        assertNotNull(list.games());
        assertEquals(1, list.games().size());
        assertEquals("joiner", list.games().get(0).whiteUsername());
    }

    @Test
    public void joinGameNegativeBadAuthReturnsUnauthorized() {
        var auth = createRegisteredUserAndReturnAuthToken("joiner");
        var created = facade.createGame(auth, "joinable");

        var ex = assertThrows(
            ServerFacade.ServerFacadeException.class,
            () -> facade.joinGame("invalid-token", "WHITE", created.gameID())
        );
        assertEquals(401, ex.statusCode());
    }

    private String createRegisteredUserAndReturnAuthToken(String username) {
        var email = username + "@test.com";
        var result = facade.register(username, "pass", email);
        return result.authToken();
    }

}
