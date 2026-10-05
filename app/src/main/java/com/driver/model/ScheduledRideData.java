package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ScheduledRideData {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("result")
    public ArrayList<ScheduledRide> result = new ArrayList<>();

    public static class ScheduledRide {
        @SerializedName("onride_id")
        public String onrideId;

        @SerializedName("user_name")
        public String userName;

        @SerializedName("pickup_location")
        public String pickupLocation;

        @SerializedName("drop_location")
        public String dropLocation;

        @SerializedName("schedule_time")
        public long scheduleTime;

        @SerializedName("ride_status")
        public String rideStatus;

        @SerializedName("category_name")
        public String categoryName;

        @SerializedName("service_type")
        public String serviceType;
    }
}
