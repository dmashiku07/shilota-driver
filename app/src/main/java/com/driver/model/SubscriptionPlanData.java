package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SubscriptionPlanData {
    @SerializedName("status")
    public String status;

    @SerializedName("result")
    public ArrayList<Result> result = new ArrayList<>();

    public static class Result {
        @SerializedName("_id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("description")
        public String description;

        @SerializedName("price")
        public double price;

        @SerializedName("currency")
        public String currency;

        @SerializedName("duration_unit")
        public String durationUnit;

        @SerializedName("duration_value")
        public int durationValue;

        @SerializedName("daily_ride_limit")
        public Integer dailyRideLimit;

        @SerializedName("is_unlimited")
        public boolean isUnlimited;

        @SerializedName("validity_label")
        public String validityLabel;
    }
}
