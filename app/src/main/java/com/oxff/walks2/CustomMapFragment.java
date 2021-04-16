package com.oxff.walks2;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.oxff.walks2.db.CheckPoint;

import java.util.ArrayList;
import java.util.List;

import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class CustomMapFragment extends Fragment {

    private GoogleMap map;

    private final LatLng PARKING = new LatLng(49.8222141,23.9840754);

    private Marker markerParking;

    private OnMapReadyCallback mapCallback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(GoogleMap googleMap) {
            PolylineOptions polylineOptions = new PolylineOptions().clickable(true);
            for (LatLng point : setArrayOfCoordinatesForMap(MainActivity.walkPointDao.getAll())) {
                polylineOptions.add(point);
            }
            Polyline polyline1 = googleMap.addPolyline(polylineOptions);
            googleMap.setOnInfoWindowClickListener(windowClickListener);
            map = googleMap;
        }
    };

    private GoogleMap.OnInfoWindowClickListener windowClickListener = new GoogleMap.OnInfoWindowClickListener() {
        @Override
        public void onInfoWindowClick(Marker marker) {
            Toast.makeText(getContext(), "Info window clicked", Toast.LENGTH_SHORT).show();
        }
    };

    public CustomMapFragment() {}

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.map, container, false);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(mapCallback);
        }
    }

    public List<LatLng> setArrayOfCoordinatesForMap(List<CheckPoint> coordinates){
        List<LatLng> google = new ArrayList<LatLng>();
        for (CheckPoint checkpoint:coordinates) {
            google.add(new LatLng(checkpoint.latitude,checkpoint.longitude));
        }
        return google;
    }
}