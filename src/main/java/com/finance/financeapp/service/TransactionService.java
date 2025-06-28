package com.finance.financeapp.service;

import com.finance.financeapp.dao.TransactionDAOInterface;
import com.finance.financeapp.model.Transaction;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TransactionService {
    private final TransactionDAOInterface transactionDAO;

    public TransactionService(TransactionDAOInterface transactionDAO) {
        this.transactionDAO = transactionDAO;
    }

    public List<Transaction> getFilteredTransactions(String type, String category, String searchText) {
        List<Transaction> all = transactionDAO.getAllTransactions();
        return all.stream()
            .filter(t -> type == null || type.equals("Всі") || t.getType().equals(type))
            .filter(t -> category == null || category.equals("Всі категорії") || t.getCategory().equals(category))
            .filter(t -> searchText == null || searchText.isEmpty() ||
                    t.getCategory().toLowerCase().contains(searchText.toLowerCase()) ||
                    t.getType().toLowerCase().contains(searchText.toLowerCase()) ||
                    String.valueOf(t.getAmount()).contains(searchText))
            .collect(Collectors.toList());
    }

    public List<Transaction> getAllTransactions() {
        return transactionDAO.getAllTransactions();
    }

    // Інші методи сервісу...
}
