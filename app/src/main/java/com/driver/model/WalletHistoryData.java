package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class WalletHistoryData {
    @SerializedName("status")
    public String status;

    @SerializedName("payment_list")
    public ArrayList<Result> result = new ArrayList<>();

    public class Result {
        @SerializedName("_id")
        public String id;

        @SerializedName("transaction")
        public String transaction;

        @SerializedName("type")
        public String type;

        @SerializedName("amount")
        public String amount;

        @SerializedName("updated_at")
        public String date;
    }
}
