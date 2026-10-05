package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by hitasoft on 23/3/18.
 */


public class Dashboard {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("rating")
    public String rating;

    @SerializedName("ride_today")
    public String ride_today;

    @SerializedName("ridebymonth")
    public String ride_month;

    @SerializedName("ad_km")
    public String ad_km;

    @SerializedName("ad_earning")
    public String ad_earning;

    @SerializedName("total_earning")
    public String total_earning;

    @SerializedName("booking_count")
    public String booking_count;

    @SerializedName("cash_collected")
    public String cash_collected;

    @SerializedName("available_amount")
    public String available_amount;

    @SerializedName("livestatus")
    public String livestatus;

    @SerializedName("subscription_eligible")
    public String subscription_eligible;

    @SerializedName("subscription_error_code")
    public String subscription_error_code;

    @SerializedName("subscription_message")
    public String subscription_message;

    @SerializedName("subscription_rides_used_today")
    public String subscription_rides_used_today;

    @SerializedName("subscription_daily_limit")
    public String subscription_daily_limit;

    @SerializedName("subscription_remaining_rides")
    public String subscription_remaining_rides;

    @SerializedName("subscription_is_unlimited")
    public String subscription_is_unlimited;

    @SerializedName("subscription_reached_daily_limit")
    public String subscription_reached_daily_limit;

    @SerializedName("cumulative_report")
    public ArrayList<cumulative_report> cumulative_report = new ArrayList<>();

    public static class cumulative_report {
        @SerializedName("month")
        public String month;

        @SerializedName("count")
        public String count;

    }
}
