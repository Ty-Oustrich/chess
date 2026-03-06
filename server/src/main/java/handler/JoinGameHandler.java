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
            sendSuccess(ctx);
        } catch (JsonSyntaxException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error: bad request"));
        } catch (BadRequestException e) {
            ctx.status(400);
            ctx.json(new ErrorResponse("Error: bad request"));
        } catch (UnauthorizedException e) {
            ctx.status(401);
            ctx.json(new ErrorResponse("Error: unauthorized"));
        } catch (AlreadyTakenException e) {
            ctx.status(403);
            ctx.json(new ErrorResponse("Error: already taken"));
        } catch (Exception e) {
            sendUnexpectedError(ctx, e);
        }
    }

    private void sendSuccess(Context ctx) {
        ctx.status(200);
        ctx.json(new EmptyResponse());
    }

    private void sendUnexpectedError(Context ctx, Exception e) {
        String errorMessage = "Error: " + e.getMessage();
        ErrorResponse errorResponse = new ErrorResponse(errorMessage);
        ctx.status(500);
        ctx.json(errorResponse);
    }

    record EmptyResponse() {
    }

    record ErrorResponse(String message) {
    }
}
