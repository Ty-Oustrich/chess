package client;

import chess.ChessGame;
import ui.BoardPrinter;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.LoadGameMessage;

import java.util.Scanner;

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

   //command loop
    public void gameLoop() {
        webSocketFacade.sendConnect(authToken, gameID);

        System.out.println("Connected to game " + gameID);
        System.out.println("Type 'help' for game commands");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.print("GAME>>> ");
            String userInput = scanner.nextLine();
            running = processCommand(userInput, scanner);
        }
        scanner.close();
    }


    private boolean processCommand(String userInput, Scanner scanner) {
        String trimmedInput = userInput == null ? "" : userInput.trim();
        if (trimmedInput.isEmpty()) {
            System.out.println("Please enter a command");
            return true;
        }

        String command = trimmedInput.toLowerCase();
        return switch (command) {
            case "help" -> {
                printHelp();
                yield true;
            }
            case "redraw" -> {
                redrawBoard();
                yield true;
            }
            case "leave" -> {
                webSocketFacade.sendLeave(authToken, gameID);
                System.out.println("You have left the gaem");
                yield false;
            }
            case "resign" -> {
                handleResign(scanner);
                yield true;
            }
            default -> {
                System.out.println("Unknown command. Type 'help' for game commands.");
                yield true;
            }
        };
    }

    
//Sends a resign command after a yes no confirmation 
    private void handleResign(Scanner scanner) {
        System.out.print("Resign this game? (yes/no): ");
        String answer = scanner.nextLine().trim().toLowerCase();

        switch (answer) {
            case "yes" -> {
                webSocketFacade.sendResign(authToken, gameID);
                System.out.println("resignation sent");
            }
            case "no" -> System.out.println("staying in the game");
            default -> System.out.println("invalid command: not resigning");
        }
    }


    private void redrawBoard() {
        if (game == null || game.getBoard() == null) {
            System.out.println("No game state loaded yet");
            return;
        }

        BoardPrinter.printBoard(game.getBoard(), isWhitePerspective());
    }


    private void printHelp() {
        System.out.println("help    - show available game commands");
        System.out.println("redraw  - print the current board");
        System.out.println("resign  - resign from the game");
        System.out.println("leave   - leave the game and return");
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
