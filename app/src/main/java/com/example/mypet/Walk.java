package com.example.mypet;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.ArrayList;
import java.util.List;

@IgnoreExtraProperties
public class Walk {
    public String id, date, time, distance;
    public List<LatLngData> path;

    public Walk() {}


    public Walk(String id, String date, String time, String distance, List<org.osmdroid.util.GeoPoint> pathPoints) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.distance = distance;
        this.path = convertGeoPoints(pathPoints);
    }


    private List<LatLngData> convertGeoPoints(List<org.osmdroid.util.GeoPoint> geoPoints) {
        List<LatLngData> latLngDataList = new ArrayList<>();
        if (geoPoints != null) {
            for (org.osmdroid.util.GeoPoint point : geoPoints) {
                LatLngData data = new LatLngData(point.getLatitude(), point.getLongitude());
                latLngDataList.add(data);
            }
        }
        return latLngDataList;
    }


    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
    public String getDistance() { return distance; }
    public void setDistance(String distance) { this.distance = distance; }
    public List<LatLngData> getPath() { return path; }
    public void setPath(List<LatLngData> path) { this.path = path; }
}
