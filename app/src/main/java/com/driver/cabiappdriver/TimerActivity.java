package com.driver.cabiappdriver;

import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.driver.cabiappdriver.R;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TimerActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = TimerActivity.class.getSimpleName();
    private static final String ERROR_SUBSCRIPTION_REQUIRED = "DRIVER_SUBSCRIPTION_REQUIRED";
    private static final String ERROR_SUBSCRIPTION_EXPIRED = "DRIVER_SUBSCRIPTION_EXPIRED";
    private static final String ERROR_SUBSCRIPTION_SUSPENDED = "DRIVER_SUBSCRIPTION_SUSPENDED";
    private static final String ERROR_SUBSCRIPTION_LIMIT_REACHED = "DRIVER_SUBSCRIPTION_LIMIT_REACHED";
    public static SharedPreferences.Editor editor;
    static ApiInterface apiInterface;
    HashMap<String, String> rideData = new HashMap<>();
    String rideid, pickupLocation, pickupLat, pickupLng, dropLocation, dropLat, dropLng;
    SharedPreferences pref;
    SwitchCompat onlineswitch;
    NotificationManager notificationManager;
    private FusedLocationProviderClient mFusedLocationClient;
    private CountDownTimer countDownTimer;
    private long timeCountInMilliSeconds = 1 * 60000;
    private TextView textViewTime, acceptRide, txtPickupLocation, txtDropLocation;
    private EditText offerPriceInput;
    private TextView requestTripEstimate, requestPickupEstimate;
    private final java.util.concurrent.ExecutorService locationNames = java.util.concurrent.Executors.newFixedThreadPool(2);
    private final java.util.List<Call<HashMap<String, String>>> estimateCalls = new java.util.ArrayList<>();
    private ProgressBar progressBarCircle;
    private String requestType;
    private String passengerOfferedPrice;
    private boolean acceptingRide;
    private String assignedRideStatus;
    NumberFormat numberFormat;
    DecimalFormat decimalFormat;

    private boolean isSubscriptionRestrictionCode(String errorCode) {
        return ERROR_SUBSCRIPTION_REQUIRED.equals(errorCode)
                || ERROR_SUBSCRIPTION_EXPIRED.equals(errorCode)
                || ERROR_SUBSCRIPTION_SUSPENDED.equals(errorCode)
                || ERROR_SUBSCRIPTION_LIMIT_REACHED.equals(errorCode);
    }

    private void openSubscriptionPlans() {
        Intent subscriptionPlans = new Intent(TimerActivity.this, SubscriptionPlansActivity.class);
        startActivity(subscriptionPlans);
    }

    private void showSubscriptionRestriction(String message, boolean finishAfter) {
        String displayMessage = message;
        if (displayMessage == null || displayMessage.trim().isEmpty()) {
            displayMessage = getString(R.string.subscription_pick_plan_hint);
        }
        Toast.makeText(getApplicationContext(), displayMessage, Toast.LENGTH_LONG).show();
        openSubscriptionPlans();
        if (finishAfter) {
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        progressBarCircle = findViewById(R.id.progressBarCircle);
        textViewTime = findViewById(R.id.textViewTime);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        acceptRide = findViewById(R.id.acceptRide);
        onlineswitch = findViewById(R.id.onlineSwitch);
        txtPickupLocation = findViewById(R.id.txtPickupLocation);
        txtDropLocation = findViewById(R.id.txtDropLocation);
        offerPriceInput = findViewById(R.id.offerPriceInput);
        requestTripEstimate = findViewById(R.id.requestTripEstimate);
        requestPickupEstimate = findViewById(R.id.requestPickupEstimate);

        numberFormat = NumberFormat.getInstance(Locale.ENGLISH);

        rideid = sanitizeIdValue(getIntent().getStringExtra("rideid"));
        pickupLocation = getIntent().getStringExtra("pickup_location");
        pickupLat = getIntent().getStringExtra("pickup_lat");
        pickupLng = getIntent().getStringExtra("pickup_lng");
        dropLocation = getIntent().getStringExtra("drop_location");
        dropLat = getIntent().getStringExtra("drop_lat");
        dropLng = getIntent().getStringExtra("drop_lng");
        requestType = getIntent().getStringExtra("request_type");
        passengerOfferedPrice = getIntent().getStringExtra("passenger_offered_price");
        if (rideid == null || rideid.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(pref.getString("userId", null));
            GetSet.setUserName(pref.getString("userName", null));
            GetSet.setFullName(pref.getString("fullName", null));
            GetSet.setImageUrl(pref.getString("userImage", null));
            GetSet.setToken(pref.getString("token", null));
            GetSet.setWallet(pref.getString("wallet", null));
        }

        resolveLocationName(txtPickupLocation, R.string.ride_pickup_value, pickupLocation, pickupLat, pickupLng);
        resolveLocationName(txtDropLocation, R.string.ride_destination_value, dropLocation, dropLat, dropLng);
        loadEstimate(pickupLat, pickupLng, dropLat, dropLng, requestTripEstimate, R.string.request_trip_estimate);
        if ("PASSENGER_PRICE_BROADCAST".equalsIgnoreCase(requestType)) {
            if (passengerOfferedPrice != null && !passengerOfferedPrice.trim().isEmpty()) {
                offerPriceInput.setText(passengerOfferedPrice);
            }
            offerPriceInput.setEnabled(false);
            offerPriceInput.setFocusable(false);
            offerPriceInput.setHint(getString(R.string.passenger_price_hint));
            acceptRide.setText(getString(R.string.accept_passenger_price));
        }

        startCountDownTimer();
        acceptRide.setOnClickListener(this);
        onlineswitch.setChecked(true);

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {

                            MainActivity.lat = location.getLatitude();
                            MainActivity.lng = location.getLongitude();
                            if (android.os.SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()
                                    <= TimeUnit.MINUTES.toNanos(5)) {
                                loadEstimate(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()),
                                        pickupLat, pickupLng, requestPickupEstimate, R.string.request_pickup_estimate);
                            }

                        }
                    }
                });

        onlineswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    changeOnlineStatus("online");

                } else {
                    changeOnlineStatus("offline");
                    editor.putString("online", "0");
                    editor.commit();
                    notificationManager.cancel(Integer.valueOf(pref.getString("online", null)));
                    finish();
                }
            }
        });

    }

    private boolean validPoint(String lat, String lng) {
        try {
            double latitude = Double.parseDouble(lat), longitude = Double.parseDouble(lng);
            return !Double.isNaN(latitude) && !Double.isNaN(longitude)
                    && Math.abs(latitude) <= 90 && Math.abs(longitude) <= 180;
        } catch (Exception invalid) { return false; }
    }

    private void resolveLocationName(TextView view, int label, String original, String lat, String lng) {
        if (!com.driver.helper.RequestLocationLabel.needsResolution(original)) {
            view.setText(getString(label, original.trim()));
            return;
        }
        String area = com.driver.helper.RequestLocationLabel.areaFallback(original);
        String fallback = area.isEmpty() ? getString(R.string.request_pin_location)
                : getString(R.string.request_area_location, area);
        view.setText(getString(label, fallback));
        if (!validPoint(lat, lng) || !android.location.Geocoder.isPresent()) return;
        locationNames.execute(() -> {
            try {
                java.util.List<android.location.Address> addresses = new android.location.Geocoder(this, Locale.getDefault())
                        .getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);
                if (addresses == null || addresses.isEmpty()) return;
                android.location.Address address = addresses.get(0);
                String name = com.driver.helper.RequestLocationLabel.readableParts(
                        address.getFeatureName(), address.getThoroughfare(), address.getSubLocality(), address.getLocality());
                if (name.isEmpty()) return;
                // Reverse-geocoded labels describe the area around the exact passenger pin.
                String resolved = getString(R.string.request_near_location, name);
                runOnUiThread(() -> {
                    if (!isFinishing() && !isDestroyed()) view.setText(getString(label, resolved));
                });
            } catch (Exception unavailable) {
                // Keep the explicit area/pin fallback; never invent a landmark.
            }
        });
    }

    private void loadEstimate(String fromLat, String fromLng, String toLat, String toLng,
                              TextView view, int label) {
        if (!validPoint(fromLat, fromLng) || !validPoint(toLat, toLng)) return;
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", GetSet.getUserId());
        params.put("pickup_lat", fromLat); params.put("pickup_lng", fromLng);
        params.put("drop_lat", toLat); params.put("drop_lng", toLng);
        if (view == requestTripEstimate) {
            String stops = getIntent().getStringExtra("ride_stops");
            if (stops != null) params.put("ride_stops", stops);
        }
        Call<HashMap<String, String>> call = apiInterface.routePath(GetSet.getToken(), params);
        estimateCalls.add(call);
        call.enqueue(new Callback<HashMap<String, String>>() {
            @Override public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (isFinishing() || isDestroyed()) return;
                HashMap<String, String> data = response.body();
                if (data == null || !Constants.TRUE.equals(data.get("status"))) return;
                try {
                    double meters = Double.parseDouble(data.get("distance_meters"));
                    double seconds = Double.parseDouble(data.get("duration_seconds"));
                    if (Double.isNaN(meters) || Double.isInfinite(meters) || meters < 0
                            || Double.isNaN(seconds) || Double.isInfinite(seconds) || seconds < 0) return;
                    String distance = String.format(Locale.getDefault(), "%.1f", meters / 1000);
                    view.setText(getString(label, distance, Math.max(1, (int) Math.ceil(seconds / 60))));
                } catch (Exception unavailable) { /* Keep unavailable text for older servers. */ }
            }
            @Override public void onFailure(Call<HashMap<String, String>> call, Throwable error) { }
        });
    }

    private void startCountDownTimer() {

        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTime.setText(hmsTimeFormatter(millisUntilFinished)+" sec");
                progressBarCircle.setProgress((int) (millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {
                textViewTime.setText(hmsTimeFormatter(timeCountInMilliSeconds)+" sec");
                // call to initialize the progress bar values
                setProgressBarValues();
                if (!acceptingRide && assignedRideStatus == null) finish();
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
            }

        }.start();
    }

    private String safeDisplayValue(String value) {
        return value == null || value.trim().isEmpty() ? getString(R.string.not_available) : value.trim();
    }

    @Override
    public void onBackPressed() {
    }


    private void setProgressBarValues() {

        progressBarCircle.setMax((int) timeCountInMilliSeconds / 1000);
        progressBarCircle.setProgress((int) timeCountInMilliSeconds / 1000);
    }

    private String hmsTimeFormatter(long milliSeconds) {
        /*String hours = numberFormat.format(TimeUnit.MILLISECONDS.toHours(milliSeconds));
        String minutes = numberFormat.format(TimeUnit.MILLISECONDS.toHours(milliSeconds) -
                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(milliSeconds)));
        String seconds = numberFormat.format(TimeUnit.MILLISECONDS.toSeconds(milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
        Log.i(TAG, "hmsTimeFormatter: " + hours + ":" + minutes + ":" + seconds);*/

        String hms = String.format(Locale.ENGLISH,"%02d",

                TimeUnit.MILLISECONDS.toSeconds(
                        milliSeconds) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliSeconds)));
        /*String hms = String.format("%02d:%02d:%02d",
                hours,
                minutes,
                seconds);*/
        return hms;
    }

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
                    if ("online".equals(stats) && map != null && isSubscriptionRestrictionCode(map.get("error_code"))) {
                        editor.putString("online", "0");
                        editor.commit();
                        notificationManager.cancel(Integer.valueOf(pref.getString("online", "0")));
                        showSubscriptionRestriction(map.get(Constants.TAG_MESSSAGE), true);
                        return;
                    }
                }
                editor.putString("online", "0");
                editor.commit();
                notificationManager.cancel(Integer.valueOf(pref.getString("online", "0")));
                finish();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }


    private void acceptRide() {
        if (acceptingRide) return;
        // A failed details request must retry navigation, not submit another offer.
        if (assignedRideStatus != null) {
            openAssignedRide();
            return;
        }
        String userId = sanitizeIdValue(GetSet.getUserId());
        String onRideId = sanitizeIdValue(rideid);
        boolean passengerPriceRequest = "PASSENGER_PRICE_BROADCAST".equalsIgnoreCase(requestType);
        String offeredPrice = passengerPriceRequest && passengerOfferedPrice != null
                ? passengerOfferedPrice.trim()
                : offerPriceInput.getText().toString().trim();
        if (offeredPrice.isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.offer_price_required), Toast.LENGTH_LONG).show();
            return;
        }
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, userId);
        map.put(Constants.TAG_ONRIDE_ID, onRideId);
        map.put("offered_price", offeredPrice);
        map.put("currency", "TZS");
        if (passengerPriceRequest) {
            map.put("offer_type", "ACCEPT_PASSENGER_PRICE");
        }
        acceptingRide = true;
        acceptRide.setEnabled(false);
        Log.v(TAG, "submitRideOfferParams: " + map.toString());
        Call<HashMap<String, Object>> call3 = apiInterface.submitRideOffer(GetSet.getToken(), map);
        call3.enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                try {
                    acceptingRide = false;
                    acceptRide.setEnabled(true);
                    HashMap<String, Object> offerResponse = response.body();
                    Log.v(TAG, "submitRideOfferRes:" + (offerResponse == null ? "" : offerResponse.toString()));
                    if (isFinishing() || isDestroyed()) return;
                    if (offerResponse != null && Constants.TRUE.equals(String.valueOf(offerResponse.get(Constants.TAG_STATUS)))) {
                        if (Constants.TRUE.equals(String.valueOf(offerResponse.get("assigned")))) {
                            Object status = offerResponse.get("ride_status");
                            assignedRideStatus = status == null ? "accepted" : String.valueOf(status);
                            if (countDownTimer != null) countDownTimer.cancel();
                            onlineswitch.setEnabled(false);
                            acceptRide.setText(R.string.open_accepted_ride);
                            openAssignedRide();
                            return;
                        }
                        Toast.makeText(getApplicationContext(), getString(R.string.offer_submitted), Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        String errorCode = offerResponse == null ? null : String.valueOf(offerResponse.get("error_code"));
                        if (isSubscriptionRestrictionCode(errorCode)) {
                            String message = offerResponse == null ? null : String.valueOf(offerResponse.get(Constants.TAG_MESSSAGE));
                            showSubscriptionRestriction(message, true);
                            return;
                        }
                        String message = offerResponse == null ? getString(R.string.something_wrong) : String.valueOf(offerResponse.get(Constants.TAG_MESSSAGE));
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                acceptingRide = false;
                acceptRide.setEnabled(true);
                Toast.makeText(TimerActivity.this, R.string.something_wrong, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openAssignedRide() {
        acceptingRide = true;
        acceptRide.setEnabled(false);
        HashMap<String, String> params = new HashMap<>();
        params.put("user_id", GetSet.getUserId());
        params.put("onride_id", rideid);
        params.put("type", assignedRideStatus);
        apiInterface.ridedetails(GetSet.getToken(), params).enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                if (isFinishing() || isDestroyed()) return;
                HashMap<String, String> data = response.body();
                if (data != null && Constants.TRUE.equals(data.get(Constants.TAG_STATUS))) {
                    data.put(Constants.TAG_ONRIDE_ID, rideid);
                    data.put("from", assignedRideStatus);
                    startActivity(new Intent(TimerActivity.this, MapsActivity.class)
                            .putExtra("data", data).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    finish();
                } else {
                    showRideDetailsRetry();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable error) {
                if (!isFinishing() && !isDestroyed()) showRideDetailsRetry();
            }
        });
    }

    private void showRideDetailsRetry() {
        acceptingRide = false;
        acceptRide.setEnabled(true);
        Toast.makeText(this, R.string.accepted_ride_load_failed, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.acceptRide) {
            acceptRide();
        }
    }

    private String sanitizeIdValue(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replace("\"", "");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationNames.shutdownNow();
        for (Call<HashMap<String, String>> call : estimateCalls) call.cancel();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
