package com.driver.cabiappdriver;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import com.driver.cabiappdriver.databinding.ActivityLostItemsBinding;
import com.driver.cabiappdriver.databinding.LostItemRowBinding;
import com.driver.helper.LocaleManager;
import com.driver.model.LostItemData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LostItemsActivity extends BaseActivity implements View.OnClickListener {
    ActivityLostItemsBinding binding;
    ImageView backBtn;
    RelativeLayout nullLay, progress;
    TextView nullText;
    ApiInterface apiInterface;
    RecyclerAdapter recyclerAdapter;
    ArrayList<LostItemData.Result> lostItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lost_items);
        backBtn = findViewById(R.id.backBtn);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        progress = findViewById(R.id.progress);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        backBtn.setRotation(LocaleManager.isRTL() ? 180 : 0);

        nullText.setText(getString(R.string.no_lost_items));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration divider = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
        binding.recyclerView.addItemDecoration(divider);
        recyclerAdapter = new RecyclerAdapter(this, lostItems);
        binding.recyclerView.setAdapter(recyclerAdapter);

        loadLostItems();
    }

    private void loadLostItems() {
        progress.setVisibility(View.VISIBLE);
        Call<LostItemData> call = apiInterface.getLostItems(GetSet.getToken(), GetSet.getUserId());
        call.enqueue(new Callback<LostItemData>() {
            @Override
            public void onResponse(Call<LostItemData> call, Response<LostItemData> response) {
                progress.setVisibility(View.GONE);
                lostItems.clear();
                LostItemData data = response.body();
                if (data != null && "true".equals(data.status) && data.result != null) {
                    lostItems.addAll(data.result);
                }
                recyclerAdapter.notifyDataSetChanged();
                nullLay.setVisibility(lostItems.size() == 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onFailure(Call<LostItemData> call, Throwable t) {
                call.cancel();
                progress.setVisibility(View.GONE);
                nullLay.setVisibility(lostItems.size() == 0 ? View.VISIBLE : View.GONE);
            }
        });
    }

    private void updateStatus(final LostItemData.Result item, String status) {
        Map<String, String> params = new HashMap<>();
        params.put("status", status);
        params.put("driver_notes", status);
        Call<Map<String, String>> call = apiInterface.updateLostItemStatus(GetSet.getToken(), item.id, params);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.body() != null && "true".equals(response.body().get("status"))) {
                    item.status = params.get("status");
                    recyclerAdapter.notifyDataSetChanged();
                    Toast.makeText(LostItemsActivity.this, getString(R.string.lost_item_status_updated), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(LostItemsActivity.this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                Toast.makeText(LostItemsActivity.this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void callRider(LostItemData.Result item) {
        String phone = "";
        if (item != null && item.rider != null && item.rider.phoneNumber != null) {
            phone = item.rider.phoneNumber;
        } else if (item != null && item.contactPhone != null) {
            phone = item.contactPhone;
        }
        if (phone.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + Uri.encode(phone)));
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backBtn) {
            onBackPressed();
        }
    }

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        Context context;
        ArrayList<LostItemData.Result> items;

        RecyclerAdapter(Context context, ArrayList<LostItemData.Result> items) {
            this.context = context;
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LostItemRowBinding rowBinding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.lost_item_row, parent, false);
            return new ViewHolder(rowBinding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            final LostItemData.Result item = items.get(position);
            String riderName = item.rider == null || item.rider.fullName == null ? "Rider" : item.rider.fullName;
            String phone = item.rider == null || item.rider.phoneNumber == null ? item.contactPhone : item.rider.phoneNumber;
            String pickup = item.ride == null || item.ride.pickupLocation == null ? "-" : item.ride.pickupLocation;
            String drop = item.ride == null || item.ride.dropLocation == null ? "-" : item.ride.dropLocation;

            holder.binding.riderName.setText(riderName + (phone == null || phone.isEmpty() ? "" : " - " + phone));
            holder.binding.itemDescription.setText(item.itemDescription == null ? "-" : item.itemDescription);
            holder.binding.tripDetail.setText(pickup + "\n" + drop);
            holder.binding.statusText.setText("Status: " + (item.status == null ? "open" : item.status));
            String latestPreview = item.lastMessagePreview == null || item.lastMessagePreview.isEmpty()
                    ? context.getString(R.string.latest_update) + ": -"
                    : context.getString(R.string.latest_update) + ": " + item.lastMessagePreview;
            if (item.unreadDriverCount > 0) {
                latestPreview = latestPreview + " (" + item.unreadDriverCount + ")";
            }
            holder.binding.latestMessage.setText(latestPreview);
            holder.binding.callRider.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    callRider(item);
                }
            });
            holder.binding.openChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, LostItemChatActivity.class);
                    intent.putExtra(LostItemChatActivity.EXTRA_LOST_ITEM_ID, item.id);
                    intent.putExtra(LostItemChatActivity.EXTRA_RIDER_NAME, riderName);
                    context.startActivity(intent);
                }
            });
            holder.binding.markFound.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateStatus(item, "found");
                }
            });
            holder.binding.markReturned.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    updateStatus(item, "returned");
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            LostItemRowBinding binding;

            ViewHolder(LostItemRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
