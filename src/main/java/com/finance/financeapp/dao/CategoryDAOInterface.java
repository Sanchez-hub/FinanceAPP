package com.finance.financeapp.dao;

import com.finance.financeapp.model.Category;
import java.util.List;

public interface CategoryDAOInterface {
    List<Category> getAllCategories();
    List<Category> getCategoriesByType(String type);
    boolean insertCategory(Category category);
    void deleteCategoriesWithoutType();
}
