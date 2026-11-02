package bank.api;

import bank.db.AccountRepository;
import bank.model.CheckingAccount;
import bank.model.SavingsAccount;
import bank.service.BankManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class BankConfig {

    @Bean
    public AccountRepository accountRepository() throws URISyntaxException {
        String databaseUrl = System.getenv("DATABASE_URL");

        AccountRepository repo;
        if (databaseUrl == null || databaseUrl.isBlank()) {
            // Local run: SQLite file, same as before
            repo = new AccountRepository();
        } else {
            // Cloud run: Render gives postgresql://user:password@host/dbname
            URI uri = new URI(databaseUrl);
            String[] login = uri.getUserInfo().split(":", 2);
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String jdbcUrl = "jdbc:postgresql://" + uri.getHost() + ":" + port + uri.getPath();
            repo = new AccountRepository(jdbcUrl, login[0], login[1]);
        }

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