package com.finance.financeapp;

import com.finance.financeapp.database.DatabaseHelper;
import com.finance.financeapp.model.Category;
import com.finance.financeapp.service.CategoryService;
import com.finance.financeapp.service.TransactionService;
import com.finance.financeapp.dao.CategoryDAO;
import com.finance.financeapp.dao.TransactionDAO;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;
import com.finance.financeapp.controller.MainViewController;
import javafx.scene.control.TableView;

public class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        DatabaseHelper.initializeDatabase();

        DatabaseHelper dbHelper = new DatabaseHelper();
        TransactionService transactionService = new TransactionService(new TransactionDAO(dbHelper));
        CategoryService categoryService = new CategoryService(new CategoryDAO());

        categoryService.initializeDefaultCategories();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/finance/financeapp/main-view.fxml"));
        loader.setControllerFactory(param -> {
            if (param == MainViewController.class) {
                return new MainViewController(transactionService, categoryService);
            }
            // Add other controllers as needed
            try {
                return param.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        Scene scene = new Scene(loader.load());
        scene.getStylesheets().add(getClass().getResource("/com/finance/financeapp/styles.css").toExternalForm());
        primaryStage.setScene(scene);
        // primaryStage.setResizable(false);
        primaryStage.setMinHeight(900);
        primaryStage.setMinWidth(1200);
        primaryStage.setTitle("Finance App");
        primaryStage.setMaximized(true);
        primaryStage.getIcons().add(
            new javafx.scene.image.Image(getClass().getResourceAsStream("/com/finance/financeapp/icons/main-icon.png"))
        );
        primaryStage.show();

        //transactionsTable.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    public static void main(String[] args) {
        launch(args);
    }
}