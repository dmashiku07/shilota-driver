package com.driver.cabiappdriver;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.driver.helper.LocaleManager;
import com.driver.cabiappdriver.R;
import com.driver.model.ProfileData;
import com.driver.cabiappdriver.databinding.ActivityVehicleInspAndSecurityBinding;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 23/3/18.
 */

public class VehicleInspAndSecurityActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = VehicleInspAndSecurityActivity.class.getSimpleName();
    static ApiInterface apiInterface;
    ActivityVehicleInspAndSecurityBinding securityBinding;
    ImageView backBtn;
    String from;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_insp_and_security);
        securityBinding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_insp_and_security);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setVisibility(View.VISIBLE);
        from = getIntent().getStringExtra("from");
        if (LocaleManager.isRTL()){
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        String content;
        try {
            if (from != null && from.equals("vehicleinspection")) {
                securityBinding.PageTitle.setText(getString(R.string.vehicle_inspection));
                content = SplashActivity.adminData.driverdocs.get(0).content;

            } else {
                securityBinding.PageTitle.setText(getString(R.string.security_deposit));
                content = SplashActivity.adminData.driverdocs.get(1).content;
            }
        } catch (Exception e) {
            content = "";
        }
        securityBinding.webView.loadData(content, "text/html", "UTF-8");
        securityBinding.agreeBtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.agreeBtn) {
            AcceptTerms();
        } else if (id == R.id.backBtn) {
            onBackPressed();
        }
    }


    private void AcceptTerms() {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        if (from != null && from.equals("vehicleinspection"))
            map.put("vehicle_inspection", "Complete");
        else
            map.put("security_deposit", "Complete");

        Call<ProfileData> call3 = apiInterface.updateProfile(GetSet.getToken(), map);
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                try {
                    ProfileData data = response.body();
                    if (data.status.equals("true")) {
                        if (from != null && from.equals("vehicleinspection")) {
                            VehicleInspectionActivity.saveSectionStatus("vehicle_inspection", "Complete");
                            Intent agreeIntent = new Intent(VehicleInspAndSecurityActivity.this,
                                    VehicleInspectionActivity.class);
                            startActivity(agreeIntent);
                        } else {
                            VehicleInspectionActivity.saveSectionStatus("security_deposit", "Complete");
                            Intent agreeIntent = new Intent(VehicleInspAndSecurityActivity.this,
                                    VehicleInspectionActivity.class);
                            startActivity(agreeIntent);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), data.message, Toast.LENGTH_LONG).show();
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
}
