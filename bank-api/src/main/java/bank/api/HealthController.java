package bank.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import java.util.logging.Logger;

/**
 * For uptime monitoring: checks that the app runs AND the database answers.
 * Open without login, so it says only UP or DOWN and never shows any data.
 */
@RestController
public class HealthController {

    private static final int TIMEOUT_SECONDS = 2;
    private static final Logger logger = Logger.getLogger(HealthController.class.getName());

    private final DatabaseSettings db;

    public HealthController(DatabaseSettings db) {
        this.db = db;
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> health() {
        try (Connection conn = db.connect()) {
            if (conn.isValid(TIMEOUT_SECONDS)) {
                return ResponseEntity.ok(Map.of("status", "UP"));
            }
            logger.warning("Health check: database connection is not valid");
        } catch (SQLException e) {
            // Log the reason for us, but never send database details to the caller
            logger.warning("Health check: database not reachable: " + e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status", "DOWN"));
    }
}