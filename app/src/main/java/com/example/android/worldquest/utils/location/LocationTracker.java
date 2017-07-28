package com.example.android.worldquest.utils.location;

import android.location.Location;

/**
 * Created by SterlingRyan on 7/28/2017.
 */

public interface LocationTracker {

    public interface LocationUpdateListener{
        public void onUpdate(Location oldLoc, long oldTime, Location newLoc, long newTime);
    }

    public void start();
    public void start(LocationUpdateListener update);

    public void stop();

    public boolean hasLocation();

    public boolean hasPossibilityStaleLocation();

    public Location getLocation();

    public Location getPossiblyStaleLocation();
}
