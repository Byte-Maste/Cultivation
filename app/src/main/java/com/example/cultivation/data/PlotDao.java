package com.example.cultivation.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlotDao {
    @Query("SELECT * FROM plots")
    List<Plot> getAll();

    @Insert
    void insert(Plot plot);

    @Delete
    void delete(Plot plot);

    @Query("SELECT COUNT(*) FROM plots")
    int getCount();

    @Query("SELECT * FROM plots ORDER BY healthScore DESC LIMIT 1")
    Plot getBestPlot();
}
