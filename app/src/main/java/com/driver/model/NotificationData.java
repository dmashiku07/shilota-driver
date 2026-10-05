package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by hitasoft on 23/3/18.
 */

public class NotificationData {
    @SerializedName("status")
    public String status;
    @SerializedName("result")
    public ArrayList<Result> result=new ArrayList<>();

    public class Result {

        @SerializedName("title")
        public String title;

        @SerializedName("message")
        public String message;

        @SerializedName("notified_at")
        public String notified_at;
    }
}
