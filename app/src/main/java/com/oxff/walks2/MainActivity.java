package com.oxff.walks2;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import com.android.volley.Request;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.oxff.walks2.data.DataFetcher;
import com.oxff.walks2.data.Requests;
import com.oxff.walks2.data.User;
import com.oxff.walks2.db.CheckPoint;
import com.oxff.walks2.db.CheckPointDao;
import com.oxff.walks2.db.PointsDatabase;
import com.oxff.walks2.services.LocationLoggingService;
import com.oxff.walks2.utils.Files;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final double STEP_M = 0.78;
    private static final int PERMISSIONS_REQUEST = 0;
    private static final int GOOGLE_SIGN_IN_CODE = 10005;

    public static final String settingsFileName = "settings.json";
    public static JSONObject settings;

    public static PointsDatabase walkDb;
    public static CheckPointDao walkPointDao;

    private TextView stepsTextView;
    private TextView kcalTextView;
    private TextView kmTextView;

    private SignInButton signInBtn;
    private GoogleSignInOptions gso;
    private GoogleSignInClient signInClient;

    public LoginButton facebookLoginButton;
    public CallbackManager facebookCallbackManager;

    private double magnitudePrevious = 0;
    private int todaySteps = 0;
    private int stepCount = 0;

    public static LocationLoggingService locationLoggingService = null;
    public static ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.e(TAG, "Binder: " + service);

            LocationLoggingService.LocationLoggingServiceBinder binder =
                    (LocationLoggingService.LocationLoggingServiceBinder) service;

            locationLoggingService = binder.getService();

            locationLoggingService.setLocationChangedCallback(location -> {
                    Observable.fromCallable(() -> {
                        walkPointDao.insertAll(
                                new CheckPoint(Calendar.getInstance().getTime(), location));
                        return 0; })
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe();
                    Log.d(TAG, "Location Changed Callback: " + location);
            });
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        loadSettings(this);

