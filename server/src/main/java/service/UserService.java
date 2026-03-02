package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import java.util.UUID;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public LoginResult login(LoginRequest request) throws DataAccessException, BadRequestException, UnauthorizedException {
        String username = request.username();
        String password = request.password();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadRequestException();
        }

        UserData user = dataAccess.getUser(username);

        if (user == null || !user.password().equals(password)) {
            throw new UnauthorizedException();
        }

        String token = UUID.randomUUID().toString();
        String userNameForToken = user.username();
        AuthData authData = new AuthData(token, userNameForToken);
        dataAccess.createAuth(authData);

        return new LoginResult(userNameForToken, token);
    }
}