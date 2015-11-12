package com.wifi.adp.freewifi;

import com.google.android.gms.maps.model.Marker;

public class WifiObject {

    private int id;
    private String name_en;
    private double longitude;
    private double latitude;
    private Marker marker;
    private double distance;

    public WifiObject(int id, String name_en, double latitude, double longitude, double distance, Marker marker) {
        this.name_en = name_en;
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.marker = marker;
        this.distance = distance;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName_en() {
        return name_en;
    }

    public void setName_en(String name_en) {
        this.name_en = name_en;
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

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public Marker getMarker() {
        return marker;
    }

    public void setMarker(Marker marker) {
        this.marker = marker;
    }
}