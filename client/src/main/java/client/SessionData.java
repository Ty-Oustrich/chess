package client;


//holds session info to pass from pre to post login
public record SessionData(String username, String authToken) {
}
