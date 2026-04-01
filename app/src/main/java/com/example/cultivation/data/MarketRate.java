package com.example.cultivation.data;

public class MarketRate {
    public String location;
    public String price;
    public String trend; // "up", "down", "stable"

    public MarketRate() {
    } // Required for Firestore

    public MarketRate(String location, String price, String trend) {
        this.location = location;
        this.price = price;
        this.trend = trend;
    }
}
