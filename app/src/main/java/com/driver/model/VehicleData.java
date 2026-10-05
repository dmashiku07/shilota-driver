package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by hitasoft on 23/3/18.
 */

public class VehicleData {
    @SerializedName("status")
    public String status;

    @SerializedName("message")
    public String message;

    @SerializedName("vehicle_details")
    public String vehicle_details;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("vehicle_name")
    public String vehicle_name;

    @SerializedName("available_seats")
    public String available_seats;

    @SerializedName("body_type")
    public String vehicle_type;

    @SerializedName("service_types")
    public ArrayList<String> service_types = new ArrayList<>();

    @SerializedName("vehicle_number")
    public String vehicle_number;

    @SerializedName("licence_no")
    public String licence_no;

    @SerializedName(value = "licensedoc", alternate = {"licence_doc"})
    public String licence_doc;

    @SerializedName("licence_date")
    public String licence_date;


    @SerializedName("insurance_no")
    public String insurance_no;

    @SerializedName("insurance_date")
    public String insurance_date;

    @SerializedName(value = "insurancedoc", alternate = {"insurance_doc"})
    public String insurance_doc;


    @SerializedName("book_no")
    public String rcbook_no;

    @SerializedName(value = "bookdoc", alternate = {"book_doc"})
    public String rcbook_doc;

    @SerializedName("book_date")
    public String rcbook_date;

    @SerializedName("amenities")
    public ArrayList<VehicleData.amenitites> amenitites = new ArrayList<>();

    public static class amenitites implements Serializable {
        @SerializedName("id")
        public String id;

        @SerializedName("name")
        public String name;
    }


}
