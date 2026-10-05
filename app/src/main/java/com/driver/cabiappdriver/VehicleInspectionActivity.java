package com.driver.cabiappdriver;

import android.content.Intent;
import android.content.SharedPreferences;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import com.driver.helper.NetworkReceiver;
import com.driver.cabiappdriver.databinding.ActivityVehicleInspectionBinding;
import com.google.android.material.snackbar.Snackbar;

import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.driver.cabiappdriver.R;

import com.driver.model.UserData;
import com.driver.model.VehicleData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 23/3/18.
 */

public class VehicleInspectionActivity extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = VehicleInspectionActivity.class.getSimpleName();
    public static String vehicledetail = "", paymentdetail = "", securitydeposit = "", vehicleinspection = "", vehicleApprove = "";
    ActivityVehicleInspectionBinding binding;
    Snackbar snackbar;
    NetworkReceiver receiver;
    int exit = 0;
    ApiInterface apiInterface;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle_inspection);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_inspection);
        binding.paymentDetailsLay.setOnClickListener(this);
        receiver = new NetworkReceiver();
        ApplicationClass.getInstance().setConnectivityListener(this);
        binding.vehicleDetailsLay.setOnClickListener(this);
        binding.licenseUploadLay.setOnClickListener(this);
        binding.insuranceUploadLay.setOnClickListener(this);
        binding.bookUploadLay.setOnClickListener(this);
        binding.logoutLay.setOnClickListener(this);
        snackbar = Snackbar.make(findViewById(R.id.activity_vehicle_inspection), getString(R.string.exit_msg), Snackbar.LENGTH_SHORT);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                exit = 0;
            }

            @Override
            public void onShown(Snackbar snackbar) {
                exit = 1;
            }
        });

        loadSectionStatusFromPrefs();
        setVehicleStatus(vehicleApprove);
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.paymentDetailsLay) {
            Intent paymentIntent = new Intent(this, PaymentActivity.class);
            startActivity(paymentIntent);
        } else if (id == R.id.vehicleDetailsLay) {
            Intent vehicleDetailIntent = new Intent(this, VehicleDetailsActivity.class);
            startActivity(vehicleDetailIntent);
        } else if (id == R.id.licenseUploadLay || id == R.id.insuranceUploadLay || id == R.id.bookUploadLay) {
            Intent vehicleDetailIntent = new Intent(this, VehicleDetailsActivity.class);
            startActivity(vehicleDetailIntent);
        } else if (id == R.id.logoutLay) {
            logoutAndOpenLogin();
        }
    }


    @Override
    public void onBackPressed() {
        if (exit == 0) {
            snackbar.show();
        } else {
            VehicleInspectionActivity.this.finishAffinity();
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.activity_vehicle_inspection), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, ApplicationClass.intentFilter);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_vehicle_inspection), NetworkReceiver.isConnected());
        loadSectionStatusFromPrefs();
        setVehicleStatus(vehicleApprove);
        signin();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_vehicle_inspection), true);
    }

    public void signin() {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_EMAIL, GetSet.getEmail());
        map.put(Constants.TAG_PASSWORD, GetSet.getPassword());
        Log.v(TAG, "signInParams: " + map);
        Call<UserData> call3 = apiInterface.signin(map);
        call3.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                try {
                    UserData userdata = response.body();
                    if (userdata.status.equals("true")) {

                        if (userdata.admin_approved.equals("true")) {
                            GetSet.Logingin(userdata.user_id, GetSet.getEmail(), userdata.full_name, userdata.imageurl, userdata.token, userdata.walletmoney);
                            editor.putBoolean("isLogged", true);
                            editor.putString("userId", userdata.user_id);
                            editor.putString("userImage", userdata.imageurl);
                            editor.putString("fullName", userdata.full_name);
                            editor.putString("email", GetSet.getEmail());
                            editor.putString("token", userdata.token);
                            editor.putString("wallet", userdata.walletmoney);
                            editor.putString("password", userdata.password);
                            editor.putString("admin_approved", userdata.admin_approved);
                            String vehicleDetailsStatus = normalizeSectionStatus(userdata.vehicle_details);
                            String paymentDetailsStatus = mergeSectionStatus(userdata.payment_details, pref.getString("payment_details", paymentdetail));
                            String securityDepositStatus = mergeSectionStatus(userdata.security_deposit, pref.getString("security_deposit", securitydeposit));
                            String vehicleInspectionStatus = mergeSectionStatus(userdata.vehicle_inspection, pref.getString("vehicle_inspection", vehicleinspection));
                            editor.putString("vehicle_approval", userdata.vehicle_approval);
                            editor.putString("vehicle_details", vehicleDetailsStatus);
                            editor.putString("payment_details", paymentDetailsStatus);
                            editor.putString("security_deposit", securityDepositStatus);
                            editor.putString("vehicle_inspection", vehicleInspectionStatus);
                            editor.commit();
                            GetSet.setPassword(userdata.password);
                            GetSet.setToken(userdata.token);
                            GetSet.setWallet(userdata.walletmoney);

                            VehicleInspectionActivity.vehicledetail = vehicleDetailsStatus;
                            VehicleInspectionActivity.paymentdetail = paymentDetailsStatus;
                            VehicleInspectionActivity.securitydeposit = securityDepositStatus;
                            VehicleInspectionActivity.vehicleinspection = vehicleInspectionStatus;

                            vehicleApprove = normalizeApprovalStatus(userdata.vehicle_approval);
                            setVehicleStatus(vehicleApprove);
                            refreshVehicleDetailsCompletion();
                            Log.v(TAG, "ADMINAPPROVE: " + userdata.admin_approved);
                        } else {
                            removeToken();
                            SharedPreferences pref = VehicleInspectionActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.clear();
                            editor.commit();
                            GetSet.logout();
                            Intent j = new Intent(getApplicationContext(), WelcomeActivity.class);
                            startActivity(j);
                        }

                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                    } else if (userdata.status.equals("false")) {
                        Toast.makeText(getApplicationContext(), userdata.message, Toast.LENGTH_SHORT).show();
                        logoutAndOpenLogin();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logoutAndOpenLogin();
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Log.v(TAG, t.getMessage());
                call.cancel();
            }
        });

    }

    private void logoutAndOpenLogin() {
        SharedPreferences pref = VehicleInspectionActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        GetSet.logout();
        Intent intent = new Intent(VehicleInspectionActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void setVehicleStatus(String vehicleApprove) {
        binding.title1.setText(R.string.driver_onboarding);
        updateStatus(binding.vehicleApprovalStatusValue, vehicleApprove);

        if (isApprovedStatus(vehicleApprove)) {
            binding.onboardingMessage.setText(R.string.onboarding_message_approved);
        } else {
            binding.onboardingMessage.setText(R.string.onboarding_message_not_approved);
        }
    }

    private void updateStatus(TextView view, String status) {
        String value = isApprovedStatus(status)
                ? getString(R.string.status_approved)
                : getString(R.string.status_not_approved);
        view.setText(value);
        if (isApprovedStatus(status)) {
            view.setTextColor(getResources().getColor(R.color.green));
        } else {
            view.setTextColor(getResources().getColor(R.color.red));
        }
    }

    private void loadSectionStatusFromPrefs() {
        vehicledetail = normalizeSectionStatus(pref.getString("vehicle_details", vehicledetail));
        paymentdetail = normalizeSectionStatus(pref.getString("payment_details", paymentdetail));
        securitydeposit = normalizeSectionStatus(pref.getString("security_deposit", securitydeposit));
        vehicleinspection = normalizeSectionStatus(pref.getString("vehicle_inspection", vehicleinspection));
        vehicleApprove = normalizeApprovalStatus(pref.getString("vehicle_approval", vehicleApprove));
    }

    public static void saveSectionStatus(String key, String value) {
        String status = normalizeSectionStatus(value);
        if ("vehicle_details".equals(key)) {
            vehicledetail = status;
        } else if ("payment_details".equals(key)) {
            paymentdetail = status;
        } else if ("security_deposit".equals(key)) {
            securitydeposit = status;
        } else if ("vehicle_inspection".equals(key)) {
            vehicleinspection = status;
        }
        if (editor != null) {
            editor.putString(key, status);
            editor.apply();
        }
    }

    private void refreshVehicleDetailsCompletion() {
        Call<VehicleData> call = apiInterface.getVehicleDetails(GetSet.getToken(), GetSet.getUserId());
        call.enqueue(new Callback<VehicleData>() {
            @Override
            public void onResponse(Call<VehicleData> call, Response<VehicleData> response) {
                VehicleData vehicleData = response.body();
                if (vehicleData != null && "true".equalsIgnoreCase(vehicleData.status)) {
                    saveSectionStatus("vehicle_details", vehicleData.vehicle_details);
                    setVehicleStatus(vehicleApprove);
                }
            }

            @Override
            public void onFailure(Call<VehicleData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private static String normalizeSectionStatus(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "InComplete";
        }
        return isCompleteStatus(value) ? "Complete" : value.trim();
    }

    private String mergeSectionStatus(String serverValue, String savedValue) {
        if (isCompleteStatus(serverValue) || isCompleteStatus(savedValue)) {
            return "Complete";
        }
        return normalizeSectionStatus(serverValue);
    }

    private String normalizeApprovalStatus(String value) {
        return isApprovedStatus(value) ? "approved" : "not_approved";
    }

    private static boolean isCompleteStatus(String value) {
        return "Complete".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private boolean isApprovedStatus(String value) {
        return "approved".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value) || "1".equals(value);
    }

    private void removeToken() {

        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("device_id", deviceId);
        Log.v(TAG, "removeTokenParams= " + map);
        Call<Map<String, String>> call3 = apiInterface.pushsignout(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v(TAG, "removeTokenResponse= " + data);
                GetSet.logout();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                GetSet.logout();
            }
        });
    }
}
