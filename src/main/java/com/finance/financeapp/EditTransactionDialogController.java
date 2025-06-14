package com.finance.financeapp;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.ChoiceBox;

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
        typeChoiceBox.setItems(FXCollections.observableArrayList("Дохід", "Витрата"));
        typeChoiceBox.setOnAction(e -> updateCategories());
    }

    public void setTransaction(Transaction transaction, List<String> incomeCategories, List<String> expenseCategories) {
        this.transaction = transaction;
        this.incomeCategories = incomeCategories;
        this.expenseCategories = expenseCategories;
        
        // Заповнюємо поля даними з транзакції
        typeChoiceBox.setValue(transaction.getType());
        datePicker.setValue(transaction.getDate());
        amountField.setText(String.valueOf(transaction.getAmount()));
        
        // Оновлюємо категорії
        updateCategories();
        categoryComboBox.setValue(transaction.getCategory());
    }
    
    private void updateCategories() {
        String selectedType = typeChoiceBox.getValue();
        if ("Дохід".equals(selectedType)) {
            categoryComboBox.setItems(FXCollections.observableArrayList(incomeCategories));
        } else if ("Витрата".equals(selectedType)) {
            categoryComboBox.setItems(FXCollections.observableArrayList(expenseCategories));
        } else {
            categoryComboBox.setItems(FXCollections.observableArrayList());
        }
    }

    public boolean isSaved() { return saved; }

    @FXML
    private void handleSave() {
        try {
            transaction.setType(typeChoiceBox.getValue());
            transaction.setCategory(categoryComboBox.getValue());
            transaction.setDate(datePicker.getValue());
            transaction.setAmount(Double.parseDouble(amountField.getText()));
            saved = true;
            ((Stage)saveButton.getScene().getWindow()).close();
        } catch (NumberFormatException e) {
            // Обробка помилки введення суми
        }
    }

    @FXML
    private void handleCancel() {
        saved = false;
        ((Stage)cancelButton.getScene().getWindow()).close();
    }
}
