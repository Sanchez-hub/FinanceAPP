package com.finance.financeapp;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javafx.collections.FXCollections;

public class CategoryManager {
    public static List<String> loadCategories() {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories ORDER BY name";
        try (Connection conn = DatabaseHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public static boolean insertCategory(String name) {
        String sql = "INSERT INTO categories(name) VALUES (?)";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            // Якщо категорія вже існує
            if (e.getMessage().contains("UNIQUE")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public static boolean insertCategory(String name, String type) {
        String sql = "INSERT INTO categories(name, type) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setString(2, type);
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage().contains("UNIQUE")) {
                return false;
            }
            e.printStackTrace();
            return false;
        }
    }

    public static List<String> loadCategories(String type) {
        List<String> categories = new ArrayList<>();
        String sql = "SELECT name FROM categories WHERE TRIM(type) = ? ORDER BY name";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.trim());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categories.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    public static void deleteCategoriesWithoutType() {
        String sql = "DELETE FROM categories WHERE type IS NULL OR TRIM(type) = ''";
        try (Connection conn = DatabaseHelper.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void initializeDefaultCategories() {
        List<String> defaultIncomeCategories = Arrays.asList(
            "Зарплата", "Підробіток", "Відсотки по депозиту", "Продаж речей", "Подарунки"
        );
        
        List<String> defaultExpenseCategories = Arrays.asList(
            "Продукти", "Транспорт", "Комунальні послуги", "Одяг", "Розваги", "Здоров'я", "Освіта"
        );

        // Додаємо категорії доходів
        for (String category : defaultIncomeCategories) {
            try {
                insertCategory(category, "Дохід");
            } catch (Exception e) {
                // Ігноруємо помилки дублікатів
            }
        }

        // Додаємо категорії витрат
        for (String category : defaultExpenseCategories) {
            try {
                insertCategory(category, "Витрата");
            } catch (Exception e) {
                // Ігноруємо помилки дублікатів
            }
        }
    }
}
