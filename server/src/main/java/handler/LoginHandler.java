package handler;


import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import service.BadRequestException;
import service.LoginRequest;
import service.LoginResult;
import service.UnauthorizedException;
import service.UserService;


public class LoginHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }
    
    public void handle(Context ctx) {
        try {
            String requestBody = ctx.body();
            LoginRequest request = gson.fromJson(requestBody, LoginRequest.class);
            boolean isMissingRequest = request == null;
            if (isMissingRequest) {
                throw new BadRequestException();
            }
            LoginResult result = userService.login(request);
            sendSuccess(ctx, result);
        } catch (JsonSyntaxException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error: bad request"));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error: bad request"));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.json(new ErrorResponse("Error: unauthorized"));
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

    record ErrorResponse(String message) {}
}

//translates HTTP POST /session requests into LoginRequest objects
// passes request to UserService.login() and serializes the result back to JSON
 //returns 200 with authToken on success or 401 500 on failure
