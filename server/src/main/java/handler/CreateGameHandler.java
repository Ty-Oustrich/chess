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
            CreateGameResult createGameResult = gameService.createGame(authToken, createGameRequest.gameName());sendSuccess(ctx, createGameResult);
        } catch (JsonSyntaxException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error: bad request"));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error: bad request"));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.json(new ErrorResponse("Error:unauthorized"));
        } catch (Exception e) {
            sendUnexpectedError(ctx, e);
        }
    }

    private void sendSuccess(Context ctx, CreateGameResult result) {
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
