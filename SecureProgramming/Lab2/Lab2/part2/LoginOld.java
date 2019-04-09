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
import java.util.Scanner;

public class Login {
    private String username;

    private void usage() {
        System.err.println("Usage: ./Login command username password");
        System.err.println("  where command is:");
        System.err.println("    login to login to the database");
        System.err.println("    add   to add a user to the database");
    }

    private Connection getConnection() throws SQLException {
        // Register the driver
        String sDriverName = "org.sqlite.JDBC";

        try {
            Class.forName(sDriverName);
        } catch (ClassNotFoundException e) {
            throw new SecurityException("Couldn't initialize JDBC");
        }

        // Create a database connection
        return DriverManager.getConnection("jdbc:sqlite:users.db");
    }

    private String hashPassword(String password) throws SecurityException {
        MessageDigest crypt;

        try {
            crypt = MessageDigest.getInstance("SHA-1");
            crypt.reset();
            crypt.update(password.getBytes("utf-8"));

            return new BigInteger(1, crypt.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            throw new SecurityException("Misconfigured Java crypto library.");
        } catch (UnsupportedEncodingException e) {
            throw new SecurityException("Misconfigured Java crypto library.");
        }
    }

    public void login(String username, String password) throws SQLException {
        Connection connection = getConnection();

        try {
            String hPassword = hashPassword(password);
            if (hPassword == null) {
                throw new SecurityException("Failed to hash password");
            }

            PreparedStatement statement =
                connection.prepareStatement(
                    "SELECT *"          +
                    "  FROM db_users"   +
                    " WHERE username=?" +
                    "   AND password=?" );
            statement.setString(1, username);
            statement.setString(2, hPassword);
            statement.setQueryTimeout(5);
            ResultSet rs = statement.executeQuery();

            if (!rs.next()) {
		statement.close();
                throw new SecurityException("Username or password incorrect");
            }

	    statement.close();
            System.out.println("Password correct!");
            this.username = username;

        } finally {
            try {
                connection.close();
            } catch (SQLException x) {
                throw new SecurityException("Couldn't disconnect from database");
            }
        }
    }

    public void changePassword() throws SQLException {
        Connection connection = getConnection();

        try {
            System.out.println("Enter new password:");
            Scanner s = new Scanner(System.in);
            String password = s.nextLine();
            String hPassword = hashPassword(password);
            if (hPassword == null) {
                throw new SecurityException("Failed to hash password");
            }


            PreparedStatement statement =
                connection.prepareStatement(
                    "UPDATE db_users" +
                    "   SET password=?"+
                    " WHERE username='"+this.username+"'");
            statement.setString(1, hPassword);

            statement.setQueryTimeout(5);

            try {
                statement.execute();
            } catch (SQLException e) {
                System.err.println("Couldn't update password");
                statement.close();
                return;
            }
            statement.close();

        } finally {
            try {
                connection.close();
            } catch (SQLException x) {
                throw new SecurityException("Couldn't disconnect from database");
            }
        }

    }

    public void addUser(String username, String password) throws SQLException {
        Connection connection = getConnection();

        try {
            String hPassword = hashPassword(password);
            if (hPassword == null) {
                throw new SecurityException("Failed to hash password");
            }
            PreparedStatement statement =
                connection.prepareStatement(
                    "INSERT INTO db_users" +
                    "     VALUES (?, ?)"   );
            statement.setString(1, username);
            statement.setString(2, hPassword);

            statement.setQueryTimeout(5);

            try {
                if (statement.execute()) {
                    statement.close();
                    throw new SecurityException("Username or password incorrect");
                }
            } catch (SQLException e) {
                System.err.println("Couldn't add user: "+e);
                statement.close();
                return;
            }
            statement.close();

            System.out.println("Added user: "+username);
        } finally {
            try {
                connection.close();
            } catch (SQLException x) {
                throw new SecurityException("Couldn't disconnect from database");
            }
        }
    }

    public void interact() {
        Scanner s = new Scanner(System.in);
        System.out.println("Hello "+this.username);
        System.out.println("Enter command number:");
        System.out.println(" 1. Change password");
        System.out.println(" 2. Quit");

        switch (s.nextInt()) {
        case 1:
            try {
                this.changePassword();
            } catch (SQLException e) {
                System.err.println("Something went wrong: "+e);
            }
            break;
        case 2:
            break;
        default:
            System.err.println("Unrecognised command");
            this.interact();
        }
    }

    public static void main (String[] args) throws Exception {
        Login db = new Login();
        String command;
        String username;
        String password;

        if (args.length < 3) {
            db.usage();
            System.exit(1);
        }

        command = args[0];
        username = args[1];
        password = args[2];

        try {
            switch (command) {
            case "add":
                db.addUser(username, password);
                break;

            case "login":
                db.login(username, password);
                db.interact();
                break;

            default:
                System.err.println("Unrecognised command '"+command+"'");
                db.usage();
                System.exit(1);
            }
        } catch (SecurityException e) {
            System.err.println(":-( "+e.getMessage());
        }
    }
}


