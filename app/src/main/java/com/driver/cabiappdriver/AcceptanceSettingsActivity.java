package com.driver.cabiappdriver;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.driver.model.AcceptanceSettingsData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AcceptanceSettingsActivity extends BaseActivity implements View.OnClickListener {
    private ImageView backBtn;
    private CheckBox autoAccept;
    private CheckBox rideTypeRide;
    private CheckBox rideTypeDelivery;
    private CheckBox rideTypePool;
    private EditText maxPickupDistance;
    private EditText minFare;
    private Button saveButton;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acceptance_settings);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = findViewById(R.id.backBtn);
        autoAccept = findViewById(R.id.autoAccept);
        rideTypeRide = findViewById(R.id.rideTypeRide);
        rideTypeDelivery = findViewById(R.id.rideTypeDelivery);
        rideTypePool = findViewById(R.id.rideTypePool);
        maxPickupDistance = findViewById(R.id.maxPickupDistance);
        minFare = findViewById(R.id.minFare);
        saveButton = findViewById(R.id.saveButton);

        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        saveButton.setOnClickListener(this);

        loadSettings();
    }

    private void loadSettings() {
        apiInterface.getAcceptanceSettings(GetSet.getToken(), GetSet.getUserId()).enqueue(new Callback<AcceptanceSettingsData>() {
            @Override
            public void onResponse(Call<AcceptanceSettingsData> call, Response<AcceptanceSettingsData> response) {
                AcceptanceSettingsData body = response.body();
                if (body == null || body.acceptance_settings == null) {
                    return;
                }
                autoAccept.setChecked(body.acceptance_settings.auto_accept);
                maxPickupDistance.setText(String.valueOf(body.acceptance_settings.max_pickup_distance_km));
                minFare.setText(String.valueOf(body.acceptance_settings.min_fare));
                ArrayList<String> rideTypes = body.acceptance_settings.ride_types;
                rideTypeRide.setChecked(rideTypes == null || rideTypes.size() == 0 || rideTypes.contains("ride"));
                rideTypeDelivery.setChecked(rideTypes != null && rideTypes.contains("delivery"));
                rideTypePool.setChecked(rideTypes != null && rideTypes.contains("pool"));
            }

            @Override
            public void onFailure(Call<AcceptanceSettingsData> call, Throwable t) {
                Toast.makeText(AcceptanceSettingsActivity.this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSettings() {
        ArrayList<String> rideTypes = new ArrayList<>();
        if (rideTypeRide.isChecked()) {
            rideTypes.add("ride");
        }
        if (rideTypeDelivery.isChecked()) {
            rideTypes.add("delivery");
        }
        if (rideTypePool.isChecked()) {
            rideTypes.add("pool");
        }
        if (rideTypes.isEmpty()) {
            Toast.makeText(this, R.string.acceptance_settings_pick_type, Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("auto_accept", autoAccept.isChecked() ? "true" : "false");
        params.put("max_pickup_distance_km", TextUtils.isEmpty(maxPickupDistance.getText()) ? "0" : maxPickupDistance.getText().toString().trim());
        params.put("min_fare", TextUtils.isEmpty(minFare.getText()) ? "0" : minFare.getText().toString().trim());
        params.put("ride_types", new JSONArray(rideTypes).toString());

        apiInterface.saveAcceptanceSettings(GetSet.getToken(), params).enqueue(new Callback<AcceptanceSettingsData>() {
            @Override
            public void onResponse(Call<AcceptanceSettingsData> call, Response<AcceptanceSettingsData> response) {
                Toast.makeText(AcceptanceSettingsActivity.this, R.string.acceptance_settings_saved, Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onFailure(Call<AcceptanceSettingsData> call, Throwable t) {
                Toast.makeText(AcceptanceSettingsActivity.this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.backBtn) {
            finish();
        } else if (v.getId() == R.id.saveButton) {
            saveSettings();
        }
    }
}
