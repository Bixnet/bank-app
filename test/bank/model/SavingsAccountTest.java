package bank.model;

import bank.exceptions.InsufficientFundsException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class SavingsAccountTest {

    @Test
    void depositIncreasesBalance() {
        SavingsAccount acc = new SavingsAccount("Test User", "111", "Berlin", 1000, 0.05);
        acc.deposit(500);
        assertEquals(1500, acc.getBalance());
    }

    @Test
    void withdrawWithinBalanceSucceeds() throws InsufficientFundsException {
        SavingsAccount acc = new SavingsAccount("Test User", "111", "Berlin", 1000, 0.05);
        acc.withdraw(300);
        assertEquals(700, acc.getBalance());
    }

    @Test
    void withdrawMoreThanBalanceThrowsException() {
        SavingsAccount acc = new SavingsAccount("Test User", "111", "Berlin", 1000, 0.05);
        // assertThrows checks that the exception actually happens — if it doesn't, the test fails
        assertThrows(InsufficientFundsException.class, () -> acc.withdraw(5000));
    }

    @Test
    void addInterestIncreasesBalanceCorrectly() {
        SavingsAccount acc = new SavingsAccount("Test User", "111", "Berlin", 1000, 0.05);
        acc.addInterest();
        assertEquals(1050, acc.getBalance()); // 1000 + (1000 * 0.05)
    }
}