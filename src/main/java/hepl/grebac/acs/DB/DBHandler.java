package hepl.grebac.acs.DB;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBHandler {
    private static final Connection con = connectToSqlite();

    public DBHandler() {
    }

    public String getPublickeyByBankNumber(String bankNumber) {
        try {
            String query = "SELECT publicKey FROM token WHERE bankNumber = '" + bankNumber + "';";

            var answer = con.createStatement().executeQuery(query);

            var publicKey = answer.getString("publickey");

            if(publicKey != null)
                return publicKey;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public String generateTokenForBanknumber(String bankNumber) {
        try {
            var token = tokenGenerator.generateRandomToken(8);
            String query = "update token set tokenValidity = datetime('now', '+1 hour'), token = '" + token + "' where bankNumber = '" + bankNumber + "';";

            con.createStatement().execute(query);

            return token;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean checkTokenValidity(String token) {
        try {
            String query = """
                    SELECT
                        CASE
                            WHEN tokenValidity > datetime('now') THEN 1 -- True
                            ELSE 0\s
                            END AS is_token_valid
                    FROM token
                    WHERE token = '%s';
                    """.formatted(token);

            var answer = con.createStatement().executeQuery(query);

            var tokenValidity = answer.getString("is_token_valid");

            if(tokenValidity != null)
                return tokenValidity.equals("1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public static Connection connectToSqlite() {
        try {
            String url = "jdbc:sqlite:tokens.sqlite";

            // create a connection to the database
            Connection conn = DriverManager.getConnection(url);

            System.out.println("Connection to SQLite has been established.");

            return conn;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            System.exit(-1);
        }
        return null;
    }
}