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
import javafx.geometry.Insets;

import javafx.util.Callback;
import javafx.util.StringConverter;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.sql.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.scene.layout.VBox;
import java.util.stream.Collectors;


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

    public MainViewController() {
        this.databaseHelper = new DatabaseHelper();
    }

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
                        DatabaseHelper.deleteTransaction(transaction.getId());
                        loadTransactionsFromDB();
                        updateStatsPanel();
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
        /*setupObjectMapper();*/
        setupTable();
        loadTransactionsFromDB();
        setupControls();
        
        // Ініціалізуємо дефолтні категорії
        CategoryManager.initializeDefaultCategories();

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
            
        });

       
    

        monthlyBudget = DatabaseHelper.getBudget();
        if (monthlyBudget > 0) {
            budgetField.setText(String.valueOf(monthlyBudget));
        }

        refreshCategories();

        addCategoryButton.setOnAction(e -> openAddCategoryDialog());

        
        setupChart();
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
                        try {
                            // Додаємо логування тут
                            System.out.println("Transaction for edit: " + transaction);
                            List<String> allCategories = new ArrayList<>();
                            allCategories.addAll(incomeCategories);
                            allCategories.addAll(expenseCategories);
                            System.out.println("Categories: " + allCategories);
                            
                            System.out.println("setTransaction called with: " + transaction);
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("edit-transaction-dialog.fxml"));
                            Parent root = loader.load();
                            EditTransactionDialogController controller = loader.getController();
                            controller.setTransaction(transaction, new ArrayList<>(incomeCategories), new ArrayList<>(expenseCategories));

                            Stage dialogStage = new Stage();
                            dialogStage.initModality(Modality.APPLICATION_MODAL);
                            dialogStage.setTitle("Редагувати запис");
                            dialogStage.setScene(new Scene(root));
                            dialogStage.showAndWait();

                            if (controller.isSaved()) {
                                DatabaseHelper.updateTransaction(transaction, transaction.getId());
                                loadTransactionsFromDB();
                                updateStatsPanel();
                            }
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            showError("Помилка відкриття діалогу: " + ex.getMessage());
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
        transactionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);

        dateColumn.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (!empty && item != null && startDatePicker.getValue() != null && endDatePicker.getValue() != null) {
                    if (!item.isBefore(startDatePicker.getValue()) && !item.isAfter(endDatePicker.getValue())) {
                        setStyle("-fx-background-color: #e3f2fd;"); // світло-блакитний
                    } else {
                        setStyle("");
                    }
                } else {
                    setStyle("");
                }
                setText(empty || item == null ? "" : item.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
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
        categoryComboBox.setEditable(true);

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
        filterTypeComboBox.setOnAction(e -> applyFilters());

        // Категорії для фільтрації
        List<String> allCategories = new ArrayList<>();
        allCategories.add("Всі категорії");
        allCategories.addAll(incomeCategories);
        allCategories.addAll(expenseCategories);
        filterCategoryComboBox.setItems(FXCollections.observableArrayList(allCategories));
        filterCategoryComboBox.setValue("Всі категорії");
        filterCategoryComboBox.setOnAction(e -> applyFilters());

        searchField.textProperty().addListener((obs, oldVal, newVal) -> applyFilters());

        // Налаштування для періоду
        

        // Налаштування DatePicker'ів
        startDatePicker.setValue(LocalDate.now().minusMonths(1));
        endDatePicker.setValue(LocalDate.now());

        // Приховуємо DatePicker'и спочатку
        startDatePicker.setVisible(false);
        endDatePicker.setVisible(false);

        // Додаємо обробники подій
        

        startDatePicker.setOnAction(e -> updateStatsPanel());
        endDatePicker.setOnAction(e -> updateStatsPanel());

        periodComboBox.setOnAction(e -> {
            String selected = periodComboBox.getValue();
            if ("Власний період".equals(selected)) {
                startDatePicker.setVisible(true);
                endDatePicker.setVisible(true);
            } else {
                startDatePicker.setVisible(false);
                endDatePicker.setVisible(false);
                // Можна одразу застосовувати фільтр для інших періодів
                applyPeriodFilter(selected);
            }
        });
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

            if (editingTransaction != null) {
                DatabaseHelper.updateTransaction(newTransaction, editingTransaction.getId());
                editingTransaction = null;
                addButton.setText("Додати");
            } else {
                DatabaseHelper.insertTransaction(newTransaction);
            }
            if (editingTransaction == null) { // тільки для нових транзакцій
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
        transactions.setAll(DatabaseHelper.getAllTransactions());
        applyFilters();
        updateStatsPanel();
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
        transactions.setAll(filtered);
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
            List<String> categories = CategoryManager.loadCategories(selectedType);
            categoryComboBox.setItems(FXCollections.observableArrayList(categories));
            
            // Оновлюємо фільтр категорій
            List<String> allCategories = new ArrayList<>();
            allCategories.add("Всі категорії");
            allCategories.addAll(CategoryManager.loadCategories("Дохід"));
            allCategories.addAll(CategoryManager.loadCategories("Витрата"));
            filterCategoryComboBox.setItems(FXCollections.observableArrayList(allCategories));
        } else {
            categoryComboBox.setItems(FXCollections.observableArrayList());
        }
    }

    private void openAddCategoryDialog() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("Додати категорію");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(20));

        TextField categoryField = new TextField();
        categoryField.setPromptText("Назва категорії");

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.setPromptText("Оберіть тип");
        typeBox.getItems().addAll("Дохід", "Витрата");

        Button saveBtn = new Button("Зберегти");
        Button cancelBtn = new Button("Скасувати");
        saveBtn.setDisable(true);

        // Валідація для активації кнопки "Зберегти"
        Runnable validate = () -> {
            String name = categoryField.getText().trim();
            String type = typeBox.getValue();
            saveBtn.setDisable(name.isEmpty() || type == null || type.isEmpty());
        };
        categoryField.textProperty().addListener((obs, oldVal, newVal) -> validate.run());
        typeBox.valueProperty().addListener((obs, oldVal, newVal) -> validate.run());

        // Ось тут — обробник кнопки "Зберегти"
        saveBtn.setOnAction(ev -> {
            String name = categoryField.getText().trim();
            String type = typeBox.getValue();

            if (name.isEmpty() || type == null || type.isEmpty()) {
                showError("Введіть назву та оберіть тип!");
                return;
            }

            // Перевірка на дублікати
            List<String> existing = CategoryManager.loadCategories(type);
            if (existing.contains(name)) {
                showError("Категорія з такою назвою вже існує для цього типу!");
                return;
            }

            boolean success = CategoryManager.insertCategory(name, type);
            if (success) {
                refreshCategories();
                categoryComboBox.setValue(name);
                dialog.close();
            } else {
                showError("Категорія вже існує!");
            }
        });

        cancelBtn.setOnAction(ev -> dialog.close());

        HBox btnBox = new HBox(10, saveBtn, cancelBtn);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        vbox.getChildren().addAll(
            new Label("Нова категорія:"),
            categoryField,
            new Label("Тип:"),
            typeBox,
            btnBox
        );

        dialog.setScene(new Scene(vbox));
        dialog.showAndWait();
    }

    private void setupChart() {
        Map<String, Double> categoryTotals = new HashMap<>();
        
        // Збираємо суми по категоріях
        for (Transaction transaction : databaseHelper.getAllTransactions()) {
            if ("Витрата".equals(transaction.getType())) {
                String category = transaction.getCategory();
                double amount = transaction.getAmount();
                categoryTotals.merge(category, amount, Double::sum);
            }
        }

    
       
        
        
        
        
    }

    private void filterByCategory(String category) {
        currentCategoryFilter = category;
        List<Transaction> filteredTransactions = databaseHelper.getAllTransactions().stream()
            .filter(t -> t.getCategory().equals(category))
            .collect(Collectors.toList());
        transactionsTable.setItems(FXCollections.observableArrayList(filteredTransactions));
    }

    @FXML
    private void handleResetFilter() {
        currentCategoryFilter = null;
        loadTransactionsFromDB(); // Завантажуємо всі транзакції
    }
}

