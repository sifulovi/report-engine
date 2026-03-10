package com.jasperframework.example.model;

/**
 * Represents a line item on an invoice.
 */
public class InvoiceItem {

    private final String item;
    private final int quantity;
    private final double unitPrice;

    public InvoiceItem(String item, int quantity, double unitPrice) {
        this.item = item;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public String getItem() { return item; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getLineTotal() { return quantity * unitPrice; }
}
