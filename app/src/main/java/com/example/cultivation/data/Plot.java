package com.example.cultivation.data;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "plots")
public class Plot {
    @PrimaryKey(autoGenerate = true)
    public int id;

    public String name;
    public String area; // e.g., "2.5 Acres"
    public String cropVariety;
    public String plantingDate;
    public int healthScore;

    public Plot(String name, String area, String cropVariety, String plantingDate) {
        this.name = name;
        this.area = area;
        this.cropVariety = cropVariety;
        this.plantingDate = plantingDate;
        this.healthScore = (int) (Math.random() * 20) + 80; // Random 80-99 for demo
    }
}
