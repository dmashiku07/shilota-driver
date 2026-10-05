package com.driver.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by hitasoft on 22/3/18.
 */

public class UserData {

    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("otp")
    public String otp;
    @SerializedName("full_name")
    public String full_name;

    @SerializedName("user_image")
    public String imageurl = null;

    @SerializedName("email")
    public String email;

    @SerializedName("password")
    public String password;

    @SerializedName("country_code")
    public String country_code;

    @SerializedName("phone_number")
    public String phone_number;

    @SerializedName("token")
    public String token;

    @SerializedName("walletmoney")
    public String walletmoney;

    @SerializedName("payment_url")
    public String payment_url;

    @SerializedName("return_url")
    public String return_url;

    @SerializedName("transid")
    public String transid;

    @SerializedName("paidamount")
    public String paidamount;

    @SerializedName("vehicle_inspection")
    public String vehicle_inspection;

    @SerializedName("security_deposit")
    public String security_deposit;

    @SerializedName("vehicle_details")
    public String vehicle_details;

    @SerializedName("payment_details")
    public String payment_details;

    @SerializedName("approval")
    public String admin_approved;

    @SerializedName("vehicle_approval")
    public String vehicle_approval;
}
