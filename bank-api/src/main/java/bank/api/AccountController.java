package bank.api;

import bank.exceptions.AccountNotFoundException;
import bank.exceptions.InsufficientFundsException;
import bank.exceptions.InvalidAmountException;
import bank.model.Account;
import bank.service.BankManager;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    /** Body of a deposit or withdrawal: {"amount": 50.25}. */
    public record AmountRequest(Double amount) {
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
        Account account = requireAccount(accountNumber);
        if (!mayAccess(authentication, accountNumber)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(AccountDto.from(account));
    }

    /** Owner or ADMIN only. Returns the account with its new balance. */
    @PostMapping("/accounts/{accountNumber}/deposit")
    public ResponseEntity<AccountDto> deposit(@PathVariable("accountNumber") String accountNumber,
                                              @RequestBody AmountRequest request,
                                              Authentication authentication) {
        requireAccount(accountNumber);
        if (!mayAccess(authentication, accountNumber)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Account account = bank.deposit(accountNumber, requireAmount(request));
        return ResponseEntity.ok(AccountDto.from(account));
    }

    /** Owner or ADMIN only. 409 when the account cannot cover the amount. */
    @PostMapping("/accounts/{accountNumber}/withdraw")
    public ResponseEntity<AccountDto> withdraw(@PathVariable("accountNumber") String accountNumber,
                                               @RequestBody AmountRequest request,
                                               Authentication authentication) throws InsufficientFundsException {
        requireAccount(accountNumber);
        if (!mayAccess(authentication, accountNumber)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Account account = bank.withdraw(accountNumber, requireAmount(request));
        return ResponseEntity.ok(AccountDto.from(account));
    }

    // ---------- Turning exceptions into clear HTTP answers ----------

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> accountNotFound(AccountNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "This account does not exist."));
    }

    @ExceptionHandler(InvalidAmountException.class)
    public ResponseEntity<Map<String, String>> invalidAmount(InvalidAmountException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, String>> insufficientFunds(InsufficientFundsException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", "Not enough money in this account for this withdrawal."));
    }

    // ---------- Helpers ----------

    private Account requireAccount(String accountNumber) {
        Account account = bank.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return account;
    }

    private boolean mayAccess(Authentication authentication, String accountNumber) {
        return isAdmin(authentication)
                || users.findAccountNumbersOwnedBy(authentication.getName()).contains(accountNumber);
    }

    private static double requireAmount(AmountRequest request) {
        if (request == null || request.amount() == null) {
            throw new InvalidAmountException("Amount is required.");
        }
        return request.amount();
    }

    private static boolean isAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
    }
}