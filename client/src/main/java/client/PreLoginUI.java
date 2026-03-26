package client;

import java.util.Scanner;


public class PreLoginUI {
    void preLoginLoop(){
        System.out.println("Welcome to my chess server, you will need to type commands. Type 'help' for help");
        Scanner scanner = new Scanner(System.in);
        while(true){
            System.out.print(">>> : ");
            String line = scanner.nextLine();
            boolean thereIsInput = processCommand(line);
            if (!thereIsInput) break;
        }
    }

    void printerPreLogin(){return;}

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
            case "quit" -> false;
            
            case "login" -> {
                handleLogin();
                yield true;
            }
            case "register" -> {
                handleRegister();
                yield true;
            }
            default -> {
                System.out.println("invalid token, type 'help' for a list of commands");
                yield true;
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
