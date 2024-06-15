package com.example.lifesync.model;

public class Version {
    String app_version,db_version;
    public Version(){

    }
    public Version(String app_version, String db_version) {
        this.app_version = app_version;
        this.db_version = db_version;
    }

    public String getApp_version() {
        return app_version;
    }

    public void setApp_version(String app_version) {
        this.app_version = app_version;
    }

    public String getDb_version() {
        return db_version;
    }

    public void setDb_version(String db_version) {
        this.db_version = db_version;
    }
}
