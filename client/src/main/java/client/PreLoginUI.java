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
        if (trimmedInput.equalsIgnoreCase("quit")) return false;
        return true;
    }

    void findCommandKeyword(String userInput){return;}

    void handleRegister(){return;}

    void handleLogin(){return;}


    
}
