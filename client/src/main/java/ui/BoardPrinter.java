package ui;
import chess.ChessPiece;
import chess.ChessGame;
import chess.ChessBoard;
import chess.ChessPosition;

import static ui.EscapeSequences.*;

public class BoardPrinter {

    public static void printBoard(ChessBoard board, boolean whitePerspective) {
        System.out.println();
        printColumnHeaders(whitePerspective);

        if (whitePerspective) {
            for (int row = 8; row >= 1; row--) {
                printBoardRow(board, row, true);
            }
        } else {
            for (int row = 1; row <= 8; row++) {
                printBoardRow(board, row, false);
            }
        }

        printColumnHeaders(whitePerspective);
        System.out.println();
    }

    private static void printBoardRow(ChessBoard board, int row, boolean whitePerspective) {
        StringBuilder line = new StringBuilder();

        // left row label
        line.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        line.append(" ").append(row).append(" ");

        int colStart = whitePerspective ? 1 : 8;
        int colEnd   = whitePerspective ? 8 : 1;
        int colStep  = whitePerspective ? 1 : -1;

        for (int col = colStart; col != colEnd + colStep; col += colStep) {
            boolean isLightSquare = (row + col) % 2 != 0;
            String squareBg = isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREEN;
            line.append(squareBg);

            ChessPiece piece = board.getPiece(new ChessPosition(row, col));
            if (piece == null) {
                line.append(EMPTY);
            } else {
                String textColor = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                        ? SET_TEXT_COLOR_RED
                        : SET_TEXT_COLOR_BLUE;
                line.append(textColor).append(getPieceSymbol(piece));
            }
        }

        // right row label
        line.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        line.append(" ").append(row).append(" ");

        line.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
        System.out.println(line);
    }


    private static void printColumnHeaders(boolean whitePerspective) {
        StringBuilder header = new StringBuilder();
        header.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        header.append("   "); // corner

        String[] columns;
        if (whitePerspective) {
            columns = new String[]{"a", "b", "c", "d", "e", "f", "g", "h"};
        } else {
            columns = new String[]{"h", "g", "f", "e", "d", "c", "b", "a"};
        }

        for (String colLetter : columns) {
            header.append("\u2003").append(colLetter).append(" "); //stupid em space for alignment
        }

        header.append("   "); // corner
        header.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
        System.out.println(header);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;

        return switch (piece.getPieceType()) {
            case KING   -> isWhite ? WHITE_KING   : BLACK_KING;
            case QUEEN  -> isWhite ? WHITE_QUEEN  : BLACK_QUEEN;
            case BISHOP -> isWhite ? WHITE_BISHOP : BLACK_BISHOP;
            case KNIGHT -> isWhite ? WHITE_KNIGHT : BLACK_KNIGHT;
            case ROOK   -> isWhite ? WHITE_ROOK   : BLACK_ROOK;
            case PAWN   -> isWhite ? WHITE_PAWN   : BLACK_PAWN;
        };
    }
}
