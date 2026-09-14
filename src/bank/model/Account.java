package bank.model;

import bank.exceptions.InsufficientFundsException;

public abstract class Account implements BankOperations {
    protected String name;
    protected String accountNumber;
    protected String branch;
    protected double balance;

    public Account(String name, String accountNumber, String branch, double balance) {
        this.name = name;
        this.accountNumber = accountNumber;
        this.branch = branch;
        this.balance = balance;
    }

    @Override
    public void deposit(double amount) {
        balance += amount;
        System.out.println("Deposited: " + amount);
    }

    @Override
    public abstract void withdraw(double amount) throws InsufficientFundsException;

    @Override
    public void showDetails() {
        System.out.println("Name: " + name);
        System.out.println("Account Number: " + accountNumber);
        System.out.println("Branch: " + branch);
        System.out.println("Balance: " + balance);
    }

    public String getAccountNumber() { return accountNumber; }
    public String getName() { return name; }
    public String getBranch() { return branch; }
    public double getBalance() { return balance; }
}