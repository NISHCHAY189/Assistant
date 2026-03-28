package com.example.khaatabook;

import java.io.Serializable;

public class Customer implements Serializable {
    private int id;
    private String name;
    private String phone;
    private int outstanding;

    public Customer(int id, String name, String phone, int outstanding) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.outstanding = outstanding;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public int getOutstanding() { return outstanding; }
    
    public void setOutstanding(int outstanding) {
        this.outstanding = outstanding;
    }
    
    public void updateBalance(int amount, boolean isLend) {
        if (isLend) {
            this.outstanding += amount;
        } else {
            this.outstanding -= amount;
        }
    }
}
