package service;

import chess.ChessGame;
import dataaccess.DataAccessException;
import dataaccess.MySqlDataAccess;
import dataaccess.MySqlTestHelper;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class GameServiceMySqlIntegrationTest {

    private static MySqlDataAccess dataAccess;
    private GameService gameService;

    @BeforeAll
    public static void setupDatabase() throws DataAccessException {
        MySqlTestHelper.createTablesIfNotExist();
        dataAccess = new MySqlDataAccess();
    }

    @BeforeEach
    public void setUp() throws DataAccessException {
        dataAccess.clear();
        gameService = new GameService(dataAccess);
    }

    @Test
    public void createGameAndListGamesPositiveFlow() throws Exception {
        String username = "game-user";
        String password = "pass";
        String email = "g@test.com";
        String authToken = "game-auth";
        String gameName = "My Game";
        UserData userToCreate = new UserData(username, password, email);
        AuthData authToCreate = new AuthData(authToken, username);
        dataAccess.createUser(userToCreate);
        dataAccess.createAuth(authToCreate);

        CreateGameResult createResult = gameService.createGame(authToken, gameName);
        int gameId = createResult.gameID();

        assertNotNull(gameId);
        assertNotNull(dataAccess.getGame(gameId));

        ListGamesResult listResult = gameService.listGames(authToken);
        List<ListGamesResult.GameSummary> games = listResult.games();
        assertNotNull(games);
        int expectedGameCount = 1;
        assertEquals(expectedGameCount, games.size());
        ListGamesResult.GameSummary firstGame = games.get(0);
        assertEquals(gameId, firstGame.gameID());
        assertEquals(gameName, firstGame.gameName());
    }

    @Test
    public void listGamesNegativeInvalidAuthThrowsUnauthorized() {
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("bad-token"));
        assertThrows(UnauthorizedException.class, () -> gameService.listGames(null));
    }

    @Test
    public void joinGamePositiveUpdatesGameInDatabase() throws Exception {
        String username = "join-user";
        String password = "pass";
        String email = "j@test.com";
        String authToken = "join-auth";
        int gameId = 42;
        String gameName = "JoinGame";
        UserData userToCreate = new UserData(username, password, email);
        AuthData authToCreate = new AuthData(authToken, username);
        GameData gameToCreate = new GameData(gameId, null, null, gameName, new ChessGame());
        dataAccess.createUser(userToCreate);
        dataAccess.createAuth(authToCreate);
        dataAccess.createGame(gameToCreate);

        gameService.joinGame(authToken, "WHITE", gameId);

        GameData updated = dataAccess.getGame(gameId);
        assertNotNull(updated);
        assertEquals(username, updated.whiteUsername());
        assertNull(updated.blackUsername());
    }

    @Test
    public void joinGameNegativeMissingGameThrowsBadRequest() throws Exception {
        String username = "join-user";
        String password = "pass";
        String email = "j@test.com";
        String authToken = "join-auth";
        int nonExistentGameId = 99999;
        UserData userToCreate = new UserData(username, password, email);
        AuthData authToCreate = new AuthData(authToken, username);
        dataAccess.createUser(userToCreate);
        dataAccess.createAuth(authToCreate);

        assertThrows(BadRequestException.class, () -> gameService.joinGame(authToken, "WHITE", nonExistentGameId));
    }
}
