package service;

public record LoginRequest(String username, String password) {
    
}

//the request to login a user
//contains the username and password of the user
//used by LoginHandler to pass the request to the UserService
//validated by LoginHandler before passing to UserService