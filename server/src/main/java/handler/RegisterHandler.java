package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

public class RegisterHandler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    public void handle(Context ctx) {
        try {
            String requestBody = ctx.body();
            RegisterRequest registerRequest = gson.fromJson(requestBody, RegisterRequest.class);
            if (registerRequest == null) {
                throw new BadRequestException();
            }
            RegisterResult registerResult = userService.register(registerRequest);
            String responseJson = gson.toJson(registerResult);
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result(responseJson);
        } catch (JsonSyntaxException e) {
            HandlerResponseUtil.sendError(ctx, gson, 400, "Error: bad request");
        } catch (BadRequestException e) {
            HandlerResponseUtil.sendError(ctx, gson, 400, "Error: bad request");
        } catch (AlreadyTakenException e) {
            HandlerResponseUtil.sendError(ctx, gson, 403, "Error: already taken");
        } catch (Exception e) {
            HandlerResponseUtil.sendError(ctx, gson, 500, "Error: " + e.getMessage());
        }
    }
}
