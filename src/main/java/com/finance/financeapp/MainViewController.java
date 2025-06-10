package com.finance.financeapp;

import javafx.scene.control.cell.PropertyValueFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import java.util.Optional;
import javafx.geometry.Pos;

import javafx.util.Callback;
import javafx.util.StringConverter;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class MainViewController {
    @FXML
    private AnchorPane rootPane;
    @FXML
    private DatePicker datePicker;
    @FXML
    private Button addButton;
    @FXML
    private ChoiceBox<String> typeChoiceBox;
    @FXML
    private ComboBox<String> categoryComboBox;
    @FXML
    private TextField amountField;
    @FXML
    private Transaction editingTransaction = null;




    private ObservableList<String> entries = FXCollections.observableArrayList();
    private ObservableList<String> incomeCategories = FXCollections.observableArrayList(
            "Зарплата", "Підробіток", "Відсотки по депозиту", "Продаж речей", "Подарунки"
    );
    private ObservableList<String> expenseCategories = FXCollections.observableArrayList(
            "Продукти", "Транспорт", "Комунальні послуги", "Одяг", "Розваги", "Здоров'я", "Освіта"
    );
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private static final String DATA_FILE = "finance_data.json";
    private static final String CATEGORIES_FILE = "categories.json";
    private ObjectMapper objectMapper;


    @FXML
    private TableView<Transaction> transactionsTable;
    @FXML
    private TableColumn<Transaction, LocalDate> dateColumn;
    @FXML
    private TableColumn<Transaction, String> typeColumn;
    @FXML
    private TableColumn<Transaction, String> categoryColumn;
    @FXML
    private TableColumn<Transaction, Double> amountColumn;
    @FXML
    private TableColumn<Transaction, Void> actionColumn;




    @FXML
    public Callback<TableColumn<Transaction, Void>, TableCell<Transaction, Void>> deleteButtonCellFactory() {
        return column -> new TableCell<>() {
            private final Button deleteButton = new Button("Видалити");
            {
                deleteButton.getStyleClass().add("delete-button");
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableRow().getItem();
                    if (transaction != null) {
                        transactions.remove(transaction);
                        saveData();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteButton);
            }
        };
    }


    @FXML
    public void initialize() {
        setupObjectMapper();
        loadData();
        setupControls();
        setupTable();


        try {
            var cssResource = getClass().getResource("styles.css");
            if (cssResource != null) {
                rootPane.getStylesheets().add(cssResource.toExternalForm());
            } else {
                System.err.println("CSS файл не знайдено в /styles.css");
            }
        } catch (Exception e) {
            System.err.println("Помилка завантаження CSS: " + e.getMessage());
        }







        Platform.runLater(() -> {
            Stage stage = (Stage) datePicker.getScene().getWindow();
            stage.setOnCloseRequest(event -> saveData());
        });
    }



    @SuppressWarnings("unchecked")
    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton = new Button("\u270E"); // символ олівця
            private final Button deleteButton = new Button("\u2716"); // символ хрестика
            private final HBox buttonBox = new HBox(5); // відступ між кнопками 5 пікселів

            {
                // Налаштування стилів кнопок
                editButton.getStyleClass().addAll("icon-button", "edit-icon");
                deleteButton.getStyleClass().addAll("icon-button", "delete-icon");

                // Налаштування кнопки редагування
                editButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction != null) {
                        editingTransaction = transaction;
                        editTransaction(transaction);
                        updateAddButtonText();
                    }
                });

                // Налаштування кнопки видалення
                deleteButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction != null) {
                        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                        alert.setTitle("Підтвердження видалення");
                        alert.setHeaderText(null);
                        alert.setContentText("Ви впевнені, що хочете видалити цей запис?");

                        Optional<ButtonType> result = alert.showAndWait();
                        if (result.isPresent() && result.get() == ButtonType.OK) {
                            transactions.remove(transaction);
                            saveData();
                        }
                    }
                });

                buttonBox.getChildren().addAll(editButton, deleteButton);
                buttonBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        });

        transactionsTable.setItems(transactions);

        // Додаємо тільки потрібні колонки (на всяк випадок)
        transactionsTable.getColumns().setAll(dateColumn, typeColumn, categoryColumn, amountColumn, actionColumn);

        // Встановлюємо політику resize
        transactionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }




    private void setupObjectMapper() {
                    objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                }

    private void setupControls() {
        datePicker.setValue(LocalDate.now());
        datePicker.setConverter(new StringConverter<LocalDate>() {
        private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

        @Override
        public String toString(LocalDate date) {
            return (date != null) ? dateFormatter.format(date) : "";
        }

        @Override
        public LocalDate fromString(String string) {
            return (string != null && !string.isEmpty())
                    ? LocalDate.parse(string, dateFormatter)
                    : null;
        }
        });


        typeChoiceBox.setItems(FXCollections.observableArrayList("Дохід", "Витрата"));
        categoryComboBox.setEditable(true);

        addButton.setText("Додати");

        typeChoiceBox.setOnAction(e -> updateCategories());
        categoryComboBox.setOnAction(e -> {
        String newCategory = categoryComboBox.getValue();
        if (newCategory != null && !newCategory.trim().isEmpty()) {
            addNewCategory(newCategory);
        }
        });

        transactions.addListener((ListChangeListener.Change<? extends Transaction> c) -> {
            transactionsTable.refresh();
        });

    }




    private void loadData() {
        try {
            if (Files.exists(Paths.get(CATEGORIES_FILE))) {
                Map<String, List<String>> categoriesMap = objectMapper.readValue(
                        new File(CATEGORIES_FILE),
                        new TypeReference<Map<String, List<String>>>() {
                        }
                );


                incomeCategories.setAll(categoriesMap.getOrDefault("income", new ArrayList<>()));
                expenseCategories.setAll(categoriesMap.getOrDefault("expense", new ArrayList<>()));
            }

            if (Files.exists(Paths.get(DATA_FILE))) {
                List<Transaction> loadedTransactions = objectMapper.readValue(
                        new File(DATA_FILE),
                        objectMapper.getTypeFactory().constructCollectionType(List.class, Transaction.class)
                );
                transactions.setAll(loadedTransactions);
                updateListView();
            }
        } catch (IOException e) {
            showError("Помилка завантаження даних: " + e.getMessage());
        }
    }

    private void saveData() {
        try {
            Map<String, List<String>> categoriesMap = new HashMap<>();
            categoriesMap.put("income", new ArrayList<>(incomeCategories));
            categoriesMap.put("expense", new ArrayList<>(expenseCategories));
            objectMapper.writeValue(new File(CATEGORIES_FILE), categoriesMap);
            objectMapper.writeValue(new File(DATA_FILE), new ArrayList<>(transactions));
        } catch (IOException e) {
            showError("Помилка збереження даних: " + e.getMessage());
        }
    }

    private void updateListView() {
        entries.clear();
        for (Transaction transaction : transactions) {
            entries.add(transaction.toString());
        }
    }

    private void updateCategories() {
        String selectedType = typeChoiceBox.getValue();
        if ("Дохід".equals(selectedType)) {
            categoryComboBox.setItems(incomeCategories);
        } else if ("Витрата".equals(selectedType)) {
            categoryComboBox.setItems(expenseCategories);
        }
    }

    private void addNewCategory(String newCategory) {
        String selectedType = typeChoiceBox.getValue();
        if (selectedType == null) return;

        ObservableList<String> categories = "Дохід".equals(selectedType)
                ? incomeCategories
                : expenseCategories;

        if (!categories.contains(newCategory)) {
            categories.add(newCategory);
            categories.sort(String::compareTo);
        }
    }

    private void updateAddButtonText() {
        if (editingTransaction != null) {
            addButton.setText("Зберегти зміни");
        } else {
            addButton.setText("Додати");
        }
    }



    @FXML
    private void handleAddEntry() {
        try {
            String type = typeChoiceBox.getValue();
            String category = categoryComboBox.getValue();
            String amountStr = amountField.getText();
            LocalDate date = datePicker.getValue();

            if (type == null || category == null || category.isEmpty() || amountStr.isEmpty() || date == null) {
                showError("Будь ласка, заповніть всі поля");
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    showError("Сума повинна бути більше 0");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Некоректний формат суми");
                return;
            }

            Transaction newTransaction = new Transaction(date, type, category, amount);

            if (editingTransaction != null) {
                // Режим редагування
                int index = transactions.indexOf(editingTransaction);
                if (index != -1) {
                    transactions.set(index, newTransaction);
                }
                editingTransaction = null;
                addButton.setText("Додати"); // повертаємо оригінальний текст кнопки
            } else {
                // Режим додавання нового запису
                transactions.add(newTransaction);
            }

            addNewCategory(category);
            clearFields();
            saveData();

        } catch (Exception e) {
            showError("Помилка при додаванні/редагуванні запису: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        editingTransaction = null;
        clearFields();
        addButton.setText("Додати");
    }



    private void editTransaction(Transaction transaction) {
        datePicker.setValue(transaction.getDate());
        typeChoiceBox.setValue(transaction.getType());
        updateCategories(); // оновлюємо список категорій відповідно до вибраного типу
        categoryComboBox.setValue(transaction.getCategory());
        amountField.setText(String.valueOf(transaction.getAmount()));
        addButton.setText("Зберегти зміни"); // змінюємо текст кнопки
    }



    private void clearFields() {
        datePicker.setValue(LocalDate.now());
        categoryComboBox.setValue(null);
        amountField.clear();
        typeChoiceBox.setValue(null);
        editingTransaction = null; // очищаємо режим редагування
    }


    private void showError(String message) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Помилка");
                    alert.setHeaderText(null);
                    alert.setContentText(message);
                    alert.showAndWait();
                }
            }



