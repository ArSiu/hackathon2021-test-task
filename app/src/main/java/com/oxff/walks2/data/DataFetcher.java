package com.oxff.walks2.data;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;
import com.oxff.walks2.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

public class DataFetcher {
    private static final String TAG = "DataFetcher";

    public static final String API_URL = "https://xdfh48fg4h58f4gh5.000webhostapp.com";
    public static final String ADD_USER = API_URL + "/add_user.php";
    public static final String ADD_STEPS = API_URL + "/add_steps.php";
    public static final String GET_STEPS = API_URL + "/get_steps.php";

    public static String registerUser(User user, Context context) {
        Requests.Request request = new Requests.Request(
                ADD_USER + "?id=" + user.getId() + "&isgoogle=" + (user.getIsGoogle() ? "1" : "0"),
                Request.Method.POST,
                new JSONObject(),
                context);

        JSONObject json =  Requests.sendObjectRequest(request);

        try {
            return json.getString("id");
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        return null;
    }
}
