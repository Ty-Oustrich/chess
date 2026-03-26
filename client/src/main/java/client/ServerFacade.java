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

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;

    public ServerFacade(int port) {
        this("localhost", port);
    }

    private HttpRequest buildPostRequest(String path, String bodyJson) {
        URI uri = URI.create(baseUrl + path);
        Duration timeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(bodyJson);

        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .POST(body)
                .build();

    }

    public ServerFacade(String host, int port) {
        HttpClient.Builder httpClientBuilder = HttpClient.newBuilder();
        Duration connectTimeout = Duration.ofSeconds(CONNECT_TIMEOUT_SECONDS);
        httpClientBuilder = httpClientBuilder.connectTimeout(connectTimeout);
        HttpClient builtHttpClient = httpClientBuilder.build();

        this.httpClient = builtHttpClient;
        this.gson = new Gson();



        String builtBaseUrl = "http://" + host + ":" + port;
        this.baseUrl = builtBaseUrl;
    }

    public RegisterResult register(String username, String password, String email) {
        RegisterRequest registerRequest = new RegisterRequest(username, password, email);
        HttpRequest request = buildPostRequest(REGISTER_PATH, gson.toJson(registerRequest));
        HttpResponse<String> response = send(request, "register user");
        return parseResponse(response, RegisterResult.class);
    }

    public LoginResult login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        HttpRequest request = buildPostRequest(LOGIN_PATH, gson.toJson(loginRequest));
        HttpResponse<String> response = send(request, "login user");
        return parseResponse(response, LoginResult.class);
    }

    public void clear() {
        HttpRequest request = buildDeleteRequest(CLEAR_PATH);
        HttpResponse<String> response = send(request, "clear database");
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300) return;
        throw new ServerFacadeException(statusCode, extractErrorMessage(response.body()));
    }



    private HttpRequest buildDeleteRequest(String path) {
        URI uri = URI.create(baseUrl + path);
        Duration timeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);

        return HttpRequest.newBuilder()
                .uri(uri)
                .timeout(timeout)
                .header("Content-Type", "application/json")
                .DELETE()
                .build();
    }

    /** Parses a good response into  given type or throws error code. */
    private <T> T parseResponse(HttpResponse<String> response, Class<T> resultClass) {
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 300)
            return gson.fromJson(response.body(), resultClass);
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
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            return response;
        } catch (IOException e) {
            throw new ServerFacadeException(0, "error while attempting to " + action);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerFacadeException(0, "request interrupted while attempting to " + action);
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
