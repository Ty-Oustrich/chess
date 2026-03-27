package client;

import chess.*;

public class ClientMain {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Client: " + piece);



        ServerFacade serverFacade = new ServerFacade(8080);
        PreLoginUI preLoginUI = new PreLoginUI(serverFacade);
        SessionData session = preLoginUI.preLoginLoop();
        if (session == null) {
            System.out.println("goodbye");
            return;
        }

        System.out.println("You are now logged in.");
        PostLoginUI postLoginUI = new PostLoginUI(serverFacade);
        postLoginUI.postLoginLoop(session);
    }
}
