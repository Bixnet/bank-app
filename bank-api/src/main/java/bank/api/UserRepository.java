package bank.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Stores users and which accounts they own.
 * Used only by bank-api; the desktop app never touches these tables.
 * The SQL works on both SQLite (local) and Postgres (Render).
 */
public class UserRepository {

    private final DatabaseSettings db;

    public UserRepository(DatabaseSettings db) {
        this.db = db;
    }

    public void createTables() {
        String users = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password_hash TEXT NOT NULL, " +
                "role TEXT NOT NULL)";
        String owners = "CREATE TABLE IF NOT EXISTS account_owners (" +
                "username TEXT NOT NULL REFERENCES users(username), " +
                "account_number TEXT NOT NULL, " +
                "PRIMARY KEY (username, account_number))";
        try (Connection conn = db.connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(users);
            stmt.execute(owners);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not create the user tables", e);
        }
    }

    /** Saves a new user. Returns false if the username is already taken. */
    public boolean create(User user) {
        String sql = "INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?) " +
                "ON CONFLICT (username) DO NOTHING";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.username());
            ps.setString(2, user.passwordHash());
            ps.setString(3, user.role());
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Could not save user " + user.username(), e);
        }
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT username, password_hash, role FROM users WHERE username = ?";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new User(
                            rs.getString("username"),
                            rs.getString("password_hash"),
                            rs.getString("role")));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load user " + username, e);
        }
    }

    /** Links an account to a user. Adding the same link twice is harmless. */
    public void addOwner(String username, String accountNumber) {
        String sql = "INSERT INTO account_owners (username, account_number) VALUES (?, ?) " +
                "ON CONFLICT (username, account_number) DO NOTHING";
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, accountNumber);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not link account " + accountNumber + " to user " + username, e);
        }
    }

    public List<String> findAccountNumbersOwnedBy(String username) {
        String sql = "SELECT account_number FROM account_owners WHERE username = ? " +
                "ORDER BY account_number";
        List<String> result = new ArrayList<>();
        try (Connection conn = db.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(rs.getString("account_number"));
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not load accounts of user " + username, e);
        }
        return result;
    }
}