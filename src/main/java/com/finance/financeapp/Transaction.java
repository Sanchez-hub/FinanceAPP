package com.finance.financeapp;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Transaction {
    private LocalDate date;
    private String type;
    private String category;
    private double amount;

    // Порожній конструктор для Jackson
    public Transaction() {}

    // Конструктор з анотаціями Jackson
    @JsonCreator
    public Transaction(
            @JsonProperty("date") LocalDate date,
            @JsonProperty("type") String type,
            @JsonProperty("category") String category,
            @JsonProperty("amount") double amount) {
        this.date = date;
        this.type = type;
        this.category = category;
        this.amount = amount;
    }

    // Гетери і сетери з анотаціями Jackson
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



