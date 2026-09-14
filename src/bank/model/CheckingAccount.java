package bank.model;

import bank.exceptions.InsufficientFundsException;

public class CheckingAccount extends Account {
    private double overdraftLimit;

    public CheckingAccount(String name, String accountNumber, String branch, double balance, double overdraftLimit) {
        super(name, accountNumber, branch, balance);
        this.overdraftLimit = overdraftLimit;
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > balance + overdraftLimit) {
            throw new InsufficientFundsException("Overdraft limit exceeded on account " + accountNumber);
        }
        balance -= amount;
        System.out.println("Withdrew: " + amount);
    }

    @Override
    public void showDetails() {
        super.showDetails();
        System.out.println("Overdraft Limit: " + overdraftLimit);
    }

    public double getOverdraftLimit() { return overdraftLimit; }
}