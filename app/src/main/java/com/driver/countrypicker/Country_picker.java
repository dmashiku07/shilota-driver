package com.driver.countrypicker;

import static android.content.ContentValues.TAG;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.driver.cabiappdriver.R;
//import com.hbb20.CountryCodePicker;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class Country_picker extends AppCompatActivity {

/*
    CountryCodePicker country_picker;

    RequestQueue queue;
    EditText number_edittext;

    CardView otp_bt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_country_picker);
        queue = Volley.newRequestQueue(this);
        country_picker = (CountryCodePicker) findViewById(R.id.country_picker);
        number_edittext = (EditText) findViewById(R.id.number_edittext);
        otp_bt = (CardView) findViewById(R.id.otp_bt);

        otp_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = country_picker.getSelectedCountryCode();
//                String country = country_picker.getSelectedCountryEnglishName();
                String number = number_edittext.getText().toString();

                Log.d("codeaaaa", code + " " + number);

                Toast.makeText(Country_picker.this, code + " " + number, Toast.LENGTH_SHORT).show();
                Activate_Account();
//                Log.d("countrysss",country);
//                Log.d("numbersqsqs",number);
            }
        });
    }


    private void Activate_Account() {


        // Get Variable
//        String code = country_picker.getSelectedCountryCode();
//        String country = country_picker.getSelectedCountryEnglishName();
//        String number = number_edittext.getText().toString();


        String url = "https://apiotp.beem.africa/v1/request";
//        String encode_value = encodeToBase64("03895cbc0d4af385:Mjg0MDFhMzIzZjU3MThiZGM3MjY3NzRlMzk4NjQ4NzU2MmQ3OWJiMDNlYmEzMWM1NmMxYzdmNTgyNWNlMTc4MA==\n" +
        String encode_value = encodeToBase64("d76e3f89b7b1d075:NzE1MzZjZjJiYzM2YTgzNDUwYmU1MjY4NTNjNTljOGYwNDkzNjRjYWU2MzVkZmUxZTc5YjU4ZmMzYzU2NTM0ZA==");

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                        Log.d("onResponsesss", res);
                    }
                }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Error: " + error.getMessage());
//                Log.d("onErrorResponse",error.getMessage());


                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        Log.d(TAG, "onErrorResponse1: " + res);
                        JSONObject obj = new JSONObject(res);
                        Log.d(TAG, "onErrorResponse2: " + obj);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                }
                VolleyLog.d(TAG, "payBraintreeError:3 " + error.getMessage());


            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));


                map.put("Authorization", "Basic " + encode_value);


//                map.put("Content-Type", "application/json");
//                map.put("Authorization", "Bearer " + "03895cbc0d4af385");

//                "Content-Type": "application/json",
//                        "Authorization": "Bearer YOUR_API_KEY"

//                Log.v(TAG, "Authorization=" + map);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("appId", "1728");
                map.put("msisdn", "919659606838");
//                Log.v(TAG, "createChatParams=" + map);
                return map;
            }
        };

//        getApplication().getInstance().addToRequestQueue(req, TAG);
        queue.add(req);
    }

    public static String encodeToBase64(String originalString) {
        // Encode the string to Base64
//        byte[] bytesEncoded = Base64.encode(originalString.getBytes(), Base64.DEFAULT);
        String bytesEncoded = Base64.encodeToString(originalString.getBytes(), Base64.NO_WRAP);
        return bytesEncoded;
    }
*/


}