package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class LostItemMessageData {
    @SerializedName("status")
    public String status;

    @SerializedName("result")
    public ArrayList<Result> result = new ArrayList<>();

    public static class Result {
        @SerializedName("_id")
        public String id;

        @SerializedName("sender_type")
        public String senderType;

        @SerializedName("sender_id")
        public String senderId;

        @SerializedName("message")
        public String message;

        @SerializedName("created_at")
        public String createdAt;
    }
}
