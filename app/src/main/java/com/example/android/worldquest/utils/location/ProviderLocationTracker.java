package com.example.android.worldquest.utils.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

/**
 * Created by SterlingRyan on 7/28/2017.
 */

public class ProviderLocationTracker implements LocationListener, LocationTracker {

    // The minimum distance to change Updates in meters
    private static long minimumUpdateDistance = 10;

    // The minimum time between updates in milliseconds
    private static long minimumUpdateTime = 1000 * 60;

    private LocationManager locationManager;

    public enum ProviderType{
        NETWORK,
        GPS
    }
    private String provider;

    private Location lastLocation;
    private Long lastTime;

    private boolean isRunning;

    private LocationUpdateListener listener;

    public ProviderLocationTracker(Context context, ProviderType type) {
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if(type == ProviderType.NETWORK){
            provider = LocationManager.NETWORK_PROVIDER;
        }
        else {
            provider = LocationManager.GPS_PROVIDER;
        }
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        long now = System.currentTimeMillis();
        if(listener != null){
            listener.onUpdate(lastLocation, lastTime, newLocation, now);
        }
        lastLocation = newLocation;
        lastTime = now;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void start() {
        if(isRunning){
            // Already running do nothing
            return;
        }

        // The provider is on, so start getting updates. Update current location
        isRunning = true;
        try{
            locationManager.requestLocationUpdates(provider, minimumUpdateTime, minimumUpdateDistance, this);
            lastLocation = null;
            lastTime = (long) 0;
        } catch (SecurityException e){
            // TODO Call for location permission dialogue
        }

    }

    @Override
    public void start(LocationUpdateListener update) {
        start();
        listener = update;
    }

    @Override
    public void stop() {
        if(isRunning){
            locationManager.removeUpdates(this);
            isRunning = false;
            listener = null;
        }
    }

    @Override
    public boolean hasLocation() {
        if(lastLocation == null){
            return false;
        }
        if(System.currentTimeMillis() - lastTime > 5 * minimumUpdateTime){
            // The location data is stale
            return false;
        }
        return true;
    }

    @Override
    public boolean hasPossibilityStaleLocation() {
        if(lastLocation != null){
            return true;
        }
        try{
            return locationManager.getLastKnownLocation(provider) != null;
        } catch (SecurityException e){
            e.printStackTrace();
        }
        return  false;
    }

    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public Location getPossiblyStaleLocation() {
        return null;
    }
}
