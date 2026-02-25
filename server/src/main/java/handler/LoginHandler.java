package handler;

public class LoginHandler {

}

//translates HTTP POST /session requests into LoginRequest objects
// passes request to UserService.login() and serializes the result back to JSON
 //returns 200 with authToken on success or 401 500 on failure
