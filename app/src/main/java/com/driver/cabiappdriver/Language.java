package com.driver.cabiappdriver;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.driver.helper.LocaleManager;
import com.driver.helper.SharedPrefManager;

import com.driver.cabiappdriver.databinding.ActivityLangBinding;
import com.driver.cabiappdriver.databinding.HelpItemLayBinding;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Language extends BaseActivity {

    private static final String TAG = Language.class.getSimpleName();
    ActivityLangBinding langBinding;
    ImageView backBtn;
    ArrayList<String> langAry = new ArrayList<>();
    LangAdapter langAdapter;
    private ApiInterface apiInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        langBinding = DataBindingUtil.setContentView(this, R.layout.activity_lang);
        backBtn = findViewById(R.id.backBtn);
        backBtn.setVisibility(View.VISIBLE);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        langAry.add("English-en");
       // langAry.add("العربية-ar");
        langBinding.helpList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        langBinding.helpList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        langAdapter = new LangAdapter(this, langAry);
        langBinding.helpList.setAdapter(langAdapter);

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public class LangAdapter extends RecyclerView.Adapter<LangAdapter.HelpViewHolder> {
        ArrayList<String> helpTitleLists;
        Context context;

        public LangAdapter(Context context, ArrayList<String> helpTitleLists) {
            this.helpTitleLists = helpTitleLists;
            this.context = context;
        }

        @Override
        public HelpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HelpItemLayBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.help_item_lay, parent, false);
            return new HelpViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(HelpViewHolder viewHolder, int position) {
            Log.v("Tag", "helpList=" + helpTitleLists);
            String lang[] = helpTitleLists.get(position).split("-");
            String langTitle = lang[0];
            String langCode = lang[1];
            viewHolder.getBinding().helpTitle.setText(langTitle);
            viewHolder.getBinding().mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    LocaleManager.setNewLocale(Language.this, langCode, langTitle);
                    langAdapter.notifyDataSetChanged();
                    addDeviceId(Language.this);
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);

                }
            });

            if (langCode.equals(LocaleManager.getLanguageCode(context))) {
                viewHolder.getBinding().tick.setVisibility(View.VISIBLE);
            } else {
                viewHolder.getBinding().tick.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return helpTitleLists.size();
        }

        public class HelpViewHolder extends RecyclerView.ViewHolder {
            HelpItemLayBinding helpItemLayBinding;

            public HelpViewHolder(HelpItemLayBinding layoutBinding) {
                super(layoutBinding.getRoot());
                helpItemLayBinding = layoutBinding;
            }

            public HelpItemLayBinding getBinding() {
                return helpItemLayBinding;
            }
        }
    }

    private void addDeviceId(final Context context) {

        final String token = SharedPrefManager.getInstance(context).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(context.getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("device_token", token);
        map.put("device_type", "1");
        map.put("device_id", deviceId);
        map.put("lang_type", LocaleManager.getLanguageCode(this));

        Log.v(TAG, "addDeviceIdParams= " + map);
        Call<Map<String, String>> call3 = apiInterface.pushsignin(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v(TAG, "addDeviceIdResponse= " + data);
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                t.printStackTrace();
                call.cancel();
            }
        });

    }
}
