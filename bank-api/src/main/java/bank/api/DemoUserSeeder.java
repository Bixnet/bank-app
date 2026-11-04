package bank.api;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.logging.Logger;

/**
 * Creates the demo users once, when the app starts.
 * Passwords come only from environment variables, never from the code:
 * DEMO_PASSWORD for anna and max, ADMIN_PASSWORD for admin.
 * A missing variable means that user is simply not created.
 */
@Component
public class DemoUserSeeder implements ApplicationRunner {

    static final Set<String> RESERVED_USERNAMES = Set.of("anna", "max", "admin");

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Logger logger = Logger.getLogger(DemoUserSeeder.class.getName());

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public DemoUserSeeder(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        String demoPassword = System.getenv("DEMO_PASSWORD");
        if (isUsable(demoPassword, "DEMO_PASSWORD")) {
            seed("anna", demoPassword, User.ROLE_USER, "654321");
            seed("max", demoPassword, User.ROLE_USER, "123456");
        }

        String adminPassword = System.getenv("ADMIN_PASSWORD");
        if (isUsable(adminPassword, "ADMIN_PASSWORD")) {
            seed("admin", adminPassword, User.ROLE_ADMIN, null);
        }
    }

    private void seed(String username, String password, String role, String accountNumber) {
        boolean created = users.create(new User(username, passwordEncoder.encode(password), role));
        // Link the account only to a user we just created ourselves, never to an existing one
        if (created) {
            if (accountNumber != null) {
                users.addOwner(username, accountNumber);
            }
            logger.info("Created demo user " + username);
        }
    }

    private static boolean isUsable(String password, String variableName) {
        if (password == null || password.isBlank()) {
            logger.info(variableName + " is not set, so its demo user is not created");
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            logger.warning(variableName + " is shorter than 8 characters, so its demo user is not created");
            return false;
        }
        return true;
    }
}