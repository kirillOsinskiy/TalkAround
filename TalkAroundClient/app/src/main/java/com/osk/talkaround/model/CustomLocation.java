package com.osk.talkaround.model;

import java.io.Serializable;

/**
 * Created by Kirill on 31.03.2016.
 */
public class CustomLocation implements Serializable {
    private static final long serialVersionUID = 207967188772438351L;
    private double longitude;
    private double latitude;

    public CustomLocation() {
    }

    public CustomLocation(double longitude, double latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}
