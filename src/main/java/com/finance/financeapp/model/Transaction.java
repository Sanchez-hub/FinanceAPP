package com.finance.financeapp.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private int id;
    private LocalDate date;
    private String type;
    private String category;
    private double amount;
    private String description;

    // Порожній конструктор для Jackson
    public Transaction() {}

    // Конструктор з анотаціями Jackson
    @JsonCreator
    public Transaction(
            @JsonProperty("id") int id,
            @JsonProperty("date") LocalDate date,
            @JsonProperty("type") String type,
            @JsonProperty("category") String category,
            @JsonProperty("amount") double amount,
            @JsonProperty("description") String description) {
        this.id = id;
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    public Transaction(LocalDate date, String type, String category, double amount) {
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = ""; // або null, якщо description не потрібен
    }

    public Transaction(LocalDate date, String type, String category, double amount, String description) {
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
    }

    // Гетери і сетери з анотаціями Jackson
    @JsonProperty("id")
    public int getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(int id) {
        this.id = id;
    }

    @JsonProperty("date")
    public LocalDate getDate() {
        return date;
    }

    @JsonProperty("date")
    public void setDate(LocalDate date) {
        this.date = date;
    }

    @JsonProperty("type")
    public String getType() {
        return type;
    }

    @JsonProperty("type")
    public void setType(String type) {
        this.type = type;
    }

    @JsonProperty("category")
    public String getCategory() {
        return category;
    }

    @JsonProperty("category")
    public void setCategory(String category) {
        this.category = category;
    }

    @JsonProperty("amount")
    public double getAmount() {
        return amount;
    }

    @JsonProperty("amount")
    public void setAmount(double amount) {
        this.amount = amount;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return String.format("%s | %s | %s | %.2f грн",
            date.format(formatter),
            type,
            category,
            amount);
    }
}



