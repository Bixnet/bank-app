package bank.db;

import bank.model.Account;
import bank.model.SavingsAccount;
import bank.model.CheckingAccount;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class AccountRepository {
    private static final String DEFAULT_URL = "jdbc:sqlite:bank.db";
    private static final Logger logger = Logger.getLogger(AccountRepository.class.getName());

    private final String url;
    private final String user;
    private final String password;

    // Desktop app and tests: local SQLite file, no login needed
    public AccountRepository() {
        this(DEFAULT_URL, null, null);
    }

    // Cloud: any JDBC URL (for example Postgres) with a login
    public AccountRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    private Connection connect() throws SQLException {
        if (user == null) {
            return DriverManager.getConnection(url);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS accounts (" +
                "accountNumber TEXT PRIMARY KEY, " +
                "name TEXT, " +
                "branch TEXT, " +
                "balance DOUBLE PRECISION, " +
                "type TEXT, " +
                "extra DOUBLE PRECISION)";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            logger.severe("Error creating table: " + e.getMessage());
        }
    }

    public void save(Account acc) {
        String sql = "INSERT INTO accounts (accountNumber, name, branch, balance, type, extra) " +
                "VALUES (?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (accountNumber) DO UPDATE SET " +
                "name = excluded.name, branch = excluded.branch, " +
                "balance = excluded.balance, type = excluded.type, extra = excluded.extra";
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, acc.getAccountNumber());
            ps.setString(2, acc.getName());
            ps.setString(3, acc.getBranch());
            ps.setDouble(4, acc.getBalance());

            if (acc instanceof SavingsAccount sa) {
                ps.setString(5, "SAVINGS");
                ps.setDouble(6, sa.getInterestRate());
            } else if (acc instanceof CheckingAccount ca) {
                ps.setString(5, "CHECKING");
                ps.setDouble(6, ca.getOverdraftLimit());
            }

            ps.executeUpdate();
        } catch (SQLException e) {
            logger.severe("Error saving account: " + e.getMessage());
        }
    }

    public List<Account> findAll() {
        List<Account> result = new ArrayList<>();
        String sql = "SELECT * FROM accounts";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String type = rs.getString("type");
                String name = rs.getString("name");
                String accNum = rs.getString("accountNumber");
                String branch = rs.getString("branch");
                double balance = rs.getDouble("balance");
                double extra = rs.getDouble("extra");

                if ("SAVINGS".equals(type)) {
                    result.add(new SavingsAccount(name, accNum, branch, balance, extra));
                } else if ("CHECKING".equals(type)) {
                    result.add(new CheckingAccount(name, accNum, branch, balance, extra));
                }
            }
        } catch (SQLException e) {
            logger.severe("Error loading accounts: " + e.getMessage());
        }
        return result;
    }
}