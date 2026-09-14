package bank.gui;

import bank.model.Account;
import bank.service.BankManager;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class BankFrame extends JFrame {
    private BankManager bank;
    private JTextField accountNumberField;
    private JTextField amountField;
    private DefaultTableModel tableModel;
    private JTable table;

    public BankFrame(BankManager bank) {
        this.bank = bank;

        setTitle("Bank App");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel();
        inputPanel.add(new JLabel("Account #:"));
        accountNumberField = new JTextField(10);
        inputPanel.add(accountNumberField);

        inputPanel.add(new JLabel("Amount:"));
        amountField = new JTextField(8);
        inputPanel.add(amountField);

        JButton depositBtn = new JButton("Deposit");
        JButton withdrawBtn = new JButton("Withdraw");
        JButton refreshBtn = new JButton("Refresh");
        inputPanel.add(depositBtn);
        inputPanel.add(withdrawBtn);
        inputPanel.add(refreshBtn);

        add(inputPanel, BorderLayout.NORTH);

        String[] columns = {"Type", "Account #", "Name", "Branch", "Balance"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        table = new JTable(tableModel);
        add(new JScrollPane(table), BorderLayout.CENTER);

        depositBtn.addActionListener(e -> handleDeposit());
        withdrawBtn.addActionListener(e -> handleWithdraw());
        refreshBtn.addActionListener(e -> refreshTable());

        refreshTable();
    }

    private void handleDeposit() {
        String accNum = accountNumberField.getText().trim();
        String amountText = amountField.getText().trim();

        if (accNum.isEmpty()) {
            showError("Please enter an account number.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
            return;
        }

        if (amount <= 0) {
            showError("Amount must be greater than zero.");
            return;
        }

        bank.depositTo(accNum, amount);
        refreshTable();
    }

    private void handleWithdraw() {
        String accNum = accountNumberField.getText().trim();
        String amountText = amountField.getText().trim();

        if (accNum.isEmpty()) {
            showError("Please enter an account number.");
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountText);
        } catch (NumberFormatException e) {
            showError("Amount must be a valid number.");
            return;
        }

        if (amount <= 0) {
            showError("Amount must be greater than zero.");
            return;
        }

        bank.withdrawFrom(accNum, amount);
        refreshTable();
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (Account acc : bank.getAllAccounts()) {
            tableModel.addRow(new Object[]{
                    acc.getClass().getSimpleName(),
                    acc.getAccountNumber(),
                    acc.getName(),
                    acc.getBranch(),
                    acc.getBalance()
            });
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid Input", JOptionPane.ERROR_MESSAGE);
    }
}