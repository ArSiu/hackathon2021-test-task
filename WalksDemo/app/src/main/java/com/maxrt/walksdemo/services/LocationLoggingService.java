package com.maxrt.walksdemo.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.maxrt.walksdemo.MainActivity;
import com.maxrt.walksdemo.R;

public class LocationLoggingService extends Service {

    public interface IOnLocationChangedListener {
        void onEvent(Location location);
    }


    private class LocationListener implements android.location.LocationListener {
        private Location lastLocation;
        private IOnLocationChangedListener listener = null;

        public LocationListener(String provider) {
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location Changed: " + location);
            lastLocation.set(location);
            if (listener != null) {
                listener.onEvent(location);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {}

        @Override
        public void onProviderDisabled(String provider) {}

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {}

        public Location getLastLocation() {
            return lastLocation;
        }

        public void setLocationChangedCallback(IOnLocationChangedListener cb) {
            listener = cb;
        }
    }


    public class LocationLoggingServiceBinder extends Binder {
        public LocationLoggingService getService() {
            return LocationLoggingService.this;
        }
    }


    private static final String TAG = "LocationLoggingService";
    private static final int NOTIFICATION_ID = 421544;
    private static final String NOTIFICATION_CHANNEL_ID = "com.maxrt.walksdemo";
    private static final String NOTIFICATION_CHANNEL_NAME = "Location Logging Service";
    private static final int LOCATION_INTERVAL = 100;
    private static final float LOCATION_DISTANCE = 10f;

    private static boolean isRunning = false;

    private Notification notification = null;
    private LocationManager locationManager = null;
    private Handler mainThreadHandler = null;
    IBinder binder = new LocationLoggingServiceBinder();
    private IOnLocationChangedListener locationUpdateListener;

    private LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "LocationLoggingService.onCreate()");

        isRunning = true;
        mainThreadHandler = new Handler(Looper.getMainLooper());
        initializeLocationService();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "LocationLoggingService.onDestroy()");
        isRunning = false;
        super.onDestroy();
        if (locationManager != null) {
            for (LocationListener locationListener : locationListeners) {
                try {
                    locationManager.removeUpdates(locationListener);
                } catch (Exception e) {
                    Log.i(TAG, "Failed to remove location listener, ignoring", e);
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void startTracking() {
        locationListeners[0] = new LocationListener(LocationManager.GPS_PROVIDER);
        locationListeners[1] = new LocationListener(LocationManager.NETWORK_PROVIDER);
        updateLocationListenersCb();

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    locationListeners[1]);
        } catch (java.lang.SecurityException e) {
            Log.e(TAG, "Failed to get permission to use network location.", e);
            makeToast("Failed to get permission to use network location.");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Network provider does not exist: " + e.getMessage());
            makeToast("Network provider does not exist: " + e.getMessage());
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_INTERVAL,
                    LOCATION_DISTANCE,
                    locationListeners[0]);
        } catch (java.lang.SecurityException e) {
            Log.e(TAG, "Failed to get permission to use gps location.", e);
            makeToast("Failed to get permission to use gps location.");
        } catch (IllegalArgumentException e) {
            Log.e(TAG, "GPS provider does not exist: " + e.getMessage());
            makeToast("GPS provider does not exist: " + e.getMessage());
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        notification =
                notificationBuilder.setOngoing(true)
                        .setContentTitle(getText(R.string.notification_title))
                        .setContentText(getText(R.string.notification_message))
                        .setSmallIcon(R.drawable.ic_home_black_24dp)
                        .setContentIntent(pendingIntent)
                        .setCategory(Notification.CATEGORY_SERVICE)
                        .build();

        startForeground(NOTIFICATION_ID, notification);
    }

    public void stopTracking() {
        stopForeground(true);
        this.onDestroy();
    }

    public void setLocationChangedCallback(IOnLocationChangedListener listener) {
        locationUpdateListener = listener;
        updateLocationListenersCb();
    }

    private void updateLocationListenersCb() {
        if (locationManager != null) {
            for (LocationListener locationListener : locationListeners) {
                locationListener.setLocationChangedCallback(locationUpdateListener);
            }
        }
    }

    private void initializeLocationService() {
        if (locationManager == null) {
            locationManager = (LocationManager)getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private void makeToast(String msg) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(
                        LocationLoggingService.this.getApplicationContext(),
                        msg,
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }
}
