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
            String errorJson = gson.toJson(new ErrorResponse("Error: unauthorized"));
            ctx.status(401);
            ctx.contentType("application/json");
            ctx.result(errorJson);
        } catch (Exception e) {
            String errorMessage = "Error: " + e.getMessage();
            String errorJson = gson.toJson(new ErrorResponse(errorMessage));
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(errorJson);
        }
    }

    record ErrorResponse(String message) {}
}
