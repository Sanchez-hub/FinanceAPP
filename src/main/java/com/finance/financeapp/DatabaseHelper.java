package com.finance.financeapp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

public class DatabaseHelper {
    private static final String DB_NAME = "new_finance.db";
    private static final String DB_URL = "jdbc:sqlite:" + DB_NAME;

    public static void initializeDatabase() {
        try {
            // Перевіряємо чи існує файл БД
            File dbFile = new File(DB_NAME);
            boolean isNewDatabase = !dbFile.exists();

            // Створюємо підключення до БД
            try (Connection conn = connect()) {
                if (isNewDatabase) {
                    System.out.println("Creating new database: " + DB_NAME);
                    createTables(conn);
                } else {
                    System.out.println("Using existing database: " + DB_NAME);
                }
            }
        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Connection connect() throws SQLException {
        try {
            // Завантажуємо драйвер SQLite
            Class.forName("org.sqlite.JDBC");
            return DriverManager.getConnection(DB_URL);
        } catch (ClassNotFoundException e) {
            throw new SQLException("SQLite JDBC driver not found", e);
        }
    }

    private static void createTables(Connection conn) throws SQLException {
        String[] createTableQueries = {
            // Таблиця для транзакцій
            """
            CREATE TABLE IF NOT EXISTS transactions (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                type TEXT NOT NULL,
                category TEXT NOT NULL,
                amount REAL NOT NULL,
                description TEXT
            )
            """,
            
            // Таблиця для налаштувань
            """
            CREATE TABLE IF NOT EXISTS settings (
                key TEXT PRIMARY KEY,
                value REAL
            )
            """,
            
            // Таблиця для категорій
            """
            CREATE TABLE IF NOT EXISTS categories (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                UNIQUE(name, type)
            )
            """
        };

        try (Statement stmt = conn.createStatement()) {
            for (String query : createTableQueries) {
                stmt.execute(query);
            }
            System.out.println("All tables created successfully");
        }
    }

    /**
     * Повертає суму транзакцій за типом і періодом.
     * @param type "Дохід" або "Витрата"
     * @param fromDate початкова дата (включно)
     * @param toDate кінцева дата (включно)
     */
    public static double getSumByTypeAndPeriod(String type, LocalDate fromDate, LocalDate toDate) {
        String sql = "SELECT SUM(amount) FROM transactions WHERE type = ? AND date >= ? AND date <= ?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type);
            pstmt.setString(2, fromDate.toString());
            pstmt.setString(3, toDate.toString());
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            System.out.println("Failed to get sum: " + e.getMessage());
            return 0.0;
        }
    }

    public static List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction t = new Transaction(
                    rs.getInt("id"),
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("type"),
                    rs.getString("category"),
                    rs.getDouble("amount"),
                    rs.getString("description")
                );
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Select failed: " + e.getMessage());
        }
        return list;
    }

    public static void insertTransaction(Transaction t) {
        String sql = "INSERT INTO transactions(date, type, category, amount, description) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getDate().toString());
            pstmt.setString(2, t.getType());
            pstmt.setString(3, t.getCategory());
            pstmt.setDouble(4, t.getAmount());
            pstmt.setString(5, t.getDescription() == null ? "" : t.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Insert failed: " + e.getMessage());
        }
    }

    public static void updateTransaction(Transaction t, int id) {
        String sql = "UPDATE transactions SET date=?, type=?, category=?, amount=?, description=? WHERE id=?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, t.getDate().toString());
            pstmt.setString(2, t.getType());
            pstmt.setString(3, t.getCategory());
            pstmt.setDouble(4, t.getAmount());
            pstmt.setString(5, t.getDescription() == null ? "" : t.getDescription());
            pstmt.setInt(6, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Update failed: " + e.getMessage());
        }
    }

    public static void deleteTransaction(int id) {
        String sql = "DELETE FROM transactions WHERE id=?";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Delete failed: " + e.getMessage());
        }
    }

    public static void saveBudget(double budget) {
        String sql = "UPDATE settings SET value = ? WHERE key = 'monthly_budget'";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, budget);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double getBudget() {
        String sql = "SELECT value FROM settings WHERE key = 'monthly_budget'";
        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("value");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public static void setBudget(double budget) {
        String sql = "INSERT OR REPLACE INTO settings (key, value) VALUES ('monthly_budget', ?)";
        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, budget);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Set budget failed: " + e.getMessage());
        }
    }
}
