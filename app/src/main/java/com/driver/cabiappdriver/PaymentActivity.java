package com.driver.cabiappdriver;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.driver.helper.LocaleManager;
import com.driver.cabiappdriver.databinding.ActivityPaymentBinding;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;


import org.json.JSONObject;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 22/3/18.
 */

public class PaymentActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = PaymentActivity.class.getSimpleName();
    public static String routing_no = "", bank_ac = "", name = "", address = "";
    static ApiInterface apiInterface;
    ImageView backBtn;
    ActivityPaymentBinding paymentBinding;
    String type = "cash", from;
    FrameLayout mAdView;
    RelativeLayout bannerLay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        paymentBinding = DataBindingUtil.setContentView(this, R.layout.activity_payment);
        backBtn = findViewById(R.id.backBtn);
        bannerLay = findViewById(R.id.bannerlay);
        mAdView = findViewById(R.id.adLay);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        paymentBinding.braintreeSelectedView.setVisibility(View.INVISIBLE);
        paymentBinding.bankAccSelectedView.setVisibility(View.INVISIBLE);
        if (!VehicleInspectionActivity.paymentdetail.equals("InComplete")) {
            getPaymentInfo();
        }

        if (LocaleManager.isRTL()){
            backBtn.setRotation(180);
            paymentBinding.icon.setRotation(90);
        } else {
            backBtn.setRotation(0);
            paymentBinding.icon.setRotation(270);
        }
        from = getIntent().getStringExtra("from");
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        paymentBinding.braintreeLay.setOnClickListener(this);
        paymentBinding.bankAccLay.setOnClickListener(this);
        paymentBinding.nextBtn.setOnClickListener(this);




        RelativeLayout.LayoutParams banerparams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        banerparams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        if (Constants.GOOGLE_ADS) {
            loadAd();
        }
    }

//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.braintreeLay:
//                type = "bankaccount";
//                paymentBinding.braintreeSelectedView.setVisibility(View.VISIBLE);
//                paymentBinding.bankAccSelectedView.setVisibility(View.INVISIBLE);
//                Intent paypalIntent = new Intent(this, AddBraintreeActivity.class);
//                startActivity(paypalIntent);
//                break;
//            case R.id.bankAccLay:
//                type = "cash";
//                paymentBinding.braintreeSelectedView.setVisibility(View.INVISIBLE);
//                paymentBinding.bankAccSelectedView.setVisibility(View.VISIBLE);
//                break;
//            case R.id.backBtn:
//                onBackPressed();
//                break;
//            case R.id.nextBtn:
//                ApplicationClass.preventMultipleClick(view);
//                if (type.equals("bankaccount") && (routing_no.equals("") || bank_ac.equals("") || name.equals("") || address.equals("")))
//                    Toast.makeText(PaymentActivity.this, getString(R.string.braintree_details), Toast.LENGTH_SHORT).show();
//                else postPaymentInfo();
//                break;
//        }
//    }
@Override
public void onClick(View view) {
    int id = view.getId();

    if (id == R.id.braintreeLay) {
        type = "bankaccount";
        paymentBinding.braintreeSelectedView.setVisibility(View.VISIBLE);
        paymentBinding.bankAccSelectedView.setVisibility(View.INVISIBLE);
        Intent paypalIntent = new Intent(this, AddBraintreeActivity.class);
        startActivity(paypalIntent);
    } else if (id == R.id.bankAccLay) {
        type = "cash";
        paymentBinding.braintreeSelectedView.setVisibility(View.INVISIBLE);
        paymentBinding.bankAccSelectedView.setVisibility(View.VISIBLE);
    } else if (id == R.id.backBtn) {
        onBackPressed();
    } else if (id == R.id.nextBtn) {
        ApplicationClass.preventMultipleClick(view);
        if (type.equals("bankaccount")) {
            if (bank_ac.isEmpty() || name.isEmpty() || address.isEmpty()) {
                Toast.makeText(PaymentActivity.this, getString(R.string.braintree_details), Toast.LENGTH_SHORT).show();
            } else {
                postPaymentInfo();
            }
        } else {
            postPaymentInfo();
        }
    }
}


