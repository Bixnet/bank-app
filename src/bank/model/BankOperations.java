package bank.model;

import bank.exceptions.InsufficientFundsException;

public interface BankOperations {
    void deposit(double amount);
    void withdraw(double amount) throws InsufficientFundsException;
    void showDetails();
}