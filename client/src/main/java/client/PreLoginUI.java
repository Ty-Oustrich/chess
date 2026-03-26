package client;

import java.util.Scanner;


public class PreLoginUI {

    /**
     * Result of one prelogin command: keep reading commands, quit the app, or hand off to postlogin.
     */
    private record PreLoginResult(boolean keepPreloginLoop, SessionData sessionIfLoggedIn) {
        static PreLoginResult continuePrelogin() {
            return new PreLoginResult(true, null);
        }

        static PreLoginResult quitApp() {
            return new PreLoginResult(false, null);
        }

        static PreLoginResult loggedIn(SessionData session) {
            return new PreLoginResult(false, session);
        }
    }

    private final ServerFacade serverFacade;
    private Scanner scanner;

    public PreLoginUI(ServerFacade serverFacade) {
        this.serverFacade = serverFacade;
    }


    SessionData preLoginLoop() {
        System.out.println("Welcome to my chess server, you will need to type commands. Type 'help' for help");
        Scanner scanner = new Scanner(System.in);
        this.scanner = scanner;
        while (true) {
            System.out.print(">>> : ");
            String line = scanner.nextLine();
            PreLoginResult outcome = processCommand(line);
            if (!outcome.keepPreloginLoop()) {
                return outcome.sessionIfLoggedIn();
            }
        }
    }

    void printerPreLogin(){return;}

    PreLoginResult processCommand(String userInput) {
        String trimmedInput = userInput == null ? "" : userInput.trim();
        if (trimmedInput.isEmpty()) {
            System.out.println("enter a command please");
            return PreLoginResult.continuePrelogin();
        }
        String[] tokens = trimmedInput.split("\\s+");
        String command = tokens[0].toLowerCase();
        return switch (command) {
            case "help" -> {
                printHelp();
                yield PreLoginResult.continuePrelogin();
            }
            case "quit" -> PreLoginResult.quitApp();
            case "login" -> handleLogin();
            case "register" -> handleRegister();
            default -> {
                System.out.println("invalid token, type 'help' for a list of commands");
                yield PreLoginResult.continuePrelogin();
            }
        };
    }

    private void printHelp() {
        System.out.println("register - create an account");
        System.out.println("login    - sign in");
        System.out.println("help     - show this message");
        System.out.println("quit     - exit");
    }

    void findCommandKeyword(String userInput){return;}

    void handleRegister(){return;}

    void handleLogin(){return;}


    
}
