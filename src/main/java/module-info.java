module com.finance.financeapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jsr310;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.ikonli.fontawesome5;
    requires com.google.gson;
    requires javafx.media;

    opens com.finance.financeapp to javafx.fxml;
    opens com.finance.financeapp.controller to javafx.fxml;
    exports com.finance.financeapp;
    exports com.finance.financeapp.controller;
    exports com.finance.financeapp.model;
    exports com.finance.financeapp.service;

}