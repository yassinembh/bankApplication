import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import java.sql.*;

public class BankApplicationTest {
    private static Connection conn;

    @BeforeAll
    public static void setup() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite:bank_test.db");
        createTableIfNotExists();
    }

    @BeforeEach
    public void beforeEachTest() throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("DELETE FROM accounts");
        }
    }

    @AfterAll
    public static void teardown() throws SQLException {
        if (conn != null) {
            conn.close();
        }
    }

    @Test
    public void testCreateAccount() throws SQLException {
        createAccount("Alice Smith", 5000, "savings");
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT * FROM accounts WHERE name = 'Alice Smith'");
            assertTrue(rs.next());
            assertEquals(5000, rs.getDouble("balance"));
            assertEquals("savings", rs.getString("type"));
        }
    }

    @Test
    public void testDepositMoney() throws SQLException {
        int accountId = createAccount("Alice Smith", 5000, "savings");
        depositMoney(accountId, 1000);
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT balance FROM accounts WHERE id = " + accountId);
            assertTrue(rs.next());
            assertEquals(6000, rs.getDouble("balance"));
        }
    }

    @Test
    public void testWithdrawMoney() throws SQLException {
        int accountId = createAccount("Alice Smith", 5000, "checking");
        withdrawMoney(accountId, 1000);
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT balance FROM accounts WHERE id = " + accountId);
            assertTrue(rs.next());
            assertEquals(4000, rs.getDouble("balance"));
        }
    }

    @Test
    public void testDisplayBalance() throws SQLException {
        int accountId = createAccount("Alice Smith", 5000, "savings");
        double balance = getBalance(accountId);
        assertEquals(5000, balance);
    }

    @Test
    public void testCalculateInterest() throws SQLException {
        int accountId = createAccount("Alice Smith", 5000, "savings");
        calculateInterest(accountId);
        try (Statement stmt = conn.createStatement()) {
            ResultSet rs = stmt.executeQuery("SELECT balance FROM accounts WHERE id = " + accountId);
            assertTrue(rs.next());
            assertEquals(5250, rs.getDouble("balance"));
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

    private int createAccount(String name, double initialBalance, String type) throws SQLException {
        String sql = "INSERT INTO accounts (name, balance, type) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setDouble(2, initialBalance);
            pstmt.setString(3, type);
            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    private void depositMoney(int accountId, double amount) throws SQLException {
        String sql = "UPDATE accounts SET balance = balance + ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        }
    }

    private void withdrawMoney(int accountId, double amount) throws SQLException {
        String sql = "SELECT balance, type FROM accounts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                String type = rs.getString("type");
                if (type.equals("checking") && amount <= balance) {
                    updateBalance(accountId, balance - amount);
                } else if (type.equals("savings") && amount <= balance && amount <= 1000) {
                    updateBalance(accountId, balance - amount);
                }
            }
        }
    }

    private double getBalance(int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("balance");
            }
        }
        return 0;
    }

    private void calculateInterest(int accountId) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE id = ? AND type = 'savings'";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                double balance = rs.getDouble("balance");
                double interest = balance * 0.05;
                updateBalance(accountId, balance + interest);
            }
        }
    }

    private void updateBalance(int accountId, double newBalance) throws SQLException {
        String sql = "UPDATE accounts SET balance = ? WHERE id = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, accountId);
            pstmt.executeUpdate();
        }
    }
}
