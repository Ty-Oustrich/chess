# tips relevant right now
- In order for the tests to pass, you are required to override the equals() and hashCode() methods in your class implementations as necessary. This includes the ChessPosition, ChessPiece, ChessMove, and ChessBoard classes in particular. To do this automatically in IntelliJ, right click on the class code and select Code > Generate... > equals() and hashCode(). It is a good idea to generate these methods fairly early. However, IntelliJ will not be able to generate them properly until you have added the required fields to the class.
- To understand why we need to override the equals() and hashCode() methods, see the instruction page on Java Object Class. https://github.com/softwareconstruction240/softwareconstruction/blob/main/instruction/java-object-class/java-object-class.md

Tip

Debugging is often much easier if you also override the toString() method and return a concise representation of the object. This is not required, but highly recommended. This can be generated in the same way that the equals() and hashcode() were.




# classes
- ChessGame - Serves as the top-level management of the chess game. It is responsible for executing moves as well as recording the game status.
- ChessBoard - Stores all the uncaptured pieces in a Game. It needs to support adding and removing pieces for testing, as well as a resetBoard() method that sets the standard Chess starting configuration.
- ChessPiece - This class represents a single chess piece, with its corresponding type and team color. It contains the PieceType enumeration that defines the different types of pieces. ChessPiece implements rules that define how a piece moves independent of other chess rules such as whose turn it is, check, stalemate, or checkmate. Contains pieceMoves method
- ChessMove - This class represents a possible move a chess piece could make. It contains the starting and ending positions. It also contains a field for the type of piece a pawn is being promoted to. If the move would not result in a pawn being promoted, the promotion type field should be null.
- ChessPosition - This represents a location on the chessboard. This should be represented as a row number from 1-8, and a column number from 1-8. For example, (1,1) corresponds to the bottom left corner (which in chess notation is denoted a1). (8,8) corresponds to the top right corner (h8 in chess notation).