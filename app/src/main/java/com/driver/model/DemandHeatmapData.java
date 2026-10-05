package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DemandHeatmapData {
    @SerializedName("status")
    public String status;

    @SerializedName("data")
    public ArrayList<Hotspot> data = new ArrayList<>();

    public static class Hotspot {
        @SerializedName("latest_location")
        public String latestLocation;

        @SerializedName("latest_lat")
        public String latestLat;

        @SerializedName("latest_lng")
        public String latestLng;

        @SerializedName("count")
        public int count;

        @SerializedName("driver_count")
        public int driverCount;

        @SerializedName("projected_15_min_count")
        public int projected15MinCount;

        @SerializedName("projected_surge")
        public boolean projectedSurge;

        @SerializedName("intensity")
        public String intensity;
    }
}
