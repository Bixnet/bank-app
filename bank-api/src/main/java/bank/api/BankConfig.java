package bank.api;

import bank.db.AccountRepository;
import bank.model.CheckingAccount;
import bank.model.SavingsAccount;
import bank.service.BankManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BankConfig {

    @Bean
    public AccountRepository accountRepository() {
        AccountRepository repo = new AccountRepository();
        repo.createTable();
        return repo;
    }

    @Bean
    public BankManager bankManager(AccountRepository repo) {
        BankManager bank = new BankManager(repo);

        var existing = repo.findAll();
        if (existing.isEmpty()) {
            bank.addAccount(new SavingsAccount("Anna Schmidt", "654321", "Berlin", 2000, 0.05));
            bank.addAccount(new CheckingAccount("Max Müller", "123456", "Frankfurt", 1000, 500));
        } else {
            existing.forEach(bank::addAccount);
        }
        return bank;
    }
}