package bank;

import bank.model.SavingsAccount;
import bank.model.CheckingAccount;
import bank.service.BankManager;
import bank.gui.BankFrame;
import bank.db.AccountRepository;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        FlatLightLaf.setup();

        AccountRepository repo = new AccountRepository();
        repo.createTable();

        BankManager bank = new BankManager(repo);

        var existing = repo.findAll();
        if (existing.isEmpty()) {
            bank.addAccount(new SavingsAccount("Anna Schmidt", "654321", "Berlin", 2000, 0.05));
            bank.addAccount(new CheckingAccount("Max Müller", "123456", "Frankfurt", 1000, 500));
        } else {
            existing.forEach(bank::addAccount);
        }

        SwingUtilities.invokeLater(() -> {
            BankFrame frame = new BankFrame(bank);
            frame.setVisible(true);
        });
    }
}