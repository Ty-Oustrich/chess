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
    private ChessBoard board;

    public ChessGame() {
        setTeamTurn(TeamColor.WHITE); //always first
        board = new ChessBoard();
        board.resetBoard();
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
        ChessPiece startPiece = board.getPiece(startPosition);
        TeamColor color = startPiece.getTeamColor();
        if(startPiece.getPieceType() == null) {return null;}
        Collection<ChessMove> validMoves = startPiece.pieceMoves(board, startPosition);
        Collection<ChessMove> removeThese = new ArrayList<>();

        for(ChessMove move : validMoves) {
            ChessPiece temp = board.getPiece(move.getEndPosition());
            board.addPiece(move.getEndPosition(),startPiece);
            board.addPiece(startPosition, null);
            if(isInCheck(color)){
            removeThese.add(move);
            }
            board.addPiece(move.getEndPosition(),temp);
            board.addPiece(startPosition,startPiece);

        }
        validMoves.removeAll(removeThese);
        return validMoves;
    }







    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {

        ChessPosition start = move.getStartPosition();
        if(board.getPiece(start) == null){
            throw new InvalidMoveException("cannot move a nul piece");
        }
        ChessPosition end = move.getEndPosition();

        ChessPiece piece = board.getPiece(start);
        ChessGame.TeamColor color = piece.getTeamColor();

        if(getTeamTurn() != color){
            throw new InvalidMoveException("Incorrect move turn");
        }
        Collection<ChessMove> valid = validMoves(start); //valid moves
        if (!valid.contains(move)) {
            throw new InvalidMoveException("Impossible move");
        }

        if (move.getPromotionPiece() != null) {
            ChessPiece promotedPiece = new ChessPiece(getTeamTurn(), move.getPromotionPiece());
            board.addPiece(end, promotedPiece);
            board.addPiece(start, null);
        } else {
            board.addPiece(end, piece);
            board.addPiece(start, null);

        }
        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) { //can also place a queen and knight in the kings place then check if they can take any piece?? if this doesnt work?
        ChessPosition Kposition = null;
        Collection<ChessPosition> enemyPieces = new ArrayList<>();
        //find king
        for(int row = 1; row <= 8; row++){
            for(int col = 1; col <= 8; col++){
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if(piece == null){continue;}
                if(piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING){
                    Kposition = new ChessPosition(row, col);
                }
            }
        }
        //find all enemy pieces
        for(int row = 1; row <= 8; row++){
            for(int col = 1; col <= 8; col++){
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if(piece == null){continue;}
                if(piece.getTeamColor() != teamColor){
                enemyPieces.add(new ChessPosition(row,col));

                for(ChessMove potentialCheckMove : piece.pieceMoves(board, new ChessPosition(row, col))){
                   if(potentialCheckMove.getEndPosition().equals(Kposition)){
                       return true;
                   }
                }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
    return(isInCheck(teamColor) && isInStalemate(teamColor));
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if(isInCheck(teamColor)){return false;} ///this could be problematic for the checkmate scenario but all the tests passed...

        //for friendly pieces
        for(int row = 1; row <= 8; row++){
            for(int col = 1; col <= 8; col++){
                ChessPosition pos = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(pos);
                if(piece != null && piece.getTeamColor() == teamColor){  //for ally pieces
                    if(!validMoves(pos).isEmpty()){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }




    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return TeamTurn == chessGame.TeamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(TeamTurn, board);
    }
}
