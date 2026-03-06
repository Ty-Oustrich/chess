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
            sendSuccess(ctx, registerResult);
        } catch (JsonSyntaxException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error:incorrect request"));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error:incorrect request"));
        } catch (AlreadyTakenException e) {
            ctx.status(403);
            ctx.json(new ErrorResponse("Error:already taken"));
        } catch (Exception e) {
            sendUnexpectedError(ctx, e);
        }
    }

    private void sendSuccess(Context ctx, RegisterResult result) {
        ctx.status(200);
        ctx.json(result);
    }

    private void sendUnexpectedError(Context ctx, Exception e) {
        String errorMessage = "Error: " + e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        ctx.status(500);
        ctx.json(errorResponse);
    }

    record ErrorResponse(String message) {
    }
}
