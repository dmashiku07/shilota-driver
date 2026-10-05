package com.driver.cabiappdriver;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driver.helper.LocaleManager;
import com.driver.model.WalletHistoryData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletHistory extends BaseActivity implements View.OnClickListener {
    private static final String TAG = WalletHistory.class.getSimpleName();

    ImageView backBtn, nullImage;
    RecyclerView recyclerView;
    RelativeLayout nullLay, progress;
    TextView nullText;
    ApiInterface apiInterface;
    RecyclerViewAdapter recyclerViewAdapter;
    ArrayList<WalletHistoryData.Result> datas = new ArrayList<>();
    public static FrameLayout mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wallet_history);

        backBtn = findViewById(R.id.backBtn);
        recyclerView = findViewById(R.id.recyclerView);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        nullImage = findViewById(R.id.nullImage);
        progress = findViewById(R.id.progress);
        mAdView = findViewById(R.id.adLay);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn.setVisibility(View.VISIBLE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL);
        dividerItemDecoration.setDrawable(getResources().getDrawable(R.drawable.recyclerview_divider));
        recyclerView.addItemDecoration(dividerItemDecoration);

        recyclerViewAdapter = new RecyclerViewAdapter(this, datas);
        recyclerView.setAdapter(recyclerViewAdapter);
        backBtn.setOnClickListener(this);

        nullImage.setImageResource(R.drawable.no_wallet);
        nullText.setText(getString(R.string.no_wallet));
        progress.setVisibility(View.VISIBLE);

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }

        getData();
    }

    private void getData() {
        Call<WalletHistoryData> call = apiInterface.walletHistory(GetSet.getToken(), GetSet.getUserId());
        call.enqueue(new Callback<WalletHistoryData>() {
            @Override
            public void onResponse(Call<WalletHistoryData> call, Response<WalletHistoryData> response) {
                progress.setVisibility(View.GONE);
                try {
                    WalletHistoryData data = response.body();
                    if (data != null && "true".equals(data.status)) {
                        datas.addAll(data.result);
                        recyclerViewAdapter.notifyDataSetChanged();
                    }
                    nullLay.setVisibility(datas.isEmpty() ? View.VISIBLE : View.GONE);
                } catch (Exception e) {
                    e.printStackTrace();
                    nullLay.setVisibility(datas.isEmpty() ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            public void onFailure(Call<WalletHistoryData> call, Throwable t) {
                call.cancel();
                progress.setVisibility(View.GONE);
                nullLay.setVisibility(datas.isEmpty() ? View.VISIBLE : View.GONE);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.backBtn) {
            finish();
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
        ArrayList<WalletHistoryData.Result> items;
        Context context;

        public RecyclerViewAdapter(Context context, ArrayList<WalletHistoryData.Result> items) {
            this.items = items;
            this.context = context;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_item, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            WalletHistoryData.Result data = items.get(position);
            if ("credit".equals(data.type)) {
                holder.image.setImageResource(R.drawable.up);
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.green));
            } else {
                holder.image.setImageResource(R.drawable.down);
                holder.amount.setTextColor(ContextCompat.getColor(context, R.color.red));
            }
            holder.amount.setText(Constants.currency_symbol + " " + data.amount);
            if ("addmoney".equals(data.transaction)) {
                holder.transaction.setText(context.getString(R.string.addmoney));
            } else {
                holder.transaction.setText(data.transaction);
            }
            holder.date.setText(ApplicationClass.getRideTimeFromTimeStamp(context, Long.parseLong(data.date)));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView transaction, amount, date;
            ImageView image;

            MyViewHolder(View view) {
                super(view);
                image = view.findViewById(R.id.image);
                transaction = view.findViewById(R.id.transaction);
                amount = view.findViewById(R.id.amount);
                date = view.findViewById(R.id.date);
            }
        }
    }
}
