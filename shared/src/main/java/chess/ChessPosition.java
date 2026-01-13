package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {

    private final int row;
    private final int col;

    public ChessPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn(){
        return col;}

    @Override
    public String toString() {
        return "ChessPosition{" +
                "row=" + row +
                ", col=" + col +
                '}';
    }

    /**
     * @return bool of the proposed move, true if it is in bounds.
     */
    public boolean in_bounds(int x, int y ){
        return x >= 0 && x <= 7 && y >= 0 && y <= 7;
    }
/// all piece moves
    public Collection<ChessMove> KingMoves() {
        int[][] allDirections = {{-1,-1},{-1,0}, {-1,1}, {0,-1}, {0,1}, {1,-1}, {1,0}, {1,1}};
        Collection<ChessMove> possible_moves = List<ChessPosition>;

        int currentRow = this.row;
        int currentCol = this.col;
        for(int i=1; i<8; i++){
            int x = allDirections[i][0];
            int y = allDirections[i][1];
            if (in_bounds(currentCol + x,currentRow + y)){
            possible_moves.add(new ChessMove(this,new ChessPosition(currentCol+x,currentRow+y), null));
            }
            return possible_moves;
        }
        //add to the positions, if in bounds add to collection



    }
}
