package com.finance.financeapp.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceBox;
import com.finance.financeapp.model.Transaction;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javafx.util.StringConverter;
import java.util.List;
import java.util.ArrayList;

public class EditTransactionDialogController {
    @FXML private ChoiceBox<String> typeChoiceBox;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField amountField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private Transaction transaction;
    private boolean saved = false;

    private List<String> incomeCategories = new ArrayList<>();
    private List<String> expenseCategories = new ArrayList<>();

    @FXML
    public void initialize() {
        System.out.println("Initializing EditTransactionDialogController");
        
        // Налаштування ChoiceBox для типу
        typeChoiceBox.setItems(FXCollections.observableArrayList("Дохід", "Витрата"));
        typeChoiceBox.setOnAction(e -> updateCategories());
        
        // Налаштування DatePicker
        datePicker.setConverter(new StringConverter<LocalDate>() {
            private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            
            @Override
            public String toString(LocalDate date) {
                return date != null ? formatter.format(date) : "";
            }
            
            @Override
            public LocalDate fromString(String string) {
                return string != null && !string.isEmpty() ? LocalDate.parse(string, formatter) : null;
            }
        });
        
        // Налаштування TextField для суми
        amountField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                amountField.setText(oldVal);
            }
        });
    }

    public void setTransaction(Transaction transaction, List<String> incomeCategories, List<String> expenseCategories) {
        System.out.println("Setting transaction in dialog: " + transaction);
        this.transaction = transaction;
        this.incomeCategories = incomeCategories;
        this.expenseCategories = expenseCategories;
        
        // Заповнюємо поля даними з транзакції
        typeChoiceBox.setValue(transaction.getType());
        datePicker.setValue(transaction.getDate());
        amountField.setText(String.format("%.2f", transaction.getAmount()));
        
        // Оновлюємо категорії
        updateCategories();
        categoryComboBox.setValue(transaction.getCategory());
        
        System.out.println("Transaction data set in dialog");
    }
    
    private void updateCategories() {
        System.out.println("Updating categories for type: " + typeChoiceBox.getValue());
        String selectedType = typeChoiceBox.getValue();
        if ("Дохід".equals(selectedType)) {
            categoryComboBox.setItems(FXCollections.observableArrayList(incomeCategories));
        } else if ("Витрата".equals(selectedType)) {
            categoryComboBox.setItems(FXCollections.observableArrayList(expenseCategories));
        } else {
            categoryComboBox.setItems(FXCollections.observableArrayList());
        }
    }

    public boolean isSaved() { 
        return saved; 
    }

    @FXML
    private void handleSave() {
        try {
            System.out.println("Handling save in dialog");
            
            // Валідація полів
            if (typeChoiceBox.getValue() == null || categoryComboBox.getValue() == null || 
                datePicker.getValue() == null || amountField.getText().isEmpty()) {
                showError("Будь ласка, заповніть всі поля");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountField.getText());
                if (amount <= 0) {
                    showError("Сума повинна бути більше 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Некоректний формат суми");
                return;
            }

            // Оновлюємо транзакцію
            transaction.setType(typeChoiceBox.getValue());
            transaction.setCategory(categoryComboBox.getValue());
            transaction.setDate(datePicker.getValue());
            transaction.setAmount(amount);
            
            System.out.println("Transaction updated in dialog: " + transaction);
            saved = true;
            ((Stage)saveButton.getScene().getWindow()).close();
        } catch (Exception e) {
            e.printStackTrace();
            showError("Помилка при збереженні: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Помилка");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Transaction getUpdatedTransaction() {
        return transaction;
    }

    @FXML
    private void handleCancel() {
        System.out.println("Dialog cancelled");
        saved = false;
        ((Stage)cancelButton.getScene().getWindow()).close();
    }
}
