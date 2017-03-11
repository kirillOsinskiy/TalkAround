package com.osk.talkaround.model;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by KOsinsky on 20.03.2016.
 */
@XmlRootElement
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
