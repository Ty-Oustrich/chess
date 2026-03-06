package handler;

import io.javalin.http.Context;
import service.UnauthorizedException;
import service.UserService;

public class LogoutHandler {
    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            userService.logout(authToken);
            sendSuccess(ctx);
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.json(new ErrorResponse("Error: unauthorized"));
        } catch (Exception e) {
            sendUnexpectedError(ctx, e);
        }
    }

    private void sendSuccess(Context ctx) {
        ctx.status(200);
        ctx.json(new EmptyResponse());
    }

    private void sendUnexpectedError(Context ctx, Exception e) {
        String errorMessage = "Error: " + e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        ctx.status(500);
        ctx.json(errorResponse);
    }

    record EmptyResponse() {}
    record ErrorResponse(String message) {}
}
//translates HTTP DELETE /session requests and gets the authToken from the header.
