package com.example.stock;

public class DataHolder {
    private static DataHolder instance = new DataHolder();
    private String sharedVariable;

    private DataHolder() {}  // Private constructor to ensure singleton

    public static DataHolder getInstance() {
        return instance;
    }

    public String getSharedVariable() {
        return sharedVariable;
    }

    public void setSharedVariable(String sharedVariable) {
        this.sharedVariable = sharedVariable;
    }
}
