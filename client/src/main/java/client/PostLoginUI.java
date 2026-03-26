







package client;

import java.util.Scanner;



public class PostLoginUI {
    void postLoginLoop(SessionData session) {
        System.out.println("Logged in as " + session.username());
        System.out.println("Type 'help' for help");

        @SuppressWarnings("resource") // Scanner would close System.in; we keep it open for the whole app
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print(">>> : ");
            String line = scanner.nextLine();
            boolean thereIsInput = processCommand(line);
            if (!thereIsInput) break;
        }
    }

    boolean processCommand(String userInput) {
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
            case "logout" -> {
                //implement
                System.out.println("Logged out... peace");
                yield false;
            }
            default -> {
                System.out.println("invalid token, type 'help' for a list of commands");
                yield true;
            }
        };
    }


    private void printHelp() {
        System.out.println("help  - show this message");

        System.out.println("create  - create a new game");
        System.out.println("join  - join an existing game");
        System.out.println("list  - list all games");
        System.out.println("observe  - watch a game");
        System.out.println("logout  - sign out");
    }
}
