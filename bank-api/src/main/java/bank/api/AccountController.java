package bank.api;

import bank.model.Account;
import bank.service.BankManager;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class AccountController {

    private final BankManager bank;

    public AccountController(BankManager bank) {
        this.bank = bank;
    }

    public record AccountDto(String accountNumber, String name, String branch,
                             double balance, String type) {
        static AccountDto from(Account a) {
            return new AccountDto(a.getAccountNumber(), a.getName(), a.getBranch(),
                    a.getBalance(), a.getClass().getSimpleName());
        }
    }

    @GetMapping("/accounts")
    public List<AccountDto> getAccounts() {
        return bank.getAllAccounts().stream().map(AccountDto::from).toList();
    }
}