package com.example.cultivation.data;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = { Plot.class, ScanLog.class }, version = 3, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract PlotDao plotDao();

    public abstract ScanLogDao scanLogDao();
}
