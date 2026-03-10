package com.jasperframework.example.model;

/**
 * Represents a product in the catalogue.
 */
public class Product {

    private final String name;
    private final String sku;
    private final String category;
    private final double price;
    private final int stock;

    public Product(String name, String sku, String category, double price, int stock) {
        this.name = name;
        this.sku = sku;
        this.category = category;
        this.price = price;
        this.stock = stock;
    }

    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
}
