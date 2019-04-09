import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.PreparedStatement;

public class Login
{
    private Connection getConnection()
    throws SQLException {
        // Register the driver 
        String sDriverName = "org.sqlite.JDBC";

        try { Class.forName(sDriverName); }
        catch (ClassNotFoundException e) {
            // Handle error
        }

        // Create a database connection
        return DriverManager.getConnection("jdbc:sqlite:users.db");
    }

    private String hashPassword(String password)
    {
        MessageDigest crypt;

        try {
            crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("utf-8"));

            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            // Handle error
        } catch (UnsupportedEncodingException e) {
            // Handle error
        }
return null;
     }

    public void doPrivilegedAction(String username, String password)
    throws SQLException {
        Connection connection = getConnection();
        if (connection == null) {
            // Handle error
        }

        try {
            String hPassword = hashPassword(password);
            if (hPassword == null)
            {
                throw new SecurityException("Failed to hash password");
            }
	    
            
	    PreparedStatement stmt = connection.prepareStatement("SELECT * FROM db_users WHERE username=? AND password=?");
	    stmt.setString(1, username);
	    stmt.setString(2, hPassword);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                throw new SecurityException("Username or password incorrect");
            }

            System.out.println("Password correct!");
        } finally {
            try {
                connection.close();
            } catch (SQLException x) {
                // Handle error
            }
        }
    }

    public static void main (String[] args) throws Exception
    {
        Login db = new Login();

    try {
            db.doPrivilegedAction(args[0], args[1]);
        } catch (SecurityException e) {
            System.err.println(":-( "+e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.err.println("Usage: ./Login username password");
        }
    }
}

