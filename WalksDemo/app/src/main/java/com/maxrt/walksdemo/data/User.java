package com.maxrt.walksdemo.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class User {
    private static final String TAG = "User";

    private int uid;
    private String name;
    private String imageUrl;
    private int stepsNumber;

    public User(int uid, String name, String imageUrl, int stepsNumber) {
        this.uid = uid;
        this.name = name;
        this.imageUrl = imageUrl;
        this.stepsNumber = stepsNumber;
    }

    public User(JSONObject jsonObject) {
        try {
            this.uid = Integer.parseInt(jsonObject.getString("uid"));
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }

        try {
            this.name = jsonObject.getString("name");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }

        try {
            this.imageUrl = jsonObject.getString("image");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }

        try {
            this.stepsNumber = Integer.parseInt(jsonObject.getString("steps_number"));
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }
    }

    public int getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getStepsNumber() {
        return stepsNumber;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "User(id: %d, name: %s, image: %s, steps: %d)",
                this.uid, this.name, this.imageUrl, this.stepsNumber);
    }
}
