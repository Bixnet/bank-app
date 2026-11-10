package bank.api;

import bank.service.BankManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Import(TestDatabaseConfig.class)
class TransactionApiTest {

    private static final String ANNAS_ACCOUNT = "654321";  // savings, owned by test-anna
    private static final String MAXS_ACCOUNT = "123456";   // checking, not owned by test-anna

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository users;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private BankManager bank;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).apply(springSecurity()).build();
        if (users.create(new User("test-anna", passwordEncoder.encode("test-password-1"), User.ROLE_USER))) {
            users.addOwner("test-anna", ANNAS_ACCOUNT);
        }
    }

    private static RequestPostProcessor anna() {
        return user("test-anna").roles("USER");
    }

    private static RequestPostProcessor admin() {
        return user("test-admin").roles("ADMIN");
    }

    private ResultActions send(String accountNumber, String action, String json, RequestPostProcessor who)
            throws Exception {
        return mvc.perform(post("/accounts/" + accountNumber + "/" + action)
                .with(who).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    private double balance(String accountNumber) {
        return bank.findByAccountNumber(accountNumber).getBalance();
    }

    @Test
    void ownerCanDepositAndWithdraw() throws Exception {
        double before = balance(ANNAS_ACCOUNT);

        send(ANNAS_ACCOUNT, "deposit", "{\"amount\": 50.25}", anna()).andExpect(status().isOk());
        assertEquals(before + 50.25, balance(ANNAS_ACCOUNT), 0.001);

        send(ANNAS_ACCOUNT, "withdraw", "{\"amount\": 50.25}", anna()).andExpect(status().isOk());
        assertEquals(before, balance(ANNAS_ACCOUNT), 0.001);
    }

    @Test
    void withdrawingTooMuchIsConflictAndChangesNothing() throws Exception {
        double before = balance(ANNAS_ACCOUNT);
        send(ANNAS_ACCOUNT, "withdraw", "{\"amount\": 1000000}", anna()).andExpect(status().isConflict());
        assertEquals(before, balance(ANNAS_ACCOUNT), 0.001);
    }

    @Test
    void invalidAmountsAreBadRequest() throws Exception {
        double before = balance(ANNAS_ACCOUNT);
        send(ANNAS_ACCOUNT, "deposit", "{\"amount\": -5}", anna()).andExpect(status().isBadRequest());
        send(ANNAS_ACCOUNT, "deposit", "{\"amount\": 0.001}", anna()).andExpect(status().isBadRequest());
        send(ANNAS_ACCOUNT, "withdraw", "{\"amount\": 0}", anna()).andExpect(status().isBadRequest());
        send(ANNAS_ACCOUNT, "deposit", "{}", anna()).andExpect(status().isBadRequest());
        assertEquals(before, balance(ANNAS_ACCOUNT), 0.001);
    }

    @Test
    void someoneElsesAccountIsForbiddenAndUnchanged() throws Exception {
        double before = balance(MAXS_ACCOUNT);
        send(MAXS_ACCOUNT, "deposit", "{\"amount\": 10}", anna()).andExpect(status().isForbidden());
        send(MAXS_ACCOUNT, "withdraw", "{\"amount\": 10}", anna()).andExpect(status().isForbidden());
        assertEquals(before, balance(MAXS_ACCOUNT), 0.001);
    }

    @Test
    void adminCanUseAnyAccount() throws Exception {
        double before = balance(MAXS_ACCOUNT);
        send(MAXS_ACCOUNT, "deposit", "{\"amount\": 10}", admin()).andExpect(status().isOk());
        assertEquals(before + 10, balance(MAXS_ACCOUNT), 0.001);
    }

    @Test
    void unknownAccountIsNotFound() throws Exception {
        send("000000", "deposit", "{\"amount\": 10}", admin()).andExpect(status().isNotFound());
    }

    @Test
    void notLoggedInIsUnauthorized() throws Exception {
        mvc.perform(post("/accounts/" + ANNAS_ACCOUNT + "/deposit").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 10}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void missingCsrfTokenIsForbidden() throws Exception {
        double before = balance(ANNAS_ACCOUNT);
        mvc.perform(post("/accounts/" + ANNAS_ACCOUNT + "/deposit").with(anna())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"amount\": 10}"))
                .andExpect(status().isForbidden());
        assertEquals(before, balance(ANNAS_ACCOUNT), 0.001);
    }
}