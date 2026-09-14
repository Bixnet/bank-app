package bank.service;

import bank.model.Account;
import bank.exceptions.InsufficientFundsException;
import bank.db.AccountRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BankManager {
    private static final Logger logger = Logger.getLogger(BankManager.class.getName());

    private List<Account> accounts = new ArrayList<>();
    private AccountRepository repository;

    public BankManager(AccountRepository repository) {
        this.repository = repository;
    }

    public void addAccount(Account account) {
        accounts.add(account);
        repository.save(account);
        logger.info("Account added: " + account.getClass().getSimpleName() + " (" + account.getAccountNumber() + ")");
    }

    public Account findByAccountNumber(String accountNumber) {
        for (Account acc : accounts) {
            if (acc.getAccountNumber().equals(accountNumber)) {
                return acc;
            }
        }
        return null;
    }

    public void showAllAccounts() {
        for (Account acc : accounts) {
            acc.showDetails();
        }
    }

    public void depositTo(String accountNumber, double amount) {
        Account acc = findByAccountNumber(accountNumber);
        if (acc != null) {
            acc.deposit(amount);
            repository.save(acc);
            logger.info("Deposited " + amount + " to " + accountNumber);
        } else {
            logger.warning("Deposit failed — account not found: " + accountNumber);
        }
    }

    public void withdrawFrom(String accountNumber, double amount) {
        Account acc = findByAccountNumber(accountNumber);
        if (acc != null) {
            try {
                acc.withdraw(amount);
                repository.save(acc);
                logger.info("Withdrew " + amount + " from " + accountNumber);
            } catch (InsufficientFundsException e) {
                logger.warning("Withdrawal failed: " + e.getMessage());
            }
        } else {
            logger.warning("Withdrawal failed — account not found: " + accountNumber);
        }
    }

    public List<Account> getAllAccounts() {
        return accounts;
    }
}