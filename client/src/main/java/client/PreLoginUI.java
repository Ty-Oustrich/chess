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


    private PreLoginResult handleRegister() {
        System.out.print("Enter username, password, and email ");
        String[] parts = scanner.nextLine().trim().split("\\s+");
        if (parts.length != 3) {
            System.out.println("Please provide {username} {password} {email}");
            return PreLoginResult.continuePrelogin();
        }
        String username = parts[0];
        String password = parts[1];
        String email = parts[2];
        try {
            ServerFacade.RegisterResult result = serverFacade.register(username, password, email);
            SessionData session = new SessionData(result.username(), result.authToken());
            System.out.println("Registration successful!");
            return PreLoginResult.loggedIn(session);
        } catch (ServerFacade.ServerFacadeException e) {
            System.out.println("registration failed -> " + e.getMessage());
            return PreLoginResult.continuePrelogin();
        }
    }


    private PreLoginResult handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();
        if (username.isEmpty() || password.isEmpty()) {
            System.out.println("Username and password cannot be empty.");
            return PreLoginResult.continuePrelogin();
        }
        try {
            ServerFacade.LoginResult result = serverFacade.login(username, password);
            SessionData session = new SessionData(result.username(), result.authToken());
            System.out.println("Login successful!");
            return PreLoginResult.loggedIn(session);
        } catch (ServerFacade.ServerFacadeException e) {
            System.out.println("Login failed: " + e.getMessage());
            return PreLoginResult.continuePrelogin();
        }
    }
}
