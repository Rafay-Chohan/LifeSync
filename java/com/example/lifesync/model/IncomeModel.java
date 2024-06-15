package com.example.lifesync.model;

public class IncomeModel {
    String userId,docId;
    int income;
    public IncomeModel(){

    }
    public IncomeModel(String userId, String docId, int income) {
        this.userId = userId;
        this.docId = docId;
        this.income = income;
    }

    public String getDocId() {
        return docId;
    }

    public void setDocId(String docId) {
        this.docId = docId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getIncome() {
        return income;
    }

    public void setIncome(int income) {
        this.income = income;
    }
}
