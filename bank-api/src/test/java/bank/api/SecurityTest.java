package bank.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestDatabaseConfig.class)
class SecurityTest {

    private static final String PASSWORD = "test-password-1";

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        // MockMvc sends fake HTTP requests through the whole app, including Spring Security
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();

        // Test users of our own, so the tests never depend on DEMO_PASSWORD
        if (users.create(new User("test-anna", passwordEncoder.encode(PASSWORD), User.ROLE_USER))) {
            users.addOwner("test-anna", "654321");
        }
        users.create(new User("test-admin", passwordEncoder.encode(PASSWORD), User.ROLE_ADMIN));
    }

    // ---------- Not logged in ----------

    @Test
    void accountsNeedLogin() throws Exception {
        mvc.perform(get("/accounts")).andExpect(status().isUnauthorized());
    }

    @Test
    void whoAmINeedsLogin() throws Exception {
        mvc.perform(get("/api/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void healthIsOpenAndSaysUp() throws Exception {
        String body = mvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(body.contains("UP"));
    }

    // ---------- Who sees which accounts ----------

    @Test
    void userSeesOnlyOwnAccounts() throws Exception {
        String body = mvc.perform(get("/accounts").with(user("test-anna").roles("USER")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(body.contains("654321"));
        assertFalse(body.contains("123456"));
    }

    @Test
    void someoneElsesAccountIsForbidden() throws Exception {
        mvc.perform(get("/accounts/654321").with(user("test-anna").roles("USER")))
                .andExpect(status().isOk());
        mvc.perform(get("/accounts/123456").with(user("test-anna").roles("USER")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminSeesAllAccounts() throws Exception {
        String body = mvc.perform(get("/accounts").with(user("test-admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        assertTrue(body.contains("654321"));
        assertTrue(body.contains("123456"));
    }

    @Test
    void unknownAccountIsNotFound() throws Exception {
        mvc.perform(get("/accounts/000000").with(user("test-admin").roles("ADMIN")))
                .andExpect(status().isNotFound());
    }

    // ---------- Login and CSRF ----------

    @Test
    void loginWithRightPasswordWorks() throws Exception {
        mvc.perform(post("/api/login").with(csrf())
                        .param("username", "test-anna").param("password", PASSWORD))
                .andExpect(status().isNoContent());
    }

    @Test
    void loginWithWrongPasswordFails() throws Exception {
        mvc.perform(post("/api/login").with(csrf())
                        .param("username", "test-anna").param("password", "wrong-password"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void postWithoutCsrfTokenIsRejected() throws Exception {
        mvc.perform(post("/api/login")
                        .param("username", "test-anna").param("password", PASSWORD))
                .andExpect(status().isForbidden());
    }

    // ---------- Registration ----------

    @Test
    void registerStoresOnlyABcryptHash() throws Exception {
        mvc.perform(post("/api/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newbie\",\"password\":\"plain-text-pw-1\"}"))
                .andExpect(status().isCreated());

        String storedHash = users.findByUsername("newbie").orElseThrow().passwordHash();
        assertNotEquals("plain-text-pw-1", storedHash);
        assertTrue(storedHash.startsWith("$2"), "expected a BCrypt hash");
        assertTrue(passwordEncoder.matches("plain-text-pw-1", storedHash));
    }

    @Test
    void reservedUsernameCannotBeRegistered() throws Exception {
        mvc.perform(post("/api/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"whatever-123\"}"))
                .andExpect(status().isConflict());
    }
}