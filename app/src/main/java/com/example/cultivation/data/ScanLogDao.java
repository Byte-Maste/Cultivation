package com.example.cultivation.data;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ScanLogDao {
    @Query("SELECT * FROM scan_logs ORDER BY id DESC")
    List<ScanLog> getAll();

    @Insert
    void insert(ScanLog log);
}
