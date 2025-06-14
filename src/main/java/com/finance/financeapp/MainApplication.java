package com.finance.financeapp;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        try {
            // Ініціалізуємо базу даних
            DatabaseHelper.initializeDatabase();
            System.out.println("Database initialized successfully");
            
            // Залишок коду без змін...
            URL fxmlUrl = getClass().getResource("/com/finance/financeapp/main-view.fxml");
            if (fxmlUrl == null) {
                System.err.println("Cannot find main-view.fxml");
                System.err.println("Current directory: " + System.getProperty("user.dir"));
                throw new RuntimeException("Cannot find main-view.fxml");
            }
            System.out.println("Found FXML at: " + fxmlUrl);
            
            FXMLLoader fxmlLoader = new FXMLLoader(fxmlUrl);
            Scene scene = new Scene(fxmlLoader.load());
            stage.setTitle("Finance App");
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Error during application start: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public static void main(String[] args) {
        launch();
    }
}