//        AccessToken accessToken = AccessToken.getCurrentAccessToken();
//        boolean isFacebookLoggedIn = accessToken != null && !accessToken.isExpired();

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        boolean isGoogleLoggedIn = (account != null);

        if (!isGoogleLoggedIn) {
            signIn();
        } else {
            setViewMain();
            init();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Observable.fromCallable(() -> {
            try {
                Requests.Request request = new Requests.Request(
                        String.format(Locale.US, "%s?id=%s&steps=%d",
                                DataFetcher.ADD_STEPS,
                                settings.getString("id"), stepCount),
                        Request.Method.GET,
                        new JSONObject(),
                        this
                );
                Requests.sendObjectRequest(request);
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            return 0; })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
        saveSettings(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GOOGLE_SIGN_IN_CODE) {
            GoogleSignInResult result = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data);
            handleGoogleAuthResult(result);
        }
    }



    private void init() {
        walkDb = Room.databaseBuilder(getApplicationContext(), PointsDatabase.class, "check-points-db").build();
        walkPointDao = walkDb.checkPointDao();

        checkPermissions();

        Intent intent = new Intent(getApplicationContext(), LocationLoggingService.class);
        startService(intent);
        bindService(intent, MainActivity.serviceConnection, Context.BIND_AUTO_CREATE);

        setViewMain();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        SensorEventListener stepDetector = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if (sensorEvent != null) {
                    float xAcceleration = sensorEvent.values[0];
                    float yAcceleration = sensorEvent.values[1];
                    float zAcceleration = sensorEvent.values[2];

                    double magnitude = Math.sqrt(xAcceleration*xAcceleration + yAcceleration*yAcceleration + zAcceleration*zAcceleration);
                    double magnitudeDelta = magnitude - magnitudePrevious;
                    magnitudePrevious = magnitude;

                    if (magnitudeDelta > 6) {
                        stepCount++;
                    }
                    stepsTextView.setText(String.format(Locale.US, "%d", todaySteps + stepCount));
                    kmTextView.setText(String.format(Locale.US, "%d", stepsToMeters(todaySteps + stepCount)));
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {}
        };

        sensorManager.registerListener(stepDetector, sensor, SensorManager.SENSOR_DELAY_NORMAL);

        Observable.fromCallable(() -> {
            try {
                Requests.Request request = new Requests.Request(
                        String.format(Locale.US, "%s?user_id=%s",
                                DataFetcher.GET_STEPS, settings.getString("id")),
                        Request.Method.POST,
                        new JSONObject(),
                        this
                );
                try {
                    todaySteps = Integer.parseInt(Requests.sendObjectRequest(request).getString("suma"));
                } catch (JSONException | NullPointerException e) {
                    Log.e(TAG, "Invalid json: " + e.getMessage());
                }
            } catch (JSONException e) {
                Log.e(TAG, "Invalid json: " + e.getMessage());
            }
            return 0; })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    private void signIn() {
        setContentView(R.layout.login);
        signInBtn = findViewById(R.id.google_btn);

        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        signInClient = GoogleSignIn.getClient(this, gso);

        signInBtn.setOnClickListener(v -> {
            Intent sign = signInClient.getSignInIntent();
            startActivityForResult(sign, GOOGLE_SIGN_IN_CODE);
        });

        facebookLoginButton = findViewById (R.id.facebook_btn_v);
        facebookCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton.setReadPermissions(Arrays.asList("first_name"));

        facebookLoginButton.registerCallback(facebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                try {
                    settings.put("name", loginResult.getAccessToken().getUserId());
                    settings.put("image", "https://graph.facebook.com/" +
                        loginResult.getAccessToken().getUserId() +
                        "/picture?type=large&access_token=" +
                        loginResult.getAccessToken().getToken());
                    Profile profile = Profile.getCurrentProfile();
                    new GraphRequest(AccessToken.getCurrentAccessToken(),
                        "/"+loginResult.getAccessToken().getUserId()+"/",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                try {
                                    settings.put("name", response.getJSONObject().getString("name"));
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    ).executeAsync();

                    //Picasso.get().load(facebookImageURL).into(profile);
                    setViewMain();
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        @Override
            public void onCancel() {
                setContentView(R.layout.login);

            }

            @Override
            public void onError(FacebookException error) {
                setContentView(R.layout.login);

            }

        });
    }

    private void handleGoogleAuthResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Log.e(TAG, account.getDisplayName());

            if (account == null) {
                Log.e(TAG, "GoogleSignInAccount == null");
                return;
            }

            try {
                settings.put("name", account.getDisplayName());
                settings.put("email", account.getEmail());
            } catch (JSONException e) {
                Log.e(TAG, "Invalid json: " + e.getMessage());
            }

            if (account.getPhotoUrl() != null) {
                try {
                    settings.put("photo", account.getPhotoUrl().toString());
                    Log.e(TAG, settings.getString("photo"));
                } catch (JSONException e) {
                    Log.e(TAG, "Invalid json: " + e.getMessage());
                }
            }

            try {
                settings.put("logged_in", "true");
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }

            setViewMain();
            init();
                Observable.fromCallable(() -> {
                    try {
                        User user = new User(Integer.parseInt(settings.getString("id")), true);
                        DataFetcher.registerUser(user, getApplicationContext());
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                    return 0;
                })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe();
        } else {
            try {
                settings.put("logged_in", "false");
            } catch (JSONException e) {
                Log.e(TAG, e.getMessage());
            }
            setContentView(R.layout.login);
        }
    }

    private void addButtons() {
        ImageButton home = (ImageButton)findViewById(R.id.home_btn);
        ImageButton map = (ImageButton)findViewById(R.id.map_btn);
        ImageButton sett = (ImageButton)findViewById(R.id.settings_btn);

        home.setOnClickListener(v -> { setViewMain(); });
        map.setOnClickListener(v -> { setViewMap(); });
        sett.setOnClickListener(v -> { setViewSettings(); });
    }

    public void setViewMain() {
        setContentView(R.layout.main);
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault());
        String currentDateAndTime = sdf.format(new Date());
        TextView date = (TextView)findViewById(R.id.date);
        date.setText(currentDateAndTime);
        addButtons();

        stepsTextView = (TextView) findViewById(R.id.step_count);
        kcalTextView = (TextView) findViewById(R.id.kcal_count);
        kmTextView = (TextView) findViewById(R.id.km_count);

        View beginViewBtn = findViewById(R.id.start_);
        beginViewBtn.setOnClickListener(v -> { MainActivity.locationLoggingService.startTracking(); });

        View stopViewBtn = findViewById(R.id.stop_);
        stopViewBtn.setOnClickListener(v -> { MainActivity.locationLoggingService.stopTracking(); });
    }

    public void setViewMap() {
        setContentView(R.layout.map);
        addButtons();
    }

    public void setViewSettings(){
        setContentView(R.layout.sett);
        addButtons();

        TextView nameTextView = findViewById(R.id.stop_extView);
        try {
            nameTextView.setText(settings.getString("name"));
        } catch (JSONException e) {
            Log.e(TAG, "Invalid json: " + e.getMessage());
        }

        try {
            ImageView image = findViewById(R.id.imageView2);
            Picasso.get().load(settings.getString("image")).into(image);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public static void loadSettings(Context context) {
        if (Files.fileExists(context, settingsFileName)) {
            String text = Files.readFile(context, settingsFileName);
            try {
                settings = new JSONObject(text);
            } catch (org.json.JSONException e) {
                Log.e(TAG, "loadSettings failed: " + e.getMessage());
            }
        } else {
            try {
                settings = new JSONObject("{\"logged_in\"=\"true\"}");
            } catch (org.json.JSONException e) {
                Log.e(TAG,  "loadSettings failed: " + e.getMessage());
            }
        }
    }

    public static void saveSettings(Context context) {
        Files.writeFile(context, settingsFileName, settings.toString(), false);
    }

    protected void onPause() {
        super.onPause();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }

    protected void onStop() {
        super.onStop();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("stepCount", stepCount);
        editor.apply();
    }

    protected void onResume() {
        super.onResume();

        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        stepCount = sharedPreferences.getInt("stepCount", 0);
    }

    private void checkPermissions() {
        if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, PERMISSIONS_REQUEST);
        }
    }

    private int stepsToMeters(int steps) {
        double meters = STEP_M * steps;
        return (int) meters;
    }
}