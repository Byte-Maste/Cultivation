package com.example.cultivation.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "scan_logs")
public class ScanLog {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String disease;
    public float confidence;
    public String date;
    public String imagePath; // Path to saved image

    public ScanLog(String disease, float confidence, String date, String imagePath) {
        this.disease = disease;
        this.confidence = confidence;
        this.date = date;
        this.imagePath = imagePath;
    }
}
