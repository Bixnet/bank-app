package bank.api;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;

import java.util.Locale;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Page files: the pages themselves decide what to show
                        .requestMatchers(HttpMethod.GET,
                                "/", "/index.html", "/login.html", "/register.html",
                                "/styles.css", "/app.js", "/api.js", "/login.js", "/register.js",
                                "/favicon.ico").permitAll()
                        // Needed before anyone is logged in
                        .requestMatchers(HttpMethod.GET, "/api/csrf").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
                        // Still open on purpose until BA-34 protects it
                        .requestMatchers(HttpMethod.GET, "/accounts").permitAll()
                        // Spring's error page, so real errors are not hidden behind a 401
                        .requestMatchers("/error").permitAll()
                        // Everything else needs a logged-in user
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login.html")
                        .loginProcessingUrl("/api/login")
                        .successHandler((request, response, authentication) ->
                                response.setStatus(HttpServletResponse.SC_NO_CONTENT))
                        .failureHandler((request, response, exception) ->
                                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED))
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/api/logout")
                        .logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler()))
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)));
        // CSRF protection stays on (Spring's default); the page gets its token from GET /api/csrf
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository users) {
        return username -> users.findByUsername(normalize(username))
                .map(user -> org.springframework.security.core.userdetails.User
                        .withUsername(user.username())
                        .password(user.passwordHash())
                        .roles(user.role())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("Unknown user"));
    }

    /** Usernames are stored lowercase, so "Anna" and "anna" are the same user. */
    static String normalize(String username) {
        return username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
    }
}