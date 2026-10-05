package com.driver.cabiappdriver;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.driver.cabiappdriver.R;
import com.driver.model.AdminData;
import com.driver.model.UserData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.Manifest.permission.READ_CONTACTS;

public class SplashActivity extends BaseActivity {
    private static final String TAG = SplashActivity.class.getSimpleName();
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int REQUEST_APP_PERMISSIONS = 600;
    private static final int REQUEST_LOCATION_PERMISSIONS = 700;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static AdminData adminData = null;
    public static ArrayList<AdminData.BodyType> bodyTypeArrayList = new ArrayList<>();
    public static ArrayList<AdminData.Amenities> amenitiesArrayList = new ArrayList<>();
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static int displayHeight;
    private static int SPLASH_TIME_OUT = 2000;
    ApiInterface apiInterface;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    NotificationManager notificationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (com.driver.helper.AcceptedOfferHandler.openNotification(this)) return;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        setContentView(R.layout.activity_splash);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        if (pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(pref.getString("userId", null));
            GetSet.setEmail(pref.getString("email", null));
            GetSet.setUserName(pref.getString("userName", null));
            GetSet.setFullName(pref.getString("fullName", null));
            GetSet.setImageUrl(pref.getString("userImage", null));
            GetSet.setToken(pref.getString("token", null));
            GetSet.setWallet(pref.getString("wallet", null));
            GetSet.setPassword(pref.getString("password", null));
            MapsActivity.pickupdistance = pref.getFloat("pickupdistance", 0);
            MapsActivity.destinationdistance = pref.getFloat("destdistance", 0);
        }
        displayHeight = getUsableHeight();
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        mSettingsClient = LocationServices.getSettingsClient(this);

        /*Marshmallow Permission*/

