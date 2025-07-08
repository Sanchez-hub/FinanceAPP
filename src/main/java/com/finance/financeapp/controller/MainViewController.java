package com.finance.financeapp.controller;

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
import javafx.geometry.Insets;

import javafx.util.Callback;
import javafx.util.StringConverter;
import java.io.*;
import java.net.URL;
import java.nio.file.*;
import java.util.*;
import java.sql.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.scene.layout.VBox;
import java.util.stream.Collectors;
import com.finance.financeapp.service.TransactionService;
import com.finance.financeapp.service.CategoryService;
import com.finance.financeapp.model.Transaction;
import com.finance.financeapp.database.DatabaseHelper;
import javafx.scene.chart.PieChart;
import com.finance.financeapp.model.FinanceRecord;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.geometry.Side;
import javafx.geometry.Insets;


// Контролер головного вікна додатку фінансів
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

    @FXML
    private Button addCategoryButton;

    private ObservableList<String> entries = FXCollections.observableArrayList();
    private ObservableList<String> incomeCategories = FXCollections.observableArrayList(
            "Зарплата", "Підробіток", "Відсотки по депозиту", "Продаж речей", "Подарунки"
    );
    private ObservableList<String> expenseCategories = FXCollections.observableArrayList(
            "Продукти", "Транспорт", "Комунальні послуги", "Одяг", "Розваги", "Здоров'я", "Освіта"
    );
    private ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    /*private static final String DATA_FILE = "finance_data.json";*/
    /*private static final String CATEGORIES_FILE = "categories.json";*/
    /*private ObjectMapper objectMapper;*/


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
    private Label incomeLabel;
    @FXML
    private Label expenseLabel;
    @FXML
    private Label balanceLabel;
    @FXML
    private Label budgetLabel;
   
    @FXML
    private ComboBox<String> filterTypeComboBox;
    @FXML
    private ComboBox<String> filterCategoryComboBox;
    @FXML
    private TextField searchField;

    @FXML
    private TextField budgetField;
    @FXML
    private Button setBudgetButton;
    private double monthlyBudget = 0.0;

    @FXML
    private DatePicker startDatePicker;
    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<String> editCategoryComboBox;

    @FXML
    private ComboBox<String> periodComboBox;

    @FXML
    private Button resetFilterButton;

    private String currentCategoryFilter = null;

    private final DatabaseHelper databaseHelper;
    private final TransactionService transactionService;
    private final CategoryService categoryService;

    @FXML private PieChart expensePieChart;
    @FXML private LineChart<String, Number> expenseLineChart;

    private ObservableList<Transaction> allTransactions = FXCollections.observableArrayList();
    private ObservableList<Transaction> filteredTransactions = FXCollections.observableArrayList();


    @FXML
    private Button reminderButton;

    public MainViewController(TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.databaseHelper = new DatabaseHelper();
    }

    @FXML
    public Callback<TableColumn<Transaction, Void>, TableCell<Transaction, Void>> deleteButtonCellFactory() {
        return column -> new TableCell<>() {
            private final Button deleteButton;
            private final HBox buttonBox = new HBox(5); // відступ між кнопками 5 пікселів

            {
                // DELETE ICON
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/finance/financeapp/icons/delete.png")));
                deleteIcon.setFitWidth(18);
                deleteIcon.setFitHeight(18);
                deleteButton = new Button();
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().addAll("icon-button", "delete-icon");
                deleteButton.setTooltip(new Tooltip("Видалити"));

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
                            DatabaseHelper.deleteTransaction(transaction.getId());
                            loadTransactionsFromDB();
                            updateStatsPanel();
                        }
                    }
                });

                buttonBox.getChildren().addAll(deleteButton);
                buttonBox.setAlignment(Pos.CENTER);
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttonBox);
            }
        };
    }


    @FXML
    public void initialize() {
        /*setupObjectMapper();*/
        setupTable();
        loadTransactionsFromDB();
        setupControls();
        
        
       

        try {
            var cssResource = getClass().getResource("/com/finance/financeapp/styles.css");
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
            
        });

       
    

        monthlyBudget = DatabaseHelper.getBudget();
        if (monthlyBudget > 0) {
            budgetField.setText(String.valueOf(monthlyBudget));
        }

        refreshCategories();

        addCategoryButton.setOnAction(e -> openAddCategoryDialog());

        
        setupChart();
        transactionsTable.setItems(filteredTransactions);
        resetFilterButton.setOnAction(e -> resetPieChartFilter());

        transactionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));

        actionColumn.setCellFactory(column -> new TableCell<>() {
            private final Button editButton;
            private final Button deleteButton;
            private final HBox buttonBox = new HBox(5); // відступ між кнопками 5 пікселів

            {
                // EDIT ICON
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/finance/financeapp/icons/edit.png")));
                editIcon.setFitWidth(18);
                editIcon.setFitHeight(18);
                editButton = new Button();
                editButton.setGraphic(editIcon);
                editButton.getStyleClass().addAll("icon-button", "edit-icon");
                editButton.setTooltip(new Tooltip("Редагувати"));

                // DELETE ICON
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/finance/financeapp/icons/delete.png")));
                deleteIcon.setFitWidth(18);
                deleteIcon.setFitHeight(18);
                deleteButton = new Button();
                deleteButton.setGraphic(deleteIcon);
                deleteButton.getStyleClass().addAll("icon-button", "delete-icon");
                deleteButton.setTooltip(new Tooltip("Видалити"));

                // Налаштування кнопки редагування
                editButton.setOnAction(event -> {
                    Transaction transaction = getTableView().getItems().get(getIndex());
                    if (transaction != null) {
                        try {
                            System.out.println("Starting edit for transaction: " + transaction);
                            editingTransaction = transaction;
                            URL fxmlUrl = getClass().getResource("/com/finance/financeapp/edit-transaction-dialog.fxml");
                            if (fxmlUrl == null) {
                                throw new IOException("Cannot find edit-transaction-dialog.fxml");
                            }
                            FXMLLoader loader = new FXMLLoader(fxmlUrl);
                            Parent root = loader.load();
                            EditTransactionDialogController controller = loader.getController();
                            List<String> incomeCats = categoryService.getCategoriesByType("Дохід")
                                .stream().map(c -> c.getName()).collect(Collectors.toList());
                            List<String> expenseCats = categoryService.getCategoriesByType("Витрата")
                                .stream().map(c -> c.getName()).collect(Collectors.toList());
                            controller.setTransaction(transaction, incomeCats, expenseCats);
                            Stage dialogStage = new Stage();
                            dialogStage.initModality(Modality.APPLICATION_MODAL);
                            dialogStage.setTitle("Редагувати запис");
                            Scene scene = new Scene(root);
                            scene.getStylesheets().add(getClass().getResource("/com/finance/financeapp/styles.css").toExternalForm());
                            dialogStage.setScene(scene);
                            dialogStage.showAndWait();
                            if (controller.isSaved()) {
                                Transaction updatedTransaction = controller.getUpdatedTransaction();
                                DatabaseHelper.updateTransaction(updatedTransaction, transaction.getId());
                                loadTransactionsFromDB();
                                updateStatsPanel();
                                setupChart();
                                setupLineChart();
                            }
                            editingTransaction = null;
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            showError("Помилка відкриття діалогу: " + ex.getMessage());
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            showError("Помилка при редагуванні: " + ex.getMessage());
                        }
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
                            DatabaseHelper.deleteTransaction(transaction.getId());
                            loadTransactionsFromDB();
                            updateStatsPanel();
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


        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                    setStyle("-fx-background-color: #232b36; -fx-text-fill: #fff;");
                } else if (startDatePicker.getValue() != null && endDatePicker.getValue() != null
                        && !item.isBefore(startDatePicker.getValue()) && !item.isAfter(endDatePicker.getValue())) {
                    setText(item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    setStyle("-fx-background-color: #e3f2fd; -fx-text-fill: #232b36;");
                } else {
                    setText(item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
                    setStyle("-fx-background-color: #232b36; -fx-text-fill: #fff;");
                }
            }
        });
    }




    /*private void setupObjectMapper() {
                    objectMapper = new ObjectMapper();
                    objectMapper.registerModule(new JavaTimeModule());
                    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
                } */

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
        categoryComboBox.setEditable(false);

        addButton.setText("Додати");

        typeChoiceBox.setOnAction(e -> refreshCategories());
        categoryComboBox.setOnAction(e -> {
        String newCategory = categoryComboBox.getValue();
        if (newCategory != null && !newCategory.trim().isEmpty()) {
            addNewCategory(newCategory);
        }
        });

        transactions.addListener((ListChangeListener.Change<? extends Transaction> c) -> {
            transactionsTable.refresh();
        });

        filterTypeComboBox.setValue("Всі");
        filterTypeComboBox.setOnAction(e -> {
            updateFilterCategories();
            applyFilters();
        });

        // Категорії для фільтрації
        updateFilterCategories();
        filterCategoryComboBox.setValue("Всі категорії");
        filterCategoryComboBox.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Налаштування для періоду
        // ALWAYS SHOW period pickers by default
        startDatePicker.setVisible(true);
        endDatePicker.setVisible(true);
        // periodComboBox is always visible by default

        // Налаштування DatePicker'ів
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // Додаємо обробники подій
        startDatePicker.setOnAction(e -> updateStatsPanel());
        endDatePicker.setOnAction(e -> updateStatsPanel());

        periodComboBox.setOnAction(e -> {
            String selected = periodComboBox.getValue();
            if ("Власний період".equals(selected)) {
                startDatePicker.setVisible(true);
                endDatePicker.setVisible(true);
            } else {
                startDatePicker.setVisible(true); // always visible
                endDatePicker.setVisible(true);   // always visible
                // Можна одразу застосовувати фільтр для інших періодів
                applyPeriodFilter(selected);
            }
        });
    }

    private void updateFilterCategories() {
        String selectedType = filterTypeComboBox.getValue();
        List<String> categories = new ArrayList<>();
        categories.add("Всі категорії");
        if ("Дохід".equals(selectedType)) {
            categories.addAll(
                categoryService.getCategoriesByType("Дохід").stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList())
            );
        } else if ("Витрата".equals(selectedType)) {
            categories.addAll(
                categoryService.getCategoriesByType("Витрата").stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList())
            );
        } else { // "Всі"
            categories.addAll(
                categoryService.getCategoriesByType("Дохід").stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList())
            );
            categories.addAll(
                categoryService.getCategoriesByType("Витрата").stream()
                    .map(c -> c.getName())
                    .collect(Collectors.toList())
            );
        }
        filterCategoryComboBox.setItems(FXCollections.observableArrayList(categories));
        filterCategoryComboBox.setValue("Всі категорії");
    }

 
    /*private void loadData() {
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
    */
    
    
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

        // Оновити фільтр категорій
        List<String> allCategories = new ArrayList<>();
        allCategories.add("Всі категорії");
        allCategories.addAll(incomeCategories);
        allCategories.addAll(expenseCategories);
        filterCategoryComboBox.setItems(FXCollections.observableArrayList(allCategories));
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

            Transaction newTransaction = new Transaction(date, type, category, amount, "");

            if (editingTransaction == null) {
                DatabaseHelper.insertTransaction(newTransaction);
                setupLineChart();
            } else {
                DatabaseHelper.updateTransaction(newTransaction, editingTransaction.getId());
                setupLineChart();
                editingTransaction = null;
                addButton.setText("Додати");
            }
            if (editingTransaction == null) {
                checkBudgetStatus();
            }

            addNewCategory(category);
            clearFields();
            loadTransactionsFromDB();
            updateStatsPanel();

            if (editingTransaction == null) {
                checkBudgetStatus();
            }

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
            

                private void updateStatsPanel() {
                    LocalDate from = startDatePicker.getValue();
                    LocalDate to = endDatePicker.getValue();
                
                    // Якщо обидві дати не вибрані — показуємо статистику за весь час
                    if (from == null && to == null) {
                        from = LocalDate.of(1970, 1, 1);
                        to = LocalDate.of(2100, 12, 31);
                    } else if (from == null) {
                        from = to;
                    } else if (to == null) {
                        to = from;
                    }
                
                    // Перевірка коректності діапазону
                    if (from.isAfter(to)) {
                        showError("Початкова дата не може бути пізніше кінцевої");
                        return;
                    }
                
                    double income = DatabaseHelper.getSumByTypeAndPeriod("Дохід", from, to);
                    double expense = DatabaseHelper.getSumByTypeAndPeriod("Витрата", from, to);
                    double balance = income - expense;
                
                    incomeLabel.setText("Дохід: " + (int)income + " грн");
                    expenseLabel.setText("Витрати: " + (int)expense + " грн");
                    balanceLabel.setText("Баланс: " + (balance >= 0 ? "+" : "") + (int)balance + " грн");
                
                    // Оновлення інформації про бюджет
                    if (monthlyBudget > 0) {
                        double currentExpenses = DatabaseHelper.getSumByTypeAndPeriod("Витрата", from, to);
                        double percentage = (currentExpenses / monthlyBudget) * 100;
                        String budgetInfo = String.format("Бюджет: %.0f грн (використано: %.0f%%)", 
                            monthlyBudget, percentage);
                        budgetLabel.setText(budgetInfo);
                    }
                }

    


    public static List<Transaction> getAllTransactions() {
        List<Transaction> list = new ArrayList<>();
        String sql = "SELECT * FROM transactions";
        try (Connection conn = DatabaseHelper.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Transaction t = new Transaction(
                    LocalDate.parse(rs.getString("date")),
                    rs.getString("type"),
                    rs.getString("category"),
                    rs.getDouble("amount")
                    // додайте description, якщо потрібно
                );
                list.add(t);
            }
        } catch (SQLException e) {
            System.out.println("Select failed: " + e.getMessage());
        }
        return list;
    }

    private void loadTransactionsFromDB() {
        allTransactions.setAll(transactionService.getAllTransactions());
        filteredTransactions.setAll(allTransactions);
        setupChart();
        setupLineChart();
    }

    private void applyFilters() {
        String typeFilter = filterTypeComboBox.getValue();
        if (typeFilter == null) typeFilter = "Всі";
        String categoryFilter = filterCategoryComboBox.getValue();
        if (categoryFilter == null) categoryFilter = "Всі категорії";
        String searchText = searchField.getText() != null ? searchField.getText().toLowerCase() : "";
        
        List<Transaction> all = DatabaseHelper.getAllTransactions();
        List<Transaction> filtered = new ArrayList<>();
        
        for (Transaction t : all) {
            boolean matchesType = typeFilter.equals("Всі") || t.getType().equals(typeFilter);
            boolean matchesCategory = categoryFilter.equals("Всі категорії") || t.getCategory().equals(categoryFilter);
            boolean matchesSearch = searchText.isEmpty() ||
                    t.getCategory().toLowerCase().contains(searchText) ||
                    t.getType().toLowerCase().contains(searchText) ||
                    String.valueOf(t.getAmount()).contains(searchText);
    
            if (matchesType && matchesCategory && matchesSearch) {
                filtered.add(t);
            }
        }
        filteredTransactions.setAll(filtered);
        updateStatsPanel();
    }

    @FXML
    private void handleSetBudget() {
        try {
            String budgetText = budgetField.getText();
            if (budgetText != null && !budgetText.isEmpty()) {
                monthlyBudget = Double.parseDouble(budgetText);
                DatabaseHelper.setBudget(monthlyBudget);
                checkBudgetStatus();
                updateStatsPanel();
            }
        } catch (NumberFormatException e) {
            showError("Будь ласка, введіть коректну суму");
        }
    }

    private void checkBudgetStatus() {
        if (monthlyBudget > 0) {
            double currentExpenses = DatabaseHelper.getSumByTypeAndPeriod(
                "Витрата", 
                LocalDate.now().withDayOfMonth(1),
                LocalDate.now()
            );
            
            double percentage = (currentExpenses / monthlyBudget) * 100;
            
            if (percentage >= 80 && percentage < 100) {
                showBudgetAlert("Увага!", "Ви витратили вже " + (int)percentage + "% вашого бюджету!");
            } else if (percentage >= 100) {
                showBudgetAlert("Перевищення бюджету!", "Ви перевищили бюджет на " + (int)(percentage - 100) + "%!");
            }
        }
    }

    private void showBudgetAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    @FXML
    private void handleApplyDateRange() {
        LocalDate from = startDatePicker.getValue();
        LocalDate to = endDatePicker.getValue();
        if (from == null || to == null) {
            showError("Оберіть обидві дати!");
            return;
        }
        if (from.isAfter(to)) {
            showError("Початкова дата не може бути пізніше кінцевої!");
            return;
        }
        applyDateRangeFilter(from, to);
    }

    @FXML
    private void handleResetDateRange() {
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
        loadTransactionsFromDB(); // або applyFilters() якщо у вас є складніша фільтрація
    }

    private void applyDateRangeFilter(LocalDate from, LocalDate to) {
        List<Transaction> all = DatabaseHelper.getAllTransactions();
        List<Transaction> filtered = new ArrayList<>();
        for (Transaction t : all) {
            if ((t.getDate().isEqual(from) || t.getDate().isAfter(from)) &&
                (t.getDate().isEqual(to) || t.getDate().isBefore(to))) {
                filtered.add(t);
            }
        }
        transactions.setAll(filtered);
        updateStatsPanelWithRange(from, to);
    }

    private void updateStatsPanelWithRange(LocalDate from, LocalDate to) {
        double income = DatabaseHelper.getSumByTypeAndPeriod("Дохід", from, to);
        double expense = DatabaseHelper.getSumByTypeAndPeriod("Витрата", from, to);
        double balance = income - expense;
        incomeLabel.setText("Дохід: " + (int)income + " грн");
        expenseLabel.setText("Витрати: " + (int)expense + " грн");
        balanceLabel.setText("Баланс: " + (balance >= 0 ? "+" : "") + (int)balance + " грн");
        // ... бюджет, якщо потрібно
    }

    private void applyPeriodFilter(String period) {
        LocalDate now = LocalDate.now();
        LocalDate from = null, to = null;
        switch (period) {
            case "Сьогодні":
                from = to = now;
                break;
            case "Цей місяць":
                from = now.withDayOfMonth(1);
                to = now;
                break;
            case "Інший місяць":
                // Можна додати ще один DatePicker для вибору місяця
                break;
            case "Весь час":
                from = LocalDate.of(1970, 1, 1);
                to = LocalDate.of(2100, 12, 31);
                break;
        }
        if (from != null && to != null) {
            applyDateRangeFilter(from, to);
        }
    }

    private void refreshCategories() {
        String selectedType = typeChoiceBox.getValue();
        if ("Дохід".equals(selectedType) || "Витрата".equals(selectedType)) {
            List<String> categories = categoryService.getCategoriesByType(selectedType)
                .stream()
                .map(category -> category.getName())
                .collect(Collectors.toList());
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            
            // Оновлюємо фільтр категорій
            List<String> allCategories = new ArrayList<>();
            allCategories.add("Всі категорії");
            allCategories.addAll(
                categoryService.getCategoriesByType("Дохід").stream().map(c -> c.getName()).collect(Collectors.toList())
            );
            allCategories.addAll(
                categoryService.getCategoriesByType("Витрата").stream().map(c -> c.getName()).collect(Collectors.toList())
            );
            filterCategoryComboBox.setItems(FXCollections.observableArrayList(allCategories));
        } else {
            categoryComboBox.setItems(FXCollections.observableArrayList());
        }
    }

    private void openAddCategoryDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Додати категорію");

        VBox vbox = new VBox(14);
        vbox.setPadding(new Insets(24));
        vbox.setStyle("-fx-background-color: #232b36; -fx-background-radius: 14px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 18, 0.18, 0, 4);");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Назва категорії");
        categoryField.setStyle("-fx-background-color: #2a3441; -fx-text-fill: #fff; -fx-border-color: #3a4451; -fx-border-radius: 6px; -fx-background-radius: 6px; -fx-font-size: 15px; -fx-padding: 8px; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.07), 2, 0, 0, 1);");

        Label typeLabel = new Label("Тип");
        typeLabel.setStyle("-fx-text-fill: #fff; -fx-font-size: 14px; -fx-font-weight: 600;");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.setPromptText("Оберіть тип");
        typeBox.getItems().addAll("Дохід", "Витрата");
        typeBox.getStyleClass().add("custom-combobox");

        HBox typeBoxRow = new HBox(8, typeLabel, typeBox);
        typeBoxRow.setAlignment(Pos.CENTER_LEFT);

        Button saveBtn = new Button("Зберегти");
        Button cancelBtn = new Button("Скасувати");
        saveBtn.setDisable(true);
        saveBtn.setStyle("-fx-background-color: #00AF66; -fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 0 24px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.13), 6, 0, 0, 1); -fx-min-height: 38px; -fx-pref-height: 38px; -fx-max-height: 38px;");
        cancelBtn.setStyle("-fx-background-color: #3a4451; -fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: 600; -fx-background-radius: 8px; -fx-border-radius: 8px; -fx-padding: 0 24px; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.13), 6, 0, 0, 1); -fx-min-height: 38px; -fx-pref-height: 38px; -fx-max-height: 38px;");

        Runnable validate = () -> {
            String name = categoryField.getText().trim();
            String type = typeBox.getValue();
            saveBtn.setDisable(name.isEmpty() || type == null || type.isEmpty());
        };
        categoryField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> validate.run());

        HBox buttonBox = new HBox(10, saveBtn, cancelBtn);
        vbox.getChildren().addAll(categoryField, typeBoxRow, buttonBox);

        // Додаємо обробники ДО показу діалогу
        saveBtn.setOnAction(e -> {
            String name = categoryField.getText().trim();
            String type = typeBox.getValue();
            if (!name.isEmpty() && type != null && !type.isEmpty()) {
                categoryService.addCategory(name, type);
                refreshCategories();
                dialog.close();
            }
        });
        cancelBtn.setOnAction(e -> dialog.close());

        Scene scene = new Scene(vbox);
        scene.getStylesheets().add(getClass().getResource("/com/finance/financeapp/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    private void setupChart() {
        expensePieChart.getData().clear();

        Map<String, Double> categorySums = new HashMap<>();
        for (Transaction t : allTransactions) {
            if ("Витрата".equals(t.getType())) {
                categorySums.put(t.getCategory(),
                    categorySums.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
            }
        }
        for (Map.Entry<String, Double> entry : categorySums.entrySet()) {
            PieChart.Data slice = new PieChart.Data(entry.getKey(), entry.getValue());
            expensePieChart.getData().add(slice);
        }
        expensePieChart.setLegendVisible(true);
        expensePieChart.setLabelsVisible(true);

        // Збільшіть розмір графіка
        expensePieChart.setPrefWidth(320);
        expensePieChart.setPrefHeight(220);

        // Додаємо Tooltips для кожного сектора
        for (PieChart.Data data : expensePieChart.getData()) {
            Tooltip tooltip = new Tooltip(data.getName() + ": " + (int)data.getPieValue());
            Tooltip.install(data.getNode(), tooltip);
        }

        // Стилізуємо підписи (якщо вони з'являються)
        javafx.application.Platform.runLater(() -> {
            expensePieChart.lookupAll(".chart-pie-label").forEach(node -> {
                node.setStyle("-fx-text-fill: #fff; -fx-font-size: 15px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, #232b36, 2, 0.2, 0, 1);");
            });
            // Стилізуємо текст легенди
            expensePieChart.lookupAll(".chart-legend-item .label").forEach(node -> {
                node.setStyle("-fx-text-fill: #fff; -fx-font-size: 14px; -fx-font-weight: bold;");
            });
        });
    }

    private void setupLineChart() {
        expenseLineChart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Витрати");

        // Групуємо витрати по датах
        Map<String, Double> dateSums = new TreeMap<>();
        for (Transaction t : allTransactions) {
            if ("Витрата".equals(t.getType())) {
                String date = t.getDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                dateSums.put(date, dateSums.getOrDefault(date, 0.0) + t.getAmount());
            }
        }
        for (Map.Entry<String, Double> entry : dateSums.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }
        expenseLineChart.getData().add(series);
        expenseLineChart.setLegendVisible(false);
    }

    private void resetPieChartFilter() {
        // Example: reload all transactions and update charts
        loadTransactionsFromDB();
    }

    @FXML
    private void handleResetFilter() {
        resetPieChartFilter();
    }
}