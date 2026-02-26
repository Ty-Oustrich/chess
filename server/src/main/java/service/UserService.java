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

    public LoginResult login(LoginRequest request) throws DataAccessException {
        UserData user = dataAccess.getUser(request.username());

        if (user == null) {
            throw new DataAccessException("Error: username was wrong");
        }
        if (!user.password().equals(request.password())) {
            throw new DataAccessException("Error: password was wrong");
        }

        String token = UUID.randomUUID().toString();
        String username = user.username();
        AuthData authData = new AuthData(token, username);
        dataAccess.createAuth(authData);
        
        return new LoginResult(username, token);
    }
}