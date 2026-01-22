package chess;

import java.util.*;

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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return Objects.equals(pieceColor, that.pieceColor) && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
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
        return pieceColor;
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
    /**
     * @return Set of valid moves for requested piece, or null if no piece at startPosition
     */


    /**
     * @return true if the proposed row/col is within board bounds.
     */
    private boolean inBounds(int col, int row) {
        return col >= 1 && col <= 8 && row >= 1 && row <= 8;
    }

    /**
     * @return Collection of king moves from the start position on an empty board.
     */
    public Collection<ChessMove> KingMoves(ChessBoard board, ChessPosition startPosition) {
        int[][] allDirections = {{-1,-1},{-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        Collection<ChessMove> possible_moves = new ArrayList<>();

        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();
        for(int i=1; i<8; i++){
            int x = allDirections[i][0];
            int y = allDirections[i][1];
            if (inBounds(currentCol + x,currentRow + y)){
                possible_moves.add(new ChessMove(startPosition,new ChessPosition(currentCol+x,currentRow+y), null));
            }
        }
        return possible_moves;
    }

    /**
     * @return Collection of rook moves from the start position on an empty board.
     */
    public Collection<ChessMove> RookMoves(ChessBoard board, ChessPosition startPosition) {
        int [][] rookDirs = {{1,0},{-1,0},{0,1}, {0,-1}};
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> possible_moves = new ArrayList<>();

        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();
        for (int[] rookDir : rookDirs) {
            int x = rookDir[0];
            int y = rookDir[1];
            int nextCol = currentCol + x;
            int nextRow = currentRow + y;
            while (inBounds(nextCol, nextRow)) {
                ChessPosition move = new ChessPosition(nextRow, nextCol);
                ChessPiece targetPiece = board.getPiece(move);
                if (targetPiece != null) { //if piece is filled
                    if (targetPiece.pieceColor != currPiece.pieceColor) { //if different color
                        possible_moves.add(new ChessMove(startPosition, move, null));
                    }
                    break;
                }
                possible_moves.add(new ChessMove(startPosition, move, null));
                nextCol += x;
                nextRow += y;
            }
        }
        return possible_moves;
    }

    /**
     * @return Collection of bishop moves from the start position on an empty board.
     */
    public Collection<ChessMove> BishopMoves(ChessBoard board, ChessPosition startPosition) {
        int[][] bishDirs = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> possible_moves = new ArrayList<>();

        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();
        for (int[] BDir : bishDirs) {
            int x = BDir[0];
            int y = BDir[1];
            int nextCol = currentCol + x;
            int nextRow = currentRow + y;
            while (inBounds(nextCol, nextRow)) {
                ChessPosition move = new ChessPosition(nextRow, nextCol);
                ChessPiece targetPiece = board.getPiece(move);
                if (targetPiece != null) {
                    if (targetPiece.pieceColor != currPiece.pieceColor) {
                        possible_moves.add(new ChessMove(startPosition, move, null));
                    }
                    break;
                }
                possible_moves.add(new ChessMove(startPosition, move, null));
                nextCol += x;
                nextRow += y;
            }
        }
        return possible_moves;
        }

    /**
     * @return Collection of queen moves from the start position on an empty board.
     */
    public Collection<ChessMove> QueenMoves(ChessBoard board, ChessPosition startPosition) {
        int[][] QueenDirs = {{-1,-1},{-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> possible_moves = new ArrayList<>();

        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();
        for (int[] QDir : QueenDirs) {
            int x = QDir[0];
            int y = QDir[1];
            int nextCol = currentCol + x;
            int nextRow = currentRow + y;
            while (inBounds(nextCol, nextRow)) {
                ChessPosition move = new ChessPosition(nextRow, nextCol);
                ChessPiece targetPiece = board.getPiece(move);
                if (targetPiece != null) {
                    if (targetPiece.pieceColor != currPiece.pieceColor) {
                        possible_moves.add(new ChessMove(startPosition, move, null));
                    }
                    break;
                }
                possible_moves.add(new ChessMove(startPosition, move, null));
                nextCol += x;
                nextRow += y;
            }
        }
        return possible_moves;
    }

    public Collection<ChessMove> KnightMoves(ChessBoard board, ChessPosition startPosition){
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> possible_moves = new ArrayList<>();
        int[][] knightDirs = {{2,-1},{2,1}, {1,-2}, {-1,-2}, {-2,1}, {-2,-1}, {-1,2}, {1,2}};

        for(int[] Kdir : knightDirs){
            ChessPosition move = new ChessPosition(Kdir[0], Kdir[1]);
            ChessPiece targetPiece = board.getPiece(move);
            if(inBounds(Kdir[0],Kdir[1])){
                if (targetPiece != null) { //if piece is filled
                    if (targetPiece.pieceColor == currPiece.pieceColor) { //if same color
                        continue;
                    }
                }
                possible_moves.add(new ChessMove(startPosition, move, null));
            }
        }
        return possible_moves;
    }


    public Collection<ChessMove> PawnMoves(ChessBoard board, ChessPosition startPosition) {
        Collection<ChessMove> possible_moves = new ArrayList<>(); //arraylist of possible moves
        ChessPiece currPiece = board.getPiece(startPosition); //current pawn object
        ChessGame.TeamColor color = getTeamColor();
        boolean promotionrow;
        int moveDirection;

        if(color == ChessGame.TeamColor.WHITE) {
            moveDirection = 1;
        } else {
            moveDirection = -1;
        }

        if ((currPiece.getTeamColor() == ChessGame.TeamColor.WHITE && startPosition.getRow() == 7) ||
                ((currPiece.getTeamColor() == ChessGame.TeamColor.BLACK && startPosition.getRow() == 2))){
            promotionrow = true;
        }
        else{
            promotionrow = false;
        }

        //standard moves check

        ChessPosition move = new ChessPosition(startPosition.getRow() + moveDirection, startPosition.getColumn() );
        ChessPiece targetPiece = board.getPiece(move);
        if(inBounds(startPosition.getRow() + moveDirection, startPosition.getColumn())){
            if (targetPiece == null) { //if piece is empty
                addMoveswithPromotionRowCheck(startPosition, possible_moves, promotionrow, move);
                possible_moves.add(new ChessMove(startPosition, move, null));
            }
        }

        //first move check (double move)
        if ((currPiece.getTeamColor() == ChessGame.TeamColor.WHITE && startPosition.getRow() == 2) ||
                ((currPiece.getTeamColor() == ChessGame.TeamColor.BLACK && startPosition.getRow() == 7))){
            if(inBounds(startPosition.getRow() + moveDirection *2, startPosition.getColumn())){
                ChessPosition endpos = new ChessPosition(startPosition.getRow() + moveDirection *2, startPosition.getColumn());
                possible_moves.add(new ChessMove(startPosition, endpos, null));
            }
        }
        //capture moves
        ChessPosition diagL = new ChessPosition(moveDirection, -1);
        ChessPosition diagR = new ChessPosition(moveDirection, 1);
        ChessPiece L = board.getPiece(diagL);
        ChessPiece R = board.getPiece(diagR);
        if(L.getTeamColor()!= color){
            addMoveswithPromotionRowCheck(startPosition, possible_moves, promotionrow, diagL);
        }
        if(R.getTeamColor()!= color){
            addMoveswithPromotionRowCheck(startPosition, possible_moves, promotionrow, diagR);
        }

            return possible_moves;
    }

    private void addMoveswithPromotionRowCheck(ChessPosition startPosition, Collection<ChessMove> possible_moves, boolean promotionrow, ChessPosition proposedPosition) {
        ChessMove proposedMove = new ChessMove(startPosition, proposedPosition, null);
        if (promotionrow) {
            possible_moves.add(new ChessMove(startPosition, proposedPosition, PieceType.QUEEN));
            possible_moves.add(new ChessMove(startPosition, proposedPosition, PieceType.BISHOP));
            possible_moves.add(new ChessMove(startPosition, proposedPosition, PieceType.KNIGHT));
            possible_moves.add(new ChessMove(startPosition, proposedPosition, PieceType.ROOK));
        } else {
            possible_moves.add(proposedMove);
        }
    }


    /**
         * Calculates all the positions a chess piece can move to
         * Does not take into account moves that are illegal due to leaving the king in
         * danger
         *
         * @return Collection of valid moves
         */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece piece = board.getPiece(myPosition); //board
        ChessPiece.PieceType type = piece.getPieceType(); // piece type
        Collection<ChessMove> pieceMovesOnAnEmptyBoard = new ArrayList<>();

        return switch (type) {
            case KING ->  pieceMovesOnAnEmptyBoard = KingMoves(board, myPosition);
            case QUEEN -> pieceMovesOnAnEmptyBoard = QueenMoves(board, myPosition);
            case BISHOP -> pieceMovesOnAnEmptyBoard = BishopMoves(board, myPosition);
            case KNIGHT -> pieceMovesOnAnEmptyBoard = KnightMoves(board, myPosition);
            case ROOK -> pieceMovesOnAnEmptyBoard = RookMoves(board, myPosition);
            case PAWN -> pieceMovesOnAnEmptyBoard = PawnMoves(board, myPosition);


        };
    }
}

