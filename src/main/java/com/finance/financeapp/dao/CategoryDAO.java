package com.finance.financeapp.dao;

import com.finance.financeapp.database.DatabaseHelper;
import com.finance.financeapp.model.Category;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO implements CategoryDAOInterface {
    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, type FROM categories ORDER BY name";
        try (Connection conn = DatabaseHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public List<Category> getCategoriesByType(String type) {
        List<Category> categories = new ArrayList<>();
        String sql = "SELECT id, name, type FROM categories WHERE TRIM(type) = ? ORDER BY name";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, type.trim());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                categories.add(new Category(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getString("type")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categories;
    }

    @Override
    public boolean insertCategory(Category category) {
        String sql = "INSERT INTO categories(name, type) VALUES (?, ?)";
        try (Connection conn = DatabaseHelper.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, category.getName());
            pstmt.setString(2, category.getType());
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

    @Override
    public void deleteCategoriesWithoutType() {
        String sql = "DELETE FROM categories WHERE type IS NULL OR TRIM(type) = ''";
        try (Connection conn = DatabaseHelper.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
