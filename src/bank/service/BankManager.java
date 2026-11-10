package bank.service;

import bank.model.Account;
import bank.exceptions.AccountNotFoundException;
import bank.exceptions.InsufficientFundsException;
import bank.exceptions.InvalidAmountException;
import bank.db.AccountRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class BankManager {
    private static final Logger logger = Logger.getLogger(BankManager.class.getName());

    /** Highest amount allowed in one deposit or withdrawal (strict methods only). */
    public static final double MAX_AMOUNT = 1_000_000;

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

    public int getAccountCount() {
        return accounts.size();
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

    // ---------- Strict versions for the web API (Phase 9) ----------
    // The methods above only log problems, which the desktop app relies on.
    // These throw instead, so the caller knows exactly what went wrong.
    // synchronized: only one money operation runs at a time, so two requests
    // can never both pass the balance check before either one subtracts.

    /** Adds money. Throws AccountNotFoundException or InvalidAmountException. */
    public synchronized Account deposit(String accountNumber, double amount) {
        Account acc = requireAccount(accountNumber);
        requireValidAmount(amount);
        acc.deposit(amount);
        repository.save(acc);
        logger.info("Deposited " + amount + " to " + accountNumber);
        return acc;
    }

    /** Takes money out. Also throws InsufficientFundsException when the account cannot cover it. */
    public synchronized Account withdraw(String accountNumber, double amount) throws InsufficientFundsException {
        Account acc = requireAccount(accountNumber);
        requireValidAmount(amount);
        acc.withdraw(amount);
        repository.save(acc);
        logger.info("Withdrew " + amount + " from " + accountNumber);
        return acc;
    }

    private Account requireAccount(String accountNumber) {
        Account acc = findByAccountNumber(accountNumber);
        if (acc == null) {
            throw new AccountNotFoundException(accountNumber);
        }
        return acc;
    }

    private static void requireValidAmount(double amount) {
        if (Double.isNaN(amount) || Double.isInfinite(amount) || amount <= 0) {
            throw new InvalidAmountException("Amount must be greater than 0.");
        }
        if (amount > MAX_AMOUNT) {
            throw new InvalidAmountException("Amount must not be more than 1,000,000.");
        }
        double cents = amount * 100;
        if (Math.abs(cents - Math.round(cents)) > 1e-6) {
            throw new InvalidAmountException("Amount must have at most 2 decimal places.");
        }
    }
}