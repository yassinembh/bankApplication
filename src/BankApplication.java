import java.sql.*;
import java.util.*;


public class BankApplication {
    private static final String DB_URL = "jdbc:sqlite:bank.db";
    private static Connection conn;
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        try {
            conn = DriverManager.getConnection(DB_URL);
            createTableIfNotExists();
            while (true) {
                System.out.println("1. Create an account");
                System.out.println("2. Deposit money");
                System.out.println("3. Withdraw money");
                System.out.println("4. Display balance");
                System.out.println("5. Calculate interest for a savings account");
                System.out.println("6. Quit");
                System.out.print("Enter your choice: ");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        createAccount();
                        break;
                    case 2:
                        depositMoney();
                        break;
                    case 3:
                        withdrawMoney();
                        break;
                    case 4:
                        displayBalance();
                        break;
                    case 5:
                        calculateInterest();
                        break;
                    case 6:
                        System.out.println("Goodbye!");
                        closeConnection();
                        System.exit(0);
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createTableIfNotExists() throws SQLException {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS accounts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT NOT NULL, " +
                "balance REAL NOT NULL, " +
                "type TEXT NOT NULL)";
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        }
    }

    private static void createAccount() {
        System.out.print("Enter your name: ");
        String name = scanner.next();
        System.out.print("Enter the initial balance: ");
        double initialBalance = scanner.nextDouble();
        System.out.print("Choose the account type (1 for checking, 2 for savings): ");
        int type = scanner.nextInt();
        String accountType = (type == 1) ? "checking" : "savings";
        try (PreparedStatement pstmt = conn.prepareStatement("INSERT INTO accounts (name, balance, type) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, initialBalance);
            pstmt.setString(3, accountType);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int accountId = rs.getInt(1);
                        System.out.println(accountType.substring(0, 1).toUpperCase() + accountType.substring(1) + " account created successfully. Account identifier: " + accountId);
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void depositMoney() {
        System.out.print("Enter your account identifier: ");
        int accountId = scanner.nextInt();
        System.out.print("Enter the amount to deposit: ");
        double amount = scanner.nextDouble();
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                System.out.println(amount + " euros have been deposited into your account.");
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void withdrawMoney() {
        System.out.print("Enter your account identifier: ");
        int accountId = scanner.nextInt();
        System.out.print("Enter the amount to withdraw: ");
        double amount = scanner.nextDouble();
        String sql = "SELECT balance, type FROM accounts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                String type = rs.getString("type");
                if (type.equals("checking")) {
                    if (amount > 0 && amount <= balance) {
                        updateBalance(accountId, balance - amount);
                        System.out.println(amount + " euros have been withdrawn from your account.");
                    } else {
                        System.out.println("Insufficient balance or invalid amount.");
                    }
                } else if (type.equals("savings")) {
                    if (amount > 0 && amount <= balance && amount <= 1000) {
                        updateBalance(accountId, balance - amount);
                        System.out.println(amount + " euros have been withdrawn from your account.");
                    } else {
                        System.out.println("Invalid amount or exceeds the maximum allowed withdrawal.");
                    }
                }
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void displayBalance() {
        System.out.print("Enter your account identifier: ");
        int accountId = scanner.nextInt();
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                System.out.println("Your balance is " + balance + " euros.");
            } else {
                System.out.println("Account not found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void calculateInterest() {
        System.out.print("Enter your account identifier: ");
        int accountId = scanner.nextInt();
        String sql = "SELECT balance FROM accounts WHERE id = ? AND type = 'savings'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                double interest = balance * 0.05;
                updateBalance(accountId, balance + interest);
                System.out.println("Interest for this month is " + interest + " euros.");
            } else {
                System.out.println("Interest calculation is only applicable to savings accounts or account not found.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void updateBalance(int accountId, double newBalance) {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void closeConnection() {
        try {
            if (conn != null) {
                conn.close();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}