package bank.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Pattern USERNAME_RULE = Pattern.compile("[a-z0-9._-]{3,30}");
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MAX_PASSWORD_BYTES = 72; // BCrypt only looks at the first 72 bytes

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserRepository users, PasswordEncoder passwordEncoder) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
    }

    public record RegisterRequest(String username, String password) {
        // Never print the password, even by accident in a log
        @Override
        public String toString() {
            return "RegisterRequest[username=" + username + "]";
        }
    }

    /** Hands the page the CSRF token it must send with every POST. */
    @GetMapping("/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest request) {
        String username = SecurityConfig.normalize(request.username());
        String password = request.password() == null ? "" : request.password();

        if (!USERNAME_RULE.matcher(username).matches()) {
            return error(HttpStatus.BAD_REQUEST,
                    "Username must be 3 to 30 characters: letters, digits, dot, dash or underscore.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_PASSWORD_BYTES) {
            return error(HttpStatus.BAD_REQUEST,
                    "Password must be at least 8 characters and not longer than 72 bytes.");
        }
        if (DemoUserSeeder.RESERVED_USERNAMES.contains(username)) {
            return error(HttpStatus.CONFLICT, "This username is already taken.");
        }

        boolean created = users.create(
                new User(username, passwordEncoder.encode(password), User.ROLE_USER));
        if (!created) {
            return error(HttpStatus.CONFLICT, "This username is already taken.");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", username));
    }

    /** Who is logged in? Returns 401 when nobody is (handled by SecurityConfig). */
    @GetMapping("/me")
    public Map<String, String> me(Authentication authentication) {
        String role = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("")
                .replaceFirst("^ROLE_", "");
        return Map.of("username", authentication.getName(), "role", role);
    }

    private static ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("error", message));
    }
}