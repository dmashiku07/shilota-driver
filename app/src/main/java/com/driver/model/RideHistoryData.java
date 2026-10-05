package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by hitasoft on 23/4/18.
 */

public class RideHistoryData {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;
    @SerializedName("result")
    public ArrayList<result> result = new ArrayList<>();

    public class result {

        @SerializedName("onride_id")
        public String onride_id;

        @SerializedName("pickup_time")
        public String pickup_time;

        @SerializedName("category_image")
        public String category_image;

        @SerializedName("category_name")
        public String category_name;

        @SerializedName("vehicle_no")
        public String vehicle_no;

        @SerializedName("total_price")
        public String total_price;

        @SerializedName("ride_status")
        public String ride_status;
    }
}
