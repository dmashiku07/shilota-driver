package com.driver.cabiappdriver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.driver.helper.LocaleManager;
import com.driver.cabiappdriver.R;

import com.driver.model.NotificationData;
import com.driver.cabiappdriver.databinding.ActivityNotificationBinding;
import com.driver.cabiappdriver.databinding.NotificationItemBinding;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 22/3/18.
 */

public class NotificationActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = NotificationActivity.class.getSimpleName();
    public static NotificationData data = null;
    ActivityNotificationBinding binding;
    ImageView backBtn, nullImage;
    RelativeLayout nullLay, progress;
    TextView nullText;
    FrameLayout mAdView;
    ApiInterface apiInterface;
    RecyclerAdapter recyclerAdapter;
    LinearLayoutManager linearLayoutManager;
    ArrayList<NotificationData.Result> notificationList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notification);
        backBtn = findViewById(R.id.backBtn);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        nullImage = findViewById(R.id.nullImage);
        progress = findViewById(R.id.progress);
        mAdView = findViewById(R.id.adLay);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerView.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
        binding.recyclerView.addItemDecoration(dividerItemDecoration);

        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        nullImage.setImageResource(R.drawable.no_notification);
        nullText.setText(getString(R.string.no_notification));
        progress.setVisibility(View.VISIBLE);
        getUsrNotifications();
        recyclerAdapter = new RecyclerAdapter(this, notificationList);
        binding.recyclerView.setAdapter(recyclerAdapter);


        RelativeLayout.LayoutParams banerparams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        banerparams.addRule(RelativeLayout.CENTER_HORIZONTAL);


        if (Constants.GOOGLE_ADS) {
            loadAd();
        }


    }

    private void getUsrNotifications() {
        Call<NotificationData> call3 = apiInterface.getNotifications(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<NotificationData>() {
            @Override
            public void onResponse(Call<NotificationData> call, Response<NotificationData> response) {
                try {
                    progress.setVisibility(View.GONE);
                    data = response.body();
                    if (data.status.equals("true")) {
                        notificationList.addAll(data.result);
                        recyclerAdapter.notifyDataSetChanged();
                    } else if (data.status.equals("false")) {

                    } else if (data.status.equalsIgnoreCase("error")) {
                        GetSet.logout();
                        Intent b = new Intent(NotificationActivity.this, WelcomeActivity.class);
                        b.putExtra("from", "deleteaccount");
                        Toast.makeText(getApplicationContext(), getString(R.string.accountdeleted), Toast.LENGTH_LONG).show();
                        startActivity(b);
                    }

                    if (notificationList.size() == 0) {
                        nullLay.setVisibility(View.VISIBLE);
                    } else {
                        nullLay.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<NotificationData> call, Throwable t) {
                call.cancel();
                progress.setVisibility(View.GONE);
                if (notificationList.size() == 0) {
                    nullLay.setVisibility(View.VISIBLE);
                } else {
                    nullLay.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override

    public void onClick(View view) {
        if (view.getId() == R.id.backBtn) {
            onBackPressed();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        ArrayList<NotificationData.Result> notifyList;
        Context context;

        public RecyclerAdapter(Context context, ArrayList<NotificationData.Result> items) {
            notifyList = items;
            this.context = context;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            NotificationItemBinding itemBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.notification_item, parent, false);
            return new ViewHolder(itemBinding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, final int position) {
            switch (("" + notifyList.get(position).title)) {
                case "Payment Completed":
                    holder.getBinding().notificationTitle.setText(getString(R.string.payment_completed));
                    break;
                case "Wallet Credited":
                    holder.getBinding().notificationTitle.setText(getString(R.string.wallet_credited));
                    break;
                case "Admin":
                    holder.getBinding().notificationTitle.setText(getString(R.string.admin));
                    break;
                case "Ride Accepted":
                    holder.getBinding().notificationTitle.setText(getString(R.string.ride_accepted));
                    break;
                case "Ride Completed":
                    holder.getBinding().notificationTitle.setText(getString(R.string.ride_completed));
                    break;
                case "Ride Cancelled":
                    holder.getBinding().notificationTitle.setText(getString(R.string.ride_cancelled));
                    break;
                default:
                    holder.getBinding().notificationTitle.setText(notifyList.get(position).title);
                    break;
            }

            holder.getBinding().notificaitonText.setText(notifyList.get(position).message);
            holder.getBinding().notificaitonTime.setText(ApplicationClass.getRideTimeFromTimeStamp(context, Long.parseLong(
                    notifyList.get(position).notified_at)));
            if (notifyList.get(position).title.contains("cancel") || notifyList.get(position).title.contains("decline")) {
                holder.getBinding().notificaitonText.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(R.drawable.ic_cancelled)
                        .into(holder.getBinding().notificationImage);
                holder.getBinding().notificationImage.setImageDrawable(getResources().getDrawable(R.drawable.ic_cancelled));
            } else if (notifyList.get(position).title.equals("admin") || notifyList.get(position).title.equals("Admin")) {
                holder.getBinding().notificaitonText.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(R.drawable.admin_final)
                        .into(holder.getBinding().notificationImage);
            } else {
                holder.getBinding().notificaitonText.setVisibility(View.GONE);
                Glide.with(context)
                        .load(R.drawable.ic_completed)
                        .into(holder.getBinding().notificationImage);
            }

        }

        @Override
        public int getItemCount() {
            return notifyList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            NotificationItemBinding itemBinding;

            public ViewHolder(NotificationItemBinding notifyBinding) {
                super(notifyBinding.getRoot());
                itemBinding = notifyBinding;
            }

            public NotificationItemBinding getBinding() {
                return itemBinding;
            }
        }

    }

    private void loadAd() {
        //Addon for googleads
        //BannerAdUtils.loadBannerAd(NotificationActivity.this, mAdView);
    }
}
