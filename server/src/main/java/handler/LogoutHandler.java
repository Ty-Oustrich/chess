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
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
        } catch (UnauthorizedException e) {
            writeError(ctx, 401, "Error: unauthorized");
        } catch (Exception e) {
            writeError(ctx, 500, "Error: " + e.getMessage());
        }
    }

    private void writeError(Context ctx, int statusCode, String message) {
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.json(new ErrorResponse(message));
    }

    record ErrorResponse(String message) {}
}
