package handler;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.GameService;
import service.JoinGameRequest;
import service.UnauthorizedException;

public class JoinGameHandler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    public void handle(Context ctx) {
        try {
            String authToken = ctx.header("authorization");
            String requestBody = ctx.body();
            JoinGameRequest joinGameRequest = gson.fromJson(requestBody, JoinGameRequest.class);
            boolean isMissingRequest = joinGameRequest == null;
            if (isMissingRequest) {
                throw new BadRequestException();
            }
            gameService.joinGame(authToken, joinGameRequest.playerColor(), joinGameRequest.gameID());
            ctx.contentType("application/json");
            ctx.status(200);
            ctx.result("{}");
        } catch (JsonSyntaxException e) {
            sendError(ctx, 400, "Error: bad request");
        } catch (BadRequestException e) {
            sendError(ctx, 400, "Error: bad request");
        } catch (UnauthorizedException e) {
            sendError(ctx, 401, "Error: unauthorized");
        } catch (AlreadyTakenException e) {
            sendError(ctx, 403, "Error: already taken");
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
