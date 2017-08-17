package com.example.android.worldquest.ui;

import com.google.firebase.auth.FirebaseAuth;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import android.Manifest;
import com.example.android.worldquest.R;
import com.example.android.worldquest.ui.MapBoxUtilityClasses.PulseMarkerView;
import com.example.android.worldquest.ui.MapBoxUtilityClasses.PulseMarkerViewOptions;
import com.example.android.worldquest.utils.location.ProviderLocationTracker;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.BuildConfig;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.ResultCodes;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.BaseMarkerViewOptions;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.MarkerViewManager;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationSource;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsListener;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;

/**
 * Main screen showing the map and collectors page
 */


public class MainActivity extends AppCompatActivity implements PermissionsListener{

    private MapView mapView;
    private MapboxMap map;
    private MarkerView userMarker;
    private LocationEngine locationEngine;
    private LocationEngineListener locationListener;
    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Mapbox access token is configured here. This needs to be called either in your application
        // object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));

        // This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_main);

        // Get the location engine object for later use.
        locationEngine = new LocationSource(this);
        locationEngine.activate();

        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;

                mapboxMap.getMarkerViewManager().addMarkerViewAdapter(
                        new PulseMarkerViewAdapter(MainActivity.this));

                userMarker = mapboxMap.addMarker(
                        new PulseMarkerViewOptions()
                                .position(new LatLng(0, 0))
                                .anchor(0.5f, 0.5f),
                        new MarkerViewManager.OnMarkerViewAddedListener() {
                            @Override
                            public void onViewAdded(@NonNull MarkerView markerView) {
                                // Check if user has granted location permission
                                if (!PermissionsManager.areLocationPermissionsGranted(MainActivity.this)) {
                                    permissionsManager = new PermissionsManager(MainActivity.this);
                                    permissionsManager.requestLocationPermissions(MainActivity.this);
                                } else {
                                    enableLocation();
                                }
                                animateMarker(markerView);
                            }
                        });
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
        if (locationEngine != null && locationListener != null) {
            locationEngine.activate();
            locationEngine.requestLocationUpdates();
            locationEngine.addLocationEngineListener(locationListener);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
        if (locationEngine != null && locationListener != null) {
            locationEngine.removeLocationEngineListener(locationListener);
            locationEngine.removeLocationUpdates();
            locationEngine.deactivate();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void animateMarker(MarkerView marker) {
        View view = map.getMarkerViewManager().getView(marker);
        if (view != null) {
            View backgroundView = view.findViewById(R.id.background_imageview);

            ValueAnimator scaleCircleX = ObjectAnimator.ofFloat(backgroundView, "scaleX", 1.8f);
            ValueAnimator scaleCircleY = ObjectAnimator.ofFloat(backgroundView, "scaleY", 1.8f);
            ObjectAnimator fadeOut = ObjectAnimator.ofFloat(backgroundView, "alpha", 1f, 0f);

            scaleCircleX.setRepeatCount(ValueAnimator.INFINITE);
            scaleCircleY.setRepeatCount(ValueAnimator.INFINITE);
            fadeOut.setRepeatCount(ObjectAnimator.INFINITE);

            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(scaleCircleX).with(scaleCircleY).with(fadeOut);
            animatorSet.setDuration(1000);
            animatorSet.start();
        }
    }

    private void animateCameraPosition(){
        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 10));
        }

        CameraPosition position = new CameraPosition.Builder()
                .zoom(17)
                .bearing(90)
                .tilt(30)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 5000);
    }

    private void enableLocation() {
        // If we have the last location of the user, we can move the camera to that position.

        Location lastLocation = locationEngine.getLastLocation();
        if (lastLocation != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation), 10));
            userMarker.setPosition(new LatLng(lastLocation));
        }

        CameraPosition position = new CameraPosition.Builder()
                .zoom(17)
                .bearing(90)
                .tilt(30)
                .build();

        map.animateCamera(CameraUpdateFactory.newCameraPosition(position), 9000);

        locationListener = new LocationEngineListener() {
            @Override
            public void onConnected() {
                locationEngine.requestLocationUpdates();
            }

            @Override
            public void onLocationChanged(Location location) {
                if (location != null) {
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location), 16));
                    locationEngine.removeLocationEngineListener(this);
                    userMarker.setPosition(new LatLng(location));
                }
            }
        };
        locationEngine.addLocationEngineListener(locationListener);
    }

    private void showSnackbar(int sentenceId){
        Snackbar.make(findViewById(R.id.mainRelativeLayout), getString(sentenceId), Snackbar.LENGTH_SHORT).show();
    }

    // Custom marker view used for pulsing the background view of marker.
    private static class PulseMarkerViewAdapter extends MapboxMap.MarkerViewAdapter<PulseMarkerView> {

        private LayoutInflater inflater;

        PulseMarkerViewAdapter(@NonNull Context context) {
            super(context);
            this.inflater = LayoutInflater.from(context);
        }

        @Nullable
        @Override
        public View getView(@NonNull PulseMarkerView marker, @Nullable View convertView, @NonNull ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                viewHolder = new ViewHolder();
                convertView = inflater.inflate(R.layout.view_pulse_marker, parent, false);
                viewHolder.foregroundImageView = (ImageView) convertView.findViewById(R.id.foreground_imageView);
                viewHolder.backgroundImageView = (ImageView) convertView.findViewById(R.id.background_imageview);
                convertView.setTag(viewHolder);
            }
            return convertView;
        }

        private static class ViewHolder {
            ImageView foregroundImageView;
            ImageView backgroundImageView;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        showSnackbar(R.string.user_location_permission_explanation);
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            enableLocation();
        } else {
            showSnackbar(R.string.user_location_permission_not_granted);
            finish();
        }
    }
}