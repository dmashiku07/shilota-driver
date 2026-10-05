package com.driver.cabiappdriver;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;

import com.driver.helper.LocaleManager;
import com.driver.helper.NetworkReceiver;
import com.driver.helper.SharedPrefManager;
import com.driver.cabiappdriver.databinding.ActivityLoginBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import com.driver.model.ProfileData;
import com.driver.model.UserData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends BaseActivity implements View.OnClickListener,
        NetworkReceiver.ConnectivityReceiverListener {
    private static final String TAG = LoginActivity.class.getSimpleName();
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static int APP_REQUEST_CODE = 99;
    ApiInterface apiInterface;
    Snackbar snackbar;
    ActivityLoginBinding binding;
    NetworkReceiver receiver;
    ImageView backBtn;
    String phoneNumber = "", countryCode = "";
    ProgressDialog progressDialog;
    Dialog forgotdialog;
    UserData userdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        receiver = new NetworkReceiver();

        backBtn = findViewById(R.id.backBtn);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        backBtn.setVisibility(View.VISIBLE);
        backBtn.setOnClickListener(this);
        binding.singnin.setOnClickListener(this);
        binding.createacc.setOnClickListener(this);
        binding.forgotpassword.setOnClickListener(this);
        binding.rideLay.setOnClickListener(this);
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        ApplicationClass.getInstance().setConnectivityListener(this);
      /*
        snackbar = Snackbar.make(findViewById(R.id.VideoView), getString(R.string.exit_msg), Snackbar.LENGTH_SHORT);
        snackbar.addCallback(new Snackbar.Callback() {

            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                exit = 0;
            }

            @Override
            public void onShown(Snackbar snackbar) {
                exit = 1;
            }
        });

   */
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, ApplicationClass.intentFilter);
        if (NetworkReceiver.isConnected()) {
            ApplicationClass.showSnack(this, findViewById(R.id.activity_login), true);
        } else {
            ApplicationClass.showSnack(this, findViewById(R.id.activity_login), false);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_login), true);
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
                finish();
                Intent b = new Intent(LoginActivity.this, MainActivity.class);
                b.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(b);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                progressDialog.dismiss();
            }
        });

    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.activity_login), isConnected);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            String countrycode = data.getStringExtra("countrycode");
            String phonenumber = data.getStringExtra("phonenumber");
            Log.e(TAG,"PHONE NUMBER : " + countrycode +"," + phonenumber);
            changePhoneNumber(countrycode, phonenumber);
         /*   if (resultCode == RESULT_OK) {
                // Successfully signed in
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getPhoneNumber() != null) {
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    Log.d(TAG, "onActivityResult: " + user.getPhoneNumber());
                    try {
                        Phonenumber.PhoneNumber numberProto = phoneUtil.parse(user.getPhoneNumber(), null);

                        changePhoneNumber("" + numberProto.getCountryCode(), "" + numberProto.getNationalNumber());

                    } catch (NumberParseException e) {
                        Log.d(TAG, "NumberParseException: " + e.getMessage());
                        ApplicationClass.showToast(LoginActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT);
                        finish();
                    }
                }
                AuthUI.getInstance()
                        .signOut(this)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {

                            }
                        });
            }
*/
        }
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.backBtn) {
            onBackPressed();

        } else if (id == R.id.createacc) {
            Intent b = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(b);

        } else if (id == R.id.singnin) {
            binding.passwordlay.setError(null);
            binding.emaillay.setError(null);
            if (binding.email.getText().toString().equals("")) {
                binding.emaillay.setError(getString(R.string.emailempty_error));
                requestFocus(binding.email);
            } else if (!binding.email.getText().toString().matches(Constants.EMAIL_PATTERN)) {
                binding.emaillay.setError(getString(R.string.email_error));
                requestFocus(binding.email);
            } else if (binding.password.getText().toString().equals("")) {
                binding.passwordlay.setError(getString(R.string.passwordempty_error));
                requestFocus(binding.password);
            } else {
                signin();
            }

        } else if (id == R.id.forgotpassword) {
            final ImageView dbackBtn;
            forgotdialog = new Dialog(this, R.style.DialogStyle);
            forgotdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            forgotdialog.setContentView(R.layout.forgot_dialog);

            dbackBtn = forgotdialog.findViewById(R.id.backBtn);
            dbackBtn.setVisibility(View.VISIBLE);
            forgotdialog.setCancelable(true);

            final TextView getOtp = forgotdialog.findViewById(R.id.getotp);
            final TextView reset = forgotdialog.findViewById(R.id.reset);
            final EditText phone = forgotdialog.findViewById(R.id.phone);
            final EditText otp = forgotdialog.findViewById(R.id.otp);
            final EditText password = forgotdialog.findViewById(R.id.password);
            final TextInputLayout phonelay = forgotdialog.findViewById(R.id.phonelay);
            final TextInputLayout otplay = forgotdialog.findViewById(R.id.otplay);
            final TextInputLayout passwordlay = forgotdialog.findViewById(R.id.passwordlay);
            final String[] returnedOtp = {""};

            phone.setFocusable(true);
            if (LocaleManager.isRTL()) {
                dbackBtn.setRotation(180);
            } else {
                dbackBtn.setRotation(0);
            }
            dbackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    forgotdialog.dismiss();
                }
            });
            reset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (phone.getText().toString().trim().equals("")) {
                        phonelay.setError(getString(R.string.forgot_password_phone_error));
                        requestFocus(phone);
                    } else if (phone.getText().toString().trim().length() < 9) {
                        phonelay.setError(getString(R.string.forgot_password_phone_not_valid));
                        requestFocus(phone);
                    } else if (returnedOtp[0].equals("")) {
                        Toast.makeText(LoginActivity.this, getString(R.string.forgot_password_get_otp_first), Toast.LENGTH_SHORT).show();
                    } else if (otp.getText().toString().trim().equals("")) {
                        otplay.setError(getString(R.string.otp_empty));
                        requestFocus(otp);
                    } else if (!otp.getText().toString().trim().equals(returnedOtp[0])) {
                        otplay.setError(getString(R.string.otp_incorrect));
                        requestFocus(otp);
                    } else if (password.getText().toString().trim().equals("")) {
                        passwordlay.setError(getString(R.string.passwordempty_error));
                        requestFocus(password);
                    } else if (password.getText().toString().trim().length() < 6) {
                        passwordlay.setError(getString(R.string.password_error));
                        requestFocus(password);
                    } else {
                        ApplicationClass.hideSoftKeyboard(LoginActivity.this, v);
                        progressDialog.show();
                        forgotPassword(phone.getText().toString().trim(), otp.getText().toString().trim(), password.getText().toString().trim());
                    }
                }
            });
            getOtp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (phone.getText().toString().trim().equals("")) {
                        phonelay.setError(getString(R.string.forgot_password_phone_error));
                        requestFocus(phone);
                    } else if (phone.getText().toString().trim().length() < 9) {
                        phonelay.setError(getString(R.string.forgot_password_phone_not_valid));
                        requestFocus(phone);
                    } else {
                        ApplicationClass.hideSoftKeyboard(LoginActivity.this, v);
                        requestOtpForPasswordReset(phone.getText().toString().trim(), returnedOtp);
                    }
                }
            });
            if (!forgotdialog.isShowing()) {
                forgotdialog.show();
            }

        } else if (id == R.id.rideLay) {
            String packageName = getString(R.string.rider_package_name);

            Intent intent = getPackageManager().getLaunchIntentForPackage(packageName);

            if (intent == null) {
                try {
                    // if play store installed, open play store, else open browser
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName));
                } catch (Exception e) {
                    intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packageName));
                }
            }
            startActivity(intent);
        }
    }


    public void verifyMobileNo() {
        // Choose authentication providers
        List<AuthUI.IdpConfig> providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build());
        // Create and launch sign-in intent
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setTheme(R.style.OTPTheme)
                        .build(),
                APP_REQUEST_CODE);
    }

    private void requestOtpForPasswordReset(String phoneNumber, final String[] returnedOtp) {
        progressDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put("phone_number", phoneNumber);
        map.put("purpose", "forgot_password");
        Call<UserData> call3 = apiInterface.getDriverOTP(map);
        call3.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                UserData data = response.body();
                if (data != null && "true".equals(data.status)) {
                    returnedOtp[0] = data.otp == null ? "" : data.otp;
                    Toast.makeText(LoginActivity.this, data.message, Toast.LENGTH_SHORT).show();
                } else if (data != null) {
                    Toast.makeText(LoginActivity.this, data.message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                call.cancel();
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
    }

    private void forgotPassword(String phoneNumber, String otp, String newPassword) {
        Map<String, String> map = new HashMap<>();
        map.put("phone_number", phoneNumber);
        map.put("otp", otp);
        map.put("new_password", newPassword);
        Call<HashMap<String, String>> call3 = apiInterface.resetPasswordByOtp(map);
        call3.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (response.isSuccessful()) {
                    HashMap<String, String> data = response.body();
                    if (data.get(Constants.TAG_STATUS).equals("true")) {
                        Toast.makeText(LoginActivity.this, data.get(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                        if (forgotdialog.isShowing()) {
                            forgotdialog.dismiss();
                            if (progressDialog !=null && progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        }
                    } else if (data.get(Constants.TAG_MESSSAGE).equalsIgnoreCase("Account Deleted.")) {
                        if (progressDialog != null && progressDialog.isShowing()) {
                            progressDialog.dismiss();
                        }

                            Toast.makeText(getApplicationContext(), getString(R.string.accountdeleted), Toast.LENGTH_LONG).show();

                    }
                        else {
                            if (progressDialog!=null & progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                            Toast.makeText(LoginActivity.this, data.get(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                        }
                    }
               /* } else {
                    Toast.makeText(LoginActivity.this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                }*/

            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void changePhoneNumber(String countryCode,String phoneNumber) {
        progressDialog.show();
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, userdata.user_id);
        map.put(Constants.TAG_COUNTRY_CODE, countryCode);
        map.put(Constants.TAG_PHONE_NUMBER, phoneNumber);
        Log.d(TAG, "changePhoneNumberParams: " + map);
        Call<ProfileData> call3 = apiInterface.updateProfile(userdata.token, map);
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                try {
                    ProfileData data = response.body();
                    if (data.status.equals("true")) {
                        GetSet.Logingin(userdata.user_id, binding.email.getText().toString(), userdata.full_name, userdata.imageurl, userdata.token, userdata.walletmoney);
                        editor.putBoolean("isLogged", true);
                        editor.putString("userId", userdata.user_id);
                        editor.putString("email", binding.email.getText().toString());
                        editor.putString("userImage", userdata.imageurl);
                        editor.putString("fullName", userdata.full_name);
                        editor.putString("token", userdata.token);
                        editor.putString("wallet", userdata.walletmoney);
                        editor.putString("password", userdata.password);
                        editor.putString("admin_approved", userdata.admin_approved);
                        editor.putString("vehicle_approval", userdata.vehicle_approval);
                        editor.putString("vehicle_details", userdata.vehicle_details);
                        editor.putString("payment_details", userdata.payment_details);
                        editor.putString("security_deposit", userdata.security_deposit);
                        editor.putString("vehicle_inspection", userdata.vehicle_inspection);
                        editor.commit();
                        GetSet.setPassword(userdata.password);
                        GetSet.setToken(userdata.token);
                        GetSet.setWallet(userdata.walletmoney);


                        VehicleInspectionActivity.vehicledetail = userdata.vehicle_details;
                        VehicleInspectionActivity.paymentdetail = userdata.payment_details;
                        VehicleInspectionActivity.securitydeposit = userdata.security_deposit;
                        VehicleInspectionActivity.vehicleinspection = userdata.vehicle_inspection;
                        VehicleInspectionActivity.vehicleApprove = userdata.vehicle_approval;

                        if (userdata.admin_approved.equals("true")) {
                            switch (userdata.vehicle_approval) {
                                case "approved": {
                                    addDeviceId(LoginActivity.this);
                                    break;
                                }
                                case "rejected": {
                                    Intent nextIntent = new Intent(getApplicationContext(), VehicleInspectionActivity.class);
                                    startActivity(nextIntent);
                                    break;
                                }
                                case "pending:": {
                                    Intent nextIntent = new Intent(getApplicationContext(), VehicleInspectionActivity.class);
                                    startActivity(nextIntent);
                                    break;
                                }
                                default:
                                    break;
                            }
                        } else {
                            SharedPreferences pref = LoginActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.clear();
                            editor.commit();
                            GetSet.logout();
                            showErrorDialog(getString(R.string.account_deactivate_error),"");
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_name_unsuccessful), Toast.LENGTH_LONG).show();

                    }
                } catch (
                        Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                Log.e(TAG, "changePhoneNumberError: " + t.getMessage());
                call.cancel();
            }
        });
    }

//    public void signin() {
//        progressDialog.show();
//        HashMap<String, String> map = new HashMap<>();
//        map.put(Constants.TAG_EMAIL, binding.email.getText().toString());
//        map.put(Constants.TAG_PASSWORD, binding.password.getText().toString());
//        Log.v(TAG, "signInParams:" + map);
//        Call<UserData> call3 = apiInterface.signin(map);
//        call3.enqueue(new Callback<UserData>() {
//            @Override
//            public void onResponse(Call<UserData> call, Response<UserData> response) {
//                try {
//                    if (response.isSuccessful()) {
//                        userdata = response.body();
//                        if (userdata.status.equals("true")) {
////                            if (response.body().message.equalsIgnoreCase("e")) {
////                                showErrorDialog(getString(R.string.accountdeleted),"from");
////                            }else {
//                           // verifyMobileNo();
//                            Intent intent = new Intent(LoginActivity.this, ActivityEnterPhoneNumber.class);
//                            startActivityForResult(intent, APP_REQUEST_CODE);
//                        } else if (userdata.status.equals("error")) {
//                            if (response.body().message.equalsIgnoreCase("Account has been deleted successfully")){
//                                showErrorDialog(getString(R.string.accountdeleted),"from");
//                            }else {
//                                Toast.makeText(getApplicationContext(), userdata.message, Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    } else {
//                        Toast.makeText(getApplicationContext(), getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
//                    }
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//                progressDialog.dismiss();
//            }
//
//            @Override
//            public void onFailure(Call<UserData> call, Throwable t) {
//                Log.e(TAG, "signInFailed: " + t.getMessage());
//                call.cancel();
//                progressDialog.dismiss();
//            }
//        });
//    }

    public void signin() {
        progressDialog.show();
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_EMAIL, binding.email.getText().toString());
        map.put(Constants.TAG_PASSWORD, binding.password.getText().toString());
        Log.v(TAG, "signInParams:" + map);
        Call<UserData> call3 = apiInterface.signin(map);
        call3.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                try {
                    if (response.isSuccessful()) {
                        userdata = response.body();
                        if (userdata.status.equals("true")) {
                            GetSet.Logingin(userdata.user_id, binding.email.getText().toString(), userdata.full_name, userdata.imageurl, userdata.token, userdata.walletmoney);
                            editor.putBoolean("isLogged", true);
                            editor.putString("userId", userdata.user_id);
                            editor.putString("email", binding.email.getText().toString());
                            editor.putString("userImage", userdata.imageurl);
                            editor.putString("fullName", userdata.full_name);
                            editor.putString("token", userdata.token);
                            editor.putString("wallet", userdata.walletmoney);
                            editor.putString("password", userdata.password);
                            editor.putString("admin_approved", userdata.admin_approved);
                            editor.putString("vehicle_approval", userdata.vehicle_approval);
                            editor.putString("vehicle_details", userdata.vehicle_details);
                            editor.putString("payment_details", userdata.payment_details);
                            editor.putString("security_deposit", userdata.security_deposit);
                            editor.putString("vehicle_inspection", userdata.vehicle_inspection);
                            editor.commit();
                            GetSet.setPassword(userdata.password);
                            GetSet.setToken(userdata.token);
                            GetSet.setWallet(userdata.walletmoney);

                            VehicleInspectionActivity.vehicledetail = userdata.vehicle_details;
                            VehicleInspectionActivity.paymentdetail = userdata.payment_details;
                            VehicleInspectionActivity.securitydeposit = userdata.security_deposit;
                            VehicleInspectionActivity.vehicleinspection = userdata.vehicle_inspection;
                            VehicleInspectionActivity.vehicleApprove = userdata.vehicle_approval;

                            if (userdata.admin_approved.equals("true") && "approved".equals(userdata.vehicle_approval)) {
                                addDeviceId(LoginActivity.this);
                            } else {
                                Intent nextIntent = new Intent(getApplicationContext(), VehicleInspectionActivity.class);
                                startActivity(nextIntent);
                            }
                        } else if (userdata.status.equals("error")) {
                            if (response.body().message.equalsIgnoreCase("Account has been deleted successfully")) {
                                showErrorDialog(getString(R.string.accountdeleted), "from");
                            } else {
                                Toast.makeText(getApplicationContext(), userdata.message, Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            showIncorrectUsernameOrPasswordDialog();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Log.e(TAG, "signInFailed: " + t.getMessage());
                call.cancel();
                progressDialog.dismiss();
            }
        });
    }

    private void showIncorrectUsernameOrPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate custom layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        builder.setView(dialogView);

        // Create GradientDrawable with rounded corners
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(50); // Set your desired corner radius
        drawable.setColor(getResources().getColor(android.R.color.white)); // Set your desired background color
        dialogView.setBackground(drawable);

        // Get references to the TextView and Button in the custom layout
        TextView dialogTitle = dialogView.findViewById(R.id.dialog_title);
        TextView dialogMessage = dialogView.findViewById(R.id.dialog_message);
        Button dialogButton = dialogView.findViewById(R.id.dialog_button);

        // Set custom text for the title and message
        dialogTitle.setText("Shilota");
        dialogMessage.setText("Incorrect username or password");

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();

        // Set button click listener
        dialogButton.setOnClickListener(v -> alertDialog.dismiss());

        alertDialog.show();
    }

    public void  showErrorDialog(String title,String string) {
        Display display;
        display = LoginActivity.this.getWindowManager().getDefaultDisplay();

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView title1 = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        no.setText(getString(R.string.ok));
        title1.setText(title);
        yes.setVisibility(View.INVISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

}
