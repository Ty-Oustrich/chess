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

    private final HttpClient httpClient;
    private final Gson gson;
    private final String baseUrl;

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
        HttpResponse<String> registrationResponse = send(registrationRequest);

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

    private String extractErrorMessage(String responseJson) {
        ErrorResponse registrationError = gson.fromJson(responseJson, ErrorResponse.class);
        if (registrationError != null && registrationError.message() != null) {
            return registrationError.message();
        }
        return "Request failed";
    }

    private HttpResponse<String> send(HttpRequest request) {
        try {
            HttpResponse<String> response = httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
            );
            return response;
        } catch (IOException e) {
            throw new ServerFacadeException(0, "Network error while registering user");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ServerFacadeException(0, "Request interrupted during registration");
        }
    }

    public record RegisterRequest(String username, String password, String email) {
    }

    public record RegisterResult(String username, String authToken) {
    }

    public record ErrorResponse(String message) {
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
