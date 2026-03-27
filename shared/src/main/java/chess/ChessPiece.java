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
        ChessPiece piece = (ChessPiece) o;
        return pieceColor == piece.pieceColor && type == piece.type;
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

    /**
     * @return true if the proposed row/col is within board bounds.
     */
    private boolean inBounds(int col, int row) {
        return col >= 1 && col <= 8 && row >= 1 && row <= 8;
    }

    /**
     * @return Collection of king moves from the start position on an empty board.
     */
    public Collection<ChessMove> kingMoves(ChessBoard board, ChessPosition startPosition) {
        int[][] allDirections = {{-1,-1},{-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();
        for(int i=0; i<8; i++){
            int x = allDirections[i][0];
            int y = allDirections[i][1];
            if (inBounds(currentCol + x,currentRow + y)){
                ChessPosition move = new ChessPosition(currentRow + y, currentCol + x);
                ChessPiece targetPiece = board.getPiece(move);
                if (targetPiece != null && targetPiece.getTeamColor() == getTeamColor()) {
                    continue;
                }
                possibleMoves.add(new ChessMove(startPosition, move, null));
            }
        }
        return possibleMoves;
    }




public Collection<ChessMove> queenMoves(ChessBoard board, ChessPosition startPosition){
    Collection<ChessMove> possibleMoves = new ArrayList<>();
    int[][] kdirs = {{-1,-1},{-1,0},{-1,1},{0,-1},{0,1},{1,-1},{1,0},{1,1}};
    int startrow = startPosition.getRow();
    int startcol = startPosition.getColumn();
    ChessPiece startPiece = board.getPiece(startPosition);

    for(int i = 0; i<8; i++) {
        int row = kdirs[i][0];
        int col = kdirs[i][1];
        int nextrow = startrow + row;
        int nextcol = startcol + col;
        while (inBounds(nextcol, nextrow)){
            ChessPosition enpos = new ChessPosition(nextrow, nextcol);
            ChessMove endmove = new ChessMove(startPosition, enpos, null);
            ChessPiece endpiece = board.getPiece(enpos);
            if(endpiece != null) {
                if(startPiece.getTeamColor() != endpiece.getTeamColor()){
                    possibleMoves.add(endmove);
                }
                break;
            }
            possibleMoves.add(endmove);
            nextrow += row;
            nextcol+= col;
        }
    }
    return possibleMoves;
}
    /**
     * @return Collection of rook moves from the start position on an empty board.
     */
    public Collection<ChessMove> rookMoves(ChessBoard board, ChessPosition startPosition) {
        int [][] rookDirs = {{1,0},{-1,0},{0,1}, {0,-1}};
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>();

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
                        possibleMoves.add(new ChessMove(startPosition, move, null));
                    }
                    break;
                }
                possibleMoves.add(new ChessMove(startPosition, move, null));
                nextCol += x;
                nextRow += y;
            }
        }
        return possibleMoves;
    }

    /**
     * @return Collection of bishop moves from the start position on an empty board.
     */
    public Collection<ChessMove> bishopMoves(ChessBoard board, ChessPosition startPosition) {
        int[][] bishDirs = {{-1,-1}, {-1,1}, {1,-1}, {1,1}};
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>();

        int currentRow = startPosition.getRow();
        int currentCol = startPosition.getColumn();
        for (int[] bDir : bishDirs) {
            int x = bDir[0];
            int y = bDir[1];
            int nextCol = currentCol + x;
            int nextRow = currentRow + y;
            while (inBounds(nextCol, nextRow)) {
                ChessPosition move = new ChessPosition(nextRow, nextCol);
                ChessPiece targetPiece = board.getPiece(move);
                if (targetPiece != null) {
                    if (targetPiece.pieceColor != currPiece.pieceColor) {
                        possibleMoves.add(new ChessMove(startPosition, move, null));
                    }
                    break;
                }
                possibleMoves.add(new ChessMove(startPosition, move, null));
                nextCol += x;
                nextRow += y;
            }
        }
        return possibleMoves;
        }

    public Collection<ChessMove> knightMoves(ChessBoard board, ChessPosition startPosition){
        ChessPiece currPiece = board.getPiece(startPosition);
        Collection<ChessMove> possibleMoves = new ArrayList<>();
        int[][] knightDirs = {{2,-1},{2,1}, {1,-2}, {-1,-2}, {-2,1}, {-2,-1}, {-1,2}, {1,2}};

        for(int[] kDir : knightDirs){
            int nextRow = startPosition.getRow() + kDir[0];
            int nextCol = startPosition.getColumn() + kDir[1];
            if(inBounds(nextCol,nextRow)){
                ChessPosition endpos = new ChessPosition(nextRow, nextCol);
                ChessPiece targetPiece = board.getPiece(endpos);
                if (targetPiece != null) { //if piece is filled
                    if (targetPiece.pieceColor == currPiece.pieceColor) { //if same color
                        continue;
                    }
                }
                possibleMoves.add(new ChessMove(startPosition, endpos, null));
            }
        }
        return possibleMoves;
    }


    public Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition startPosition) {
        Collection<ChessMove> possibleMoves = new ArrayList<>(); //arraylist of possible moves
        ChessPiece currPiece = board.getPiece(startPosition); //current pawn object
        ChessGame.TeamColor color = getTeamColor();
        boolean promotionrow;
        int moveDirection;

        if(color == ChessGame.TeamColor.WHITE) {
            moveDirection = 1;
        } else {
            moveDirection = -1;
        }

        promotionrow = (currPiece.getTeamColor() == ChessGame.TeamColor.WHITE && startPosition.getRow() == 7) ||
                ((currPiece.getTeamColor() == ChessGame.TeamColor.BLACK && startPosition.getRow() == 2));

        //standard moves check

        ChessPosition move = new ChessPosition(startPosition.getRow() + moveDirection, startPosition.getColumn() );
        if(inBounds(move.getColumn(), move.getRow())){
            ChessPiece targetPiece = board.getPiece(move);
            if (targetPiece == null) { //if piece is empty
                addMoveswithPromotionRowCheck(startPosition, possibleMoves, promotionrow, move);
                
                //first move check (double move)
                if ((currPiece.getTeamColor() == ChessGame.TeamColor.WHITE && startPosition.getRow() == 2) ||
                        ((currPiece.getTeamColor() == ChessGame.TeamColor.BLACK && startPosition.getRow() == 7))){
                    ChessPosition endpos = new ChessPosition(startPosition.getRow() + moveDirection * 2, startPosition.getColumn());
                    if(inBounds(endpos.getColumn(), endpos.getRow()) && board.getPiece(endpos) == null){
                        possibleMoves.add(new ChessMove(startPosition, endpos, null));
                    }
                }
            }
        }
        //capture moves
        ChessPosition diagL = new ChessPosition(startPosition.getRow() + moveDirection, startPosition.getColumn() - 1);
        ChessPosition diagR = new ChessPosition(startPosition.getRow() + moveDirection, startPosition.getColumn() + 1);
        
        if (inBounds(diagL.getColumn(), diagL.getRow())) {
            ChessPiece leftPiece = board.getPiece(diagL);
            if(leftPiece != null && leftPiece.getTeamColor()!= color){
                addMoveswithPromotionRowCheck(startPosition, possibleMoves, promotionrow, diagL);
            }
        }
        
        if (inBounds(diagR.getColumn(), diagR.getRow())) {
            ChessPiece rightPiece = board.getPiece(diagR);
            if(rightPiece != null && rightPiece.getTeamColor()!= color){
                addMoveswithPromotionRowCheck(startPosition, possibleMoves, promotionrow, diagR);
            }
        }

            return possibleMoves;
    }

    private void addMoveswithPromotionRowCheck(
            ChessPosition startPosition,
            Collection<ChessMove> possibleMoves,
            boolean promotionrow,
            ChessPosition proposedPosition
    ) {
        ChessMove proposedMove = new ChessMove(startPosition, proposedPosition, null);
        if (promotionrow) {
            possibleMoves.add(new ChessMove(startPosition, proposedPosition, PieceType.QUEEN));
            possibleMoves.add(new ChessMove(startPosition, proposedPosition, PieceType.BISHOP));
            possibleMoves.add(new ChessMove(startPosition, proposedPosition, PieceType.KNIGHT));
            possibleMoves.add(new ChessMove(startPosition, proposedPosition, PieceType.ROOK));
        } else {
            possibleMoves.add(proposedMove);
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

        return switch (type) {
            case KING ->  kingMoves(board, myPosition);
            case QUEEN -> queenMoves(board, myPosition);
            case BISHOP -> bishopMoves(board, myPosition);
            case KNIGHT -> knightMoves(board, myPosition);
            case ROOK -> rookMoves(board, myPosition);
            case PAWN -> pawnMoves(board, myPosition);


        };
    }
}

