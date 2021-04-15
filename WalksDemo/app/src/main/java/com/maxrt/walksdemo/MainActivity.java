package com.maxrt.walksdemo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.maxrt.walksdemo.db.CheckPoint;
import com.maxrt.walksdemo.db.CheckPointDao;
import com.maxrt.walksdemo.db.PointsDatabase;
import com.maxrt.walksdemo.services.LocationLoggingService;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.room.Room;

import java.util.Calendar;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int PERMISSIONS_REQUEST = 0;

    public static PointsDatabase walkDb;
    public static CheckPointDao walkPointDao;

    public static LocationLoggingService locationLoggingService = null;
    public static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "Binder: " + service);

            LocationLoggingService.LocationLoggingServiceBinder binder =
                    (LocationLoggingService.LocationLoggingServiceBinder) service;

            locationLoggingService = binder.getService();

            locationLoggingService.setLocationChangedCallback(
                    new LocationLoggingService.IOnLocationChangedListener() {
                        @Override
                        public void onEvent(Location location) {

                            Observable.fromCallable(() -> {
                                walkPointDao.insertAll(
                                        new CheckPoint(Calendar.getInstance().getTime(), location));
                                return 0; })
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe();
                            Log.d(TAG, "Location Changed Callback: " + location);
                        }
                    });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_map, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        walkDb = Room.databaseBuilder(getApplicationContext(), PointsDatabase.class, "check-points-db").build();
        walkPointDao = walkDb.checkPointDao();

        checkPermissions();

        Intent intent = new Intent(getApplicationContext(), LocationLoggingService.class);
        startService(intent);
        bindService(intent, MainActivity.serviceConnection, Context.BIND_AUTO_CREATE);
    }

    public void checkPermissions() {
        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
         && getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERMISSIONS_REQUEST);
        }
    }
}