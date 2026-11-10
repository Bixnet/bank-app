package bank.service;

import bank.db.AccountRepository;
import bank.exceptions.AccountNotFoundException;
import bank.exceptions.InsufficientFundsException;
import bank.exceptions.InvalidAmountException;
import bank.model.CheckingAccount;
import bank.model.SavingsAccount;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BankManagerStrictTest {

    // Each test gets its own empty database file, so bank.db is never touched
    @TempDir
    Path tempDir;

    private BankManager bank;
    private AccountRepository repo;

    @BeforeEach
    void setUp() throws Exception {
        repo = new AccountRepository("jdbc:sqlite:" + tempDir.resolve("test.db"), null, null);
        repo.createTable();
        bank = new BankManager(repo);
        bank.addAccount(new SavingsAccount("Anna", "111", "Berlin", 1000, 0.05));
        bank.addAccount(new CheckingAccount("Max", "222", "Frankfurt", 100, 50));
    }

    @Test
    void depositAddsMoneyAndSavesIt() {
        bank.deposit("111", 250.50);
        assertEquals(1250.50, bank.findByAccountNumber("111").getBalance(), 0.001);
        assertEquals(1250.50, balanceInDatabase("111"), 0.001);
    }

    @Test
    void withdrawTakesMoneyAndSavesIt() throws Exception {
        bank.withdraw("111", 400);
        assertEquals(600, bank.findByAccountNumber("111").getBalance(), 0.001);
        assertEquals(600, balanceInDatabase("111"), 0.001);
    }

    @Test
    void checkingAccountMayUseItsOverdraft() throws Exception {
        bank.withdraw("222", 150); // 100 balance + 50 overdraft
        assertEquals(-50, bank.findByAccountNumber("222").getBalance(), 0.001);
    }

    @Test
    void withdrawingTooMuchThrowsAndChangesNothing() {
        assertThrows(InsufficientFundsException.class, () -> bank.withdraw("222", 150.01));
        assertEquals(100, bank.findByAccountNumber("222").getBalance(), 0.001);
    }

    @Test
    void unknownAccountThrows() {
        assertThrows(AccountNotFoundException.class, () -> bank.deposit("999", 10));
        assertThrows(AccountNotFoundException.class, () -> bank.withdraw("999", 10));
    }

    @Test
    void invalidAmountsAreRejected() {
        for (double amount : List.of(0.0, -5.0, 0.001, 1_000_000.01, Double.NaN, Double.POSITIVE_INFINITY)) {
            assertThrows(InvalidAmountException.class, () -> bank.deposit("111", amount), "amount " + amount);
            assertThrows(InvalidAmountException.class, () -> bank.withdraw("111", amount), "amount " + amount);
        }
        assertEquals(1000, bank.findByAccountNumber("111").getBalance(), 0.001);
    }

    @Test
    void largestAllowedAmountWorks() {
        bank.deposit("111", 1_000_000);
        assertEquals(1_001_000, bank.findByAccountNumber("111").getBalance(), 0.001);
    }

    private double balanceInDatabase(String accountNumber) {
        return repo.findAll().stream()
                .filter(a -> a.getAccountNumber().equals(accountNumber))
                .findFirst().orElseThrow()
                .getBalance();
    }
}