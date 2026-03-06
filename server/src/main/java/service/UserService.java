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
        boolean isMissingRequest = request == null;
        if (isMissingRequest) {
            throw new BadRequestException();
        }

        String username = request.username();
        String password = request.password();

        boolean isMissingUsername = username == null || username.isBlank();
        boolean isMissingPassword = password == null || password.isBlank();
        boolean isBadRequest = isMissingUsername || isMissingPassword;
        if (isBadRequest) {
            throw new BadRequestException();
        }

        UserData user = dataAccess.getUser(username);

        boolean isMissingUser = user == null;
        boolean isWrongPassword = !isMissingUser && !user.password().equals(password);
        boolean isUnauthorized = isMissingUser || isWrongPassword;
        if (isUnauthorized) {
            throw new UnauthorizedException();
        }

        String token = UUID.randomUUID().toString();
        String userNameForToken = user.username();
        AuthData authData = new AuthData(token, userNameForToken);
        dataAccess.createAuth(authData);

        return new LoginResult(userNameForToken, token);
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException, BadRequestException, AlreadyTakenException {
        boolean isMissingRequest = request == null;
        if (isMissingRequest) {
            throw new BadRequestException();
        }

        String username = request.username();
        String password = request.password();
        String email = request.email();

        boolean isMissingUsername = username == null || username.isBlank();
        boolean isMissingPassword = password == null || password.isBlank();
        boolean isMissingEmail = email == null || email.isBlank();
        boolean isBadRequest = isMissingUsername || isMissingPassword || isMissingEmail;
        if (isBadRequest) {
            throw new BadRequestException();
        }

        UserData existingUser = dataAccess.getUser(username);
        if (existingUser != null) {
            throw new AlreadyTakenException();
        }

        UserData userToCreate = new UserData(username, password, email);
        dataAccess.createUser(userToCreate);
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, username);
        dataAccess.createAuth(authData);

        return new RegisterResult(username, token);
    }

    public void logout(String authToken) throws DataAccessException, UnauthorizedException {
        boolean isMissingAuthToken = authToken == null;
        isMissingAuthToken = isMissingAuthToken || authToken.isBlank();
        if (isMissingAuthToken) {
            throw new UnauthorizedException();
        }

        AuthData existingAuthData = dataAccess.getAuth(authToken);
        boolean isMissingAuthData = existingAuthData == null;
        if (isMissingAuthData) {
            throw new UnauthorizedException();
        }

        dataAccess.deleteAuth(authToken);
    }
}