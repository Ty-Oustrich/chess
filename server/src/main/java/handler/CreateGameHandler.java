package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import service.BadRequestException;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.GameService;
import service.UnauthorizedException;

public class CreateGameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            String requestBody = ctx.body();
            CreateGameRequest createGameRequest = gson.fromJson(requestBody, CreateGameRequest.class);
            boolean isMissingRequest = createGameRequest == null;
            if (isMissingRequest) {
                throw new BadRequestException();
            }
            CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest.gameName());
            String responseJson = gson.toJson(createGameResult);
            ctx.status(200);
            ctx.contentType("application/json");
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
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(errorJson);
    }

    record ErrorResponse(String message) {}
}
