package client;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ServerFacade {

    private static final int CONNECT_TIMEOUT_SECONDS = 10;
    private static final int REQUEST_TIMEOUT_SECONDS = 10;
    private static final String REGISTER_PATH = "/user";
    private static final String CLEAR_PATH = "/db";
    private static final String LOGIN_PATH = "/session";
    private static final String GAME_PATH = "/game";

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;

    public ServerFacade(int port) {
        this("localhost", port);
    }

    private HttpRequest buildPostRequest(String path, String bodyJson) {
        return buildPostRequest(path, bodyJson, null);
    }

    private HttpRequest buildPostRequest(String path, String bodyJson, String authToken) {
        URI uri = URI.create(baseUrl + path);
        Duration timeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(bodyJson);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(timeout)
            .header("Content-Type", "application/json");
        if (authToken != null && !authToken.isBlank()) {
            builder.header("authorization", authToken);
        }
        return builder.POST(body).build();

    }

    public ServerFacade(String host, int port) {
        Duration connectTimeout = Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS);
        this.httpClient = HttpClient.newBuilder().connectTimeout(connectTimeout).build();
        this.gson = new Gson();
        this.baseUrl = "http://" + host + ":" + port;
    }

    public RegisterResult register(String username, String password, String email) {
        RegisterRequest registerRequest = new RegisterRequest(username, password, email);
        String payload = gson.toJson(registerRequest);
        HttpRequest request = buildPostRequest(REGISTER_PATH, payload);
        HttpResponse<String> response = send(request, "register user");
        return parseResponse(response, RegisterResult.class);
    }


    public LoginResult login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        return parseResponse(
            send(buildPostRequest(LOGIN_PATH, gson.toJson(loginRequest)), "login"),
            LoginResult.class
        );
    }

    public void logout(String authToken) {
        requireSuccess(send(buildDeleteRequest(LOGIN_PATH, authToken), "logout"));
    }

    public CreateGameResult createGame(String authToken, String gameName) {
        CreateGameRequest createGameRequest = new CreateGameRequest(gameName);
        String requestBody = gson.toJson(createGameRequest);
        HttpRequest request = buildPostRequest(GAME_PATH, requestBody, authToken);
        HttpResponse<String> response = send(request, "create game");
        return parseResponse(response, CreateGameResult.class);
    }

    public ListGamesResult listGames(String authToken) {
        return parseResponse(send(buildGetRequest(GAME_PATH, authToken), "list games"), ListGamesResult.class);
    }

    public void joinGame(String authToken, String playerColor, Integer gameID) {
        JoinGameRequest joinGameRequest = new JoinGameRequest(playerColor, gameID);
        HttpResponse<String> response = send(
            buildPutRequest(GAME_PATH, gson.toJson(joinGameRequest), authToken),
            "join game"
        );
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return;
        }
        throw new ServerFacadeException(statusCode, extractErrorMessage(response.body()));
    }

    public void clear() {
        HttpRequest deleteRequest = buildDeleteRequest(CLEAR_PATH, null);
        requireSuccess(send(deleteRequest, "clear database"));
    }



    private HttpRequest buildDeleteRequest(String path, String authToken) {
        URI uri = URI.create(baseUrl + path);
        Duration timeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);

        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(timeout)
            .header("Content-Type", "application/json");
        if (authToken != null && !authToken.isBlank()) {
            builder.header("authorization", authToken);
        }
        return builder.DELETE().build();
    }

    private HttpRequest buildGetRequest(String path, String authToken) {
        URI uri = URI.create(baseUrl + path);
        Duration timeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(timeout)
            .header("Content-Type", "application/json");
        if (authToken != null && !authToken.isBlank()) {
            builder.header("authorization", authToken);
        }
        return builder.GET().build();
    }

    private HttpRequest buildPutRequest(String path, String bodyJson, String authToken) {
        URI uri = URI.create(baseUrl + path);
        Duration timeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(bodyJson);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
            .uri(uri)
            .timeout(timeout)
            .header("Content-Type", "application/json");
        if (authToken != null && !authToken.isBlank()) {
            builder.header("authorization", authToken);
        }
        return builder.PUT(body).build();
    }

    // parse JSON if response is good, otherwise throw server error
    private <T> T parseResponse(HttpResponse<String> response, Class<T> resultClass) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) {
            return gson.fromJson(response.body(), resultClass);
        }
        throw new ServerFacadeException(statusCode, extractErrorMessage(response.body()));
    }

    private void requireSuccess(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) return;
        throw new ServerFacadeException(statusCode, extractErrorMessage(response.body()));
    }

    private String extractErrorMessage(String responseJson) {
        ErrorResponse registrationError = gson.fromJson(responseJson, ErrorResponse.class);
        if (registrationError != null && registrationError.message() != null) {
            return registrationError.message();
        }
        return "Request failed";
    }

    private HttpResponse<String> send(HttpRequest request, String action) {
        try {
            return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
        } catch (IOException e) {
            throw new ServerFacadeException(0, "network issue during " + action);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerFacadeException(0, "interrupted during " + action);
        }
    }

    public record RegisterRequest(String username, String password, String email) {
    }

    public record RegisterResult(String username, String authToken) {
    }

    public record ErrorResponse(String message) {
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResult(String username, String authToken) {
    }

    public record CreateGameRequest(String gameName) {
    }

    public record CreateGameResult(Integer gameID) {
    }

    public record JoinGameRequest(String playerColor, Integer gameID) {
    }

    public record ListGamesResult(java.util.List<GameSummary> games) {
        public record GameSummary(Integer gameID, String gameName,
             String whiteUsername, String blackUsername) {
        }
    }

    public static class ServerFacadeException extends RuntimeException {
        private final int statusCode;

        public ServerFacadeException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int statusCode() {
            return statusCode;
        }
    }
}
