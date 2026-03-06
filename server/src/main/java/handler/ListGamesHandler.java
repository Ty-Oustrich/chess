package handler;

import io.javalin.http.Context;
import service.GameService;
import service.ListGamesResult;
import service.UnauthorizedException;

public class ListGamesHandler {
    private final GameService gameService;

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListGamesResult listGamesResult = gameService.listGames(authToken);
            sendSuccess(ctx, listGamesResult);
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.json(new ErrorResponse("Error: unauthorized"));
        } catch (Exception e) {
            sendUnexpectedError(ctx, e);
        }
    }

    private void sendSuccess(Context ctx, ListGamesResult result) {
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
