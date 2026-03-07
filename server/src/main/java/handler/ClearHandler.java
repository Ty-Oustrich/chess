package handler;

import com.google.gson.Gson;
import io.javalin.http.Context;
import service.ClearService;

public class ClearHandler {
    private final ClearService clearService;
    private final Gson gson = new Gson();

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    public void handle(Context ctx) {
        try {
            clearService.clear();
            ctx.status(200);
            ctx.contentType("application/json");
            ctx.result("{}");
        } catch (Exception e) {
            String errorMessage = "Error: " + e.getMessage();
            String errorJson = gson.toJson(new ErrorResponse(errorMessage));
            ctx.status(500);
            ctx.contentType("application/json");
            ctx.result(errorJson);
        }
    }

    record ErrorResponse(String message) {}
}
