package chess;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {
    private final ChessGame.TeamColor pieceColor;
    private final ChessPiece.PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return ChessGame.TeamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

//    /**
//     * Gets possible moves for a piece at the given location given an empty board
//     *
//     * @param pieceType the piece to get valid moves for
//     * @return Set of possible moves for requested piece given an empty board, or null if no piece at
//     * startPosition
//     */
//    public Collection<ChessMove> getPossibleMoves(ChessPiece.PieceType pieceType, ChessPosition piecePosition){
//
//        return possibleMoves;
//    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves( ChessBoard board, ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);
        ChessPiece.PieceType type = piece.getPieceType();
        Collection<ChessMove> validMoves = new ArrayList<>();

        switch(type){
            case KING:
                Collection<ChessMove> potentialMoves = startPosition.KingMoves();
                break;
            case QUEEN:
                // Calculate Queen moves
                break;
            case BISHOP:
                break;
            case KNIGHT:
                break;
            case ROOK:
                break;
            case PAWN:
                break;
            default:
        }

        // helper that get the pieces possible movements on an empty board.
        //checks if the spots are valid.
        //if valid, add to the collection
        //return the collection
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> piecemoves = new ArrayList<>();
        Collection<ChessMove> possibleMovesOnAnEmptyBoard = validMoves(board, myPosition);
        //check for moves on a playing board...
        // loop through possibleMovesOnAnEmptyBoard
        //check if the spots are already occupied.

        return piecemoves;
    }
}

