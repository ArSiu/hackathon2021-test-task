package com.maxrt.walksdemo.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface CheckPointDao {
    @Query("SELECT * FROM checkpoint")
    List<CheckPoint> getAll();

    @Query("SELECT * FROM checkpoint WHERE uid IN (:uids)")
    List<CheckPoint> getByIds(int[] uids);

    // @Query("SELECT * FROM walkpoint WHERE timestamp ")

    @Insert
    void insertAll(CheckPoint... walkPoints);

    @Delete
    void delete(CheckPoint walkPoint);
}
