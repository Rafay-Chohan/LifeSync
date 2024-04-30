package com.example.lifesync;

public class LogModel {
    String logID,Name,Data,Date,userId;
    public LogModel(){

    }
    public LogModel(String logID, String name, String data, String date,String userId) {
        this.logID = logID;
        Name = name;
        Data = data;
        Date = date;
        this.userId=userId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getLogID() {
        return logID;
    }

    public void setLogID(String logID) {
        this.logID = logID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getData() {
        return Data;
    }

    public void setData(String data) {
        Data = data;
    }
}
