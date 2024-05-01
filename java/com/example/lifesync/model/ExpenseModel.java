package com.example.lifesync.model;

public class ExpenseModel {
    String ExpId,Name,Date,userId;
    int Amount;

    public ExpenseModel() {

    }

    public ExpenseModel(String expId, String name, int amount, String date, String userId) {
        ExpId = expId;
        Name = name;
        Amount = amount;
        Date = date;
        this.userId = userId;
    }

    public String getExpId() {
        return ExpId;
    }

    public void setExpId(String expId) {
        ExpId = expId;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public int getAmount() {
        return Amount;
    }

    public void setAmount(int amount) {
        Amount = amount;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
