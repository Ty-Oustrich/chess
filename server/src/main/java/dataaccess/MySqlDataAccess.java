package dataaccess;



import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import model.GameData;

import java.sql.PreparedStatement;
import java.sql.ResultSet;




public class MySqlDataAccess implements DataAccess {
    private final Gson gson = new Gson();

    @Override
    public GamesData getGame(int gameID) throws DataAccessException {
        var sql = """
        SELECT game_id, white_username, black_username, game_name, game_state_json
        FROM games
        WHERE game_id = ?        

                """;
        try(var conn = DatabaseManager.getConnection();
        PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, gameID);
        try ( ResultSet rs = ps.executeQuery()){
            if (!rs.next()) return null;

            int storedGameId = rs.getInt("game_id");
            String whiteUsername = rs.getString("white_username");
            String blackUsername = rs.getString("black_username");
            String gameName = rs.getString("game_name");



            
        }

        
    

    }
}
}
