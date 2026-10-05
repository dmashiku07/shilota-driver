package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class AcceptanceSettingsData {
    @SerializedName("status")
    public String status;

    @SerializedName("acceptance_settings")
    public AcceptanceSettings acceptance_settings = new AcceptanceSettings();

    public static class AcceptanceSettings {
        @SerializedName("auto_accept")
        public boolean auto_accept;

        @SerializedName("max_pickup_distance_km")
        public double max_pickup_distance_km;

        @SerializedName("min_fare")
        public double min_fare;

        @SerializedName("ride_types")
        public ArrayList<String> ride_types = new ArrayList<>();
    }
}
