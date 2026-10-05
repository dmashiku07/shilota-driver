package com.driver.model;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by hitasoft on 25/2/18.
 */

public class HelpData {
    @SerializedName("status")
    public String status;

    @SerializedName("result")
    public ArrayList<Result> result=new ArrayList<>();

    public class Result {

        @SerializedName("id")
        public String id;

        @SerializedName("pagetype")
        public String pagetype;

        @SerializedName("title")
        public String title;

        @SerializedName("content")
        public String content;

        @SerializedName("status")
        public String status;
    }
}
