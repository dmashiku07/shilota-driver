package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by hitasoft on 26/3/18.
 */

public class AdminData {

    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("currencysymbol")
    public String currency_symbol;

    @SerializedName("admin_enable_ads")
    public String  admin_enable_ads;

    @SerializedName("ad_unit_id")
    public String  ad_unit_id;


    @SerializedName("emergencycontact")
    public String emergencycontact;

    @SerializedName("body_type")
    public ArrayList<BodyType> bodyType = new ArrayList<>();

    @SerializedName("amenities")
    public ArrayList<Amenities> amenities = new ArrayList<>();

    @SerializedName("driverdocs")
    public ArrayList<driverdocs> driverdocs = new ArrayList<>();


    public class BodyType {

        @SerializedName("_id")
        public String id;

        @SerializedName("name")
        public String name;
    }

    public class Amenities {

        @SerializedName("_id")
        public String amentiesid;

        @SerializedName("name")
        public String amentiesname;
    }

    public class driverdocs {

        @SerializedName("_id")
        public String amentiesid;

        @SerializedName("pagetype")
        public String pagetype;

        @SerializedName("content")
        public String content;
    }

}
