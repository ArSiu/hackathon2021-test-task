package com.oxff.walks2.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;

public class User {
    private static final String TAG = "User";

    private int id;
    private boolean isGoogle;

    public User(int id, boolean isGoogle) {
        this.id = id;
        this.isGoogle = isGoogle;
    }

    public User(JSONObject jsonObject) {
        try {
            this.id = Integer.parseInt(jsonObject.getString("id"));
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }

        try {
            this.isGoogle = jsonObject.getString("is_google").equals("1");
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }
    }

    public int getId() {
        return id;
    }

    public boolean getIsGoogle() {
        return isGoogle;
    }

    public String toJsonString() {
        return String.format("{\"user_id\":\"%s\", \"is_google\":\"%s\"}", id, isGoogle ? "1": "0");
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "User [id=%d, is_google=%b]",
                this.id, this.isGoogle);
    }
}
