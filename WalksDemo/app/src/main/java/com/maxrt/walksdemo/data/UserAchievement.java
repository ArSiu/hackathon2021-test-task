package com.maxrt.walksdemo.data;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UserAchievement {
    private static final String TAG = "UserAchievement";

    private int uid;
    private int achievementId;
    private Date time;

    public UserAchievement(int uid, int achievementId) {
        this.uid = uid;
        this.achievementId = achievementId;
    }

    public UserAchievement(int id, JSONObject jsonObject) {
        this.uid = id;
        try {
            this.achievementId = Integer.parseInt(jsonObject.getString("achievements_id"));
            this.time = new SimpleDateFormat("yyyy-MM-dd  HH:mm:ss", Locale.US).parse(jsonObject.getString("text"));
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        } catch (ParseException e) {
            Log.e(TAG, "Invalid date: " + e.getMessage());
        }
    }

    public int getUid() {
        return uid;
    }

    public int getAchievementId() {
        return achievementId;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "UserAchievement(uid: %d, aid: %d, time: %s)",
                this.uid, this.achievementId, this.time);
    }
}
