package com.finance.financeapp.dao;

import com.finance.financeapp.model.Transaction;
import java.time.LocalDate;
import java.util.List;

public interface TransactionDAOInterface {
    List<Transaction> getAllTransactions();
    void insertTransaction(Transaction transaction);
    void updateTransaction(Transaction transaction, int id);
    void deleteTransaction(int id);
    double getSumByTypeAndPeriod(String type, LocalDate fromDate, LocalDate toDate);
}
