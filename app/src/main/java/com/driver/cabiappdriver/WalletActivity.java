package com.driver.cabiappdriver;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.driver.helper.LocaleManager;
import com.driver.helper.NetworkReceiver;
import com.driver.model.UserData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WalletActivity extends BaseActivity implements NetworkReceiver.ConnectivityReceiverListener, View.OnClickListener {
    private static final String TAG = WalletActivity.class.getSimpleName();
    private static final int SELCOM_REQUEST = 999;

    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;

    ImageView backBtn;
    TextView titleB, invite, moneyUsing, history, walletcurrency, plans;
    EditText walletEdit;
    ApiInterface apiInterface;
    ProgressDialog dialog;
    NetworkReceiver receiver;
    public static FrameLayout mAdView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wallet);

        backBtn = findViewById(R.id.backBtn);
        titleB = findViewById(R.id.titleB);
        invite = findViewById(R.id.invite);
        walletEdit = findViewById(R.id.walletEdit);
        moneyUsing = findViewById(R.id.moneyUsing);
        history = findViewById(R.id.history);
        plans = findViewById(R.id.plans);
        walletcurrency = findViewById(R.id.walletcurrency);
        mAdView = findViewById(R.id.adLay);

        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        receiver = new NetworkReceiver();
        ApplicationClass.getInstance().setConnectivityListener(this);

        backBtn.setVisibility(View.VISIBLE);
        history.setVisibility(View.VISIBLE);
        plans.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        invite.setOnClickListener(this);
        history.setOnClickListener(this);
        plans.setOnClickListener(this);
        walletcurrency.setText(Constants.currency_symbol);

        dialog = new ProgressDialog(this);
        dialog.setMessage(getString(R.string.pleasewait));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }

        renderBalance(GetSet.getWallet());
    }

    private void renderBalance(String amount) {
        String balance = amount == null || amount.trim().isEmpty() ? "0.00" : amount;
        linkTextView(this, titleB, getString(R.string.balance), Constants.currency_symbol + " " + balance, "title");
        linkTextView(this, moneyUsing, getString(R.string.money_using), getString(R.string.help), "moneydes");
    }

    private void getSelComUrl() {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("amount", walletEdit.getText().toString().trim());
        Log.v(TAG, "getSelComUrlParams=" + map);
        Call<UserData> call = apiInterface.getSelComUrl(GetSet.getToken(), map);
        call.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    UserData data = response.body();
                    if (data != null && "true".equals(data.status)) {
                        Intent intent = new Intent(WalletActivity.this, ActivityWebView.class);
                        intent.putExtra("webview_url", data.payment_url);
                        intent.putExtra("return_url", data.return_url);
                        intent.putExtra("transid", data.transid);
                        intent.putExtra("amount", walletEdit.getText().toString().trim());
                        startActivityForResult(intent, SELCOM_REQUEST);
                    } else if (data != null) {
                        Toast.makeText(WalletActivity.this, data.message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(WalletActivity.this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                call.cancel();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void payBySelcom(String transId, String amount) {
        HashMap<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("amount", amount);
        map.put("paynonce", transId);
        map.put("type", "wallet");

        Log.v(TAG, "payBySelcomParams=" + map);
        Call<Map<String, String>> call = apiInterface.paybyselcom(GetSet.getToken(), map);
        call.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                try {
                    Map<String, String> data = response.body();
                    if (data != null && "true".equals(data.get("status"))) {
                        String walletMoney = data.get("walletmoney");
                        walletEdit.setText("");
                        editor.putString("wallet", walletMoney);
                        editor.commit();
                        GetSet.setWallet(walletMoney);
                        renderBalance(walletMoney);
                        Toast.makeText(WalletActivity.this, getString(R.string.amount_credit_success), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(WalletActivity.this, data != null && data.get("message") != null ? data.get("message") : getString(R.string.amount_credit_unsuccess), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        });
    }

    private void linkTextView(final Context context, TextView view, final String desc, final String appendTxt, final String from) {
        SpannableStringBuilder spanTxt = new SpannableStringBuilder(desc + " ");
        int count = spanTxt.length();
        spanTxt.append(appendTxt);
        spanTxt.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                if ("moneydes".equals(from)) {
                    Intent helpIntent = new Intent(WalletActivity.this, HelpActivity.class);
                    startActivity(helpIntent);
                }
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                ds.setUnderlineText(false);
                if ("moneydes".equals(from)) {
                    ds.setColor(ContextCompat.getColor(context, R.color.green));
                } else {
                    ds.setColor(ContextCompat.getColor(context, R.color.red));
                }
            }
        }, count, spanTxt.length(), 0);

        view.setMovementMethod(LinkMovementMethod.getInstance());
        view.setText(spanTxt, TextView.BufferType.SPANNABLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELCOM_REQUEST && resultCode == RESULT_OK && data != null) {
            String transId = data.getStringExtra("transid");
            String paidAmount = data.getStringExtra("paidamount");
            if (transId != null && paidAmount != null) {
                dialog.show();
                payBySelcom(transId, paidAmount);
            }
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.backBtn) {
            finish();
        } else if (id == R.id.invite) {
            ApplicationClass.preventMultipleClick(invite);
            ApplicationClass.requestFocus(WalletActivity.this, walletEdit, false);
            if (walletEdit.getText().toString().trim().length() == 0) {
                Toast.makeText(WalletActivity.this, getString(R.string.enter_money), Toast.LENGTH_SHORT).show();
            } else {
                dialog.show();
                getSelComUrl();
            }
        } else if (id == R.id.history) {
            Intent walletHistory = new Intent(this, WalletHistory.class);
            startActivity(walletHistory);
        } else if (id == R.id.plans) {
            Intent subscriptionPlans = new Intent(this, SubscriptionPlansActivity.class);
            startActivity(subscriptionPlans);
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.activity_help), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, ApplicationClass.intentFilter);
        if (NetworkReceiver.isConnected()) {
            ApplicationClass.showSnack(this, findViewById(R.id.activity_help), true);
        } else {
            ApplicationClass.showSnack(this, findViewById(R.id.activity_help), false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_help), true);
    }
}
