package com.driver.cabiappdriver;

import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.driver.cabiappdriver.databinding.ActivityRideCompleteBinding;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;
import com.google.gson.Gson;


import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RideCompleteActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = RideCompleteActivity.class.getSimpleName();
    static ApiInterface apiInterface;
    String onride_id;
    Map<String, String> rideData;
    ActivityRideCompleteBinding binding;
    boolean fromHistory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        setContentView(R.layout.activity_ride_complete);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_ride_complete);
        onride_id = getIntent().getStringExtra("onride_id");
        fromHistory = getIntent().getBooleanExtra("from_history", false);
        binding.collect.setOnClickListener(this);
        binding.blockRider.setOnClickListener(this);
        binding.toolbar.findViewById(R.id.backBtn).setVisibility(View.VISIBLE);
        binding.toolbar.findViewById(R.id.backBtn).setOnClickListener(this);
        if (fromHistory) {
            binding.collect.setVisibility(View.GONE);
            binding.blockRider.setVisibility(View.GONE);
        }

        if (onride_id == null || onride_id.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        completeRideDetail();
    }

    private void setPaymentData() {
        if (rideData != null && !rideData.isEmpty()) {
            boolean directToDriver = "1".equals(rideData.get("payment_direct_to_driver"));
            Glide.with(this).load(rideData.get("user_image"))
                    .apply(new RequestOptions().error(R.drawable.temp))
                    .thumbnail(0.5f).into(binding.userImage);
            binding.userName.setText(rideData.get("username"));
            binding.rideFare.setText(Constants.currency_symbol + " " + rideData.get("ride_fare"));
            binding.totalBill.setText(Constants.currency_symbol + " " + rideData.get("ride_total"));
            binding.total.setText(Constants.currency_symbol + " " + rideData.get("ride_total"));
            binding.paymenttype.setText(directToDriver
                    ? getString(R.string.paid_directly_to_driver)
                    : rideData.get("payment_type"));
        }
    }


    public void completeRideDetail() {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("onride_id", onride_id);

        Log.v(TAG, "completeRideDetailParams: " + map);
        Call<Map<String, String>> call3 = apiInterface.completeridedetails(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                try {
                    Log.v(TAG, "completeRideDetailResponse=" + new Gson());
                    rideData = response.body();
                    if (rideData != null && !rideData.isEmpty()) {
                        if (Constants.TRUE.equals(rideData.get("status"))) {
                            setPaymentData();
                        } else {
                            Toast.makeText(getApplicationContext(), rideData.get(Constants.TAG_MESSSAGE) == null
                                    ? getString(R.string.something_wrong)
                                    : rideData.get(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                        finish();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "completeRideDetailError: " + t.getMessage());
                call.cancel();
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backBtn) {
            finish();
        } else if (view.getId() == R.id.collect) {
            if (fromHistory) {
                return;
            }
            payByCash();
            finish();
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } else if (view.getId() == R.id.blockRider) {
            if (fromHistory) {
                return;
            }
            blockRider();
        }
    }

    private void blockRider() {
        if (rideData == null || rideData.get("rideuserid") == null) {
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", rideData.get("rideuserid"));
        map.put("ride_id", onride_id);
        map.put("reason", "Blocked after completed ride");

        Call<Map<String, String>> call = apiInterface.blockRider(GetSet.getToken(), map);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.body() != null && "true".equals(response.body().get("status"))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.rider_blocked), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }


    private void payByCash() {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("amount", rideData.get("ride_total"));
        map.put("onride_id", onride_id);
        map.put("rideuserid", rideData.get("rideuserid"));
        map.put("iswallet", "0");

        Log.v(TAG, "payByCashParams=" + map);
        Call<Map<String, String>> call3 = apiInterface.paybycash(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }
}
