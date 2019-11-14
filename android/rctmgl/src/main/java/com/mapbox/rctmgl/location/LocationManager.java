package com.mapbox.rctmgl.location;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;

/*
import com.mapbox.android.core.location.LocationEngineListener;
import com.mapbox.android.core.location.LocationEnginePriority;
*/

import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by nickitaliano on 12/12/17.
 */

@SuppressWarnings({"MissingPermission"})
public class LocationManager implements LocationEngineCallback<LocationEngineResult> {
    static final long DEFAULT_FASTEST_INTERVAL_MILLIS = 1000;
    static final long DEFAULT_INTERVAL_MILLIS = 1000 * 5;

    public static final String LOG_TAG = LocationManager.class.getSimpleName();

    private LocationEngine locationEngine;
    private Context context;
    private List<OnUserLocationChange> listeners = new ArrayList<>();

    private boolean isActive = false;
    private Location lastLocation = null;

    private LocationEngineRequest locationEngineRequest = null;

    private static WeakReference<LocationManager> INSTANCE = null;

    public static LocationManager getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = new WeakReference<>(new LocationManager(context));
        }
        return INSTANCE.get();
    }

    public interface OnUserLocationChange {
        void onLocationChange(Location location);
    }

    private LocationManager(Context context) {
        this.context = context;
        locationEngine = LocationEngineProvider.getBestLocationEngine(context.getApplicationContext());
        // locationEngine.setPriority(LocationEnginePriority.HIGH_ACCURACY);
        locationEngineRequest = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_MILLIS)
                .setFastestInterval(DEFAULT_FASTEST_INTERVAL_MILLIS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .build();
        // locationEngine.addLocationEngineListener(this);
        //locationEngine.setFastestInterval(1000);
    }

    public void addLocationListener(OnUserLocationChange listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeLocationListener(OnUserLocationChange listener) {
        if (listeners.contains(listener)) {
            listeners.remove(listener);
        }
    }

    public void enable() {
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            return;
        }
        locationEngine.requestLocationUpdates(
                locationEngineRequest,
                this,
                Looper.getMainLooper()
        );
        isActive = true;
    }

    public void disable() {
        locationEngine.removeLocationUpdates(this);
        isActive = false;
    }

    public void dispose() {
        if (locationEngine == null) {
            return;
        }
        disable();
        locationEngine.removeLocationUpdates(this);
    }

    public boolean isActive() {
        return locationEngine != null && this.isActive;
    }

    public Location getLastKnownLocation() {
        if (locationEngine == null) {
            return null;
        }
        return lastLocation;
    }


    public void getLastKnownLocation(LocationEngineCallback<LocationEngineResult> callback) {
        if (locationEngine == null) {
            callback.onFailure(new Exception("LocationEngine not initialized"));
        }

        try {
            locationEngine.getLastLocation(callback);
        }
        catch(Exception exception) {
            Log.w(LOG_TAG, exception);
            callback.onFailure(exception);
        }
    }

    public LocationEngine getEngine() {
        return locationEngine;
    }

    private static final int TWO_MINUTES = 1000 * 60 * 2;


    /** Determines whether one Location reading is better than the current Location fix
     * taken from Android Examples https://developer.android.com/guide/topics/location/strategies.html
     *
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        float accuracyDelta = (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // events keeps firing with very low change constantly
        boolean notSignificant = accuracyDelta > -1 && accuracyDelta < 1;


        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate && !notSignificant) {
            return true;
        } else if (isNewer && !isLessAccurate && !notSignificant) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider && !notSignificant) {
            return true;
        }

        return false;
    }
    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


    public void onLocationChanged(Location location) {

        Log.d(LOG_TAG, String.format(Locale.ENGLISH, "Tick [%f, %f]", location.getLongitude(), location.getLatitude()));
        Log.d(LOG_TAG, String.format(Locale.ENGLISH, "Listener count %d", listeners.size()));

        if (isBetterLocation(location, lastLocation)) {
            for (OnUserLocationChange listener : listeners) {
                listener.onLocationChange(location);

            }

            lastLocation = location;
        }


    }

    @Override
    public void onFailure(Exception exception) {
        // FMTODO handle this.
    }

    @Override
    public void onSuccess(LocationEngineResult result) {
        onLocationChanged(result.getLastLocation());
    }
}
