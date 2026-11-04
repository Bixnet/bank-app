package bank.api;

import java.net.URI;
import java.net.URISyntaxException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Where the database is and how to log in to it.
 * Shared by every repository in bank-api, so the connection details live in one place.
 */
public record DatabaseSettings(String jdbcUrl, String user, String password) {

    // Local run: SQLite file; foreign_keys=on makes SQLite check REFERENCES like Postgres does
    private static final String LOCAL_SQLITE_URL = "jdbc:sqlite:bank.db?foreign_keys=on";

    /**
     * Builds the settings from Render's DATABASE_URL (postgresql://user:password@host/dbname).
     * No DATABASE_URL means a local run with SQLite.
     */
    public static DatabaseSettings fromDatabaseUrl(String databaseUrl) {
        if (databaseUrl == null || databaseUrl.isBlank()) {
            return new DatabaseSettings(LOCAL_SQLITE_URL, null, null);
        }

        URI uri;
        try {
            uri = new URI(databaseUrl);
        } catch (URISyntaxException e) {
            // Never put the URL itself in the message: it contains the password
            throw new IllegalStateException("DATABASE_URL is not a valid URL", e);
        }

        String userInfo = uri.getUserInfo();
        int colon = userInfo == null ? -1 : userInfo.indexOf(':');
        if (colon < 0) {
            throw new IllegalStateException(
                    "DATABASE_URL must look like postgresql://user:password@host/dbname");
        }
        String user = userInfo.substring(0, colon);
        String password = userInfo.substring(colon + 1);
        int port = uri.getPort() == -1 ? 5432 : uri.getPort();
        String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
        return new DatabaseSettings(jdbcUrl, user, password);
    }

    public Connection connect() throws SQLException {
        if (user == null) {
            return DriverManager.getConnection(jdbcUrl);
        }
        return DriverManager.getConnection(jdbcUrl, user, password);
    }

    // Records print all fields by default; hide the password so it never ends up in a log
    @Override
    public String toString() {
        return "DatabaseSettings[jdbcUrl=" + jdbcUrl + ", user=" + user + ", password=****]";
    }
}