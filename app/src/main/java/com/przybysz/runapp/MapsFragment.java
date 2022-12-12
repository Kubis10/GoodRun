package com.przybysz.runapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Locale;

public class MapsFragment extends Fragment implements OnMapReadyCallback{
    GoogleMap mMap;
    private boolean running;
    private int seconds = 0;
    private boolean wasRunning;
    private TextView timeView;
    private TextView distanceView;
    private TextView stepsView;
    ArrayList<LatLng> points = new ArrayList<LatLng>();
    double totalDistance = 0;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    private void findViews(View v) {
        timeView = v.findViewById(R.id.txtTime);
        distanceView = v.findViewById(R.id.txtDistance);
        stepsView = v.findViewById(R.id.txtPace);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews(view);
        if (savedInstanceState != null) {
            seconds
                    = savedInstanceState
                    .getInt("seconds");
            running
                    = savedInstanceState
                    .getBoolean("running");
            wasRunning
                    = savedInstanceState
                    .getBoolean("wasRunning");
        }
        runTimer();
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
        Button startStop = view.findViewById(R.id.btnStartStop);
        startStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(startStop.getText().equals("Start")){
                    startStop.setText("Stop");
                    seconds = 0;
                    running = true;
                    mMap.clear();
                }else {
                    startStop.setText("Start");
                    running = false;
                }
            }
        });
    }
    private void runTimer()
    {

        // Creates a new Handler
        final Handler handler
                = new Handler();

        handler.post(new Runnable() {
            @Override

            public void run()
            {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;

                String time
                        = String
                        .format(Locale.getDefault(),
                                "%d:%02d:%02d", hours,
                                minutes, secs);

                timeView.setText(time);

                if (running) {
                    seconds++;
                }

                handler.postDelayed(this, 1000);
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState)
    {
        savedInstanceState
                .putInt("seconds", seconds);
        savedInstanceState
                .putBoolean("running", running);
        savedInstanceState
                .putBoolean("wasRunning", wasRunning);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        wasRunning = running;
        running = false;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (wasRunning) {
            running = true;
        }
    }

    public String getTotalDistance(ArrayList<LatLng> points) {
        totalDistance = 0;

        Location start = new Location("start");
        start.setLatitude(points.get(0).latitude);
        start.setLongitude(points.get(0).longitude);

        for (int i = 1; i < points.size(); i++) {
            Location end = new Location("end");
            end.setLatitude(points.get(i).latitude);
            end.setLongitude(points.get(i).longitude);
            float distance = start.distanceTo(end);
            totalDistance += distance;
            start = end;
        }

        long rounded = (long) (totalDistance * 100);
        double result = rounded / 100.0;
        if(result < 1000){
            return result + " m";
        } else{
            rounded = (long) (result/1000 * 100);
            result = rounded / 100.0;
            return result + " km";
        }
    }

    public String getSpeed(double distance, int seconds){
        double speed = distance/seconds;
        long rounded = (long) (speed * 100);
        double result = rounded / 100.0;
        return result + " m/s";
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        mMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        } else {
            mMap.setMyLocationEnabled(true);
            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
            Criteria criteria = new Criteria();
            String provider = locationManager.getBestProvider(criteria, true);
            Location location = locationManager.getLastKnownLocation(provider);


            if (location != null) {
                LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            }
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location location) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    if(running){
                        points.add(userLocation);
                        Polyline line = mMap.addPolyline(new PolylineOptions()
                                .addAll(points)
                                .width(5)
                                .color(Color.RED));
                        distanceView.setText(getTotalDistance(points));
                        stepsView.setText(getSpeed(totalDistance, seconds));
                    }
                    else {
                        points.clear();
                    }
                    moveCamera(mMap, userLocation, 15);
                }
            });

        }
    }
    public void moveCamera(GoogleMap map, LatLng latLng, float zoomLevel) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoomLevel)
                .build();

        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        map.animateCamera(cameraUpdate);
    }
}