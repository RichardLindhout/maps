package com.mapbox.rctmgl.location;
import com.mapbox.mapboxsdk.location.modes.RenderMode;


public class UserLocationRenderMode {
    public static int fromString(String value) {
        switch (value) {
            case "gps":
                return RenderMode.GPS;
            case "compass":
                return RenderMode.COMPASS;
            default:
                return RenderMode.NORMAL;
        }
    }
}
