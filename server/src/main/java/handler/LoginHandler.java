package handler;

public class LoginHandler {
    LoginRequest request = (LoginRequest)gson.fromJson(reqData, LoginRequest.class);

LoginService service = new LoginService();
LoginResult result = service.login(request);

return gson.toJson(result);

}
