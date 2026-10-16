package com.driver.cabiappdriver;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.driver.model.UserData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
/*import com.hbb20.CountryCodePicker;*/

import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityEnterPhoneNumber extends BaseActivity implements View.OnClickListener/*, TextureView.SurfaceTextureListener*/ {

    private static final String TAG = ActivityEnterPhoneNumber.class.getSimpleName();
    ImageView ccp;
    EditText phonenumber, text1, text2, text3, text4, text5, text6;
    int exit = 0;
    AppCompatButton btngetotp, verify;
    LinearLayout layout_one, layout_two;
    ProgressDialog progressDialog;
    ApiInterface apiInterface;
    UserData userdata;
    String txtOTP ="";
    AppCompatTextView txt_resend_otp, txt_phonenumber_entered, yourOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phonenumber);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        ccp = findViewById(R.id.ccp);
        int radius = 12;
        RequestOptions options = new RequestOptions()
                .transforms(new RoundedCorners(radius));
        Glide.with(this)
                .load(R.drawable.flag_tanzania)
                .apply(options)
                .into(ccp);
        phonenumber = findViewById(R.id.phonenumber);
        btngetotp = findViewById(R.id.btngetotp);
        layout_two = findViewById(R.id.layout_two);
        layout_one = findViewById(R.id.layout_one);
        txt_resend_otp = findViewById(R.id.txt_resend_otp);
        yourOTP = findViewById(R.id.yourOTP);
        txt_phonenumber_entered = findViewById(R.id.txt_phonenumber_entered);
        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        text3 = findViewById(R.id.text3);
        text4 = findViewById(R.id.text4);
        text5 = findViewById(R.id.text5);
        text6 = findViewById(R.id.text6);
        verify = findViewById(R.id.verify);
        btngetotp.setOnClickListener(this);
        verify.setOnClickListener(this);
        txt_resend_otp.setOnClickListener(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.pleasewait));
        progressDialog.setCancelable(false);

        text1.addTextChangedListener(new OtpTextWatcher(text1, text2));
        text2.addTextChangedListener(new OtpTextWatcher(text2, text3));
        text3.addTextChangedListener(new OtpTextWatcher(text3, text4));
        text4.addTextChangedListener(new OtpTextWatcher(text4, text5));
        text5.addTextChangedListener(new OtpTextWatcher(text5, text6));
        text6.addTextChangedListener(new OtpTextWatcher(text6, null));

        // OnKeyListener for moving back to the previous EditText when the backspace key is pressed
        text1.setOnKeyListener(new GenericKeyEvent(text1, null));
        text2.setOnKeyListener(new GenericKeyEvent(text2, text1));
        text3.setOnKeyListener(new GenericKeyEvent(text3, text2));
        text4.setOnKeyListener(new GenericKeyEvent(text4,text3));
        text5.setOnKeyListener(new GenericKeyEvent(text5,text4));
        text6.setOnKeyListener(new GenericKeyEvent(text6,text5));

        /*phonenumber.setText("+255 ");
        phonenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(Editable s) {
                // Remove the default country code if the user deletes it
                if (s.length() == 0) {
                    phonenumber.setText("+255 ");
                }
            }
        });
*/
        /*SpannableString countryCode = new SpannableString("+255 ");
        countryCode.setSpan(new ForegroundColorSpan(Color.BLACK), 0, countryCode.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //countryCode.setSpan(new StyleSpan(Typeface.BOLD), 0, countryCode.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        phonenumber.setText(countryCode);
        phonenumber.setSelection(countryCode.length());
        phonenumber.setFilters(new InputFilter[] { new InputFilter() {
            @Override
            public CharSequence filter(CharSequence charSequence, int i, int i1, Spanned spanned, int i2, int i3) {
                if (i2 < countryCode.length()) {
                    return "";
                }
                return null;
            }
        }});*/
    }

    public class OtpTextWatcher implements TextWatcher {
        private EditText currentView;
        private EditText nextView;

        public OtpTextWatcher(EditText currentView, EditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s != null && s.length() == 1) {
                if (nextView != null) {
                    nextView.requestFocus();
                    currentView.setBackgroundResource(R.drawable.newbg_button_grayfilledoutline);
                    nextView.setBackgroundResource(R.drawable.newbg_button_pinkfilledoutline);
                }
            }
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // No action needed here
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No action needed here
        }
    }

    public class GenericKeyEvent implements View.OnKeyListener {
        private EditText currentView;
        private EditText previousView;

        public GenericKeyEvent(EditText currentView, EditText previousView) {
            this.currentView = currentView;
            this.previousView = previousView;
        }

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL && currentView.getId() != R.id.text1 && currentView.getText().toString().isEmpty()) {
                if (previousView != null) {
                    previousView.setText(null);
                    previousView.requestFocus();
                    currentView.setBackgroundResource(R.drawable.newbg_button_grayfilledoutline);
                    previousView.setBackgroundResource(R.drawable.newbg_button_pinkfilledoutline);
                }
                return true;
            }
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btngetotp) {
            if (!phonenumber.getText().toString().isEmpty()) {
                getOtp(phonenumber.getText().toString().trim());
            } else {
                Toast.makeText(getApplicationContext(), getString(R.string.txt_enter_your_mobile_number), Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.txt_resend_otp) {
            getOtp(phonenumber.getText().toString().trim());
        } else if (id == R.id.verify) {
            txtOTP = text1.getText().toString().trim()
                    + text2.getText().toString().trim()
                    + text3.getText().toString().trim()
                    + text4.getText().toString().trim()
                    + text5.getText().toString().trim()
                    + text6.getText().toString().trim();

            if (txtOTP.length() == 6) {
                verifyOtp(phonenumber.getText().toString().trim(), txtOTP);
            } else {
                Toast.makeText(ActivityEnterPhoneNumber.this, getString(R.string.otp_empty), Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void getOtp(String phoneNumber){
        progressDialog.show();
        HashMap<String, String> map = new HashMap<>();
        String normalizedPhoneNumber = normalizeTanzaniaPhoneNumber(phoneNumber);
        map.put(Constants.TAG_PHONE_NUMBER, normalizedPhoneNumber);
        Log.e("inputparam", "-" + map);

        Call<UserData> call3 = apiInterface.getOTP(map);
        call3.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                try {
                    userdata = response.body();

                    if (!response.isSuccessful() || userdata == null) {
                        String message = "OTP service is unavailable";
                        if (response.code() > 0) {
                            message = "OTP request failed (" + response.code() + ")";
                        }
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "getOtpFailed: code=" + response.code() + ", body=" + response.body());
                        return;
                    }

                    if ("true".equals(userdata.status)) {
                        Toast.makeText(getApplicationContext(), userdata.message, Toast.LENGTH_SHORT).show();
                        layout_one.setVisibility(View.GONE);
                        layout_two.setVisibility(View.VISIBLE);
                     //   String dynamicText = userdata.country_code + phoneNumber;
                        String dynamicText = "(+255" + " "+phoneNumber+")";

                        SpannableString spannableString = new SpannableString(getString(R.string.txt_entercode) + " "+dynamicText);
                        int startIndex = getString(R.string.txt_entercode).length(); // Start index of dynamic text
                        int endIndex = spannableString.length(); // End index of dynamic text
                        spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        spannableString.setSpan(new ForegroundColorSpan(getResources().getColor(android.R.color.black)), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        txt_phonenumber_entered.setText(spannableString);

                    } else if ("false".equals(userdata.status)) {
                        Toast.makeText(getApplicationContext(), userdata.message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Unexpected OTP response", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Log.v(TAG, "signInError: " + t.getMessage());
                call.cancel();
                if (progressDialog.isShowing())
                    progressDialog.dismiss();
            }
        });
    }

    private void verifyOtp(String phoneNumber, String otp) {
        progressDialog.show();
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_PHONE_NUMBER, normalizeTanzaniaPhoneNumber(phoneNumber));
        map.put("otp", otp);

        apiInterface.verifyOTP(map).enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }

                UserData data = response.body();
                if (response.isSuccessful() && data != null && "true".equals(data.status)) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("countrycode", "+255");
                    resultIntent.putExtra("phonenumber", phonenumber.getText().toString().trim());
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                    return;
                }

                String message = data != null && data.message != null
                        ? data.message
                        : getString(R.string.otp_incorrect);
                Toast.makeText(ActivityEnterPhoneNumber.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                call.cancel();
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                Toast.makeText(ActivityEnterPhoneNumber.this, "OTP verification is unavailable", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String normalizeTanzaniaPhoneNumber(String phoneNumber) {
        String normalized = phoneNumber == null ? "" : phoneNumber.trim().replace(" ", "");

        if (normalized.startsWith("+")) {
            normalized = normalized.substring(1);
        }

        if (normalized.startsWith("255")) {
            return normalized;
        }

        if (normalized.startsWith("0")) {
            return "255" + normalized.substring(1);
        }

        if (normalized.length() == 9) {
            return "255" + normalized;
        }

        return normalized;
    }


    @Override
    public void onBackPressed() {
        ActivityEnterPhoneNumber.this.finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

}
