package bank.model;

import bank.exceptions.InsufficientFundsException;

public class SavingsAccount extends Account {
    private double interestRate;

    public SavingsAccount(String name, String accountNumber, String branch, double balance, double interestRate) {
        super(name, accountNumber, branch, balance);
        this.interestRate = interestRate;
    }

    @Override
    public void withdraw(double amount) throws InsufficientFundsException {
        if (amount > balance) {
            throw new InsufficientFundsException("Insufficient funds in savings account " + accountNumber);
        }
        balance -= amount;
        System.out.println("Withdrew: " + amount);
    }

    public void addInterest() {
        double interest = balance * interestRate;
        balance += interest;
        System.out.println("Interest added: " + interest);
    }

    @Override
    public void showDetails() {
        super.showDetails();
        System.out.println("Interest Rate: " + interestRate);
    }

    public double getInterestRate() { return interestRate; }
}