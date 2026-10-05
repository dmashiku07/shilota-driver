package com.driver.cabiappdriver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.driver.helper.DialogOverLayPermission;
import com.driver.helper.NetworkReceiver;
import com.driver.helper.OkCallback;
import com.driver.model.AdminData;
import com.driver.model.Dashboard;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.driver.cabiappdriver.R;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.ValueDependentColor;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;

import org.json.JSONObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity
        implements View.OnClickListener, NavigationView.OnNavigationItemSelectedListener,
        NetworkReceiver.ConnectivityReceiverListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String ERROR_SUBSCRIPTION_REQUIRED = "DRIVER_SUBSCRIPTION_REQUIRED";
    private static final String ERROR_SUBSCRIPTION_EXPIRED = "DRIVER_SUBSCRIPTION_EXPIRED";
    private static final String ERROR_SUBSCRIPTION_SUSPENDED = "DRIVER_SUBSCRIPTION_SUSPENDED";
    private static final String ERROR_SUBSCRIPTION_LIMIT_REACHED = "DRIVER_SUBSCRIPTION_LIMIT_REACHED";
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    public static ImageView dashuserImage, userImage;

    public static void refreshProfilePhoto(Object image) {
        if (image == null) return;
        RequestOptions options = RequestOptions.circleCropTransform()
                .placeholder(R.drawable.temp)
                .error(R.drawable.temp)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .skipMemoryCache(true);
        if (userImage != null) {
            Glide.with(userImage).load(image).apply(options).into(userImage);
        }
        if (dashuserImage != null) {
            Glide.with(dashuserImage).load(image).apply(options).into(dashuserImage);
        }
    }
    public Dashboard dashboardData = null;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static String slocation;
    public static double lat = 0, lng = 0;
    public SwitchCompat onlineswitch;
    static ApiInterface apiInterface;
    public static AdminData adminData = null;
    DrawerLayout drawer;
    ImageView navBtn, notifyBtn, appName;
    NavigationView navigationView;
    LinearLayout usrLayout;
    TextView userName, onlineText, driverName, rating, activecount, completedcount, totalkm, earnedamount, earning, bookings, totalearning, cashcollected, availableamount, subscriptionUsageSummary;
    NetworkReceiver receiver;
    GraphView graph;
    Notification notification;
    public static SharedPreferences SavedPref;
    DialogOverLayPermission dialogOverLayPermission;
    int m;
    Handler handler = new Handler();
    NotificationManager notificationManager;
    RelativeLayout onlinelay;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private Boolean mRequestingLocationUpdates;
    private Socket mSocket;
    private boolean openedSubscriptionPlansFromDashboard = false;
    private Runnable liveLocation = new Runnable() {
        @Override
        public void run() {
            try {
                ApplicationClass app = (ApplicationClass) MainActivity.this.getApplication();
                mSocket = app.getSocket();
                mSocket.connect();
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("user_id", GetSet.getUserId().trim());
                jsonObject.put("location", slocation);
                jsonObject.put("lat", String.valueOf(lat));
                jsonObject.put("lng", String.valueOf(lng));

                mSocket.emit("sharelocation", jsonObject);
                Log.v(TAG, "shareLocation: " + jsonObject.toString());

            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.d("Handlers", "Called on main thread");
            handler.postDelayed(liveLocation, 5000);
        }
    };
    private NumberFormat numberFormat;

    private boolean isSubscriptionRestrictionCode(String errorCode) {
        return ERROR_SUBSCRIPTION_REQUIRED.equals(errorCode)
                || ERROR_SUBSCRIPTION_EXPIRED.equals(errorCode)
                || ERROR_SUBSCRIPTION_SUSPENDED.equals(errorCode)
                || ERROR_SUBSCRIPTION_LIMIT_REACHED.equals(errorCode);
    }

    private boolean isSubscriptionRestrictedResponse(Map<String, String> responseMap) {
        return responseMap != null && isSubscriptionRestrictionCode(responseMap.get("error_code"));
    }

    private void openSubscriptionPlans() {
        Intent subscriptionPlans = new Intent(MainActivity.this, SubscriptionPlansActivity.class);
        startActivity(subscriptionPlans);
    }

    private void applyOfflineState() {
        handler.removeCallbacks(liveLocation);
        onlineText.setText(getString(R.string.offline));
        editor.putString("online", "0");
        editor.commit();
        onlineswitch.setChecked(false);
        onlinelay.setVisibility(View.GONE);
        notificationManager.cancel(Integer.valueOf(pref.getString("online", "0")));
    }

    private void applyOnlineState() {
        onlineText.setText(getString(R.string.online));
        onlineswitch.setChecked(true);
        editor.putString("online", String.valueOf(m));
        editor.commit();
        onlinelay.setVisibility(View.VISIBLE);
    }

    private void showSubscriptionRestriction(String message, boolean openPlans) {
        String displayMessage = message;
        if (displayMessage == null || displayMessage.trim().isEmpty()) {
            displayMessage = getString(R.string.subscription_pick_plan_hint);
        }
        Toast.makeText(getApplicationContext(), displayMessage, Toast.LENGTH_LONG).show();
        if (openPlans) {
            openSubscriptionPlans();
        }
    }

    private void openSubscriptionPlansFromDashboard(String message) {
        if (openedSubscriptionPlansFromDashboard) {
            return;
        }
        openedSubscriptionPlansFromDashboard = true;
        showSubscriptionRestriction(message, true);
    }

    private void bindSubscriptionUsageSummary() {
        if (dashboardData == null || subscriptionUsageSummary == null) {
            return;
        }
        if (Constants.TRUE.equalsIgnoreCase(dashboardData.subscription_is_unlimited)) {
            subscriptionUsageSummary.setText(getString(R.string.unlimited_rides_badge));
            return;
        }
        String used = dashboardData.subscription_rides_used_today == null || dashboardData.subscription_rides_used_today.isEmpty()
                ? "0"
                : dashboardData.subscription_rides_used_today;
        String limit = dashboardData.subscription_daily_limit == null || dashboardData.subscription_daily_limit.isEmpty()
                ? "0"
                : dashboardData.subscription_daily_limit;
        String remaining = dashboardData.subscription_remaining_rides == null || dashboardData.subscription_remaining_rides.isEmpty()
                ? "0"
                : dashboardData.subscription_remaining_rides;
        subscriptionUsageSummary.setText(getString(R.string.subscription_usage_summary, used, limit, remaining));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navBtn = findViewById(R.id.navBtn);
        appName = findViewById(R.id.appName);
        drawer = findViewById(R.id.drawer_layout);
        onlineText = findViewById(R.id.online_text);
        onlinelay = findViewById(R.id.onlinelay);
        notifyBtn = findViewById(R.id.notifyBtn);
        driverName = findViewById(R.id.driverName);
        rating = findViewById(R.id.rating);
        activecount = findViewById(R.id.activecount);
        completedcount = findViewById(R.id.completedcount);
        totalkm = findViewById(R.id.totalkm);
        earnedamount = findViewById(R.id.earnedamount);
        earning = findViewById(R.id.earning);
        bookings = findViewById(R.id.bookings);
        totalearning = findViewById(R.id.totalearning);
        cashcollected = findViewById(R.id.cashcollected);
        availableamount = findViewById(R.id.availableamount);
        subscriptionUsageSummary = findViewById(R.id.subscriptionUsageSummary);
        dashuserImage = findViewById(R.id.dashuserImage);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        navigationView = findViewById(R.id.nav_view);
        onlineswitch = findViewById(R.id.onlineSwitch);
        receiver = new NetworkReceiver();
        ApplicationClass.getInstance().setConnectivityListener(this);
        numberFormat = NumberFormat.getNumberInstance(Locale.ENGLISH);
        View header = navigationView.getHeaderView(0);
        Random random = new Random();
        m = random.nextInt(9999 - 1000) + 1000;
        userImage = header.findViewById(R.id.userImage);
        usrLayout = header.findViewById(R.id.usrLayout);
        appName.setVisibility(View.VISIBLE);
        ApplicationClass app = (ApplicationClass) MainActivity.this.getApplication();
        mSocket = app.getSocket();
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        SavedPref = getApplicationContext().getSharedPreferences("SavedPref",
                MODE_PRIVATE);



        if (pref.getString("online", null) != null) {
            editor.putString("online", pref.getString("online", null));
        } else {
            editor.putString("online", String.valueOf(m));
        }
        editor.commit();

        userName = header.findViewById(R.id.userName);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        graph = findViewById(R.id.graph);

        getAdminData();

        onlineswitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onlineswitch.isChecked()) {
                    changeOnlineStatus("online");
                    onlineText.setText(getString(R.string.online));
                    editor.putString("online", String.valueOf(m));
                    handler.post(liveLocation);
                    createNotify();
                    onlinelay.setVisibility(View.VISIBLE);
                } else {
                    changeOnlineStatus("offline");
                    handler.removeCallbacks(liveLocation);
                    onlineText.setText(getString(R.string.offline));
                    editor.putString("online", "0");
                    notificationManager.cancel(Integer.valueOf(pref.getString("online", null)));
                    onlinelay.setVisibility(View.GONE);
                }
                editor.commit();
            }
        });

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            slocation = getAddress(lat, lng);
                        }
                    }
                });

        initPermissionDialog();

        createLocationRequest();
        createLocationCallback();
        startLocationUpdates();
        navBtn.setVisibility(View.VISIBLE);
        notifyBtn.setVisibility(View.VISIBLE);
        navBtn.setOnClickListener(this);
        notifyBtn.setOnClickListener(this);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu()
                .findItem(R.id.destination_broadcast_menu)
                .setVisible(Constants.DRIVER_DESTINATION_BROADCAST_ENABLED);
        userImage.setOnClickListener(this);
        dashuserImage.setOnClickListener(this);
        usrLayout.setOnClickListener(this);
        final ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, null, R.string.navigation_drawer_open, R.string.navigation_drawer_close) {

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawer.addDrawerListener(toggle);
        drawer.post(new Runnable() {
            @Override
            public void run() {
                toggle.syncState();
            }
        });

        navigationView.post(new Runnable() {

            @Override
            public void run() {
                Resources r = getResources();
                DisplayMetrics metrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(metrics);
                int width = metrics.widthPixels;

                float screenWidth = width / r.getDisplayMetrics().density;
                float navWidth = screenWidth - 56;

                navWidth = Math.min(navWidth, 320);

                int newWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, navWidth, r.getDisplayMetrics());

                DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) navigationView.getLayoutParams();
                params.width = newWidth;
                navigationView.setLayoutParams(params);
            }
        });
    }



    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void createLocationCallback() {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location mCurrentLocation = locationResult.getLastLocation();
                lat = mCurrentLocation.getLatitude();
                lng = mCurrentLocation.getLongitude();
                slocation = getAddress(lat, lng);
                Log.v(TAG, "Current latLng update: " + lat + "," + lng);
            }
        };
    }

    public String getAddress(double lat, double lng) {
        String add = "";
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            Address obj = addresses.get(0);
            add = obj.getAddressLine(0);

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        return add;
    }

    void createNotify() {
        notificationManager.cancel(Integer.parseInt(pref.getString("online", null)));
        Intent intent = null;
        intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        String appName = getString(R.string.app_name);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);

        long when = System.currentTimeMillis();

        PendingIntent resultPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);


        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    "CABSO", "ONLINE DRIVER!", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "CABSO");
        notification = mBuilder.setSmallIcon(R.drawable.small_notification).setTicker(appName).setWhen(when)
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.app_open)))
                .setContentTitle(appName + ": " + getString(R.string.online_notify))
                .setSmallIcon(R.drawable.small_notification)
                .setLargeIcon(bitmap)
                .setContentText(getString(R.string.app_open))
                .build();


        notification.flags |= Notification.FLAG_NO_CLEAR;

        notificationManager.notify(m, notification);

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switchActivityByNavigation(id, item);
        drawer.closeDrawer(GravityCompat.START);
        return false;
    }

    private void startLocationUpdates() {
        try {
            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }


    private void stopLocationUpdates() {
        if (!mRequestingLocationUpdates) {
            Log.d("LOC", "stopLocationUpdates: updates never requested, no-op.");
            return;
        }

        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        mFusedLocationClient.removeLocationUpdates(mLocationCallback)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        mRequestingLocationUpdates = false;
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!checkOverLayPermission()) {
            openPermissionDialog();
        }


        String stats = pref.getString("online", null);
        if (!stats.equals("0")) {
            onlineText.setText(getString(R.string.online));
            onlineswitch.setChecked(true);
            handler.post(liveLocation);
            onlinelay.setVisibility(View.VISIBLE);
        } else {
            onlineText.setText(getString(R.string.offline));
            onlineswitch.setChecked(false);
            handler.removeCallbacks(liveLocation);
            onlineText.setText(getString(R.string.offline));
            onlinelay.setVisibility(View.GONE);
        }

        if (GetSet.getFullName() != null) {
            driverName.setText(GetSet.getFullName());
            userName.setText(GetSet.getFullName());
        }
        if (GetSet.getImageUrl() != null) {
            refreshProfilePhoto(GetSet.getImageUrl());
        }
        getDashboard();
        registerReceiver(receiver, ApplicationClass.intentFilter);
        ApplicationClass.showSnack(this, findViewById(R.id.rootFrame), NetworkReceiver.isConnected());
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        ApplicationClass.showSnack(this, findViewById(R.id.rootFrame), true);
    }

    private void initPermissionDialog() {
        dialogOverLayPermission = new DialogOverLayPermission();
        dialogOverLayPermission.setContext(this);
        dialogOverLayPermission.setCancelable(false);
        dialogOverLayPermission.setCallBack(new OkCallback() {
            @Override
            public void onOkClicked(Object o) {
                boolean isAllow = (boolean) o;
                if (isAllow) {
                    requestOverLayPermission();
                    miOverLayPermission();
                }
                dialogOverLayPermission.dismissAllowingStateLoss();
            }
        });
    }

    private void requestOverLayPermission() {
// Check if Android M or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
// Show alert dialog to the user saying a separate permission is needed
// Launch the settings activity if the user prefers
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, Constants.OVERLAY_REQUEST_CODE);
        }
    }


    public void miOverLayPermission(){
        if (getDeviceName().contains("Xiaomi")){
            Log.i("TAG", "miOverLayPermission: "+getDeviceName());
            Intent localIntent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            localIntent.setClassName("com.miui.securitycenter", "com.miui.permcenter.permissions.PermissionsEditorActivity");
            localIntent.putExtra("extra_pkgname", getPackageName());
            startActivity(localIntent);
        }
    }



    public static String getDeviceName() {
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        Log.e("TAG", "getDeviceName: " + manufacturer + " " + model);
        return manufacturer + " " + model;
    }

    private boolean checkOverLayPermission() {
//Android M Or Over
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            boolean isOverLayEnabled = Settings.canDrawOverlays(this);
            //if (isOverLayEnabled)
            //editor.putBoolean("popup_window_permission", false).apply();
            return isOverLayEnabled;
        }
        return true;
    }


    public void getAdminData() {
        Call<AdminData> call = apiInterface.adminData();
        call.enqueue(new Callback<AdminData>() {
            @Override
            public void onResponse(Call<AdminData> call, Response<AdminData> response) {
                try {
                    if (response.isSuccessful()) {
                        adminData = response.body();
                        if (adminData.status.equals("true")) {

                            GetSet.setAdUnitId(adminData.ad_unit_id);
                            if(adminData.admin_enable_ads==null)
                                GetSet.setIsBannerEnable("");
                            else GetSet.setIsBannerEnable(adminData.admin_enable_ads);

                            Log.e("adunitiD","-"+GetSet.getAdUnitId()+" enable "+GetSet.getIsBannerEnable());

                        } else if (adminData.status.equals("false")) {
                            Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<AdminData> call, Throwable t) {
                Log.e(TAG, "getAdminDataError=" + t.getMessage());
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openPermissionDialog() {
        Log.e("permissionPopu","-"+SavedPref.getBoolean("popup_window_permission",true));
        if (SavedPref.getBoolean("popup_window_permission", true)) {
            if (dialogOverLayPermission != null && !dialogOverLayPermission.isAdded()) {
                dialogOverLayPermission.show(getSupportFragmentManager(), "PermissionDialog");
            }
        }
    }



    private void switchActivityByNavigation(int id, MenuItem item) {
        if (id == R.id.payment_menu) {
            Intent paymentIntent = new Intent(MainActivity.this, PaymentActivity.class);
            paymentIntent.putExtra("from", "main");
            startActivity(paymentIntent);
        } else if (id == R.id.subscription_plans_menu) {
            openSubscriptionPlans();
        } else if (id == R.id.ride_details_menu) {
            Intent ride = new Intent(MainActivity.this, RideActivity.class);
            startActivity(ride);
        } else if (id == R.id.lost_items_menu) {
            Intent lostItems = new Intent(MainActivity.this, LostItemsActivity.class);
            startActivity(lostItems);
        } else if (id == R.id.destination_broadcast_menu) {
            Intent destinationBroadcast = new Intent(MainActivity.this, DestinationBroadcastActivity.class);
            startActivity(destinationBroadcast);
        } else if (id == R.id.help_menu) {
            Intent helpIntent = new Intent(MainActivity.this, HelpActivity.class);
            startActivity(helpIntent);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.navBtn) {
            drawer.openDrawer(GravityCompat.START);
        } else if (id == R.id.notifyBtn) {
            Intent i = new Intent(this, NotificationActivity.class);
            startActivity(i);
        } else if (id == R.id.dashuserImage || id == R.id.userImage || id == R.id.usrLayout) {
            /*if (GetSet.isLogged()) {*/
            Intent p = new Intent(this, ProfileActivity.class);
            p.putExtra("userId", GetSet.getUserId());
            startActivity(p);
        /*} else {
            Intent login = new Intent(this, LoginActivity.class);
            startActivity(login);
        }*/
        }
    }


    private void getDashboard() {
        Call<Dashboard> call3 = apiInterface.getDashBoard(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<Dashboard>() {
            @Override
            public void onResponse(Call<Dashboard> call, Response<Dashboard> response) {
                try {
                    dashboardData = response.body();

                    rating.setText(dashboardData.rating);
                    activecount.setText(dashboardData.ride_today);
                    completedcount.setText(dashboardData.ride_month);

                    totalkm.setText(dashboardData.ad_km);
                    earnedamount.setText(dashboardData.ad_earning);
                    earning.setText(Constants.currency_symbol + " " + (dashboardData.total_earning != null ? dashboardData.total_earning : "" + 0));

                    if (dashboardData.livestatus.equalsIgnoreCase("true")) {
                        applyOnlineState();
                    } else {
                        applyOfflineState();
                    }
                    bindSubscriptionUsageSummary();

                    if (!Constants.TRUE.equalsIgnoreCase(dashboardData.subscription_eligible)
                            && !dashboardData.livestatus.equalsIgnoreCase("true")) {
                        openSubscriptionPlansFromDashboard(dashboardData.subscription_message);
                    }

                    bookings.setText(dashboardData.booking_count != null ? dashboardData.booking_count : "" + 0);
                    totalearning.setText(Constants.currency_symbol + " " + (dashboardData.total_earning != null ? dashboardData.total_earning : "" + 0));
                    cashcollected.setText(Constants.currency_symbol + " " + (dashboardData.cash_collected != null ? dashboardData.cash_collected : "" + 0));
                    availableamount.setText(Constants.currency_symbol + " " + (dashboardData.available_amount != null ? dashboardData.available_amount : "" + 0));

                    StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);

                    graph.getViewport().setXAxisBoundsManual(true);
                    graph.getViewport().setMinX(0.5);
                    graph.getViewport().setMaxX(6.5);

                    Calendar c = Calendar.getInstance();
                    int month = c.get(Calendar.MONTH) + 1;
                    BarGraphSeries<DataPoint> series;
                    ArrayList<Integer> list = new ArrayList<>();
                    int j = 0;
                    for (int i = 1; i <= 12; i++) {

                        if (j == dashboardData.cumulative_report.size()) {
                            list.add(i - 1, 0);
                        } else {
                            if (dashboardData.cumulative_report.get(j).month.equals(String.valueOf(i))) {
                                list.add(i - 1, Integer.valueOf(dashboardData.cumulative_report.get(j).count));
                                j++;
                            } else {
                                list.add(i - 1, 0);
                            }
                        }
                    }

                    if (month <= 6) {
                        series = new BarGraphSeries<>(new DataPoint[]{
                                new DataPoint(Long.parseLong(numberFormat.format(1)), list.get(0)),
                                new DataPoint(Long.parseLong(numberFormat.format(2)), list.get(1)),
                                new DataPoint(Long.parseLong(numberFormat.format(3)), list.get(2)),
                                new DataPoint(Long.parseLong(numberFormat.format(4)), list.get(3)),
                                new DataPoint(Long.parseLong(numberFormat.format(5)), list.get(4)),
                                new DataPoint(Long.parseLong(numberFormat.format(6)), list.get(5))
                        });

                        staticLabelsFormatter.setHorizontalLabels(new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul"});
                    } else {

                        series = new BarGraphSeries<>(new DataPoint[]{
                                new DataPoint(Long.parseLong(numberFormat.format(1)), list.get(6)),
                                new DataPoint(Long.parseLong(numberFormat.format(2)), list.get(7)),
                                new DataPoint(Long.parseLong(numberFormat.format(3)), list.get(8)),
                                new DataPoint(Long.parseLong(numberFormat.format(4)), list.get(9)),
                                new DataPoint(Long.parseLong(numberFormat.format(5)), list.get(10)),
                                new DataPoint(Long.parseLong(numberFormat.format(6)), list.get(11))
                        });

                        staticLabelsFormatter.setHorizontalLabels(new String[]{"Jul", "Aug", "Sep", "Oct", "Nov", "Dec", "Jan"});

                    }
                    //   graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.NONE);
                    //    graph.getViewport().setDrawBorder(true);

                    series.setValueDependentColor(new ValueDependentColor<DataPoint>() {
                        @Override
                        public int get(DataPoint data) {
                            if (data.getX() % 2 == 0)
                                return getResources().getColor(R.color.graphcolor);
                            else
                                return getResources().getColor(R.color.colorPrimary);
                        }
                    });
                    graph.addSeries(series);
                    series.setSpacing(45);
                    graph.getGridLabelRenderer().setTextSize(getResources().getDimension(R.dimen.text_size_tiny));
                    graph.getGridLabelRenderer().setGridColor(getResources().getColor(R.color.driver_line));
                    graph.getGridLabelRenderer().setHorizontalLabelsColor(getResources().getColor(R.color.driver_muted));
                    graph.getGridLabelRenderer().setVerticalLabelsColor(getResources().getColor(R.color.driver_muted));
                    graph.getGridLabelRenderer().setGridStyle(GridLabelRenderer.GridStyle.HORIZONTAL);
                    graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
                    /*graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter() {
                        @Override
                        public String formatLabel(double value, boolean isValueX) {
                            if (!isValueX) {
                                long xAxis = Double.valueOf(value).longValue();
                                return numberFormat.format(xAxis);
                            } else {
                                long xAxis = Double.valueOf(value).longValue();
                                return numberFormat.format(xAxis);
                            }
                        }
                    });*/
                    graph.getGridLabelRenderer().setVerticalLabelsAlign(Paint.Align.RIGHT);
                    graph.getGridLabelRenderer().setVerticalLabelsVAlign(GridLabelRenderer.VerticalLabelsVAlign.MID);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<Dashboard> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function to Change an User Password
     */

    private void changeOnlineStatus(String stats) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_STATUS, stats);

        Log.v(TAG, "changeOnlineStatusParams:" + map);
        Call<Map<String, String>> call3 = apiInterface.onlineStatus(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> map = response.body();
                Log.v(TAG, "changeOnlineStatusResponse=" + map);
                if (map == null || !Constants.TRUE.equals(map.get(Constants.TAG_STATUS))) {
                    if ("online".equals(stats)) {
                        applyOfflineState();
                        if (isSubscriptionRestrictedResponse(map)) {
                            showSubscriptionRestriction(map.get(Constants.TAG_MESSSAGE), true);
                            return;
                        }
                    } else {
                        applyOnlineState();
                    }
                    Toast.makeText(
                            getApplicationContext(),
                            map != null && map.get(Constants.TAG_MESSSAGE) != null
                                    ? map.get(Constants.TAG_MESSSAGE)
                                    : getString(R.string.you_are_already_in_live),
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.rootFrame), isConnected);
    }
}
