package com.oxff.walks2.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

public class Requests {
    private static final String TAG = "Requests";

    public interface IRequestReadyCallback {
        void onReady(JSONArray json);
    }

    public static class Request {
        public String url;
        public int method;
        public JSONObject content;
        public Context context;

        public Request(String url,
                       int method,
                       Context context) {
            this(url, method, null, context);
        }

        public Request(String url,
                       int method,
                       JSONObject content,
                       Context context) {
            this.url = url;
            this.method = method;
            this.content = content;
            this.context = context;
        }
    }

    public static JSONArray sendArrayRequest(@NonNull Request request) {
        RequestQueue queue = Volley.newRequestQueue(request.context);

        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        JsonArrayRequest jsonRequest = new JsonArrayRequest(request.method, request.url, new JSONArray(), future, future);
        queue.add(jsonRequest);

        try {
            JSONArray response = future.get();
            Log.e(TAG, "Response: " + response);
            return response;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Request failed to execute: " + e.getMessage());
        }
        return null;
    }

    public static JSONObject sendObjectRequest(@NonNull Request request) {
        RequestQueue queue = Volley.newRequestQueue(request.context);

        RequestFuture<JSONObject> future = RequestFuture.newFuture();
        JsonObjectRequest jsonRequest = new JsonObjectRequest(request.method, request.url, request.content, future, future);
        queue.add(jsonRequest);
        try {
            JSONObject response = future.get();
            Log.e(TAG, "Response: " + response);
            return response;
        } catch (InterruptedException | ExecutionException e) {
            Log.e(TAG, "Request failed to execute: " + e.getMessage());
        }

        return null;
    }
}
