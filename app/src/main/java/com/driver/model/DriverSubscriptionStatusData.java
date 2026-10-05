package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class DriverSubscriptionStatusData {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("current_subscription")
    public SubscriptionItem currentSubscription;

    @SerializedName("pending_subscription")
    public SubscriptionItem pendingSubscription;

    @SerializedName("result")
    public ArrayList<SubscriptionItem> result = new ArrayList<>();

    public static class SubscriptionItem {
        @SerializedName("_id")
        public String id;

        @SerializedName("plan_name_snapshot")
        public String planName;

        @SerializedName("price_snapshot")
        public double price;

        @SerializedName("currency_snapshot")
        public String currency;

        @SerializedName("duration_unit_snapshot")
        public String durationUnit;

        @SerializedName("duration_value_snapshot")
        public int durationValue;

        @SerializedName("daily_limit_snapshot")
        public Integer dailyLimit;

        @SerializedName("is_unlimited_snapshot")
        public boolean isUnlimited;

        @SerializedName("status")
        public String status;

        @SerializedName("payment_status")
        public String paymentStatus;

        @SerializedName("expires_at")
        public String expiresAt;

        @SerializedName("starts_at")
        public String startsAt;

        @SerializedName("rides_used_today")
        public int ridesUsedToday;

        @SerializedName("current_usage_label")
        public String currentUsageLabel;

        @SerializedName("remaining_rides")
        public Integer remainingRides;

        @SerializedName("reached_daily_limit")
        public boolean reachedDailyLimit;

        @SerializedName("validity_label")
        public String validityLabel;
    }
}
