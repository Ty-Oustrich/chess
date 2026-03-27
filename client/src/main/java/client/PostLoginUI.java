







package client;

import chess.ChessGame;
import ui.BoardPrinter;

import java.util.Scanner;



public class PostLoginUI {
    private final ServerFacade serverFacade;
    private SessionData currentSession;

    public PostLoginUI(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }

    void postLoginLoop(SessionData session) {
        this.currentSession = session;
        System.out.println("Logged in as " + session.username());
        System.out.println("Type 'help' for help");

        @SuppressWarnings("resource")
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print(">>> : ");
            String line = scanner.nextLine();
            boolean thereIsInput = processCommand(line, scanner);
            if (!thereIsInput) {
                break;
            }
        }
    }

    boolean processCommand(String userInput, Scanner scanner) {
        String trimmedInput = userInput == null ? "" : userInput.trim();
        if (trimmedInput.isEmpty()) {
            System.out.println("enter a command please");
            return true;
        }

        String[] tokens = trimmedInput.split("\\s+");
        String command = tokens[0].toLowerCase();

        return switch (command) {
            case "help" -> {
                printHelp();
                yield true;
            }
            case "join" -> {
                handleJoin(scanner);
                yield true;
            }
            case "observe" -> {
                handleObserve(scanner);
                yield true;
            }
            case "create" -> {
                handleCreate(scanner);
                yield true;
            }
            case "list" -> {
                handleList();
                yield true;
            }
            case "logout" -> {
                handleLogout();
                yield false;
            }
            default -> {
                System.out.println("invalid token, type 'help' for a list of commands");
                yield true;
            }
        };
    }



    private void handleJoin(Scanner scanner) {
        System.out.print("Enter game number: ");
        String gameNumberInput = scanner.nextLine().trim();
        Integer gameID = parseGameID(gameNumberInput);
        if (gameID == null) {
            System.out.println("game number has to be an integer");
            return;
        }

        System.out.print("Enter color (WHITE/BLACK): ");
        String colorInput = scanner.nextLine().trim().toUpperCase();
        if (!colorInput.equals("WHITE") && !colorInput.equals("BLACK")) {
            System.out.println("color must be WHITE or BLACK");
            return;
        }

        try {
            serverFacade.joinGame(currentSession.authToken(), colorInput, gameID);
            System.out.printf("Joined game %d as %s%n", gameID, colorInput);
        } catch (ServerFacade.ServerFacadeException exception) {
            System.out.println("join failed -> " + exception.getMessage());
            return;
        }

        boolean isWhite = colorInput.equals("WHITE");

        ChessGame game = new ChessGame();
        BoardPrinter.printBoard(game.getBoard(), isWhite);
    }


    private void handleObserve(Scanner scanner) {
        System.out.print("Enter game number: ");
        String gameNumberInput = scanner.nextLine().trim();
        Integer gameID = parseGameID(gameNumberInput);
        if (gameID == null) {
            System.out.println("game number has to be a whole number");
            return;
        }

        System.out.println("Observing game " + gameID);

        ChessGame game = new ChessGame();
        BoardPrinter.printBoard(game.getBoard(), true);
    }

    private void handleCreate(Scanner scanner) {
        System.out.print("Enter game name: ");
        String gameName = scanner.nextLine().trim();
        if (gameName.isEmpty()) {
            System.out.println("game name cannot be empty");
            return;
        }
        try {
            ServerFacade.CreateGameResult result = serverFacade.createGame(currentSession.authToken(), gameName);
            System.out.println("Created game " + result.gameID() + " -> " + gameName);
        } catch (ServerFacade.ServerFacadeException exception) {
            System.out.println("create failed -> " + exception.getMessage());
        }
    }

    private void handleList() {
        try {
            ServerFacade.ListGamesResult listGamesResult = serverFacade.listGames(currentSession.authToken());
            if (listGamesResult.games() == null || listGamesResult.games().isEmpty()) {
                System.out.println("No games found");
                return;
            }
            for (ServerFacade.ListGamesResult.GameSummary game : listGamesResult.games()) {
                System.out.println(formatGameSummary(game));
            }
        } catch (ServerFacade.ServerFacadeException exception) {
            System.out.println("list failed -> " + exception.getMessage());
        }
    }

    private void handleLogout() {
        try {
            serverFacade.logout(currentSession.authToken());
            System.out.println("Logged out... peace");
        } catch (ServerFacade.ServerFacadeException exception) {
            System.out.println("logout failed -> " + exception.getMessage());
        }
    }

    private Integer parseGameID(String gameIDInput) {
        try {
            return Integer.parseInt(gameIDInput);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String formatGameSummary(ServerFacade.ListGamesResult.GameSummary gameSummary) {
        String white = gameSummary.whiteUsername() == null ? "-" : gameSummary.whiteUsername();
        String black = gameSummary.blackUsername() == null ? "-" : gameSummary.blackUsername();
        return String.format(
                "%d) %s [white: %s, black: %s]",
                gameSummary.gameID(),
                gameSummary.gameName(),
                white,
                black
        );
    }

    private void printHelp() {
        System.out.println("help    - show this message");
        System.out.println("create  - create a new game");
        System.out.println("join  - join an existing game");
        System.out.println("list  - list all games");
        System.out.println("observe  - watch a game");
        System.out.println("logout  - sign out");
    }
}
