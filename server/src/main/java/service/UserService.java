package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;
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

        boolean isPasswordValid = verifyUser(username, password);
        boolean isUnauthorized = !isPasswordValid;
        if (isUnauthorized) {
            throw new UnauthorizedException();
        }

        UserData user = dataAccess.getUser(username);
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

        String passwordHash = hashPassword(password);
        UserData userToCreate = new UserData(username, passwordHash, email);
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


    private static String hashPassword(String clearTextPassword) {
        String salt = BCrypt.gensalt();
        String passwordHash = BCrypt.hashpw(clearTextPassword, salt);
        return passwordHash;
    }


    private boolean verifyUser(String username, String providedClearTextPassword) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        boolean isMissingUser = user == null;
        if (isMissingUser) {
            return false;
        }

        String hashedPassword = user.password();
        boolean isValid = BCrypt.checkpw(providedClearTextPassword, hashedPassword);
        return isValid;
    }
}