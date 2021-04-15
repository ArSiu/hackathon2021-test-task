package com.maxrt.walksdemo.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {CheckPoint.class}, version = 1)
@TypeConverters({Converters.class})
public abstract class PointsDatabase extends RoomDatabase {
    public abstract CheckPointDao checkPointDao();
}
