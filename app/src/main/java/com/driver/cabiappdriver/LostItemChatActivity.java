package com.driver.cabiappdriver;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driver.cabiappdriver.databinding.ActivityLostItemChatBinding;
import com.driver.cabiappdriver.databinding.LostItemMessageRowBinding;
import com.driver.helper.LocaleManager;
import com.driver.model.LostItemMessageData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LostItemChatActivity extends BaseActivity implements View.OnClickListener {
    public static final String EXTRA_LOST_ITEM_ID = "lost_item_id";
    public static final String EXTRA_RIDER_NAME = "rider_name";

    private ActivityLostItemChatBinding binding;
    private ImageView backBtn;
    private RelativeLayout progress;
    private ApiInterface apiInterface;
    private final ArrayList<LostItemMessageData.Result> messages = new ArrayList<>();
    private RecyclerAdapter recyclerAdapter;
    private String lostItemId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_lost_item_chat);
        backBtn = findViewById(R.id.backBtn);
        progress = findViewById(R.id.progress);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        lostItemId = getIntent().getStringExtra(EXTRA_LOST_ITEM_ID);
        String riderName = getIntent().getStringExtra(EXTRA_RIDER_NAME);

        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        backBtn.setRotation(LocaleManager.isRTL() ? 180 : 0);

        binding.title.setText(getString(R.string.lost_item_follow_up));
        binding.subtitle.setText(TextUtils.isEmpty(riderName)
                ? getString(R.string.lost_item_follow_up_subtitle)
                : getString(R.string.lost_item_follow_up_with, riderName));

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerAdapter = new RecyclerAdapter(messages);
        binding.recyclerView.setAdapter(recyclerAdapter);
        binding.sendMessage.setOnClickListener(this);

        loadMessages();
    }

    private void loadMessages() {
        if (TextUtils.isEmpty(lostItemId)) {
            Toast.makeText(this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        progress.setVisibility(View.VISIBLE);
        apiInterface.getLostItemMessages(GetSet.getToken(), lostItemId).enqueue(new Callback<LostItemMessageData>() {
            @Override
            public void onResponse(Call<LostItemMessageData> call, Response<LostItemMessageData> response) {
                progress.setVisibility(View.GONE);
                messages.clear();
                LostItemMessageData data = response.body();
                if (data != null && "true".equals(data.status) && data.result != null) {
                    messages.addAll(data.result);
                }
                recyclerAdapter.notifyDataSetChanged();
                binding.emptyText.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
                if (!messages.isEmpty()) {
                    binding.recyclerView.scrollToPosition(messages.size() - 1);
                }
            }

            @Override
            public void onFailure(Call<LostItemMessageData> call, Throwable t) {
                call.cancel();
                progress.setVisibility(View.GONE);
                binding.emptyText.setVisibility(messages.isEmpty() ? View.VISIBLE : View.GONE);
                Toast.makeText(LostItemChatActivity.this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendMessage() {
        final String message = binding.messageInput.getText().toString().trim();
        if (message.isEmpty()) {
            binding.messageInput.setError(getString(R.string.message_error));
            return;
        }

        Map<String, String> params = new HashMap<>();
        params.put("message", message);
        binding.sendMessage.setEnabled(false);
        apiInterface.sendLostItemMessage(GetSet.getToken(), lostItemId, params).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                binding.sendMessage.setEnabled(true);
                Map<String, String> body = response.body();
                if (body != null && "true".equals(body.get("status"))) {
                    binding.messageInput.setText("");
                    loadMessages();
                } else {
                    Toast.makeText(LostItemChatActivity.this, body != null && body.get("message") != null ? body.get("message") : getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                binding.sendMessage.setEnabled(true);
                Toast.makeText(LostItemChatActivity.this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.backBtn) {
            onBackPressed();
        } else if (id == R.id.sendMessage) {
            sendMessage();
        }
    }

    private static class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {
        private final ArrayList<LostItemMessageData.Result> items;

        RecyclerAdapter(ArrayList<LostItemMessageData.Result> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LostItemMessageRowBinding binding = DataBindingUtil.inflate(
                    LayoutInflater.from(parent.getContext()),
                    R.layout.lost_item_message_row,
                    parent,
                    false
            );
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            LostItemMessageData.Result item = items.get(position);
            boolean outgoing = "driver".equalsIgnoreCase(item.senderType);
            holder.binding.messageText.setText(item.message == null ? "" : item.message);
            holder.binding.senderText.setText(item.senderType == null ? "system" : item.senderType);
            holder.binding.messageCard.setBackgroundResource(
                    outgoing ? R.drawable.primary_bg : R.drawable.whitebg
            );
            holder.binding.messageText.setTextColor(
                    holder.itemView.getResources().getColor(
                            outgoing ? android.R.color.white : R.color.textPrimary
                    )
            );
            holder.binding.senderText.setTextColor(
                    holder.itemView.getResources().getColor(
                            outgoing ? android.R.color.white : R.color.textsecondary
                    )
            );
            ((ViewGroup.MarginLayoutParams) holder.binding.messageCard.getLayoutParams()).leftMargin =
                    outgoing ? 80 : 0;
            ((ViewGroup.MarginLayoutParams) holder.binding.messageCard.getLayoutParams()).rightMargin =
                    outgoing ? 0 : 80;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            LostItemMessageRowBinding binding;

            ViewHolder(LostItemMessageRowBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }
}
