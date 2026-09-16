package bank.service;

import bank.db.AccountRepository;
import bank.model.SavingsAccount;
import bank.model.CheckingAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BankManagerTest {

    private BankManager bank;

    // Runs before EVERY @Test method — gives each test a clean, fresh BankManager
    @BeforeEach
    void setUp() {
        AccountRepository repo = new AccountRepository();
        repo.createTable();
        bank = new BankManager(repo);
    }

    @Test
    void addAccountStoresItInTheList() {
        SavingsAccount acc = new SavingsAccount("Anna", "999", "Berlin", 1000, 0.05);
        bank.addAccount(acc);
        assertEquals(1, bank.getAllAccounts().size());
    }

    @Test
    void findByAccountNumberReturnsCorrectAccount() {
        SavingsAccount acc = new SavingsAccount("Anna", "999", "Berlin", 1000, 0.05);
        bank.addAccount(acc);
        assertNotNull(bank.findByAccountNumber("999"));
    }

    @Test
    void findByAccountNumberReturnsNullWhenNotFound() {
        assertNull(bank.findByAccountNumber("doesnotexist"));
    }

    @Test
    void depositToIncreasesBalance() {
        CheckingAccount acc = new CheckingAccount("Max", "888", "Frankfurt", 1000, 500);
        bank.addAccount(acc);
        bank.depositTo("888", 200);
        assertEquals(1200, bank.findByAccountNumber("888").getBalance());
    }

    @Test
    void withdrawFromDecreasesBalance() {
        CheckingAccount acc = new CheckingAccount("Max", "888", "Frankfurt", 1000, 500);
        bank.addAccount(acc);
        bank.withdrawFrom("888", 300);
        assertEquals(700, bank.findByAccountNumber("888").getBalance());
    }
}