package com.oxff.walks2.db;

import android.location.Location;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity
public class CheckPoint {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "timestamp")
    public Date timestamp;

    @ColumnInfo(name = "lattitude")
    public double latitude;

    @ColumnInfo(name = "longtitube")
    public double longitude;

    public CheckPoint(Date timestamp, double latitude, double longitude) {
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public CheckPoint(Date timestamp, Location location) {
        this.timestamp = timestamp;
        this.latitude = location.getLatitude();
        this.longitude = location.getLongitude();
    }

    @Override
    public String toString() {
        return String.format("CheckPoint(date: %s, lat: %s, lon: %s", this.timestamp, this.latitude, this.longitude);
    }
}

