package com.driver.cabiappdriver;

import android.app.Dialog;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.InputFilter;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.driver.external.ImagePicker;
import com.driver.helper.LocaleManager;
import com.driver.helper.NetworkReceiver;
import com.driver.cabiappdriver.databinding.ActivityProfileBinding;
import com.driver.cabiappdriver.databinding.NameDialogBinding;
import com.driver.cabiappdriver.databinding.PasswordUpdateLayoutBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.driver.cabiappdriver.R;

import com.driver.model.ProfileData;
import com.driver.model.VehicleData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class ProfileActivity extends BaseActivity implements View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {
    static final String TAG = ProfileActivity.class.getSimpleName();
    public static int APP_REQUEST_CODE = 99;
    public static ProfileData data = null;
    public static VehicleData vehicleData = null;
    public static ArrayList<ProfileData.EmergencyContact> emergencyList = new ArrayList<>();
    static ApiInterface apiInterface;
    ActivityProfileBinding profBinding;
    ImageView backBtn;
    FrameLayout mAdView;
    BottomSheetDialog dialog, nameDialog;
    String phoneNumber = "", countryCode = "";
    ProgressDialog progressDialog;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    NetworkReceiver receiver;
    NotificationManager notificationManager;
    RelativeLayout deleteAccount;
    private final java.util.concurrent.ExecutorService photoWorker = java.util.concurrent.Executors.newSingleThreadExecutor();
    private boolean photoBusy;
    private androidx.appcompat.app.AlertDialog photoPreview;
    private Call<Map<String, String>> photoUpload;


    public static String ConvertBitmapToString(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        profBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile);
        backBtn = findViewById(R.id.backBtn);
        receiver = new NetworkReceiver();
        ApplicationClass.getInstance().setConnectivityListener(this);
        backBtn.setVisibility(View.VISIBLE);
        pref = ProfileActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        mAdView = findViewById(R.id.adLay);
        if (data != null) {
            setProfileData();
        } else {
            profBinding.progress.setVisibility(View.VISIBLE);
            profBinding.profLay.setVisibility(View.GONE);
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.pleasewait));

        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        deleteAccount = findViewById(R.id.deleteAccount);
        profBinding.lang.setText(LocaleManager.getLanguageName(this));
        getProfileData();

        backBtn.setOnClickListener(this);
        profBinding.usrLogout.setOnClickListener(this);
        profBinding.profPassword.setOnClickListener(this);
        profBinding.userImage.setOnClickListener(this);
        profBinding.editUsr.setOnClickListener(this);
        profBinding.sosalert.setOnClickListener(this);
        profBinding.profName.setOnClickListener(this);
        profBinding.phNoVerified.setOnClickListener(this);
        profBinding.lang.setOnClickListener(this);
        profBinding.deleteAccount.setOnClickListener(this);

        LayerDrawable stars = (LayerDrawable) profBinding.ratingBar.getProgressDrawable().getCurrent();
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.notification), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(0).setColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        stars.getDrawable(2).setColorFilter(getResources().getColor(R.color.notification), PorterDuff.Mode.SRC_ATOP);

        RelativeLayout.LayoutParams banerparams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        banerparams.addRule(RelativeLayout.CENTER_HORIZONTAL);

        if (Constants.GOOGLE_ADS) {
            loadAd();
        }


    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.activity_profile), isConnected);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == 234) {
            prepareProfilePhoto(requestCode, resultCode, data);
        }

        if (requestCode == APP_REQUEST_CODE && resultCode == RESULT_OK) {
            String countrycode = data.getStringExtra("countrycode");
            String phonenumber = data.getStringExtra("phonenumber");
            Log.e(TAG,"PHONE NUMBER : " + countrycode +"," + phonenumber);
            progressDialog.show();
            changePhoneNumber(phonenumber, countrycode);
         /*   FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user != null && user.getPhoneNumber() != null) {
                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                try {
                    Phonenumber.PhoneNumber numberProto = phoneUtil.parse(user.getPhoneNumber(), null);
                    phoneNumber = "" + numberProto.getNationalNumber();
                    countryCode = "" + numberProto.getCountryCode();
                    progressDialog.show();
                    changePhoneNumber(phoneNumber, countryCode);
                } catch (NumberParseException e) {
                    Log.d(TAG, "NumberParseException: " + e.getMessage());
                    ApplicationClass.showToast(ProfileActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT);
                    finish();
                }
            }
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {

                        }
                    });*/
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePicker.pickImage(this, getString(R.string.select_your_image));
            } else {
                openImageGallery();
            }
        }
    }

    private void openImageGallery() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_your_image)), 234);
        } catch (android.content.ActivityNotFoundException e) {
            Toast.makeText(this, R.string.something_wrong, Toast.LENGTH_LONG).show();
        }
    }

    /*API Integration*/

    /**
     * Function to getItem an User Profile Datas
     */

    private void getProfileData() {

        Log.v(TAG, "profileDataToken=" + GetSet.getToken());
        Log.v(TAG, "profileDataUserId=" + GetSet.getUserId());

        Call<ProfileData> call3 = apiInterface.profile(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                try {
                    Log.d(TAG, "onResponseProfileData: " + response.toString());
                    data = response.body();
                    if (data == null) {
                        showProfileLoadFailed();
                        return;
                    }
                    if (emergencyList != null) {
                        emergencyList.clear();
                    }
                    String status = data.status == null ? "" : data.status;
                    if (status.equals("true")) {
                        if (data.walletmoney != null) {
                            editor.putString("wallet", data.walletmoney);
                            editor.commit();
                            GetSet.setWallet(data.walletmoney);
                        }
                        if (data.emergency_contact != null) {
                            emergencyList.addAll(data.emergency_contact);
                        }
                        setProfileData();
                        profBinding.progress.setVisibility(View.GONE);
                        profBinding.profLay.setVisibility(View.VISIBLE);
                    } else if (status.equalsIgnoreCase("error")) {
                        GetSet.logout();
                        Intent intent = new Intent(ProfileActivity.this, WelcomeActivity.class);
                        intent.putExtra("from", "deleteaccount");
                        Toast.makeText(getApplicationContext(), getString(R.string.accountdeleted), Toast.LENGTH_SHORT).show();
                        startActivity(intent);
                    } else {
                        showProfileLoadFailed();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showProfileLoadFailed();
                }

            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                call.cancel();
                showProfileLoadFailed();
            }
        });
    }


    /**
     * Function to Get a Vehicle Details
     */

    private void getVehicleDetails() {
        Call<VehicleData> call3 = apiInterface.getVehicleDetails(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<VehicleData>() {
            @Override
            public void onResponse(Call<VehicleData> call, Response<VehicleData> response) {
                try {
                    vehicleData = response.body();
                    if (vehicleData != null && "true".equals(vehicleData.status)) {
                        setVehicleData();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<VehicleData> call, Throwable t) {
                call.cancel();
                Toast.makeText(ProfileActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream byteBuff = new ByteArrayOutputStream();
        int buffSize = 1024;
        byte[] buff = new byte[buffSize];
        int len = 0;
        while ((len = is.read(buff)) != -1) {
            byteBuff.write(buff, 0, len);
        }
        return byteBuff.toByteArray();
    }

    /**
     * Function to Change an User Password
     */

    private void changePassword(String password, final Dialog dialog, final PasswordUpdateLayoutBinding binding) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_NEW_PASSWORD, password);

        Log.v(TAG, "changePasswordParams:" + map);
        Call<Map<String, String>> call3 = apiInterface.changePassword(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v(TAG, "changePasswordResponse=" + data);
                if (response.isSuccessful() && data != null && "true".equals(data.get(Constants.TAG_STATUS))) {
                    if (dialog.isShowing())
                        dialog.dismiss();
                    GetSet.setPassword(binding.reEnterPswd.getText().toString());
                    editor.putString("password", binding.reEnterPswd.getText().toString());
                    editor.commit();
                    profBinding.profPassword.setText(binding.reEnterPswd.getText().toString());
                    Toast.makeText(ProfileActivity.this, getString(R.string.update_password_success), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, getString(R.string.update_password_unsuccess), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                Toast.makeText(ProfileActivity.this, getString(R.string.update_password_unsuccess), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAd() {
        //Addon for googleads
        //BannerAdUtils.loadBannerAd(ProfileActivity.this, mAdView);
    }
    /*Set Data From API*/

    private void setProfileData() {
        if (data != null && "true".equals(data.status)) {
            profBinding.profName.setText(data.full_name == null ? "" : data.full_name);
            profBinding.profEmail.setText(data.email == null ? "" : data.email);
            profBinding.profPassword.setText(GetSet.getPassword());
            profBinding.profPhoneno.setText((data.country_code == null ? "" : data.country_code) + (data.phone_number == null ? "" : data.phone_number));
            //Toast.makeText(this, data.country_code, Toast.LENGTH_SHORT).show();
            if (data.nextinspectionon != null && !data.nextinspectionon.trim().isEmpty()) {
                profBinding.inspectionDate.setText(getString(R.string.nextinspection) + " " +
                        ApplicationClass.getDateTimeFromISO(data.nextinspectionon));
            }
            if (data.rating != null) {
                try {
                    profBinding.ratingBar.setRating(Float.parseFloat(data.rating));
                } catch (NumberFormatException ignored) {
                    profBinding.ratingBar.setRating(0);
                }
            }
            if (data.user_image != null)
                Glide.with(this).load(data.user_image).thumbnail(0.5f)
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.temp))
                        .transition(new DrawableTransitionOptions().crossFade())
                        .error(R.drawable.temp).placeholder(R.drawable.temp)
                        .into(profBinding.userImage);

            getVehicleDetails();
        }
    }

    private void showProfileLoadFailed() {
        if (profBinding != null) {
            profBinding.progress.setVisibility(View.GONE);
            profBinding.profLay.setVisibility(View.VISIBLE);
        }
        Toast.makeText(ProfileActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
    }


    private void setVehicleData() {
        if (vehicleData != null && "true".equals(vehicleData.status)) {
            profBinding.vehicleName.setText(vehicleData.vehicle_name);
            profBinding.vehicleNumber.setText(vehicleData.vehicle_number);
            profBinding.vehicleType.setText(vehicleData.vehicle_type);
            profBinding.driverLicense.setText(vehicleData.licence_no);
            profBinding.licenseExpiryDate.setText(vehicleData.licence_date);

            profBinding.motorInsuranceCertificate.setText(vehicleData.insurance_no);
            profBinding.insuranceCertificateExpireDate.setText(vehicleData.insurance_date);

            profBinding.rcBookNo.setText(vehicleData.rcbook_no);
            profBinding.rcBookExpireDate.setText(vehicleData.rcbook_date);

        }
    }

    private void removeToken() {

        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);
        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("device_id", deviceId);
        Log.v(TAG, "removeTokenParams= " + map);
        Call<Map<String, String>> call3 = apiInterface.pushsignout(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v(TAG, "removeTokenResponse= " + data);
                GetSet.logout();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                GetSet.logout();
            }
        });
    }

    private void changeOnlineStatus(String stats) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_STATUS, stats);

        Call<Map<String, String>> call3 = apiInterface.onlineStatus(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {


            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function for Onclick Event
     */

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.backBtn) {
            onBackPressed();

        } else if (id == R.id.profPassword) {
            final PasswordUpdateLayoutBinding binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.password_update_layout, null, false);
            dialog = new BottomSheetDialog(this);
            dialog.setContentView(binding.getRoot());
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            if (binding.oldPswdlay.getChildCount() == 2)
                binding.oldPswdlay.getChildAt(1).setVisibility(View.GONE);
            if (binding.newPswdlay.getChildCount() == 2)
                binding.newPswdlay.getChildAt(1).setVisibility(View.GONE);
            if (binding.reEnterPswdlay.getChildCount() == 2)
                binding.reEnterPswdlay.getChildAt(1).setVisibility(View.GONE);

            binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ApplicationClass.requestFocus(ProfileActivity.this, binding.oldPswd, false);
                    ApplicationClass.requestFocus(ProfileActivity.this, binding.newPswd, false);
                    ApplicationClass.requestFocus(ProfileActivity.this, binding.reEnterPswd, false);
                    dialog.dismiss();
                }
            });

            binding.saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (binding.oldPswd.getText().toString().equals("")) {
                        binding.oldPswdlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.oldPswdlay.setError(getString(R.string.em_oldpswd_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.oldPswd, true);
                    } else if (binding.oldPswd.getText().toString().length() < 6) {
                        binding.oldPswdlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.oldPswdlay.setError(getString(R.string.password_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.oldPswd, true);
                    } else if (!binding.oldPswd.getText().toString().equals(GetSet.getPassword())) {
                        binding.oldPswdlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.oldPswdlay.setError(getString(R.string.em_oldpswd_mismatch_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.oldPswd, true);
                    } else if (binding.newPswd.getText().toString().equals("")) {
                        binding.oldPswdlay.setErrorEnabled(false);
                        binding.oldPswdlay.getChildAt(1).setVisibility(View.GONE);
                        binding.newPswdlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.newPswdlay.setError(getString(R.string.em_newpswd_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.newPswd, true);
                    } else if (binding.newPswd.getText().toString().length() < 6) {
                        binding.oldPswdlay.setErrorEnabled(false);
                        binding.oldPswdlay.getChildAt(1).setVisibility(View.GONE);
                        binding.newPswdlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.newPswdlay.setError(getString(R.string.password_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.newPswd, true);
                    } else if (binding.reEnterPswd.getText().toString().equals("")) {
                        binding.newPswdlay.setErrorEnabled(false);
                        binding.newPswdlay.getChildAt(1).setVisibility(View.GONE);
                        binding.reEnterPswdlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.reEnterPswdlay.setError(getString(R.string.em_reenpswd_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.reEnterPswd, true);
                    } else if (!binding.newPswd.getText().toString().equals(binding.reEnterPswd.getText().toString())) {
                        binding.reEnterPswdlay.setErrorEnabled(false);
                        binding.newPswdlay.getChildAt(1).setVisibility(View.GONE);
                        binding.reEnterPswdlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.reEnterPswdlay.setError(getString(R.string.repassword_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.reEnterPswd, true);
                    } else {
                        dialog.dismiss();
                        binding.oldPswdlay.setErrorEnabled(false);
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.oldPswd, false);
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.newPswd, false);
                        ApplicationClass.requestFocus(ProfileActivity.this, binding.reEnterPswd, false);
                        profBinding.profPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        changePassword(binding.reEnterPswd.getText().toString(), dialog, binding);
                    }
                }
            });

        } else if (id == R.id.userImage || id == R.id.edit_usr) {
            if (photoBusy) return;
            new androidx.appcompat.app.AlertDialog.Builder(this)
                    .setTitle(R.string.select_your_image)
                    .setItems(new String[]{getString(R.string.choose_photo), getString(R.string.take_photo)}, (dialog, which) -> {
                        if (which == 0) {
                            openImageGallery();
                        } else if (ContextCompat.checkSelfPermission(this, CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(this, new String[]{CAMERA}, 100);
                        } else {
                            ImagePicker.pickImage(this, getString(R.string.select_your_image));
                        }
                    }).show();

        } else if (id == R.id.sosalert) {
            Intent intent = new Intent(this, EmergencyContactActivity.class);
            startActivity(intent);

        } else if (id == R.id.deleteAccount) {
            Display display = this.getWindowManager().getDefaultDisplay();

            final Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.setContentView(R.layout.default_popup);
            dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.setCancelable(false);

            TextView title = dialog.findViewById(R.id.title);
            TextView yes = dialog.findViewById(R.id.yes);
            TextView no = dialog.findViewById(R.id.no);
            yes.setText(getString(R.string.yes));
            title.setText(R.string.accountdelete);
            no.setVisibility(View.VISIBLE);

            yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeOnlineStatus("offline");
                    deleteAccount();
                    dialog.dismiss();
                }
            });

            no.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeOnlineStatus("online");
                    dialog.dismiss();
                }
            });

            if (!dialog.isShowing()) {
                dialog.show();
            }

        } else if (id == R.id.profName) {
            final NameDialogBinding nameBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.name_dialog, null, false);
            nameDialog = new BottomSheetDialog(this);
            nameDialog.setContentView(nameBinding.getRoot());
            nameDialog.setCanceledOnTouchOutside(true);
            nameDialog.setCancelable(true);
            nameDialog.show();

            nameBinding.edtName.setFilters(new InputFilter[]{ApplicationClass.EMOJI_FILTER, ApplicationClass.NUMBER_FILTER, new InputFilter.LengthFilter(30)});

            nameBinding.saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nameBinding.edtName.getText().toString().equals("")) {
                        nameBinding.nameLay.setError(getString(R.string.name_error));
                        ApplicationClass.requestFocus(ProfileActivity.this, v, true);
                    } else {
                        ApplicationClass.hideSoftKeyboard(ProfileActivity.this, v);
                        progressDialog.show();
                        nameDialog.hide();
                        changeName(nameBinding.edtName.getText().toString().trim());
                    }
                }
            });

            nameBinding.cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nameDialog.hide();
                }
            });

        } else if (id == R.id.phNoVerified) {
            ApplicationClass.hideSoftKeyboard(ProfileActivity.this, view);
            // verifyMobileNo(view);
            Intent intentt = new Intent(ProfileActivity.this, ActivityEnterPhoneNumber.class);
            startActivityForResult(intentt, APP_REQUEST_CODE);

        } else if (id == R.id.lang) {
            Intent lang = new Intent(ProfileActivity.this, Language.class);
            startActivity(lang);

        } else if (id == R.id.usrLogout) {
            Display display1 = this.getWindowManager().getDefaultDisplay();

            final Dialog deletedialog = new Dialog(this);
            deletedialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            deletedialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            deletedialog.setContentView(R.layout.default_popup);
            deletedialog.getWindow().setLayout(display1.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
            deletedialog.setCancelable(false);

            TextView title1 = deletedialog.findViewById(R.id.title);
            TextView yes1 = deletedialog.findViewById(R.id.yes);
            TextView no1 = deletedialog.findViewById(R.id.no);
            yes1.setText(getString(R.string.yes));
            title1.setText(R.string.really_logout);
            no1.setVisibility(View.VISIBLE);

            yes1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeOnlineStatus("offline");
                    removeToken();
                    SharedPreferences pref = ProfileActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.clear();
                    editor.commit();
                    notificationManager.cancelAll();
                    deletedialog.dismiss();
                    ProfileActivity.this.finish();
                    Intent j = new Intent(ProfileActivity.this, WelcomeActivity.class);
                    startActivity(j);
                }
            });

            no1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deletedialog.dismiss();
                }
            });

            if (!deletedialog.isShowing()) {
                deletedialog.show();
            }
        }
    }


    public void verifyMobileNo(View v) {
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

    private void changeName(final String name) {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_FULL_NAME, name);
        Call<ProfileData> call3 = apiInterface.updateProfile(GetSet.getToken(), map);
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                try {
                    data = response.body();
                    if (data != null && "true".equals(data.status)) {
                        GetSet.setFullName(name);
                        editor.putString("fullName", name);
                        editor.commit();
                        profBinding.profName.setText(name);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_name_successful), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_name_unsuccessful), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_name_unsuccessful), Toast.LENGTH_LONG).show();
                } finally {
                    progressDialog.dismiss();
                }

            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                call.cancel();
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_name_unsuccessful), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void changePhoneNumber(final String phoneNumber, final String countryCode) {
        progressDialog.show();
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_COUNTRY_CODE, countryCode);
        map.put(Constants.TAG_PHONE_NUMBER, phoneNumber);
        Call<ProfileData> call3 = apiInterface.updateProfile(GetSet.getToken(), map);
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                try {
                    data = response.body();
                    if (data != null && "true".equals(data.status)) {
                        profBinding.profPhoneno.setText(countryCode + phoneNumber);
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_phone_successful), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_phone_unsuccessful), Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_phone_unsuccessful), Toast.LENGTH_LONG).show();
                } finally {
                    progressDialog.dismiss();
                }
            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                call.cancel();
                progressDialog.dismiss();
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, ApplicationClass.intentFilter);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_profile), NetworkReceiver.isConnected());
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_profile), true);
    }

    private void setPhotoBusy(boolean busy) {
        photoBusy = busy;
        profBinding.userImage.setEnabled(!busy);
        profBinding.editUsr.setEnabled(!busy);
    }

    private void prepareProfilePhoto(int requestCode, int resultCode, Intent result) {
        if (photoBusy) return;
        setPhotoBusy(true);
        progressDialog.setCancelable(false);
        progressDialog.show();
        // Decode off the UI thread, with bounded dimensions and EXIF orientation handled by Glide.
        photoWorker.execute(() -> {
            java.io.File source = null;
            com.bumptech.glide.request.FutureTarget<Bitmap> target = null;
            try {
                source = java.io.File.createTempFile("profile-photo-", ".tmp", getCacheDir());
                try (InputStream stream = result != null && result.getData() != null
                        ? getContentResolver().openInputStream(result.getData())
                        : ImagePicker.getInputStreamFromResult(this, requestCode, resultCode, result);
                     java.io.FileOutputStream output = new java.io.FileOutputStream(source)) {
                    if (stream == null) throw new IOException("Image could not be opened");
                    byte[] buffer = new byte[8192];
                    int count;
                    while ((count = stream.read(buffer)) != -1) output.write(buffer, 0, count);
                }
                target = Glide.with(getApplicationContext()).asBitmap().load(source)
                        .fitCenter().diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).submit(1200, 1200);
                Bitmap bitmap = target.get();
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 82, output)) {
                    throw new IOException("Image could not be encoded");
                }
                byte[] bytes = output.toByteArray();
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressDialog.dismiss();
                    setPhotoBusy(false);
                    previewProfilePhoto(bytes);
                });
            } catch (Exception error) {
                runOnUiThread(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    progressDialog.dismiss();
                    setPhotoBusy(false);
                    Toast.makeText(this, R.string.profile_photo_prepare_failed, Toast.LENGTH_LONG).show();
                });
            } finally {
                if (target != null) Glide.with(getApplicationContext()).clear(target);
                if (source != null) source.delete();
            }
        });
    }

    private void previewProfilePhoto(byte[] bytes) {
        ImageView preview = new ImageView(this);
        int size = (int) (240 * getResources().getDisplayMetrics().density);
        preview.setMinimumHeight(size);
        preview.setAdjustViewBounds(true);
        preview.setMaxHeight(size);
        preview.setPadding(24, 24, 24, 24);
        preview.setContentDescription(getString(R.string.profile_photo_preview));
        Glide.with(this).load(bytes).circleCrop().into(preview);
        photoPreview = new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(R.string.profile_photo_preview)
                .setView(preview)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.save, null)
                .create();
        photoPreview.setOnShowListener(dialog -> {
            android.widget.Button saveButton = photoPreview.getButton(android.content.DialogInterface.BUTTON_POSITIVE);
            android.widget.Button cancelButton = photoPreview.getButton(android.content.DialogInterface.BUTTON_NEGATIVE);

            // The application-wide button style can leave AlertDialog actions with white text on
            // a white dialog. Give both actions explicit, accessible colours and keep Save prominent.
            saveButton.setTextColor(ContextCompat.getColor(this, R.color.colorWhite));
            saveButton.setBackgroundResource(R.drawable.button_style);
            saveButton.setMinHeight((int) (48 * getResources().getDisplayMetrics().density));
            saveButton.setPadding(32, 0, 32, 0);
            cancelButton.setTextColor(ContextCompat.getColor(this, R.color.textPrimary));
            cancelButton.setBackgroundColor(android.graphics.Color.TRANSPARENT);
            saveButton.setOnClickListener(view -> uploadImage(bytes));
        });
        photoPreview.show();
    }

    private void uploadImage(byte[] imageBytes) {
        if (photoBusy) return;
        setPhotoBusy(true);
        photoPreview.setCancelable(false);
        photoPreview.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        photoPreview.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setEnabled(false);
        progressDialog.show();
        // Send actual JPEG bytes with a matching filename and media type.
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/jpeg"), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("driverImage", "profile.jpg", requestFile);
        RequestBody userid = RequestBody.create(MediaType.parse("text/plain"), GetSet.getUserId());
        photoUpload = apiInterface.uploadProfileImage(GetSet.getToken(), body, userid);
        photoUpload.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (isFinishing() || isDestroyed()) return;
                Map<String, String> result = response.body();
                String imageUrl = result == null ? null : result.get(Constants.TAG_USER_IMAGE);
                if (response.isSuccessful() && result != null
                        && Constants.TRUE.equals(result.get(Constants.TAG_STATUS))
                        && imageUrl != null && !imageUrl.trim().isEmpty()) {
                    editor.putString("userImage", imageUrl).apply();
                    GetSet.setImageUrl(imageUrl);
                    if (ProfileActivity.data != null) ProfileActivity.data.user_image = imageUrl;
                    // Render the confirmed local JPEG immediately. Loading the newly-created URL
                    // can race the image server/CDN and used to leave the grey placeholder visible
                    // even though the server had accepted the upload.
                    Glide.with(ProfileActivity.this).load(imageBytes)
                            .apply(RequestOptions.circleCropTransform().error(R.drawable.temp))
                            .skipMemoryCache(true)
                            .into(profBinding.userImage);
                    MainActivity.refreshProfilePhoto(imageBytes);
                    finishPhotoUpload();
                    photoPreview.dismiss();
                    Toast.makeText(ProfileActivity.this, R.string.profile_photo_saved, Toast.LENGTH_SHORT).show();
                } else {
                    finishPhotoUpload();
                    String message = result == null ? null : result.get(Constants.TAG_MESSSAGE);
                    Toast.makeText(ProfileActivity.this,
                            message == null || message.trim().isEmpty() ? getString(R.string.profile_photo_upload_failed) : message,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable error) {
                if (isFinishing() || isDestroyed()) return;
                finishPhotoUpload();
                Toast.makeText(ProfileActivity.this, R.string.profile_photo_upload_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void finishPhotoUpload() {
        progressDialog.dismiss();
        setPhotoBusy(false);
        photoPreview.setCancelable(true);
        photoPreview.getButton(android.content.DialogInterface.BUTTON_POSITIVE).setEnabled(true);
        photoPreview.getButton(android.content.DialogInterface.BUTTON_NEGATIVE).setEnabled(true);
    }

    @Override
    protected void onDestroy() {
        if (photoUpload != null) photoUpload.cancel();
        photoWorker.shutdownNow();
        if (photoPreview != null) photoPreview.dismiss();
        if (progressDialog != null) progressDialog.dismiss();
        super.onDestroy();
    }

    private void deleteAccount() {

        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        Log.v(TAG, "removeTokenParams= " + map);
        Call<Map<String, String>> call3 = apiInterface.deleteAccount(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v(TAG, "removeTokenResponse= " + data);
                String status = data == null ? "" : data.get(Constants.TAG_STATUS);
                String message = data == null ? "" : data.get(Constants.TAG_MESSSAGE);
                if ("true".equalsIgnoreCase(status)) {
                    GetSet.logout();
                    removeToken();
                    ProfileActivity.this.finish();
                    Intent j = new Intent(ProfileActivity.this, WelcomeActivity.class);
                    startActivity(j);
                } else {
                    if ("false".equalsIgnoreCase(status) && "You're in ride. You can't delete this account".equalsIgnoreCase(message)) {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                GetSet.logout();
            }
        });
    }

    public void deleteDialog() {
        Display display;
        display = this.getWindowManager().getDefaultDisplay();

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        dialog.setContentView(R.layout.default_popup);
        dialog.getWindow().setLayout(display.getWidth() * 90 / 100, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.setCancelable(false);

        TextView title = dialog.findViewById(R.id.title);
        TextView yes = dialog.findViewById(R.id.yes);
        TextView no = dialog.findViewById(R.id.no);
        yes.setText(getString(R.string.ok));
        title.setText(R.string.deletestatus);
        no.setVisibility(View.GONE);


        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAccount();
                dialog.dismiss();
            }
        });


        if (!dialog.isShowing()) {
            dialog.show();
        }
    }
}
