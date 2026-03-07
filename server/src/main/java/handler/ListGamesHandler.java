package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.GameService;
import service.ListGamesResult;
import service.UnauthorizedException;

public class ListGamesHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            ListGamesResult listGamesResult = gameService.listGames(authToken);
            String responseJson = gson.toJson(listGamesResult);
            ctx.contentType("application/json");
            ctx.status(200);
            ctx.result(responseJson);
        } catch (UnauthorizedException e) {
            String errorJson = gson.toJson(new ErrorResponse("Error: unauthorized"));
            ctx.contentType("application/json");
            ctx.status(401);
            ctx.result(errorJson);
        } catch (Exception e) {
            String errorMessage = "Error: " + e.getMessage();
            String errorJson = gson.toJson(new ErrorResponse(errorMessage));
            ctx.contentType("application/json");
            ctx.status(500);
            ctx.result(errorJson);
        }
    }

    record ErrorResponse(String message) {}
}
