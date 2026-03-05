package handler;

import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void handle(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.json(new Object() {});
        } catch (Exception e) {
            ctx.status(500);
            ctx.json(new ErrorResponse("Error: " + e.getMessage()));
        }
    }

    record ErrorResponse(String message) {}
}