//    private void postPaymentInfo() {
//        HashMap<String, String> map = new HashMap<>();
//        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
//        map.put(Constants.TAG_TYPE, type);
//        if (type.equals("bankaccount")) {
//            map.put("routing_no", routing_no);
//            map.put("bank_ac", bank_ac);
//            map.put(Constants.TAG_NAME, name);
//            map.put(Constants.TAG_ADDRESS, address);
//        }
//        Log.d(TAG, "postPaymentInfo: " + new JSONObject(map));
//        Call<HashMap<String, String>> call3 = apiInterface.paymentinfo(GetSet.getToken(), map);
//        call3.enqueue(new Callback<HashMap<String, String>>() {
//            @Override
//            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
//                try {
//                    HashMap<String, String> data = response.body();
//                    if (data.get(Constants.TAG_STATUS).equals("true")) {
//                        if (from != null && from.equals("main")) {
//                            finish();
//                        } else {
//                            VehicleInspectionActivity.paymentdetail = "Complete";
//                            Intent nextIntent = new Intent(PaymentActivity.this, VehicleInspAndSecurityActivity.class);
//                            nextIntent.putExtra("from", "vehicleinspection");
//                            startActivity(nextIntent);
//                        }
//                    } else {
//                        Toast.makeText(getApplicationContext(), data.get(Constants.TAG_MESSSAGE), Toast.LENGTH_LONG).show();
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//
//            @Override
//            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
//                Log.e(TAG, "postPaymentInfo: " + t.getMessage());
//                call.cancel();
//            }
//        });
//    }
private void postPaymentInfo() {
    HashMap<String, String> map = new HashMap<>();
    map.put(Constants.TAG_USER_ID, GetSet.getUserId());
    map.put(Constants.TAG_TYPE, type);
    if (type.equals("bankaccount")) {
        map.put("bank_ac", bank_ac);
        map.put(Constants.TAG_NAME, name);
        map.put(Constants.TAG_ADDRESS, address);
        // Set a default or empty value for routing number if required
        map.put("routing_no", "");  // or you could use a default value if necessary
    }
    Log.d(TAG, "postPaymentInfo: " + new JSONObject(map));
    Call<HashMap<String, String>> call3 = apiInterface.paymentinfo(GetSet.getToken(), map);
    call3.enqueue(new Callback<HashMap<String, String>>() {
        @Override
        public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
            try {
                HashMap<String, String> data = response.body();
                if (data.get(Constants.TAG_STATUS).equals("true")) {
                    // Handle successful response
                    if (from != null && from.equals("main")) {
                            finish();
                        } else {
                            VehicleInspectionActivity.saveSectionStatus("payment_details", "Complete");
                            Intent nextIntent = new Intent(PaymentActivity.this, VehicleInspectionActivity.class);
                            nextIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                            startActivity(nextIntent);
                            finish();
                        }
                }
                else {
//                    Toast.makeText(getApplicationContext(), data.get(Constants.TAG_MESSAGE), Toast.LENGTH_LONG).show();
                    Toast.makeText(getApplicationContext(), data.get("message"), Toast.LENGTH_LONG).show(); // Replace with the expected key

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
            Log.e(TAG, "postPaymentInfo: " + t.getMessage());
            call.cancel();
        }
    });
}


    private void getPaymentInfo() {
        Call<HashMap<String, String>> call3 = apiInterface.getPaymentinfo(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                try {
                    HashMap<String, String> data = response.body();
                    if (data.get(Constants.TAG_STATUS).equals("true")) {
                        if (data.get(Constants.TAG_TYPE).equals("cash")) {
                            type = "cash";
                            paymentBinding.braintreeSelectedView.setVisibility(View.INVISIBLE);
                            paymentBinding.bankAccSelectedView.setVisibility(View.VISIBLE);
                        } else {
                            type = "bankaccount";
                            paymentBinding.braintreeSelectedView.setVisibility(View.VISIBLE);
                            paymentBinding.bankAccSelectedView.setVisibility(View.INVISIBLE);
                            routing_no = data.get(Constants.TAG_ROUTING_NO);
                            bank_ac = data.get(Constants.TAG_BANK_AC);
                            name = data.get(Constants.TAG_NAME);
                            address = data.get(Constants.TAG_ADDRESS);
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), data.get(Constants.TAG_MESSSAGE), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }
    private void loadAd() {
        //Addon for googleads
                //BannerAdUtils.loadBannerAd(PaymentActivity.this, mAdView);
    }
}
