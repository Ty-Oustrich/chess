package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class GameService {
    private final DataAccess dataAccess;
    private int nextGameID = 1;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public ListGamesResult listGames(String authToken) throws DataAccessException, UnauthorizedException {
        AuthData authData = validateAuthToken(authToken);
        boolean isMissingAuthData = false;
        isMissingAuthData = authData == null;
        if (isMissingAuthData) {
            throw new UnauthorizedException();
        }

        Collection<GameData> storedGames = dataAccess.listGames();
        List<ListGamesResult.GameSummary> gameSummaries = new ArrayList<>();
        for (GameData gameData : storedGames) {
            ListGamesResult.GameSummary gameSummary = new ListGamesResult.GameSummary(
                    gameData.gameID(),
                    gameData.gameName(),
                    gameData.whiteUsername(),
                    gameData.blackUsername()
            );
            gameSummaries.add(gameSummary);
        }

        return new ListGamesResult(gameSummaries);
    }

    public CreateGameResult createGame(String authToken, String gameName)
            throws DataAccessException, UnauthorizedException, BadRequestException {
        AuthData authData = validateAuthToken(authToken);
        boolean isMissingAuthData = authData == null;
        if (isMissingAuthData) {
            throw new UnauthorizedException();
        }

        boolean isMissingGameName = gameName == null;
        isMissingGameName = isMissingGameName || gameName.isBlank();
        if (isMissingGameName) {
            throw new BadRequestException();
        }

        int generatedGameID = generateGameID();
        ChessGame newChessGame = new ChessGame();
        GameData gameToCreate = new GameData(generatedGameID, null, null, gameName, newChessGame);
        dataAccess.createGame(gameToCreate);

        return new CreateGameResult(generatedGameID);
    }

    public void joinGame(String authToken, String playerColor, Integer gameID) throws DataAccessException, UnauthorizedException, BadRequestException, AlreadyTakenException {
        AuthData authData = validateAuthToken(authToken);
        boolean isMissingAuthData = authData == null;
        if (isMissingAuthData) {
            throw new UnauthorizedException();
        }

        boolean isMissingColor = playerColor == null;
        isMissingColor = isMissingColor || playerColor.isBlank();
        boolean isMissingGameID = false;
        isMissingGameID = gameID == null;
        boolean isBadRequest = isMissingColor || isMissingGameID;
        if (isBadRequest) {
            throw new BadRequestException();
        }

        String normalizedColor = playerColor.trim().toUpperCase();
        boolean isWhiteRequest = normalizedColor.equals("WHITE");
        boolean isBlackRequest = normalizedColor.equals("BLACK");
        boolean isUnsupportedColor = !isWhiteRequest && !isBlackRequest;
        if (isUnsupportedColor) {
            throw new BadRequestException();
        }

        GameData existingGame = dataAccess.getGame(gameID);
        boolean isMissingGame = existingGame == null;
        if (isMissingGame) {
            throw new BadRequestException();
        }

        String currentUsername = authData.username();
        String updatedWhiteUsername = existingGame.whiteUsername();
        String updatedBlackUsername = existingGame.blackUsername();

        if (isWhiteRequest) {
            boolean isWhiteAlreadyTaken = updatedWhiteUsername != null;
            if (isWhiteAlreadyTaken) {
                throw new AlreadyTakenException();
            }
            updatedWhiteUsername = currentUsername;
        }

        if (isBlackRequest) {
            boolean isBlackAlreadyTaken = updatedBlackUsername != null;
            if (isBlackAlreadyTaken) {
                throw new AlreadyTakenException();
            }
            updatedBlackUsername = currentUsername;
        }

        GameData updatedGame = new GameData(
                existingGame.gameID(),
                updatedWhiteUsername,
                updatedBlackUsername,
                existingGame.gameName(),
                existingGame.game()
        );
        dataAccess.updateGame(updatedGame);
    }

    private AuthData validateAuthToken(String authToken) throws DataAccessException {
        boolean isMissingAuthToken = authToken == null || authToken.isBlank();
        if (isMissingAuthToken) {
            return null;
        }
        return dataAccess.getAuth(authToken);
    }

    private int generateGameID() throws DataAccessException {
        int candidateGameID = nextGameID;
        GameData existingGame = dataAccess.getGame(candidateGameID);
        while (existingGame != null) {
            nextGameID = nextGameID + 1;
            candidateGameID = nextGameID;
            existingGame = dataAccess.getGame(candidateGameID);
        }
        nextGameID = nextGameID + 1;
        return candidateGameID;
    }
}

//logic for game endpoints
