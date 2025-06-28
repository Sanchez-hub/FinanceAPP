package com.finance.financeapp.service;

import com.finance.financeapp.dao.CategoryDAOInterface;
import com.finance.financeapp.model.Category;

import java.util.Arrays;
import java.util.List;

public class CategoryService {
    private final CategoryDAOInterface categoryDAO;

    public CategoryService(CategoryDAOInterface categoryDAO) {
        this.categoryDAO = categoryDAO;
    }

    public List<Category> getAllCategories() {
        return categoryDAO.getAllCategories();
    }

    public List<Category> getCategoriesByType(String type) {
        return categoryDAO.getCategoriesByType(type);
    }

    public boolean addCategory(String name, String type) {
        return categoryDAO.insertCategory(new Category(name, type));
    }

    public void deleteCategoriesWithoutType() {
        categoryDAO.deleteCategoriesWithoutType();
    }

    public void initializeDefaultCategories() {
        List<String> defaultIncomeCategories = Arrays.asList(
            "Зарплата", "Підробіток", "Відсотки по депозиту", "Продаж речей", "Подарунки"
        );
        List<String> defaultExpenseCategories = Arrays.asList(
            "Продукти", "Транспорт", "Комунальні послуги", "Одяг", "Розваги", "Здоров'я", "Освіта"
        );

        for (String category : defaultIncomeCategories) {
            try {
                addCategory(category, "Дохід");
            } catch (Exception ignored) {}
        }
        for (String category : defaultExpenseCategories) {
            try {
                addCategory(category, "Витрата");
            } catch (Exception ignored) {}
        }
    }
}
