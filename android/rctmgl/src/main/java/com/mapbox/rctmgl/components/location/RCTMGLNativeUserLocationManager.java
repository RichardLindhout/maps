package com.mapbox.rctmgl.components.location;

import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.ViewGroupManager;
import com.facebook.react.uimanager.annotations.ReactProp;

import javax.annotation.Nonnull;

public class RCTMGLNativeUserLocationManager extends ViewGroupManager<RCTMGLNativeUserLocation> {
    public static final String REACT_CLASS = RCTMGLNativeUserLocation.class.getSimpleName();

    @Nonnull
    @Override
    public String getName() {
        return REACT_CLASS;
    }

    @Nonnull
    @Override
    protected RCTMGLNativeUserLocation createViewInstance(@Nonnull ThemedReactContext reactContext) {
        return new RCTMGLNativeUserLocation(reactContext);
    }

    @ReactProp(name="renderMode")
    public void setRenderMode(RCTMGLNativeUserLocation location, String value) {
        location.setRenderMode(value);
    }
}
