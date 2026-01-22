package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor TeamTurn;

    public ChessGame() {
        this.TeamTurn = TeamColor.WHITE;
    }


    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return TeamTurn;
        //this might need some work
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {

        TeamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessBoard board = getBoard();
        ChessPiece piece = board.getPiece(startPosition);
        ChessPiece.PieceType type = piece.getPieceType();
        ChessGame.TeamColor color = getTeamTurn();
        Collection<ChessMove> validMoves = new ArrayList<>();
        validMoves = piece.pieceMoves(board, startPosition);

        for(ChessMove move : validMoves) {
        if(isInCheck() || occupiedBySameTeam(color) || pathBlocked()) {
            validMoves.remove(move);
            }
        }


            //checks if the spots are valid.
            //to check if filled with team color, if blocked
            //if valid, add to the collection
            //return the collection
    }

    private boolean pathBlocked() {
        //logic here or in the piecemoves function
    }

    private boolean occupiedBySameTeam(ChessGame.TeamColor color) {

    }





    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        //
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {

        board.resetBoard();
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return new ChessBoard();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return TeamTurn == chessGame.TeamTurn;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(TeamTurn);
    }
}
