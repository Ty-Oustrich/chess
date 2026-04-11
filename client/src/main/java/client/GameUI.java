package client;

import chess.ChessGame;
import ui.BoardPrinter;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.LoadGameMessage;

public class GameUI implements GameHandler {
    private final WebSocketFacade webSocketFacade;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor playerColor;
    private ChessGame game;

    public GameUI(String host, int port, String authToken, int gameID, ChessGame.TeamColor playerColor) {
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.webSocketFacade = new WebSocketFacade(host, port, this);
    }

 
    @Override
    public void onLoadGame(LoadGameMessage message) {
        this.game = message.getGame();
        if (this.game == null || this.game.getBoard() == null) {
            System.out.println("load game came in empty");
            return;
        }

        BoardPrinter.printBoard(this.game.getBoard(), isWhitePerspective());
    }


    @Override
    public void onError(ErrorMessage message) {
        System.out.println(message.getErrorMessage());
    }


    @Override
    public void onNotification(NotificationMessage message) {
        System.out.println(message.getMessage());
    }


    private boolean isWhitePerspective() {
        if (playerColor == null) return true;
        return playerColor == ChessGame.TeamColor.WHITE;
    }


    public WebSocketFacade getWebSocketFacade() {
        return webSocketFacade;
    }

 
    public String getAuthToken() {
        return authToken;
    }

  
    public int getGameID() {
        return gameID;
    }

 
    public ChessGame.TeamColor getPlayerColor() {
        return playerColor;
    }


    public ChessGame getGame() {
        return game;
    }
}
