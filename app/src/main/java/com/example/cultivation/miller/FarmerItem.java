package com.example.cultivation.miller;

public class FarmerItem {
    public String uid;
    public String name;
    public String location;
    public boolean isConnected;

    public FarmerItem() {
    }

    public FarmerItem(String uid, String name, String location) {
        this.uid = uid;
        this.name = name;
        this.location = location;
        this.isConnected = false;
    }
}
