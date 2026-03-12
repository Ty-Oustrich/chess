package dataaccess;

import chess.ChessGame;
import model.GameData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MySqlDataAccessGameTest {

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
    public void createGamePositiveStoresAndGetReturnsSameData() throws DataAccessException {
        int gameId = 1;
        String whiteName = "alice";
        String blackName = "ty";
        String gameName = "FirstGame";
        ChessGame chessGame = new ChessGame();
        GameData toCreate = new GameData(gameId, whiteName, blackName, gameName, chessGame);

        GameData created = dataAccess.createGame(toCreate);

        assertNotNull(created);
        assertEquals(gameId, created.gameID());
        assertEquals(whiteName, created.whiteUsername());
        assertEquals(blackName, created.blackUsername());
        assertEquals(gameName, created.gameName());
        assertNotNull(created.game());

        GameData fetched = dataAccess.getGame(gameId);
        assertNotNull(fetched);
        assertEquals(created.gameID(), fetched.gameID());
        assertEquals(created.whiteUsername(), fetched.whiteUsername());
        assertEquals(created.blackUsername(), fetched.blackUsername());
        assertEquals(created.gameName(), fetched.gameName());
    }

    @Test
    public void getGameNegativeReturnsNullWhenGameMissing() throws DataAccessException {
        int missingGameId = 99999;

        GameData result = dataAccess.getGame(missingGameId);

        assertNull(result);
    }

    @Test
    public void listGamesPositiveReturnsAllCreatedGames() throws DataAccessException {
        ChessGame gameOne = new ChessGame();
        ChessGame gameTwo = new ChessGame();
        int firstGameId = 10;
        int secondGameId = 20;
        String firstGameName = "A";



        
        String secondGameName = "B";
        GameData firstGame = new GameData(firstGameId, null, null, firstGameName, gameOne);
        GameData secondGame = new GameData(secondGameId, "w", "b", secondGameName, gameTwo);
        dataAccess.createGame(firstGame);
        dataAccess.createGame(secondGame);

        Collection<GameData> list = dataAccess.listGames();

        assertNotNull(list);
        int expectedSize = 2;
        assertEquals(expectedSize, list.size());
    }

    @Test
    public void updateGamePositiveThenGetReturnsUpdatedData() throws DataAccessException {
        int gameId = 5;
        String initialGameName = "Original";
        ChessGame initialChessGame = new ChessGame();
        GameData initial = new GameData(gameId, null, null, initialGameName, initialChessGame);
        dataAccess.createGame(initial);
        String newWhite = "whitePlayer";
        String newBlack = "blackPlayer";
        ChessGame chessGameFromInitial = initial.game();
        GameData updated = new GameData(gameId, newWhite, newBlack, initialGameName, chessGameFromInitial);

        dataAccess.updateGame(updated);

        GameData fetched = dataAccess.getGame(gameId);
        assertNotNull(fetched);
        assertEquals(newWhite, fetched.whiteUsername());
        assertEquals(newBlack, fetched.blackUsername());
    }

    @Test
    public void updateGameNegativeThrowsWhenGameDoesNotExist() {
        int nonExistentId = 88888;
        GameData fakeGame = new GameData(nonExistentId, "a", "b", "Fake", new ChessGame());

        assertThrows(DataAccessException.class, () -> dataAccess.updateGame(fakeGame));
    }
}
