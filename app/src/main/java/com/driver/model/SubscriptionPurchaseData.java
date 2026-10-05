package com.driver.model;

import com.google.gson.annotations.SerializedName;

public class SubscriptionPurchaseData {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("payment_url")
    public String paymentUrl;

    @SerializedName("return_url")
    public String returnUrl;

    @SerializedName("transid")
    public String transId;

    @SerializedName("paidamount")
    public String paidAmount;

    @SerializedName("subscription_id")
    public String subscriptionId;

    @SerializedName("result")
    public DriverSubscriptionStatusData.SubscriptionItem result;
}
