package ui;
import chess.ChessPiece;
import chess.ChessGame;
import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class BoardPrinter {

    public static void printBoard(ChessBoard board, boolean whitePerspective) {
        printBoardWithHighlights(board, whitePerspective, null);
    }

    public static void printBoardWithHighlights(ChessBoard board, boolean whitePerspective, Collection<ChessMove> moves) {
        if (board == null) {
            throw new IllegalArgumentException("board is required");
        }

        System.out.println();
        printColumnHeaders(whitePerspective);
        Set<ChessPosition> highlightedSquares = collectHighlightedSquares(moves);

        if (whitePerspective) {
            for (int row = 8; row >= 1; row--) {
                printBoardRow(board, row, true, highlightedSquares);
            }
        } else {
            for (int row = 1; row <= 8; row++) {
                printBoardRow(board, row, false, highlightedSquares);
            }
        }

        printColumnHeaders(whitePerspective);
        System.out.println();
    }

    private static Set<ChessPosition> collectHighlightedSquares(Collection<ChessMove> moves) {
        Set<ChessPosition> highlightedSquares = new HashSet<>();
        if (moves == null) {
            return highlightedSquares;
        }

        for (ChessMove move : moves) {
            highlightedSquares.add(move.getEndPosition());
        }
        return highlightedSquares;
    }

    private static void printBoardRow(
            ChessBoard board,
            int row,
            boolean whitePerspective,
            Set<ChessPosition> highlightedSquares
    ) {
        StringBuilder line = new StringBuilder();

        // left row label
        line.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        line.append(" ").append(row).append(" ");

        if (whitePerspective) {
            for (int col = 1; col <= 8; col++) {
                appendSquare(line, board, row, col, highlightedSquares);
            }
        } else {
            for (int col = 8; col >= 1; col--) {
                appendSquare(line, board, row, col, highlightedSquares);
            }
        }

        // right row label
        line.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        line.append(" ").append(row).append(" ");

        line.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
        System.out.println(line);
    }

    private static void appendSquare(
            StringBuilder line,
            ChessBoard board,
            int row,
            int col,
            Set<ChessPosition> highlightedSquares
    ) {
        ChessPosition currentPosition = new ChessPosition(row, col);
        String squareBg = getSquareBackground(row, col, currentPosition, highlightedSquares);
        line.append(squareBg);

        ChessPiece piece = board.getPiece(currentPosition);
        if (piece == null) {
            line.append(EMPTY);
            return;
        }

        String textColor;
        if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
            textColor = SET_TEXT_COLOR_RED;
        } else {
            textColor = SET_TEXT_COLOR_BLUE;
        }
        line.append(textColor).append(getPieceSymbol(piece));
    }

    private static String getSquareBackground(int row, int col, ChessPosition currentPosition,
                                              Set<ChessPosition> highlightedSquares) {
        if (highlightedSquares.contains(currentPosition)) {
            return SET_BG_COLOR_YELLOW;
        }

        boolean isLightSquare = (row + col) % 2 != 0;
        return isLightSquare ? SET_BG_COLOR_WHITE : SET_BG_COLOR_DARK_GREEN;
    }


    private static void printColumnHeaders(boolean whitePerspective) {
        StringBuilder header = new StringBuilder();
        header.append(SET_BG_COLOR_DARK_GREY).append(SET_TEXT_COLOR_WHITE);
        header.append("   "); // corner

        if (whitePerspective) {
            for (char column = 'a'; column <= 'h'; column++) {
                header.append("\u2003").append(column).append(" ");
            }
        } else {
            for (char column = 'h'; column >= 'a'; column--) {
                header.append("\u2003").append(column).append(" ");
            }
        }

        header.append("   "); // corner
        header.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR);
        System.out.println(header);
    }

    private static String getPieceSymbol(ChessPiece piece) {
        boolean isWhite = piece.getTeamColor() == ChessGame.TeamColor.WHITE;
        switch (piece.getPieceType()) {
            case KING:
                if (isWhite) {
                    return WHITE_KING;
                }
                return BLACK_KING;
            case QUEEN:
                if (isWhite) {
                    return WHITE_QUEEN;
                }
                return BLACK_QUEEN;
            case BISHOP:
                if (isWhite) {
                    return WHITE_BISHOP;
                }
                return BLACK_BISHOP;
            case KNIGHT:
                if (isWhite) {
                    return WHITE_KNIGHT;
                }
                return BLACK_KNIGHT;
            case ROOK:
                if (isWhite) {
                    return WHITE_ROOK;
                }
                return BLACK_ROOK;
            case PAWN:
                if (isWhite) {
                    return WHITE_PAWN;
                }
                return BLACK_PAWN;
            default:
                throw new IllegalStateException("unknown piece type");
        }
    }
}
