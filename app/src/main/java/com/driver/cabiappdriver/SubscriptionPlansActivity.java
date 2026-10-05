package com.driver.cabiappdriver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driver.cabiappdriver.databinding.ActivitySubscriptionPlansBinding;
import com.driver.cabiappdriver.databinding.SubscriptionPlanRowBinding;
import com.driver.helper.LocaleManager;
import com.driver.model.DriverSubscriptionStatusData;
import com.driver.model.SubscriptionPlanData;
import com.driver.model.SubscriptionPurchaseData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubscriptionPlansActivity extends BaseActivity implements View.OnClickListener {
    private static final int SUBSCRIPTION_PAYMENT_REQUEST = 1301;

    ActivitySubscriptionPlansBinding binding;
    ImageView backBtn;
    RelativeLayout nullLay, progress;
    TextView nullText;
    ApiInterface apiInterface;
    RecyclerAdapter recyclerAdapter;
    ArrayList<SubscriptionPlanData.Result> plans = new ArrayList<>();
    DriverSubscriptionStatusData.SubscriptionItem currentSubscription;
    DriverSubscriptionStatusData.SubscriptionItem pendingSubscription;
    boolean purchaseInFlight = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_subscription_plans);
        backBtn = findViewById(R.id.backBtn);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        progress = findViewById(R.id.progress);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        backBtn.setRotation(LocaleManager.isRTL() ? 180 : 0);

        nullText.setText(getString(R.string.no_subscription_plans));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
        binding.recyclerView.addItemDecoration(divider);
        recyclerAdapter = new RecyclerAdapter(this, plans);
        binding.recyclerView.setAdapter(recyclerAdapter);

        renderSubscriptionState();
        loadCurrentSubscription();
        loadPlans();
    }

    private void loadPlans() {
        progress.setVisibility(View.VISIBLE);
        Call<SubscriptionPlanData> call = apiInterface.getSubscriptionPlans(GetSet.getToken(), GetSet.getUserId());
        call.enqueue(new Callback<SubscriptionPlanData>() {
            @Override
            public void onResponse(Call<SubscriptionPlanData> call, Response<SubscriptionPlanData> response) {
                progress.setVisibility(View.GONE);
                plans.clear();
                SubscriptionPlanData data = response.body();
                if (data != null && "true".equals(data.status) && data.result != null) {
                    plans.addAll(data.result);
                }
                recyclerAdapter.notifyDataSetChanged();
                nullLay.setVisibility(plans.size() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Call<SubscriptionPlanData> call, Throwable t) {
                call.cancel();
                progress.setVisibility(View.GONE);
                nullLay.setVisibility(plans.size() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void loadCurrentSubscription() {
        Call<DriverSubscriptionStatusData> call = apiInterface.getCurrentSubscription(GetSet.getToken(), GetSet.getUserId());
        call.enqueue(new Callback<DriverSubscriptionStatusData>() {
            @Override
            public void onResponse(Call<DriverSubscriptionStatusData> call, Response<DriverSubscriptionStatusData> response) {
                DriverSubscriptionStatusData data = response.body();
                if (data != null && "true".equals(data.status)) {
                    currentSubscription = data.currentSubscription;
                    pendingSubscription = data.pendingSubscription;
                } else {
                    currentSubscription = null;
                    pendingSubscription = null;
                }
                renderSubscriptionState();
            }

            @Override
            public void onFailure(Call<DriverSubscriptionStatusData> call, Throwable t) {
                call.cancel();
                renderSubscriptionState();
            }
        });
    }

    private void initiatePurchase(SubscriptionPlanData.Result item) {
        if (purchaseInFlight) {
            return;
        }
        purchaseInFlight = true;
        progress.setVisibility(View.VISIBLE);

        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("plan_id", item.id);

        apiInterface.initiateSubscriptionPurchase(GetSet.getToken(), map).enqueue(new Callback<SubscriptionPurchaseData>() {
            @Override
            public void onResponse(Call<SubscriptionPurchaseData> call, Response<SubscriptionPurchaseData> response) {
                progress.setVisibility(View.GONE);
                purchaseInFlight = false;
                SubscriptionPurchaseData data = response.body();
                if (data != null && "true".equals(data.status) && data.paymentUrl != null) {
                    Intent intent = new Intent(SubscriptionPlansActivity.this, ActivityWebView.class);
                    intent.putExtra("webview_url", data.paymentUrl);
                    intent.putExtra("return_url", data.returnUrl);
                    intent.putExtra("verify_url", Constants.BASE_URL + "driverapi/subscriptions/verifyselcom/" + data.transId);
                    intent.putExtra("transid", data.transId);
                    intent.putExtra("amount", String.valueOf(item.price));
                    startActivityForResult(intent, SUBSCRIPTION_PAYMENT_REQUEST);
                } else {
                    Toast.makeText(SubscriptionPlansActivity.this, data != null && data.message != null ? data.message : getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<SubscriptionPurchaseData> call, Throwable t) {
                call.cancel();
                progress.setVisibility(View.GONE);
                purchaseInFlight = false;
                Toast.makeText(SubscriptionPlansActivity.this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderSubscriptionState() {
        if (currentSubscription != null) {
            binding.currentSubscriptionName.setText(valueOrFallback(currentSubscription.planName, getString(R.string.no_active_subscription)));
            binding.currentSubscriptionStatus.setText(buildStatusLine(currentSubscription.status, currentSubscription.paymentStatus));
            binding.currentSubscriptionExpiry.setText(getString(R.string.subscription_expires_on, formatDateLabel(currentSubscription.expiresAt)));
            binding.currentSubscriptionLimit.setText(currentSubscription.isUnlimited
                    ? getString(R.string.subscription_daily_limit, getString(R.string.unlimited_rides_badge))
                    : getString(R.string.subscription_daily_limit, String.valueOf(currentSubscription.dailyLimit == null ? 0 : currentSubscription.dailyLimit)));
            binding.currentSubscriptionUsage.setText(valueOrFallback(currentSubscription.currentUsageLabel, getString(R.string.subscription_usage_placeholder)));
            return;
        }

        if (pendingSubscription != null) {
            binding.currentSubscriptionName.setText(valueOrFallback(pendingSubscription.planName, getString(R.string.subscription_payment_pending)));
            binding.currentSubscriptionStatus.setText(buildStatusLine(pendingSubscription.status, pendingSubscription.paymentStatus));
            binding.currentSubscriptionExpiry.setText(getString(R.string.subscription_complete_payment_hint));
            binding.currentSubscriptionLimit.setText(pendingSubscription.isUnlimited
                    ? getString(R.string.subscription_daily_limit, getString(R.string.unlimited_rides_badge))
                    : getString(R.string.subscription_daily_limit, String.valueOf(pendingSubscription.dailyLimit == null ? 0 : pendingSubscription.dailyLimit)));
            binding.currentSubscriptionUsage.setText(getString(R.string.subscription_usage_placeholder));
            return;
        }

        binding.currentSubscriptionName.setText(getString(R.string.no_active_subscription));
        binding.currentSubscriptionStatus.setText(getString(R.string.subscription_pick_plan_hint));
        binding.currentSubscriptionExpiry.setText(getString(R.string.subscription_status_none));
        binding.currentSubscriptionLimit.setText(getString(R.string.subscription_daily_limit, "-"));
        binding.currentSubscriptionUsage.setText(getString(R.string.subscription_usage_placeholder));
    }

    private String buildStatusLine(String status, String paymentStatus) {
        String normalizedStatus = valueOrFallback(status, "PENDING_PAYMENT").replace("_", " ");
        String normalizedPaymentStatus = valueOrFallback(paymentStatus, "PENDING");
        return getString(R.string.subscription_status_format, normalizedStatus, normalizedPaymentStatus);
    }

    private String formatDateLabel(String value) {
        if (TextUtils.isEmpty(value)) {
            return "-";
        }
        int separatorIndex = value.indexOf('T');
        return separatorIndex > 0 ? value.substring(0, separatorIndex) : value;
    }

    private String valueOrFallback(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backBtn) {
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBSCRIPTION_PAYMENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.subscription_payment_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.subscription_payment_pending), Toast.LENGTH_SHORT).show();
            }
            loadCurrentSubscription();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        Context context;
        ArrayList<SubscriptionPlanData.Result> items;

        RecyclerAdapter(Context context, ArrayList<SubscriptionPlanData.Result> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SubscriptionPlanRowBinding rowBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(context),
                    R.layout.subscription_plan_row,
                    parent,
                    false
            );
            return new ViewHolder(rowBinding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            SubscriptionPlanData.Result item = items.get(position);
            holder.binding.planName.setText(item.name == null ? "-" : item.name);
            holder.binding.planPrice.setText((item.currency == null || item.currency.isEmpty() ? "TZS" : item.currency) + " " + item.price);
            holder.binding.planValidity.setText(item.validityLabel == null || item.validityLabel.isEmpty()
                    ? context.getString(R.string.subscription_validity_placeholder)
                    : item.validityLabel);
            holder.binding.planLimit.setText(item.isUnlimited
                    ? context.getString(R.string.unlimited_rides_badge)
                    : context.getString(R.string.plan_rides_per_day, item.dailyRideLimit == null ? "-" : String.valueOf(item.dailyRideLimit)));
            holder.binding.planDescription.setText(item.description == null || item.description.isEmpty()
                    ? context.getString(R.string.subscription_plan_description_placeholder)
                    : item.description);
            holder.binding.unlimitedBadge.setVisibility(item.isUnlimited ? View.VISIBLE : View.GONE);
            holder.binding.planAction.setText(resolvePlanActionLabel(item));
            holder.binding.planAction.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    initiatePurchase(item);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            SubscriptionPlanRowBinding binding;

            ViewHolder(SubscriptionPlanRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }

        private String resolvePlanActionLabel(SubscriptionPlanData.Result item) {
            if (currentSubscription != null && currentSubscription.planName != null && currentSubscription.planName.equalsIgnoreCase(item.name)) {
                return context.getString(R.string.renew_plan);
            }
            if (pendingSubscription != null && pendingSubscription.planName != null && pendingSubscription.planName.equalsIgnoreCase(item.name)) {
                return context.getString(R.string.complete_payment);
            }
            return context.getString(R.string.buy_plan);
        }
    }
}
