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
            String responseJson = gson.toJson(result);
            ctx.contentType("application/json");
            ctx.status(200);
            ctx.result(responseJson);
        } catch (JsonSyntaxException e) {
            sendError(ctx, 400, "Error: bad request");
        } catch (BadRequestException e) {
            sendError(ctx, 400, "Error: bad request");
        } catch (UnauthorizedException e) {
            sendError(ctx, 401, "Error: unauthorized");
        } catch (Exception e) {
            sendError(ctx, 500, "Error: " + e.getMessage());
        }
    }

    private void sendError(Context ctx, int status, String message) {
        String errorJson = gson.toJson(new ErrorResponse(message));
        ctx.contentType("application/json");
        ctx.status(status);
        ctx.result(errorJson);
    }

    record ErrorResponse(String message) {}
}
