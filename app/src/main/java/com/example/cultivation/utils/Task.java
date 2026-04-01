package com.example.cultivation.utils;

public class Task {
    public String title;
    public String description;
    public String plotName;
    public String date; // "Feb 15"
    public boolean isUrgent;

    public Task(String title, String description, String plotName, String date, boolean isUrgent) {
        this.title = title;
        this.description = description;
        this.plotName = plotName;
        this.date = date;
        this.isUrgent = isUrgent;
    }
}
