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
        if (request == null) throw new BadRequestException();

        String username = request.username();
        String password = request.password();

        if (username == null || username.isBlank() || password == null || password.isBlank())
            throw new BadRequestException();

        if (!verifyUser(username, password)) throw new UnauthorizedException();

        String token = UUID.randomUUID().toString();
        dataAccess.createAuth(new AuthData(token, username));

        return new LoginResult(username, token);
    }

    public RegisterResult register(RegisterRequest request)
            throws DataAccessException, BadRequestException, AlreadyTakenException {
        if (request == null) throw new BadRequestException();

        String username = request.username();
        String password = request.password();
        String email = request.email();

        if (username == null || username.isBlank() || password == null || password.isBlank() || email == null || email.isBlank())
            throw new BadRequestException();

        if (dataAccess.getUser(username) != null) throw new AlreadyTakenException();

        String passwordHash = hashPassword(password);
        UserData userToCreate = new UserData(username, passwordHash, email);
        dataAccess.createUser(userToCreate);
        String token = UUID.randomUUID().toString();
        AuthData authData = new AuthData(token, username);
        dataAccess.createAuth(authData);

        return new RegisterResult(username, token);
    }

    public void logout(String authToken) throws DataAccessException, UnauthorizedException {
        if (authToken == null || authToken.isBlank()) throw new UnauthorizedException();
        if (dataAccess.getAuth(authToken) == null) throw new UnauthorizedException();

        dataAccess.deleteAuth(authToken);
    }


    private static String hashPassword(String textPassword) {
        return BCrypt.hashpw(textPassword, BCrypt.gensalt());
    }


    private boolean verifyUser(String username, String providedTextPassword) throws DataAccessException {
        UserData user = dataAccess.getUser(username);
        if (user == null) return false;
        return BCrypt.checkpw(providedTextPassword, user.password());
    }
}