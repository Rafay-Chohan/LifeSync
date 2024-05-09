package com.example.lifesync.model;

public class ExpenseModel {
    String ExpId,Name,Date,userId, Category;
    int Amount, ExpPriority;

    public ExpenseModel() {

    }

    public ExpenseModel(String expId, String name, String date, String userId, String category, int amount, int expPriority) {
        ExpId = expId;
        Name = name;
        Date = date;
        this.userId = userId;
        Category = category;
        Amount = amount;
        ExpPriority = expPriority;
    }

    public String getCategory() {
        return Category;
    }

    public void setCategory(String category) {
        Category = category;
    }

    public int getExpPriority() {
        return ExpPriority;
    }

    public void setExpPriority(int expPriority) {
        ExpPriority = expPriority;
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
