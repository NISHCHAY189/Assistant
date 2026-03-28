package com.example.khaatabook;

import java.util.Date;

public class Transaction {
    private int id;
    private int customerId;
    private String type; // "lend" or "payment"
    private String item;
    private double qty;
    private String unit;
    private int price;
    private int total;
    private long date;
    private String note;

    // For payments
    public Transaction(int id, int customerId, String type, int amount, long date, String note) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.total = amount;
        this.date = date;
        this.note = note;
    }

    // For lend (items)
    public Transaction(int id, int customerId, String type, String item, double qty, String unit, int price, int total, long date, String note) {
        this.id = id;
        this.customerId = customerId;
        this.type = type;
        this.item = item;
        this.qty = qty;
        this.unit = unit;
        this.price = price;
        this.total = total;
        this.date = date;
        this.note = note;
    }

    // Getters
    public int getId() { return id; }
    public int getCustomerId() { return customerId; }
    public String getType() { return type; }
    public String getItem() { return item; }
    public double getQty() { return qty; }
    public String getUnit() { return unit; }
    public int getPrice() { return price; }
    public int getTotal() { return total; }
    public long getDate() { return date; }
    public String getNote() { return note; }
}
