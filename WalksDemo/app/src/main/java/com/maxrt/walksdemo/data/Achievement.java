package com.maxrt.walksdemo.data;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class Achievement {
    private static final String TAG = "Achievement";

    private int id;
    private String name;
    private String description;

    public Achievement(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public Achievement(@NonNull JSONObject jsonObject) {
        try {
            this.id = Integer.parseInt(jsonObject.getString("id"));
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }

        try {
            this.name = jsonObject.getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }

        try {
            this.description = jsonObject.getString("description");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "Achievement(id: %d, name: %s, desc: %s)",
                this.id, this.name, this.description);
    }
}
