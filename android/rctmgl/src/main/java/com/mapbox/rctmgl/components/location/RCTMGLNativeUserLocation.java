package com.mapbox.rctmgl.components.location;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;

import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.rctmgl.components.AbstractMapFeature;
import com.mapbox.rctmgl.components.mapview.RCTMGLMapView;
import com.mapbox.rctmgl.location.UserLocationRenderMode;

public class RCTMGLNativeUserLocation extends AbstractMapFeature implements OnMapReadyCallback, Style.OnStyleLoaded {
    private boolean mEnabled = true;
    private MapboxMap mMap;
    private int mRenderMode = RenderMode.NORMAL;
    private LocationComponent mLocationComponent;

    public RCTMGLNativeUserLocation(Context context) {
        super(context);
    }

    @Override
    public void addToMap(RCTMGLMapView mapView) {
        mEnabled = true;
        mapView.getMapAsync(this);
    }

    @Override
    public void removeFromMap(RCTMGLMapView mapView) {
        mEnabled = false;
        if (mMap != null) mMap.getStyle(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        mMap = mapboxMap;
        mapboxMap.getStyle(this);
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onStyleLoaded(@NonNull Style style) {
        Context context = getContext();
        if (!PermissionsManager.areLocationPermissionsGranted(context)) {
            return;
        }

        mLocationComponent = mMap.getLocationComponent();
        if (mEnabled) {
            mLocationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(context, style).build());
            mLocationComponent.setRenderMode(mRenderMode);
        }
        mLocationComponent.setLocationComponentEnabled(mEnabled);
    }


    public void setRenderMode(String userTrackingMode) {
        mRenderMode = UserLocationRenderMode.fromString(userTrackingMode);
        if (mLocationComponent != null) {
            mLocationComponent.setRenderMode(mRenderMode);
        }
    }
}
