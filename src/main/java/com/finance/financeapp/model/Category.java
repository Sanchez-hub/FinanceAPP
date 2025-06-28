package com.finance.financeapp.model;

public class Category {
    private int id;
    private String name;
    private String type;

    public Category(int id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public Category(String name, String type) {
        this(-1, name, type);
    }

    // Геттери та сеттери
    public int getId() { return id; }
    public String getName() { return name; }
    public String getType() { return type; }
    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setType(String type) { this.type = type; }
}