        if (!hasStartupPermissions()) {
            ActivityCompat.requestPermissions(SplashActivity.this, getRequiredPermissions(), REQUEST_APP_PERMISSIONS);
        } else {
            getAdminData();
        }
    }

    private boolean hasStartupPermissions() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private String[] getRequiredPermissions() {
        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(ACCESS_FINE_LOCATION);
        permissions.add(ACCESS_COARSE_LOCATION);
        permissions.add(READ_CONTACTS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(POST_NOTIFICATIONS);
        }
        return permissions.toArray(new String[0]);
    }

    @SuppressLint("NewApi")
    private int getUsableHeight() {
        // getRealMetrics is only available with API 17 and +
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return usableHeight;
        else
            return 0;

    }

    public void getAdminData() {
        Call<AdminData> call = apiInterface.adminData();
        call.enqueue(new Callback<AdminData>() {
            @Override
            public void onResponse(Call<AdminData> call, Response<AdminData> response) {
                try {
                    if (response.isSuccessful()) {
                        adminData = response.body();
                        if (adminData != null && Constants.TRUE.equals(adminData.status)) {
                            bodyTypeArrayList = new ArrayList<>();
                            amenitiesArrayList = new ArrayList<>();
                            if (adminData.bodyType != null) {
                                bodyTypeArrayList.addAll(adminData.bodyType);
                            }
                            if (adminData.amenities != null) {
                                amenitiesArrayList.addAll(adminData.amenities);
                            }
                            if (adminData.emergencycontact != null) {
                                Constants.emergencycontact = adminData.emergencycontact;
                            }
                            checkGpsAndOpenActivity();
                        } else {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
                            checkGpsAndOpenActivity();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
                        checkGpsAndOpenActivity();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    checkGpsAndOpenActivity();
                }
            }

            @Override
            public void onFailure(Call<AdminData> call, Throwable t) {
                Log.e(TAG, "getAdminDataError=" + t.getMessage());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
                call.cancel();
                checkGpsAndOpenActivity();
            }
        });
    }


    private void checkGpsAndOpenActivity() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            setLocationSettings("from dialog");
        } else {
            setLocationSettings("from start");
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            checkGpsAndOpenActivity();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.v(TAG, "requestCode=" + requestCode);
        switch (requestCode) {
            case REQUEST_APP_PERMISSIONS:
                if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //allowed
                    getAdminData();
                } else {
                    //set to never ask again
                    ActivityCompat.requestPermissions(SplashActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);
                }
                break;

            case REQUEST_LOCATION_PERMISSIONS:
                if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //allowed
                    getAdminData();
                } else {
                    //set to never ask again
                    ActivityCompat.requestPermissions(SplashActivity.this, new String[]{ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSIONS);
                }
                break;
        }
    }

    public void setLocationSettings(String mode) {
        if (mode.equals("from dialog")) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (GetSet.isLogged()) {
                        openSavedSession();
                    } else {
                        Intent j = new Intent(SplashActivity.this, WelcomeActivity.class);
                        startActivity(j);
                        finish();
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                    }

                }
            }, SPLASH_TIME_OUT);
        } else {
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                createLocationRequest();
                buildLocationSettingsRequest();
                startLocationUpdates();
            }
        }
    }

    private void openSavedSession() {
        String adminApproved = pref.getString("admin_approved", "true");
        String vehicleApproval = pref.getString("vehicle_approval", "approved");
        VehicleInspectionActivity.vehicledetail = pref.getString("vehicle_details", VehicleInspectionActivity.vehicledetail);
        VehicleInspectionActivity.vehicleApprove = vehicleApproval;
        VehicleInspectionActivity.paymentdetail = pref.getString("payment_details", VehicleInspectionActivity.paymentdetail);
        VehicleInspectionActivity.securitydeposit = pref.getString("security_deposit", VehicleInspectionActivity.securitydeposit);
        VehicleInspectionActivity.vehicleinspection = pref.getString("vehicle_inspection", VehicleInspectionActivity.vehicleinspection);

        Intent nextIntent;
        if ("true".equals(adminApproved) && "approved".equals(vehicleApproval)) {
            nextIntent = new Intent(getApplicationContext(), MainActivity.class);
        } else {
            nextIntent = new Intent(getApplicationContext(), VehicleInspectionActivity.class);
        }
        startActivity(nextIntent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void signin() {
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_EMAIL, GetSet.getEmail());
        map.put(Constants.TAG_PASSWORD, GetSet.getPassword());
        Log.v(TAG, "signInParams: " + map);
        Call<UserData> call3 = apiInterface.signin(map);
        call3.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                try {
                    UserData userdata = response.body();
                    if (userdata.status.equals("true")) {
                        GetSet.Logingin(userdata.user_id, GetSet.getEmail(), userdata.full_name, userdata.imageurl, userdata.token, userdata.walletmoney);
                        editor.putBoolean("isLogged", true);
                        editor.putString("userId", userdata.user_id);
                        editor.putString("userImage", userdata.imageurl);
                        editor.putString("fullName", userdata.full_name);
                        editor.putString("email", GetSet.getEmail());
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
                        VehicleInspectionActivity.vehicleApprove = userdata.vehicle_approval;
                        VehicleInspectionActivity.paymentdetail = userdata.payment_details;
                        VehicleInspectionActivity.securitydeposit = userdata.security_deposit;
                        VehicleInspectionActivity.vehicleinspection = userdata.vehicle_inspection;
                        Log.v(TAG, "ADMINAPPROVE: " + userdata.admin_approved);

                        if (userdata.admin_approved.equals("true")) {
                            switch (userdata.vehicle_approval) {
                                case "approved":
                                    //Addon for audiocall
                                    /*if(ApplicationClass.getCurrentActivity() instanceof CallActivity) {
                                        Log.i(TAG, "sendPushNotification: " + "splashpage onresume");
                                    }else{*/
                                        Intent mainIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivity(mainIntent);
//                                    }
                                    ApplicationClass.setCurrentActivity(null);

                                    break;

                                case "rejected":
                                    Intent rejectIntent = new Intent(getApplicationContext(), VehicleInspectionActivity.class);
                                    startActivity(rejectIntent);
                                    break;

                                case "pending":
                                    Intent pendingIntent = new Intent(getApplicationContext(), VehicleInspectionActivity.class);
                                    startActivity(pendingIntent);
                                    break;

                                default:
                                    break;

                            }
                            finish();
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        } else {
                            removeToken();
                            SharedPreferences pref = SplashActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
                            SharedPreferences.Editor editor = pref.edit();
                            editor.clear();
                            editor.commit();
                            notificationManager.cancelAll();
                            GetSet.logout();
                            showErrorDialog();
                        }

                    } else if (userdata.status.equals("error")) {
                        Intent intent =new Intent(SplashActivity.this, WelcomeActivity.class);
                        startActivity(intent);
                    } else if (userdata.status.equals("false")) {
                        Toast.makeText(getApplicationContext(), userdata.message, Toast.LENGTH_SHORT).show();
                        logoutAndOpenLogin();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logoutAndOpenLogin();
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Log.v(TAG, t.getMessage());
                call.cancel();
            }
        });
    }

    private void logoutAndOpenLogin() {
        SharedPreferences pref = SplashActivity.this.getSharedPreferences("SavedPref", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
        notificationManager.cancelAll();
        GetSet.logout();
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void showErrorDialog() {
        Display display;
        display = SplashActivity.this.getWindowManager().getDefaultDisplay();

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
        title.setText(getString(R.string.account_deactivate_error));
        no.setVisibility(View.INVISIBLE);

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent j = new Intent(getApplicationContext(), WelcomeActivity.class);
                startActivity(j);
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                dialog.dismiss();
                finish();
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

    @SuppressLint("RestrictedApi")
    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Requests location updates from the FusedLocationApi. Note: we don't call this unless location
     * runtime permission has been granted.
     */

    private void startLocationUpdates() {
        // Begin by checking if the device has the necessary location settings.
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        Log.i(TAG, "Location settings are not satisfied. Attempting to upgrade " +
                                "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult(SplashActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sie) {
                            Log.i(TAG, "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        Log.e(TAG, errorMessage);
                }
            }
        });
    }
}
