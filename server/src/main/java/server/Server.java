package server;

import dataaccess.DataAccess;
import dataaccess.MySqlDataAccess;
import handler.ClearHandler;
import handler.CreateGameHandler;
import handler.JoinGameHandler;
import handler.ListGamesHandler;
import handler.LoginHandler;
import handler.LogoutHandler;
import handler.RegisterHandler;
import io.javalin.*;
import service.ClearService;
import service.GameService;
import service.UserService;
import websocket.ConnectionManager;
import websocket.WebSocketHandler;

public class Server {
    private final Javalin javalin;

    public Server() {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        DataAccess dataAccess = new MySqlDataAccess();
        ClearService clearService = new ClearService(dataAccess);
        UserService userService = new UserService(dataAccess);
        GameService gameService = new GameService(dataAccess);
        ClearHandler clearHandler = new ClearHandler(clearService);
        RegisterHandler registerHandler = new RegisterHandler(userService);
        LoginHandler loginHandler = new LoginHandler(userService);
        LogoutHandler logoutHandler = new LogoutHandler(userService);
        ListGamesHandler listGamesHandler = new ListGamesHandler(gameService);
        CreateGameHandler createGameHandler = new CreateGameHandler(gameService);
        JoinGameHandler joinGameHandler = new JoinGameHandler(gameService);
        ConnectionManager connectionManager = new ConnectionManager();
        WebSocketHandler webSocketHandler = new WebSocketHandler(dataAccess, connectionManager);

        javalin.delete("/db", clearHandler::handle);
        javalin.post("/user", registerHandler::handle);
        javalin.post("/session", loginHandler::handle);
        javalin.delete("/session", logoutHandler::handle);
        javalin.get("/game", listGamesHandler::handle);
        javalin.post("/game", createGameHandler::handle);
        javalin.put("/game", joinGameHandler::handle);
        javalin.ws("/ws", ws -> webSocketHandler.register(ws));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
