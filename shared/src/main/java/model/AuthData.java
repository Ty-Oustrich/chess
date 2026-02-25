package model;

public record AuthData(String authToken, String username) {
}

// links an auth token to its username
// created by UserService on successful register or login. Is deleted on logout.
