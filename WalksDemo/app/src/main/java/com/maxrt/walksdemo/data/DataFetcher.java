package com.maxrt.walksdemo.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class DataFetcher {
    private static final String TAG = "DataFetcher";

    public static final String API_URL = "https://walks.ml";
    public static final String GET_ACHIEVEMENTS_URL = API_URL + "/get_achievements.php";
    public static final String GET_TOP_URL = API_URL + "/get_top.php";

    public static List<UserAchievement> getUserAchievements(Context context, int id) {
        ArrayList<UserAchievement> achievements = new ArrayList<>();

        Requests.Request request = new Requests.Request(
                GET_ACHIEVEMENTS_URL + "?id=" + id,
                Request.Method.GET,
                context);

        JSONArray jsonArray = Requests.sendRequest(request);

        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                achievements.add(new UserAchievement(id, jsonArray.getJSONObject(i)));
            } catch (JSONException e) {
                Log.e(TAG, "Invalid json: " + e.getMessage());
            }
        }

        return achievements;
    }

    public static List<User> getTopUsers(Context context) {
        ArrayList<User> topUsers = new ArrayList<>();

        Requests.Request request = new Requests.Request(
                GET_TOP_URL,
                Request.Method.GET,
                context);

        JSONArray jsonResponse = Requests.sendRequest(request);

        for (int i = 0; i < jsonResponse.length(); i++) {
            try {
                topUsers.add(new User(jsonResponse.getJSONObject(i)));
            } catch (JSONException e) {
                Log.e(TAG, "Invalid json: " + e.getMessage());
            }
        }

        return topUsers;
    }
}
