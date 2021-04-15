package com.maxrt.walksdemo.data;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;

import java.util.concurrent.ExecutionException;

public class Requests {
    private static final String TAG = "Requests";

    public interface IRequestReadyCallback {
        void onReady(JSONArray json);
    }

    public static class Request {
        public String url;
        public int method;
        public Context context;

        public Request(String url,
                       int method,
                       Context context) {
            this.url = url;
            this.method = method;
            this.context = context;
        }
    }

    public static JSONArray sendRequest(@NonNull Request request) {
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

    public static void sendAsyncRequest(@NonNull Request request, IRequestReadyCallback callback) {
        RequestQueue queue = Volley.newRequestQueue(request.context);

        JsonArrayRequest jsonRequest = new JsonArrayRequest(request.method, request.url, new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        callback.onReady(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {}
        });
        queue.add(jsonRequest);
    }
}
