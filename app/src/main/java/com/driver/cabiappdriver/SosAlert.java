package com.driver.cabiappdriver;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.driver.helper.LocaleManager;
import com.driver.cabiappdriver.R;
import com.driver.model.ProfileData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 22/4/18.
 */

public class SosAlert extends BaseActivity implements View.OnClickListener {
    private static final String TAG = SosAlert.class.getSimpleName();
    private static final String FALLBACK_POLICE_NUMBER = "112";
    ImageView backBtn;
    LinearLayout callPolice, alertEmergency;
    ApiInterface apiInterface;
    double mylat = 0, mylon = 0;
    String onrideId = "";
    ArrayList<ProfileData.EmergencyContact> emergencyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosalert);

        backBtn = findViewById(R.id.backBtn);
        callPolice = findViewById(R.id.callPolice);
        alertEmergency = findViewById(R.id.alertEmergency);

        if (getIntent().getExtras() != null) {
            mylat = getIntent().getExtras().getDouble("lat", 0);
            mylon = getIntent().getExtras().getDouble("lon", 0);
            onrideId = getIntent().getExtras().getString("onride_id", "");
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        backBtn.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()){
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }

        backBtn.setOnClickListener(this);
        callPolice.setOnClickListener(this);
        alertEmergency.setOnClickListener(this);

        getProfileData();
    }

    private void getProfileData() {
        Call<ProfileData> call3 = apiInterface.profile(GetSet.getToken(), GetSet.getUserId());
        Log.v(TAG, "token=" + GetSet.getToken());
        Log.v(TAG, "userId=" + GetSet.getUserId());
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                try {
                    ProfileData data = response.body();
                    Log.v(TAG, "getProfileDataRes=" + data.user_image);
                    if (data.status.equals("true")) {
                        emergencyList.addAll(data.emergency_contact);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.backBtn) {
            finish();
        } else if (id == R.id.callPolice) {
            sendSosAlert("Driver called police control room", "police", new Runnable() {
                @Override
                public void run() {
                    callPoliceControlRoom();
                }
            });
        } else if (id == R.id.alertEmergency) {
            if (emergencyList.size() < 2) {
                Toast.makeText(getApplicationContext(), getString(R.string.emergency_contact_minimum), Toast.LENGTH_SHORT).show();
                return;
            }
            sendSosAlert("Driver alerted emergency contacts", "emergency_contact", new Runnable() {
                @Override
                public void run() {
                    alertEmergencyContacts();
                }
            });
        }
    }

    private void callPoliceControlRoom() {
        String phoneNumber = Constants.emergencycontact == null ? "" : Constants.emergencycontact.trim();
        if (phoneNumber.length() == 0) {
            phoneNumber = FALLBACK_POLICE_NUMBER;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setData(Uri.parse("tel:" + Uri.encode(phoneNumber)));
        startActivity(intent);
    }

    private void alertEmergencyContacts() {
        String emergency = "";
        for (int i = 0; i < emergencyList.size(); i++) {
            if (i == 0)
                emergency = emergencyList.get(i).phone_no;
            else
                emergency = emergency + "," + emergencyList.get(i).phone_no;
        }
        Intent smsIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + emergency));
        smsIntent.putExtra("sms_body", "Emergency: MyLocation: https://www.google.com/maps/?q=" + mylat + "," + mylon);
        startActivity(smsIntent);
    }

    private void sendSosAlert(String message, String actionType, final Runnable afterSend) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("lat", String.valueOf(mylat));
        map.put("lng", String.valueOf(mylon));
        map.put("ride_id", onrideId == null ? "" : onrideId);
        map.put("message", message);
        map.put("action_type", actionType);
        String policeNumber = Constants.emergencycontact == null ? "" : Constants.emergencycontact.trim();
        map.put("contact_target", "police".equals(actionType) ? (policeNumber.length() == 0 ? FALLBACK_POLICE_NUMBER : policeNumber) : "emergency_contacts");

        Call<Map<String, String>> call = apiInterface.sosAlert(GetSet.getToken(), map);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.body() != null && "true".equals(response.body().get("status"))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.sos_alert_sent), Toast.LENGTH_SHORT).show();
                }
                if (afterSend != null) {
                    afterSend.run();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                if (afterSend != null) {
                    afterSend.run();
                }
            }
        });
    }

}
