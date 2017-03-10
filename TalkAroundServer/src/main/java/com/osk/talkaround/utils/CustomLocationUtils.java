package com.osk.talkaround.utils;

import com.osk.talkaround.model.CustomLocation;

/**
 * Created by Kirill on 02.04.2016.
 */
public class CustomLocationUtils {

    public static float distanceTo(CustomLocation thisLoc, CustomLocation otherLoc) {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(otherLoc.getLatitude()-thisLoc.getLatitude());
        double dLng = Math.toRadians(otherLoc.getLongitude()-thisLoc.getLongitude());
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(thisLoc.getLatitude())) * Math.cos(Math.toRadians(otherLoc.getLatitude())) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }
}
