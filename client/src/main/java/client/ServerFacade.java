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
        String registrationJson = gson.toJson(registerRequest);
        HttpRequest registrationRequest = buildRegistrationRequest(registrationJson);
        HttpResponse<String> registrationResponse = send(registrationRequest, "register user");

        int statusCode = registrationResponse.statusCode();
        String registrationResponseJson = registrationResponse.body();
        boolean isSuccessStatus = statusCode >= 200 && statusCode < 300;
        if (isSuccessStatus) {
            RegisterResult registerResult = gson.fromJson(registrationResponseJson, RegisterResult.class);
            return registerResult;
        }

        String errorMessage = extractErrorMessage(registrationResponseJson);
        throw new ServerFacadeException(statusCode, errorMessage);
    }
    public LoginResult login(String username, String password) {
        LoginRequest loginRequest = new LoginRequest(username, password);
        String loginJson = gson.toJson(loginRequest);
        HttpRequest request = buildLoginRequest(loginJson);
        HttpResponse<String> loginResponse = send(request, "login user");
    
        int statusCode = loginResponse.statusCode();
        String loginResponseJson = loginResponse.body();
        boolean isSuccessStatus = statusCode >= 200 && statusCode < 300;
        if (isSuccessStatus) {
            LoginResult loginResult = gson.fromJson(loginResponseJson, LoginResult.class);
            return loginResult;
        }
    
        String errorMessage = extractErrorMessage(loginResponseJson);
        throw new ServerFacadeException(statusCode, errorMessage);
    }


    public void clear() {
        HttpRequest clearRequest = buildClearRequest();
        HttpResponse<String> clearResponse = send(clearRequest, "clear database");

        int statusCode = clearResponse.statusCode();
        boolean isSuccessStatus = statusCode >= 200 && statusCode < 300;
        if (isSuccessStatus) return;

        String errorMessage = extractErrorMessage(clearResponse.body());
        throw new ServerFacadeException(statusCode, errorMessage);
    }

    private HttpRequest buildRegistrationRequest(String registrationJson) {
        String registrationUrl = baseUrl + REGISTER_PATH;
        URI registrationUri = URI.create(registrationUrl);
        Duration requestTimeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
        HttpRequest.BodyPublisher requestBodyPublisher = HttpRequest.BodyPublishers.ofString(registrationJson);

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder = requestBuilder.uri(registrationUri);
        requestBuilder = requestBuilder.timeout(requestTimeout);
        requestBuilder = requestBuilder.header("Content-Type", "application/json");

        HttpRequest request = requestBuilder.POST(requestBodyPublisher).build();
        return request;
    }

    private HttpRequest buildLoginRequest(String loginJson) {
        String loginUrl = baseUrl + LOGIN_PATH;
        URI loginUri = URI.create(loginUrl);
        Duration requestTimeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);
        HttpRequest.BodyPublisher requestBodyPublisher = HttpRequest.BodyPublishers.ofString(loginJson);
    
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        requestBuilder = requestBuilder.uri(loginUri);
        requestBuilder = requestBuilder.timeout(requestTimeout);
        requestBuilder = requestBuilder.header("Content-Type", "application/json");
    
        HttpRequest request = requestBuilder.POST(requestBodyPublisher).build();
        return request;
    }

    private HttpRequest buildClearRequest() {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder();
        String clearUrl = baseUrl + CLEAR_PATH;
        URI clearUri = URI.create(clearUrl);
        Duration requestTimeout = Duration.ofSeconds(REQUEST_TIMEOUT_SECONDS);

        requestBuilder = requestBuilder.uri(clearUri);
        requestBuilder = requestBuilder.timeout(requestTimeout);
        requestBuilder = requestBuilder.header("Content-Type", "application/json");

        HttpRequest request = requestBuilder.DELETE().build();
        return request;
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
            throw new ServerFacadeException(0, "Network error while attempting to " + action);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerFacadeException(0, "Request interrupted while attempting to " + action);
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
