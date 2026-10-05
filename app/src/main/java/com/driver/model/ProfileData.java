package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by hitasoft on 23/3/18.
 */

public class ProfileData {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;
    @SerializedName("user_id")
    public String user_id;

    @SerializedName("full_name")
    public String full_name;

    @SerializedName("email")
    public String email;

    @SerializedName("rating")
    public String rating="0";

    @SerializedName("user_image")
    public String user_image;

    @SerializedName("country_code")
    public String country_code;

    @SerializedName("nextinspectionon")
    public String nextinspectionon;

    @SerializedName("phone_number")
    public String phone_number;

    @SerializedName("walletmoney")
    public String walletmoney;

    @SerializedName("emergency_contact")
    public ArrayList<EmergencyContact> emergency_contact=new ArrayList<>();

    public static class EmergencyContact {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;

        @SerializedName("phone_no")
        public String phone_no;
    }
}
