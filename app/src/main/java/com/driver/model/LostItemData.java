package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LostItemData {
    @SerializedName("status")
    public String status;

    @SerializedName("result")
    public ArrayList<Result> result = new ArrayList<>();

    public static class Result {
        @SerializedName("_id")
        public String id;

        @SerializedName("item_description")
        public String itemDescription;

        @SerializedName("contact_phone")
        public String contactPhone;

        @SerializedName("status")
        public String status;

        @SerializedName("response_due_at")
        public String responseDueAt;

        @SerializedName("driver_notes")
        public String driverNotes;

        @SerializedName("last_message_preview")
        public String lastMessagePreview;

        @SerializedName("last_message_sender")
        public String lastMessageSender;

        @SerializedName("unread_driver_count")
        public int unreadDriverCount;

        @SerializedName("user_id")
        public Rider rider;

        @SerializedName("ride_id")
        public Ride ride;
    }

    public static class Rider {
        @SerializedName("full_name")
        public String fullName;

        @SerializedName("phone_number")
        public String phoneNumber;
    }

    public static class Ride {
        @SerializedName("_id")
        public String id;

        @SerializedName("pickup_location")
        public String pickupLocation;

        @SerializedName("drop_location")
        public String dropLocation;
    }
}
