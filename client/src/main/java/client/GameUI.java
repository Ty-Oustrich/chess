package client;

import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessMove;

import chess.ChessGame;
import ui.BoardPrinter;
import websocket.messages.ErrorMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.LoadGameMessage;

import java.util.Collection;
import java.util.Scanner;

public class GameUI implements GameHandler {
    private final WebSocketFacade webSocketFacade;
    private final String authToken;
    private final int gameID;
    private final ChessGame.TeamColor playerColor;
    private ChessGame game;


    public GameUI(WebSocketFacade webSocketFacade, String authToken, int gameID, ChessGame.TeamColor playerColor) {
        if (webSocketFacade == null) throw new IllegalArgumentException("webSocketFacade is required");
        this.authToken = authToken;
        this.gameID = gameID;
        this.playerColor = playerColor;
        this.webSocketFacade = webSocketFacade;
    }

    public void gameLoop() {
        System.out.println("In game " + gameID);
        System.out.println("Type 'help' for game commands");

        Scanner scanner = new Scanner(System.in);
        boolean running = true;
        while (running) {
            System.out.print("GAME>>> ");
            String userInput = scanner.nextLine();
            running = processCommand(userInput, scanner);
        }
    }


    private boolean processCommand(String userInput, Scanner scanner) {
        String trimmedInput = userInput.trim();
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
                System.out.println("left game");
                yield false;
            }
            case "resign" -> {
                handleResign(scanner);
                yield true;
            }
            case "move" -> {
                handleMove(scanner);
                yield true;
            }
            case "highlight" -> {
                handleHighlight(scanner);
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

    private void handleMove(Scanner scanner) {
        if (game == null || game.getBoard() == null) {
            System.out.println("No game loaded yet");
            return;
        }

        System.out.print("Enter a chess move (example: e2 e4): ");
        String line = scanner.nextLine().trim().toLowerCase();
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            System.out.println("Use format: e2 e4");
            return;
        }

        ChessPosition start = parsePosition(parts[0]);
        ChessPosition end = parsePosition(parts[1]);
        if (start == null || end == null) {
            System.out.println("Bad square input. Example: e2 e4");
            return;
        }

        ChessPiece movingPiece = game.getBoard().getPiece(start);
        if (movingPiece == null) {
            System.out.println("No piece at " + parts[0]);
            return;
        }

        ChessPiece.PieceType promotionPiece = null;
        if (isPawnPromotionMove(movingPiece, end)) {
            promotionPiece = promptPromotionPiece(scanner);
            if (promotionPiece == null) return;
        }

        ChessMove move = new ChessMove(start, end, promotionPiece);
        webSocketFacade.sendMakeMove(authToken, gameID, move);
    }

    private void handleHighlight(Scanner scanner) {

        if (game == null || game.getBoard() == null) {
            System.out.println("No game loaded yet");
            return;
        }

        System.out.print("Enter piece square (example: e2): ");
        String square = scanner.nextLine().trim().toLowerCase();
        ChessPosition position = parsePosition(square);
        if (position == null) {
            System.out.println("not a square");
            return;
        }

        ChessPiece selectedPiece = game.getBoard().getPiece(position);
        if (selectedPiece == null) {
            System.out.println("No piece at " + square);
            return;
        }

        Collection<ChessMove> legalMoves = game.validMoves(position);
        if (legalMoves == null || legalMoves.isEmpty()) {
            System.out.println("No legal moves from " + square);
            return;
        }

        BoardPrinter.printBoardWithHighlights(game.getBoard(), isWhitePerspective(), legalMoves);
    }

    private ChessPosition parsePosition(String square) {
        if (square == null || square.length() != 2) return null;

        char file = Character.toLowerCase(square.charAt(0));
        char rank = square.charAt(1);
        if (file < 'a' || file > 'h' || rank < '1' || rank > '8') return null;

        int col = file - 'a' + 1;
        int row = rank - '0';
        return new ChessPosition(row, col);
    }

    private boolean isPawnPromotionMove(ChessPiece movingPiece, ChessPosition end) {
        if (movingPiece.getPieceType() != ChessPiece.PieceType.PAWN) return false;

        if (movingPiece.getTeamColor() == ChessGame.TeamColor.WHITE) return end.getRow() == 8;
        return end.getRow() == 1;
    }

    private ChessPiece.PieceType promptPromotionPiece(Scanner scanner) {
        System.out.print("Promotion, type an option (queen/rook/bishop/knight): ");
        String choice = scanner.nextLine().trim().toLowerCase();

        return switch (choice) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> {
                System.out.println("Invalid promotion choice");
                yield null;
            }
        };
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
        System.out.printf("%s%n", "redraw  - print the current board");
        System.out.println("move    - make a move (example: e2 e4)");
        System.out.println("highlight - show legal moves for one piece");
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
        return playerColor == null || playerColor == ChessGame.TeamColor.WHITE;
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
