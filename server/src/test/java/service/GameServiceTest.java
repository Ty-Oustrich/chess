package service;

import chess.ChessGame;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GameServiceTest {

    @Test
    public void listGamesPositiveReturnsAllGames() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String authToken = "valid-auth-token";
        String username = "list-user";
        dataAccess.createUser(new UserData(username, "secret123", "list-user@test.com"));
        dataAccess.createAuth(new AuthData(authToken, username));
        dataAccess.createGame(new GameData(69, "white-a", "black-a", "Alpha", new ChessGame()));
        dataAccess.createGame(new GameData(3, "white-b", null, "Beta", new ChessGame()));
        GameService gameService = new GameService(dataAccess);

        ListGamesResult listGamesResult = gameService.listGames(authToken);
        List<ListGamesResult.GameSummary> returnedGames = listGamesResult.games();
        Comparator<ListGamesResult.GameSummary> byGameID = Comparator.comparingInt(gameSummary -> gameSummary.gameID());
        returnedGames.sort(byGameID);

        assertEquals(2, returnedGames.size());
        assertEquals(3, returnedGames.get(0).gameID());
        assertEquals("Beta", returnedGames.get(0).gameName());
        assertEquals("white-b", returnedGames.get(0).whiteUsername());
        assertNull(returnedGames.get(0).blackUsername());
        assertEquals(69, returnedGames.get(1).gameID());
        assertEquals("Alpha", returnedGames.get(1).gameName());
        assertEquals("white-a", returnedGames.get(1).whiteUsername());
        assertEquals("black-a", returnedGames.get(1).blackUsername());
    }

    @Test
    public void listGamesInvalidAuthThrowsUnauthorizedException() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);

        assertThrows(UnauthorizedException.class, () -> gameService.listGames("missing-auth-token"));
        assertThrows(UnauthorizedException.class, () -> gameService.listGames(null));
        assertThrows(UnauthorizedException.class, () -> gameService.listGames("   "));
    }

    @Test
    public void createGamePositiveStoresGameAndReturnsGameID() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "creator";
        String authToken = "create-auth";
        dataAccess.createUser(new UserData(username, "secret123", "creator@test.com"));
        dataAccess.createAuth(new AuthData(authToken, username));
        GameService gameService = new GameService(dataAccess);

        CreateGameResult createGameResult = gameService.createGame(authToken, "Opening Game");
        Integer createdGameID = createGameResult.gameID();
        GameData storedGameData = dataAccess.getGame(createdGameID);

        assertNotNull(createdGameID);
        assertTrue(createdGameID > 0);
        assertNotNull(storedGameData);
        assertEquals("Opening Game", storedGameData.gameName());
        assertNull(storedGameData.whiteUsername());
        assertNull(storedGameData.blackUsername());
        assertNotNull(storedGameData.game());
    }

    @Test
    public void createGameMissingGameNameThrowsBadRequestException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "creator";
        String authToken = "create-auth";
        dataAccess.createUser(new UserData(username, "secret123", "creator@test.com"));
        dataAccess.createAuth(new AuthData(authToken, username));
        GameService gameService = new GameService(dataAccess);

        assertThrows(BadRequestException.class, () -> gameService.createGame(authToken, null));
        assertThrows(BadRequestException.class, () -> gameService.createGame(authToken, ""));
        assertThrows(BadRequestException.class, () -> gameService.createGame(authToken, "   "));
    }

    @Test
    public void createGameMissingAuthThrowsUnauthorizedException() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);

        assertThrows(UnauthorizedException.class, () -> gameService.createGame("missing-auth", "Valid Name"));
        assertThrows(UnauthorizedException.class, () -> gameService.createGame(null, "Valid Name"));
    }

    @Test
    public void joinGamePositiveAddsPlayerToRequestedColor() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "join-user";
        String authToken = "join-auth";
        int gameID = 33;
        dataAccess.createUser(new UserData(username, "secret123", "join-user@test.com"));
        dataAccess.createAuth(new AuthData(authToken, username));
        dataAccess.createGame(new GameData(gameID, null, null, "Join Test", new ChessGame()));
        GameService gameService = new GameService(dataAccess);

        gameService.joinGame(authToken, "WHITE", gameID);

        GameData updatedGame = dataAccess.getGame(gameID);
        assertEquals(username, updatedGame.whiteUsername());
        assertNull(updatedGame.blackUsername());
    }

    @Test
    public void joinGameMissingAuthThrowsUnauthorizedException() {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        GameService gameService = new GameService(dataAccess);

        assertThrows(UnauthorizedException.class, () -> gameService.joinGame("missing-auth", "WHITE", 1));
        assertThrows(UnauthorizedException.class, () -> gameService.joinGame(null, "WHITE", 1));
    }

    @Test
    public void joinGameInvalidRequestThrowsBadRequestException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String username = "join-user";
        String authToken = "join-auth";
        int gameID = 8;
        dataAccess.createUser(new UserData(username, "secret123", "join-user@test.com"));
        dataAccess.createAuth(new AuthData(authToken, username));
        dataAccess.createGame(new GameData(gameID, null, null, "Join Test", new ChessGame()));
        GameService gameService = new GameService(dataAccess);

        assertThrows(BadRequestException.class, () -> gameService.joinGame(authToken, null, gameID));
        assertThrows(BadRequestException.class, () -> gameService.joinGame(authToken, "", gameID));
        assertThrows(BadRequestException.class, () -> gameService.joinGame(authToken, "GREEN", gameID));
        assertThrows(BadRequestException.class, () -> gameService.joinGame(authToken, "WHITE", null));
        assertThrows(BadRequestException.class, () -> gameService.joinGame(authToken, "WHITE", 999));
    }

    @Test
    public void joinGameTakenColorThrowsAlreadyTakenException() throws Exception {
        MemoryDataAccess dataAccess = new MemoryDataAccess();
        String originalUser = "occupied-player";
        String challengerUser = "challenger-player";
        String originalAuth = "original-auth";
        String challengerAuth = "challenger-auth";
        int gameID = 14;
        dataAccess.createUser(new UserData(originalUser, "secret123", "occupied@test.com"));
        dataAccess.createUser(new UserData(challengerUser, "secret456", "challenger@test.com"));
        dataAccess.createAuth(new AuthData(originalAuth, originalUser));
        dataAccess.createAuth(new AuthData(challengerAuth, challengerUser));
        dataAccess.createGame(new GameData(gameID, originalUser, null, "Occupied Test", new ChessGame()));
        GameService gameService = new GameService(dataAccess);

        assertThrows(AlreadyTakenException.class, () -> gameService.joinGame(challengerAuth, "WHITE", gameID));
    }
}
