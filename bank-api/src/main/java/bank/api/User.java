package bank.api;

/**
 * A person who can log in to the web app.
 * The password is never stored, only its hash (BCrypt, added in BA-33).
 */
public record User(String username, String passwordHash, String role) {

    public static final String ROLE_USER = "USER";
    public static final String ROLE_ADMIN = "ADMIN";

    // Keep the hash out of logs and error messages
    @Override
    public String toString() {
        return "User[username=" + username + ", role=" + role + "]";
    }
}