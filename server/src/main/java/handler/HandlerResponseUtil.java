package handler;

import io.javalin.http.Context;

import com.google.gson.Gson;

public final class HandlerResponseUtil {
    private HandlerResponseUtil() {}

    public static void sendError(Context ctx, Gson gson, int statusCode, String message) {
        String errorJson = gson.toJson(new ErrorResponse(message));
        ctx.status(statusCode);
        ctx.contentType("application/json");
        ctx.result(errorJson);
    }

    private record ErrorResponse(String message) {}
}
