package handler;


import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.LoginRequest;
import service.UserService;
import java.util.Map;

public class LoginHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }
    
    public void handle(Context ctx) {
        try {
            LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
            LoginResult result = userService.login(request);
            sendSuccess(ctx, result);
        } catch (DataAccessException e) {
            sendError(ctx, e);
        } catch (Exception e) {
            sendUnexpectedError(ctx, e);
        }
    }
    
    private void sendSuccess(Context ctx, LoginResult result) {
        ctx.status(200);
        ctx.json(result);
    }
    
    private void sendUnexpectedError(Context ctx, Exception e) {
        String errorMessage = "Error: " + e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        ctx.status(500);
        ctx.json(errorResponse);
    }

    private void sendError(Context ctx, DataAccessException e) {
        String errorMessage = e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
    
        if (errorMessage.equals("Error: unauthorized")) {
            ctx.status(401);
        } else {
            ctx.status(500);
        }
    
        ctx.json(errorResponse);
    }

}

//translates HTTP POST /session requests into LoginRequest objects
// passes request to UserService.login() and serializes the result back to JSON
 //returns 200 with authToken on success or 401 500 on failure
