package com.maxrt.walksdemo.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.maxrt.walksdemo.MainActivity;
import com.maxrt.walksdemo.R;
import com.maxrt.walksdemo.db.CheckPoint;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        final Button start_button = root.findViewById(R.id.button_start_tracking);
        start_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                MainActivity.locationLoggingService.startTracking();
            }
        });

        final Button stop_button = root.findViewById(R.id.button_stop_tracking);
        stop_button.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                MainActivity.locationLoggingService.stopTracking();
            }
        });

        final Button show_last_button = root.findViewById(R.id.button_show_last_location);
        show_last_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final TextView text = root.findViewById(R.id.last_location);
                // DB operations cannot be performed on the ui thread, because they potentially can block the thread
                Observable.fromCallable(() -> {
                    final List<CheckPoint> points = MainActivity.walkPointDao.getAll();
                    // Only the thread that created the view, can modify it
                    getActivity().runOnUiThread(() -> {
                        if (points.size() > 0) {
                            text.setText(points.get(points.size() - 1).toString());
                        } else {
                            text.setText("No location stored");
                        }
                    });
                    return 0;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe();
            }
        });

        return root;
    }
}