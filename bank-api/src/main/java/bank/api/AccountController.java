package bank.api;

import bank.model.Account;
import bank.service.BankManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
public class AccountController {

    private final BankManager bank;
    private final UserRepository users;

    public AccountController(BankManager bank, UserRepository users) {
        this.bank = bank;
        this.users = users;
    }

    public record AccountDto(String accountNumber, String name, String branch,
                             double balance, String type) {
        static AccountDto from(Account a) {
            return new AccountDto(a.getAccountNumber(), a.getName(), a.getBranch(),
                    a.getBalance(), a.getClass().getSimpleName());
        }
    }

    /** Only the logged-in user's accounts; ADMIN sees all of them. */
    @GetMapping("/accounts")
    public List<AccountDto> getAccounts(Authentication authentication) {
        if (isAdmin(authentication)) {
            return bank.getAllAccounts().stream().map(AccountDto::from).toList();
        }
        Set<String> owned = new HashSet<>(users.findAccountNumbersOwnedBy(authentication.getName()));
        return bank.getAllAccounts().stream()
                .filter(account -> owned.contains(account.getAccountNumber()))
                .map(AccountDto::from)
                .toList();
    }

    /** One account: 404 if it does not exist, 403 if it belongs to someone else. */
    @GetMapping("/accounts/{accountNumber}")
    public ResponseEntity<AccountDto> getAccount(@PathVariable("accountNumber") String accountNumber,
                                                 Authentication authentication) {
        Account account = bank.findByAccountNumber(accountNumber);
        if (account == null) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(authentication)
                && !users.findAccountNumbersOwnedBy(authentication.getName()).contains(accountNumber)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(AccountDto.from(account));
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}