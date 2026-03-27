package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.UnauthorizedException;
import service.UserService;

public class LogoutHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

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
            sendError(ctx, 401, "Error: unauthorized");
        } catch (Exception e) {
            sendError(ctx, 500, "Error: " + e.getMessage());
        }
    }

    private void sendError(Context ctx, int statusCode, String message) {
        String errorJson = gson.toJson(new ErrorResponse(message));
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(errorJson);
    }

    record ErrorResponse(String message) {}
}
