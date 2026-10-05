package com.driver.cabiappdriver;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentActivity;

import com.driver.helper.DirectionsJSONParser;
import com.driver.helper.LocaleManager;
import com.driver.model.DemandHeatmapData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.gms.tasks.OnSuccessListener;
import com.driver.cabiappdriver.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static com.google.android.gms.maps.model.JointType.ROUND;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private static final String PREF_DEST_DISTANCE = "destdistance";
    private static final String PREF_DEST_DISTANCE_RIDE_ID = "destdistance_onride_id";
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static float pickupdistance, destinationdistance, distance;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    static ApiInterface apiInterface;
    Handler handler = new Handler();
    RelativeLayout arrivenowLay, beginrideLay, completerideLay;
    TextView userName, address, arrivingNow, beginride, navigate, navigate2, sosAlert, complete, dropaddress, map_Call, map_call_2, demandHeatmapStatus;
    FrameLayout rootFrame;
    String parsedDistance = "0";
    LinearLayout rootLayout;
    String mobile_number = "";

    EditText otp;
    Animation slideup, slidedown;
    HashMap<String, String> rideData = new HashMap<>();
    ArrayList markerPoints = new ArrayList();
    Double preLat, preLng, postLat, postLng;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback liveLocationCallback;
    private LocationRequest liveLocationRequest;
    private Socket mSocket;
    private GoogleMap mMap;
    private List<LatLng> listLatLng = new ArrayList<>();
    private final List<Circle> demandCircles = new ArrayList<>();
    private final List<Marker> demandMarkers = new ArrayList<>();
    private Marker driverRouteMarker;
    private Polyline blackPolyLine, greyPolyLine;
    private boolean waitingForRideRoute = true;
    private final Runnable demandHeatmapRefresh = new Runnable() {
        @Override
        public void run() {
            loadDemandHeatmap();
            handler.postDelayed(this, 60000);
        }
    };
    private final Emitter.Listener socketConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v(TAG, "Ride location socket connected");
        }
    };
    private final Emitter.Listener socketDisconnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.v(TAG, "Ride location socket disconnected");
        }
    };
    private final Emitter.Listener socketErrorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.e(TAG, "Ride location socket error: " + (args != null && args.length > 0 ? args[0] : ""));
        }
    };


    private boolean callRequestInProgress = false;
    GoogleApiClient mGoogleApiClient;
    Animator.AnimatorListener polyLineAnimationListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animator) {

        }

        @Override
        public void onAnimationEnd(Animator animator) {

            List<LatLng> blackLatLng = blackPolyLine.getPoints();
            List<LatLng> greyLatLng = greyPolyLine.getPoints();

            greyLatLng.clear();
            greyLatLng.addAll(blackLatLng);
            blackLatLng.clear();
            blackPolyLine.setPoints(blackLatLng);
            greyPolyLine.setPoints(greyLatLng);
            blackPolyLine.setZIndex(2);
            animator.start();
        }

        @Override
        public void onAnimationCancel(Animator animator) {

        }

        @Override
        public void onAnimationRepeat(Animator animator) {


        }
    };
    private Runnable pickupdist = new Runnable() {
        @Override
        public void run() {
            try {
                preLat = postLat;
                preLng = postLng;

                postLat = MainActivity.lat;
                postLng = MainActivity.lng;

                pickupdistance = pickupdistance + getDistance(preLat, preLng, postLat, postLng);

                editor.putFloat("pickupdistance", pickupdistance);
                editor.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(pickupdist, 30000);
        }
    };
    private Runnable dropdist = new Runnable() {
        @Override
        public void run() {
            try {
                updateDropDistanceFromCurrentLocation();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handler.postDelayed(dropdist, 30000);
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        int flags = getWindow().getDecorView().getSystemUiVisibility();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            getWindow().getDecorView().setSystemUiVisibility(flags);
        }

        rootFrame = findViewById(R.id.rootFrame);
        arrivenowLay = findViewById(R.id.arrivenowLay);
        beginrideLay = findViewById(R.id.beginrideLay);
        completerideLay = findViewById(R.id.completerideLay);
        sosAlert = findViewById(R.id.sosAlert);
        arrivingNow = findViewById(R.id.arrivingNow);
        complete = findViewById(R.id.complete);
        otp = findViewById(R.id.otp);
        navigate = findViewById(R.id.navigate);
        navigate2 = findViewById(R.id.navigate2);
        map_Call = findViewById(R.id.map_Call);
        map_call_2 = findViewById(R.id.map_call_2);
        demandHeatmapStatus = findViewById(R.id.demandHeatmapStatus);
        userName = findViewById(R.id.userName);
        address = findViewById(R.id.address);
        dropaddress = findViewById(R.id.dropaddress);
        rootLayout = findViewById(R.id.rootll);

        beginride = findViewById(R.id.beginride);
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        slideup = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        slidedown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        rideData = (HashMap<String, String>) getIntent().getSerializableExtra("data");
        if (!hasRequiredRideData(rideData)) {
            Toast.makeText(this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        initRideLocationSocket();

        double pickupLat = safeCoordinate(rideData.get(Constants.TAG_PICKUP_LAT));
        double pickupLng = safeCoordinate(rideData.get(Constants.TAG_PICKUP_LNG));
        double dropLat = safeCoordinate(rideData.get(Constants.TAG_DROP_LAT));
        double dropLng = safeCoordinate(rideData.get(Constants.TAG_DROP_LNG));
        distance = getDistance(pickupLat, pickupLng, dropLat, dropLng);

        userName.setText(safeText(rideData.get(Constants.TAG_USER_NAME)));
        address.setText(safeText(rideData.get(Constants.TAG_PICKUP_LOCATION)));

        /*if (SplashActivity.displayHeight != 0) {
            ViewGroup.LayoutParams params = rootFrame.getLayoutParams();
            // Changes the height and width to the specified *pixels*
            params.height = SplashActivity.displayHeight;

            rootFrame.setLayoutParams(params);
        }*/


        arrivingNow.setOnClickListener(this);
        beginride.setOnClickListener(this);
        navigate.setOnClickListener(this);
        map_Call.setOnClickListener(this);
        navigate2.setOnClickListener(this);
        complete.setOnClickListener(this);
        sosAlert.setOnClickListener(this);
        sosAlert.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent sos = new Intent(MapsActivity.this, SosAlert.class);
                sos.putExtra("lat", MainActivity.lat);
                sos.putExtra("lon", MainActivity.lng);
                sos.putExtra("onride_id", rideData.get(Constants.TAG_ONRIDE_ID));
                startActivity(sos);
                return true;
            }
        });


        // Obtain the SupportMapFragment and getItem notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        BroadcastReceiver broadcast_reciever = new BroadcastReceiver() {

            @Override
            public void onReceive(Context arg0, Intent intent) {
                String action = intent.getAction();
                if (action.equals("finish")) {
                    Toast.makeText(MapsActivity.this, getString(R.string.ridecancelled), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(broadcast_reciever, new IntentFilter("finish"), Context.RECEIVER_NOT_EXPORTED);
        } else {
            registerReceiver(broadcast_reciever, new IntentFilter("finish"));
        }
    }

    private boolean hasRequiredRideData(Map<String, String> data) {
        return data != null
                && isValidCoordinate(data.get(Constants.TAG_PICKUP_LAT))
                && isValidCoordinate(data.get(Constants.TAG_PICKUP_LNG))
                && isValidCoordinate(data.get(Constants.TAG_DROP_LAT))
                && isValidCoordinate(data.get(Constants.TAG_DROP_LNG))
                && data.get(Constants.TAG_ONRIDE_ID) != null
                && !data.get(Constants.TAG_ONRIDE_ID).trim().isEmpty();
    }

    private boolean isValidCoordinate(String value) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private double safeCoordinate(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private void startDropDistanceTracking(boolean resetDistance) {
        handler.removeCallbacks(pickupdist);
        handler.removeCallbacks(dropdist);

        String onrideId = rideData.get(Constants.TAG_ONRIDE_ID);
        String trackedRideId = pref.getString(PREF_DEST_DISTANCE_RIDE_ID, "");
        boolean shouldResetDistance = resetDistance || onrideId == null || !onrideId.equals(trackedRideId);

        if (shouldResetDistance) {
            destinationdistance = 0f;
            editor.putFloat(PREF_DEST_DISTANCE, destinationdistance);
            editor.putString(PREF_DEST_DISTANCE_RIDE_ID, onrideId);
            editor.apply();
        } else {
            destinationdistance = Math.max(destinationdistance, pref.getFloat(PREF_DEST_DISTANCE, 0f));
        }

        if (hasUsableLocation(MainActivity.lat, MainActivity.lng)) {
            preLat = MainActivity.lat;
            preLng = MainActivity.lng;
            postLat = MainActivity.lat;
            postLng = MainActivity.lng;
        }

        handler.postDelayed(dropdist, 30000);
    }

    private void updateDropDistanceFromCurrentLocation() {
        if (!hasUsableLocation(MainActivity.lat, MainActivity.lng)) {
            return;
        }

        if (!hasUsableLocation(postLat, postLng)) {
            preLat = MainActivity.lat;
            preLng = MainActivity.lng;
            postLat = MainActivity.lat;
            postLng = MainActivity.lng;
            return;
        }

        preLat = postLat;
        preLng = postLng;
        postLat = MainActivity.lat;
        postLng = MainActivity.lng;

        float travelled = getDistance(preLat, preLng, postLat, postLng);
        if (travelled > 0f) {
            destinationdistance = destinationdistance + travelled;
            editor.putFloat(PREF_DEST_DISTANCE, destinationdistance);
            editor.apply();
        }
    }

    private float getBillableCompletedDistance() {
        updateDropDistanceFromCurrentLocation();
        float trackedDistance = Math.max(destinationdistance, pref.getFloat(PREF_DEST_DISTANCE, 0f));
        return Math.max(trackedDistance, 1f);
    }

    private boolean hasUsableLocation(double lat, double lng) {
        return !Double.isNaN(lat) && !Double.isNaN(lng)
                && !Double.isInfinite(lat) && !Double.isInfinite(lng)
                && !(lat == 0d && lng == 0d);
    }

    private String safeText(String value) {
        return value == null ? "" : value;
    }

    public Float getDistance(final double lat1, final double lon1, final double lat2, final double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return Math.max(results[0] / 1000f, 0f);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setRotateGesturesEnabled(false);
        try {
            // Customise the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle));
            if (!success) {
                Log.e(TAG, "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Can't find style. Error: ", e);
        }

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                //Location Permission already granted
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
            } else {
                //Request Location Permission
                checkLocationPermission();
            }
        } else {
            buildGoogleApiClient();

        }

        if (rideData.get("from") != null && rideData.get("from").equals("ontheway")) {
            arrivenowLay.setVisibility(View.GONE);
            arrivenowLay.startAnimation(slidedown);
            beginrideLay.setVisibility(View.VISIBLE);
            beginrideLay.startAnimation(slideup);
            mMap.setPadding(0, 0, 0, beginrideLay.getHeight());
            postLat = MainActivity.lat;
            postLng = MainActivity.lng;
            //    handler.post(pickupdist);
            showRideRoute();
        } else if (rideData.get("from") != null && rideData.get("from").equals("onride")) {

            startDropDistanceTracking(false);
            changeStatus("onride");
            arrivenowLay.setVisibility(View.GONE);
            beginrideLay.setVisibility(View.GONE);
            beginrideLay.startAnimation(slidedown);
            completerideLay.setVisibility(View.VISIBLE);
            completerideLay.startAnimation(slideup);
            mMap.setPadding(0, 0, 0, completerideLay.getHeight());

            dropaddress.setText(safeText(rideData.get("drop_location")));

            showRideRoute();

        } else {
            arrivenowLay.setVisibility(View.VISIBLE);
            mMap.setPadding(0, 0, 0, arrivenowLay.getHeight());
            TransitionManager.beginDelayedTransition(rootFrame);
            /*updateLocation(MainActivity.lat, MainActivity.lng, Double.valueOf(rideData.get(Constants.TAG_PICKUP_LAT)),
                    Double.valueOf(rideData.get(Constants.TAG_PICKUP_LNG)));*/
            showRideRoute();
            map_call_2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showCallOptions();
                }
            });

        }
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        createLiveLocationRequest();
        createLiveLocationCallback();
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            MainActivity.lat = location.getLatitude();
                            MainActivity.lng = location.getLongitude();
                            shareLiveLocation(location);
                            showRideRoute();
                        }
                    }
                });
        startLiveLocationUpdates();
        loadDemandHeatmap();
        handler.removeCallbacks(demandHeatmapRefresh);
        handler.postDelayed(demandHeatmapRefresh, 60000);
    }

    private void showRideRoute() {
        boolean rideStarted = "onride".equals(rideData.get("from"));
        double pickupLat = safeCoordinate(rideData.get(Constants.TAG_PICKUP_LAT));
        double pickupLng = safeCoordinate(rideData.get(Constants.TAG_PICKUP_LNG));
        double destinationLat = rideStarted ? safeCoordinate(rideData.get(Constants.TAG_DROP_LAT)) : pickupLat;
        double destinationLng = rideStarted ? safeCoordinate(rideData.get(Constants.TAG_DROP_LNG)) : pickupLng;

        // Directions must start from the driver's real location. Before the first
        // GPS fix, falling back to pickup made origin and destination identical.
        if (!hasUsableLocation(MainActivity.lat, MainActivity.lng)
                || !hasUsableLocation(destinationLat, destinationLng)) {
            waitingForRideRoute = true;
            return;
        }

        waitingForRideRoute = false;
        updateLocation(MainActivity.lat, MainActivity.lng,
                destinationLat,
                destinationLng,
                rideStarted);
    }

    private void initRideLocationSocket() {
        ApplicationClass app = (ApplicationClass) getApplication();
        mSocket = app.getSocket();
        if (mSocket == null) {
            return;
        }
        mSocket.off(Socket.EVENT_CONNECT, socketConnectListener);
        mSocket.off(Socket.EVENT_DISCONNECT, socketDisconnectListener);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, socketErrorListener);
        mSocket.on(Socket.EVENT_CONNECT, socketConnectListener);
        mSocket.on(Socket.EVENT_DISCONNECT, socketDisconnectListener);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, socketErrorListener);
        if (!mSocket.connected()) {
            mSocket.connect();
        }
    }

    private void createLiveLocationRequest() {
        liveLocationRequest = new LocationRequest();
        liveLocationRequest.setInterval(3000);
        liveLocationRequest.setFastestInterval(2000);
        liveLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void createLiveLocationCallback() {
        liveLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                MainActivity.lat = location.getLatitude();
                MainActivity.lng = location.getLongitude();
                shareLiveLocation(location);
                if (waitingForRideRoute) {
                    showRideRoute();
                }
            }
        };
    }

    private void startLiveLocationUpdates() {
        if (mFusedLocationClient == null || liveLocationRequest == null || liveLocationCallback == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationClient.requestLocationUpdates(liveLocationRequest, liveLocationCallback, getMainLooper());
    }

    private void shareLiveLocation(Location location) {
        if (mSocket == null || location == null || GetSet.getUserId() == null) {
            return;
        }
        try {
            if (!mSocket.connected()) {
                mSocket.connect();
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("user_id", GetSet.getUserId().trim());
            jsonObject.put("location", "");
            jsonObject.put("lat", String.valueOf(location.getLatitude()));
            jsonObject.put("lng", String.valueOf(location.getLongitude()));
            mSocket.emit("sharelocation", jsonObject);
            Log.v(TAG, "Ride shareLocation: " + jsonObject.toString());
        } catch (JSONException e) {
            Log.e(TAG, "Unable to share ride location", e);
        }
    }

    private void stopLiveLocationUpdates() {
        if (mFusedLocationClient != null && liveLocationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(liveLocationCallback);
        }
        if (mSocket != null) {
            mSocket.off(Socket.EVENT_CONNECT, socketConnectListener);
            mSocket.off(Socket.EVENT_DISCONNECT, socketDisconnectListener);
            mSocket.off(Socket.EVENT_CONNECT_ERROR, socketErrorListener);
        }
    }

    private void loadDemandHeatmap() {
        if (apiInterface == null || mMap == null) {
            return;
        }
        if (demandHeatmapStatus != null) {
            demandHeatmapStatus.setVisibility(View.VISIBLE);
            demandHeatmapStatus.setText(R.string.demand_heatmap_loading);
        }

        apiInterface.getDemandHeatmap(GetSet.getToken()).enqueue(new Callback<DemandHeatmapData>() {
            @Override
            public void onResponse(Call<DemandHeatmapData> call, Response<DemandHeatmapData> response) {
                DemandHeatmapData body = response.body();
                if (body != null && body.data != null && body.data.size() > 0) {
                    renderDemandHeatmap(body.data);
                } else {
                    clearDemandHeatmap();
                    if (demandHeatmapStatus != null) {
                        demandHeatmapStatus.setText(R.string.demand_heatmap_empty);
                    }
                }
            }

            @Override
            public void onFailure(Call<DemandHeatmapData> call, Throwable t) {
                call.cancel();
                if (demandHeatmapStatus != null) {
                    demandHeatmapStatus.setVisibility(View.GONE);
                }
            }
        });
    }

    private void clearDemandHeatmap() {
        for (Circle circle : demandCircles) {
            circle.remove();
        }
        demandCircles.clear();
        for (Marker marker : demandMarkers) {
            marker.remove();
        }
        demandMarkers.clear();
    }

    private void renderDemandHeatmap(ArrayList<DemandHeatmapData.Hotspot> hotspots) {
        clearDemandHeatmap();
        int surgeCount = 0;
        int renderedCount = 0;

        for (DemandHeatmapData.Hotspot hotspot : hotspots) {
            double lat = parseDouble(hotspot.latestLat);
            double lng = parseDouble(hotspot.latestLng);
            if (lat == 0 || lng == 0) {
                continue;
            }

            LatLng center = new LatLng(lat, lng);
            int projectedCount = Math.max(hotspot.projected15MinCount, hotspot.count);
            demandCircles.add(mMap.addCircle(new CircleOptions()
                    .center(center)
                    .radius(radiusForDemand(projectedCount))
                    .fillColor(fillColorForIntensity(hotspot.intensity))
                    .strokeColor(strokeColorForIntensity(hotspot.intensity))
                    .strokeWidth(3f)
                    .zIndex(1f)));

            if (hotspot.projectedSurge) {
                surgeCount += 1;
                demandCircles.add(mMap.addCircle(new CircleOptions()
                        .center(center)
                        .radius(radiusForDemand(projectedCount) + 180)
                        .fillColor(Color.TRANSPARENT)
                        .strokeColor(Color.argb(220, 255, 152, 0))
                        .strokeWidth(5f)
                        .zIndex(2f)));
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(center)
                        .title("Projected surge in 15 min")
                        .snippet((hotspot.latestLocation == null ? "Demand zone" : hotspot.latestLocation)
                                + " | Requests: " + hotspot.count
                                + " | Drivers: " + hotspot.driverCount));
                if (marker != null) {
                    demandMarkers.add(marker);
                }
            }
            renderedCount += 1;
        }

        if (demandHeatmapStatus != null) {
            if (renderedCount == 0) {
                demandHeatmapStatus.setText(R.string.demand_heatmap_empty);
            } else {
                demandHeatmapStatus.setText("Demand zones: " + renderedCount + " | 15m surge: " + surgeCount);
            }
            demandHeatmapStatus.setVisibility(View.VISIBLE);
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (Exception ignored) {
            return 0;
        }
    }

    private double radiusForDemand(int count) {
        return Math.min(1200, 250 + Math.max(count, 1) * 80);
    }

    private int fillColorForIntensity(String intensity) {
        if ("high".equalsIgnoreCase(intensity)) {
            return Color.argb(95, 229, 57, 53);
        }
        if ("medium".equalsIgnoreCase(intensity)) {
            return Color.argb(85, 255, 152, 0);
        }
        return Color.argb(75, 67, 160, 71);
    }

    private int strokeColorForIntensity(String intensity) {
        if ("high".equalsIgnoreCase(intensity)) {
            return Color.rgb(198, 40, 40);
        }
        if ("medium".equalsIgnoreCase(intensity)) {
            return Color.rgb(239, 108, 0);
        }
        return Color.rgb(46, 125, 50);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {


        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(MapsActivity.this, ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    //Toast.makeText(MapsActivity.this, "permission denied", Toast.LENGTH_LONG).show();
                }
                return;
            }
            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private void updateLocation(Double startlat, Double startlng, Double endlat, Double endlng, boolean includeRideStops) {
        try {
            Bitmap pickupBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.pickup_marker);
            pickupBitmap = scaleBitmap(pickupBitmap, 50, 100);
            Bitmap dropBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.drop_marker);
            dropBitmap = scaleBitmap(dropBitmap, 50, 100);

            LatLng startlatlng = new LatLng(startlat, startlng);
            LatLng droplatng = new LatLng(endlat, endlng);

            Log.e("gettlocation", "=start lat " + startlat + " ," + startlng + " droplng " + endlat + "," + endlng);
            markerPoints.clear();
            markerPoints.add(startlatlng);
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            MarkerOptions options = new MarkerOptions();
            options.position(startlatlng);
            options.icon(BitmapDescriptorFactory.fromBitmap(pickupBitmap));
            builder.include(startlatlng);
            mMap.addMarker(options);
            markerPoints.add(droplatng);
            options.position(droplatng);
            builder.include(droplatng);
            options.icon(BitmapDescriptorFactory.fromBitmap(dropBitmap));
            mMap.addMarker(options);
            List<LatLng> stopPoints = includeRideStops
                    ? getRouteStopLatLngs(startlatlng, droplatng)
                    : new ArrayList<LatLng>();
            addStopMarkers(builder, stopPoints);
            addDriverRouteMarker(builder);

            //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation,12));

            requestRoadRoute(startlatlng, droplatng, stopPoints);

            // begin new code:
            int width = getResources().getDisplayMetrics().widthPixels;
            int height = getResources().getDisplayMetrics().heightPixels;
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            mMap.moveCamera(cu);
            mMap.animateCamera(cu);
        } catch (Exception e) {
            //  updateLocation(startlat, startlng, endlat, endlng);
            e.printStackTrace();
        }
    }

    private void requestRoadRoute(final LatLng origin, final LatLng destination, List<LatLng> waypoints) {
        HashMap<String, String> params = new HashMap<>();
        params.put(Constants.TAG_USER_ID, GetSet.getUserId());
        params.put("pickup_lat", String.valueOf(origin.latitude));
        params.put("pickup_lng", String.valueOf(origin.longitude));
        params.put("drop_lat", String.valueOf(destination.latitude));
        params.put("drop_lng", String.valueOf(destination.longitude));
        JSONArray stops = new JSONArray();
        for (LatLng waypoint : waypoints) {
            JSONObject stop = new JSONObject();
            try {
                stop.put("latitude", waypoint.latitude);
                stop.put("longitude", waypoint.longitude);
                stops.put(stop);
            } catch (JSONException ignored) {
            }
        }
        params.put("ride_stops", stops.toString());

        apiInterface.routePath(GetSet.getToken(), params).enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                HashMap<String, String> body = response.body();
                String encoded = body == null ? null : body.get("encoded_polyline");
                if (body == null || !Constants.TRUE.equals(body.get(Constants.TAG_STATUS)) || encoded == null || encoded.isEmpty()) {
                    String message = body == null ? null : body.get("message");
                    Toast.makeText(MapsActivity.this,
                            message == null || message.trim().isEmpty() ? "Road route is temporarily unavailable" : message,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                List<LatLng> routePoints = new DirectionsJSONParser().decodePolyline(encoded);
                if (routePoints.size() < 2) {
                    Toast.makeText(MapsActivity.this, "Road route is temporarily unavailable", Toast.LENGTH_SHORT).show();
                    return;
                }
                drawRoadRoute(routePoints);
            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable throwable) {
                String message = throwable == null ? null : throwable.getMessage();
                Toast.makeText(MapsActivity.this,
                        message == null || message.trim().isEmpty() ? "Road route is temporarily unavailable" : message,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void drawRoadRoute(List<LatLng> routePoints) {
        if (blackPolyLine != null) {
            blackPolyLine.remove();
        }
        if (greyPolyLine != null) {
            greyPolyLine.remove();
            greyPolyLine = null;
        }
        listLatLng.clear();
        listLatLng.addAll(routePoints);
        PolylineOptions routeStyle = new PolylineOptions()
                .addAll(routePoints)
                .width(12)
                .color(getResources().getColor(R.color.colorPrimary))
                .startCap(new SquareCap())
                .endCap(new SquareCap())
                .jointType(ROUND);
        blackPolyLine = mMap.addPolyline(routeStyle);

        LatLngBounds.Builder routeBounds = new LatLngBounds.Builder();
        for (LatLng point : routePoints) {
            routeBounds.include(point);
        }
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(
                routeBounds.build(),
                (int) (getResources().getDisplayMetrics().widthPixels * 0.12));
        mMap.animateCamera(cameraUpdate);
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {
        return getDirectionsUrl(origin, dest, new ArrayList<LatLng>());
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest, List<LatLng> waypoints) {

        // Origin of route
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;

        // Destination of route
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude;

        // Sensor enabled
        String sensor = "sensor=false";
        String mode = "mode=driving";

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode;
        if (waypoints != null && !waypoints.isEmpty()) {
            StringBuilder waypointParam = new StringBuilder("&waypoints=");
            for (int i = 0; i < waypoints.size(); i++) {
                if (i > 0) {
                    waypointParam.append("%7C");
                }
                LatLng point = waypoints.get(i);
                waypointParam.append("via:").append(point.latitude).append(",").append(point.longitude);
            }
            parameters += waypointParam.toString();
        }

        // Output format
        String output = "json";

        // Building the url to the web service
//        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + BuildConfig.API_KEY;  //google_api_key
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + BuildConfig.API_KEY;
        return url;
    }

    private void addStopMarkers(LatLngBounds.Builder builder, List<LatLng> routeStopPoints) {
        String rideStopsJson = rideData.get("ride_stops");
        if (rideStopsJson == null || rideStopsJson.trim().isEmpty()) {
            return;
        }
        try {
            JSONArray stops = new JSONArray(rideStopsJson);
            for (int i = 0; i < stops.length(); i++) {
                JSONObject stop = stops.optJSONObject(i);
                if (stop == null) {
                    continue;
                }
                double lat = parseDouble(stop.optString("lat"));
                double lng = parseDouble(stop.optString("lng"));
                if (lat == 0 || lng == 0) {
                    continue;
                }
                LatLng stopLatLng = new LatLng(lat, lng);
                if (!containsRouteStop(routeStopPoints, stopLatLng)) {
                    continue;
                }
                builder.include(stopLatLng);
                mMap.addMarker(new MarkerOptions()
                        .position(stopLatLng)
                        .title("Stop " + stop.optString("sequence", String.valueOf(i + 1)))
                        .snippet(stop.optString("location") + " | Fare +" + stop.optString("fare_increment", "0"))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid ride stops: " + e.getMessage());
        }
    }

    private List<LatLng> getRouteStopLatLngs(LatLng pickup, LatLng drop) {
        ArrayList<JSONObject> stops = new ArrayList<>();
        String rideStopsJson = rideData.get("ride_stops");
        if (rideStopsJson == null || rideStopsJson.trim().isEmpty()) {
            return new ArrayList<>();
        }
        try {
            JSONArray jsonStops = new JSONArray(rideStopsJson);
            for (int i = 0; i < jsonStops.length(); i++) {
                JSONObject stop = jsonStops.optJSONObject(i);
                if (stop != null) {
                    stops.add(stop);
                }
            }
        } catch (JSONException e) {
            Log.e(TAG, "Invalid ride stops: " + e.getMessage());
            return new ArrayList<>();
        }

        Collections.sort(stops, new Comparator<JSONObject>() {
            @Override
            public int compare(JSONObject first, JSONObject second) {
                return Integer.compare(first.optInt("sequence", 0), second.optInt("sequence", 0));
            }
        });

        ArrayList<LatLng> points = new ArrayList<>();
        for (JSONObject stop : stops) {
            LatLng point = new LatLng(parseDouble(stop.optString("lat")), parseDouble(stop.optString("lng")));
            if (point.latitude == 0 || point.longitude == 0 || isSamePoint(point, pickup) || isSamePoint(point, drop)) {
                continue;
            }
            if (!containsRouteStop(points, point)) {
                points.add(point);
            }
        }
        return points;
    }

    private boolean containsRouteStop(List<LatLng> points, LatLng target) {
        if (points == null || target == null) {
            return false;
        }
        for (LatLng point : points) {
            if (isSamePoint(point, target)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSamePoint(LatLng first, LatLng second) {
        float[] distance = new float[1];
        Location.distanceBetween(first.latitude, first.longitude, second.latitude, second.longitude, distance);
        return distance[0] < 50;
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();
            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
        } catch (Exception e) {
            Log.e(TAG, "downloadUrlException: " + e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    private void animatePolyLine() {

        ValueAnimator animator = ValueAnimator.ofInt(0, 100);
        animator.setDuration(1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {

                List<LatLng> latLngList = blackPolyLine.getPoints();
                int initialPointSize = latLngList.size();
                int animatedValue = (int) animator.getAnimatedValue();
                int newPoints = (animatedValue * listLatLng.size()) / 100;

                if (initialPointSize < newPoints) {
                    latLngList.addAll(listLatLng.subList(initialPointSize, newPoints));
                    blackPolyLine.setPoints(latLngList);
                }
            }
        });
        animator.addListener(polyLineAnimationListener);
        animator.start();

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.arrivingNow) {
            changeStatus("ontheway");
            arrivenowLay.setVisibility(View.GONE);
            arrivenowLay.startAnimation(slidedown);
            beginrideLay.setVisibility(View.VISIBLE);
            beginrideLay.startAnimation(slideup);
            mMap.setPadding(0, 0, 0, beginrideLay.getHeight());
            postLat = MainActivity.lat;
            postLng = MainActivity.lng;
            // handler.post(pickupdist);
        } else if (id == R.id.beginride) {
            String enteredOtp = otp.getText() != null ? otp.getText().toString().trim() : "";
            if (enteredOtp.isEmpty()) {
                Toast.makeText(MapsActivity.this, getString(R.string.otp_continue), Toast.LENGTH_SHORT).show();
            } else if (!enteredOtp.equals(rideData.get("onride_otp"))) {
                Toast.makeText(MapsActivity.this, getString(R.string.incorrect_otp), Toast.LENGTH_SHORT).show();
            } else {
                startDropDistanceTracking(true);
                rideData.put("from", "onride");
                changeStatus("onride");
                arrivenowLay.setVisibility(View.GONE);
                beginrideLay.setVisibility(View.GONE);
                beginrideLay.startAnimation(slidedown);
                completerideLay.setVisibility(View.VISIBLE);
                completerideLay.startAnimation(slideup);
                mMap.setPadding(0, 0, 0, completerideLay.getHeight());
                dropaddress.setText(rideData.get("drop_location"));
                showRideRoute();

                // temporary hide code
            /*updateLocation(Double.valueOf(rideData.get(Constants.TAG_PICKUP_LAT)),
                    Double.valueOf(rideData.get(Constants.TAG_PICKUP_LNG)),
                    Double.valueOf(rideData.get(Constants.TAG_DROP_LAT)),
                    Double.valueOf(rideData.get(Constants.TAG_DROP_LNG)));*/

            /*Intent intent = new Intent(android.content.Intent.ACTION_VIEW,
                    Uri.parse("http://maps.google.com/maps?saddr=" +
                            rideData.getItem(Constants.TAG_PICKUP_LAT).trim() + "," +
                            rideData.getItem(Constants.TAG_PICKUP_LNG).trim() + "&daddr=" +
                            rideData.getItem(Constants.TAG_DROP_LAT).trim() + "," +
                            rideData.getItem(Constants.TAG_DROP_LNG).trim()));
            startActivity(intent);*/
            }
        } else if (id == R.id.navigate || id == R.id.navigate2) {
            boolean rideStarted = "onride".equals(rideData.get("from"));
            String targetLat = rideStarted
                    ? rideData.get(Constants.TAG_DROP_LAT)
                    : rideData.get(Constants.TAG_PICKUP_LAT);
            String targetLng = rideStarted
                    ? rideData.get(Constants.TAG_DROP_LNG)
                    : rideData.get(Constants.TAG_PICKUP_LNG);
            if (targetLat != null && targetLng != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("google.navigation:q=" + targetLat.trim() + "," + targetLng.trim() + "&mode=d"));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);
            } else {
                Toast.makeText(this, "Location data not available", Toast.LENGTH_SHORT).show();
            }
        } else if (id == R.id.complete) {
            completeRide(String.valueOf(getBillableCompletedDistance()));
        } else if (id == R.id.sosAlert) {
            Intent sos = new Intent(MapsActivity.this, SosAlert.class);
            sos.putExtra("lat", MainActivity.lat);
            sos.putExtra("lon", MainActivity.lng);
            sos.putExtra("onride_id", rideData.get(Constants.TAG_ONRIDE_ID));
            startActivity(sos);
        } else if (id == R.id.map_Call) {
            showCallOptions();
        }
    }


    @Override
    public void onBackPressed() {
        Intent home = new Intent(MapsActivity.this, MainActivity.class);
        home.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(home);
        finish();
    }

    @Override
    protected void onDestroy() {
        stopLiveLocationUpdates();
        handler.removeCallbacks(pickupdist);
        handler.removeCallbacks(dropdist);
        handler.removeCallbacks(demandHeatmapRefresh);
        clearDemandHeatmap();
        super.onDestroy();
    }

    private void changeStatus(String Status) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put("onride_id", rideData.get(Constants.TAG_ONRIDE_ID));
        map.put("status", Status);
        if (Status.equals("onride"))
            map.put("onride_otp", otp.getText().toString().trim());

        Log.v(TAG, "changeStatusParams:" + map);
        Call<Map<String, String>> call3 = apiInterface.changeStatus(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> map = response.body();
                if (map != null && Constants.TRUE.equals(map.get(Constants.TAG_STATUS))) {
                    Log.v(TAG, "changeStatusRes=" + map.get(Constants.TAG_MESSSAGE));
                    //  Toast.makeText(MapsActivity.this, map.getItem(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void completeRide(String distance) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put("onride_id", rideData.get(Constants.TAG_ONRIDE_ID));
        map.put("distance", distance);


        Log.v(TAG, "completeRideParams:" + map);
        Call<Map<String, String>> call3 = apiInterface.completeride(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> map = response.body();
                if (map != null && Constants.TRUE.equals(map.get(Constants.TAG_STATUS))) {
                    handler.removeCallbacks(dropdist);
                    editor.remove(PREF_DEST_DISTANCE);
                    editor.remove(PREF_DEST_DISTANCE_RIDE_ID);
                    editor.apply();
                    completerideLay.setVisibility(View.GONE);
                    completerideLay.startAnimation(slidedown);
                    mMap.setPadding(0, 0, 0, 0);

                    Intent nextIntent = new Intent(MapsActivity.this, RideCompleteActivity.class);
                    nextIntent.putExtra("onride_id", rideData.get(Constants.TAG_ONRIDE_ID));
                    startActivity(nextIntent);
                    finish();
                } else {
                    String message = map == null ? getString(R.string.something_wrong) : map.get(Constants.TAG_MESSSAGE);
                    Toast.makeText(MapsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {
            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList points = null;
            PolylineOptions lineOptions = null;
            MarkerOptions markerOptions = new MarkerOptions();

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList();
                lineOptions = new PolylineOptions();
                List<HashMap<String, String>> path = result.get(i);
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat").toString());
                    double lng = Double.parseDouble(point.get("lng").toString());
                    LatLng position = new LatLng(lat, lng);
                    points.add(position);
                }
                listLatLng.addAll(points);

                lineOptions.width(10);
                lineOptions.color(getResources().getColor(R.color.colorPrimary));
                lineOptions.startCap(new SquareCap());
                lineOptions.endCap(new SquareCap());
                lineOptions.jointType(ROUND);
                blackPolyLine = mMap.addPolyline(lineOptions);
                PolylineOptions greyOptions = new PolylineOptions();
                greyOptions.width(10);
                greyOptions.color(getResources().getColor(R.color.colorAccent));
                greyOptions.startCap(new SquareCap());
                greyOptions.endCap(new SquareCap());
                greyOptions.jointType(ROUND);
                greyPolyLine = mMap.addPolyline(greyOptions);
                animatePolyLine();

            }
        }
    }

    public static Bitmap scaleBitmap(Bitmap bitmap, int newWidth, int newHeight) {
        Bitmap scaledBitmap = Bitmap.createBitmap(newWidth, newHeight, Bitmap.Config.ARGB_8888);

        float scaleX = newWidth / (float) bitmap.getWidth();
        float scaleY = newHeight / (float) bitmap.getHeight();
        float pivotX = 0;
        float pivotY = 0;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(scaleX, scaleY, pivotX, pivotY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bitmap, 0, 0, new Paint(Paint.FILTER_BITMAP_FLAG));

        return scaledBitmap;
    }

    private void addDriverRouteMarker(LatLngBounds.Builder builder) {
        if (MainActivity.lat == 0 || MainActivity.lng == 0) {
            return;
        }
        LatLng driverLatLng = new LatLng(MainActivity.lat, MainActivity.lng);
        builder.include(driverLatLng);
        if (driverRouteMarker != null) {
            driverRouteMarker.remove();
        }
        driverRouteMarker = mMap.addMarker(new MarkerOptions()
                .position(driverLatLng)
                .anchor(0.5f, 0.5f)
                .title("Driver")
                .icon(bitmapDescriptorFromVector(R.drawable.ic_driver_car_marker)));
    }

    private BitmapDescriptor bitmapDescriptorFromVector(int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);
        if (vectorDrawable == null) {
            return BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
        }
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }


    private void makeMaskedCall() {
        if (callRequestInProgress) {
            return;
        }
        String riderId = getRiderIdForCall();
        String onrideId = rideData.get(Constants.TAG_ONRIDE_ID);
        if (onrideId == null || onrideId.trim().isEmpty()) {
            Log.e(TAG, "makeMaskedCallMissingData: riderId=" + riderId + ", rideData=" + rideData);
            Toast.makeText(getApplicationContext(), getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            return;
        }
        callRequestInProgress = true;

        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_SENDER_ID, GetSet.getUserId());
        if (riderId != null && !riderId.trim().isEmpty()) {
            map.put(Constants.TAG_RECEIVER_ID, riderId);
        }
        map.put(Constants.TAG_TYPE, Constants.TAG_AUDIO);
        map.put("user_type", "driver");
        map.put(Constants.TAG_ROOM_ID, "ride_" + onrideId);
        map.put(Constants.TAG_ONRIDE_ID, onrideId);
        map.put("platform", Constants.TAG_ANDROID);
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        Log.v(TAG, "makeMaskedCallParams: " + map);

        apiInterface.makeCall(GetSet.getToken(), map).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Log.v(TAG, "makeMaskedCallRes: " + response.body());
                callRequestInProgress = false;
                if (response.body() != null && "true".equals(response.body().get("status"))) {
                    Toast.makeText(getApplicationContext(), getString(R.string.calling_rider), Toast.LENGTH_SHORT).show();
                    String callRiderId = response.body().get(Constants.TAG_RECEIVER_ID);
                    if (callRiderId == null || callRiderId.trim().isEmpty()) {
                        callRiderId = riderId == null ? "" : riderId;
                    }
                    Intent callIntent = new Intent(MapsActivity.this, CallActivity.class);
                    callIntent.putExtra(Constants.TAG_ROOM_ID, "ride_" + onrideId);
                    callIntent.putExtra(Constants.TAG_ONRIDE_ID, onrideId);
                    callIntent.putExtra(Constants.TAG_USER_ID, callRiderId);
                    callIntent.putExtra(Constants.TAG_FULL_NAME, rideData.get(Constants.TAG_USER_NAME));
                    callIntent.putExtra(Constants.TAG_SEND, true);
                    startActivity(callIntent);
                } else {
                    String message = response.body() == null ? getString(R.string.tryagain) : response.body().get(Constants.TAG_MESSSAGE);
                    Toast.makeText(getApplicationContext(), message == null ? getString(R.string.tryagain) : message, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                Log.e(TAG, "makeMaskedCallError: " + t.getMessage());
                callRequestInProgress = false;
                call.cancel();
                Toast.makeText(getApplicationContext(), getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showCallOptions() {
        String[] options = {getString(R.string.normal_phone_call), getString(R.string.in_app_call)};
        new AlertDialog.Builder(this)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int which) {
                        if (which == 0) {
                            makeNormalPhoneCall(rideData.get(Constants.TAG_PHONE_NUMBER));
                        } else {
                            makeMaskedCall();
                        }
                    }
                })
                .show();
    }

    private void makeNormalPhoneCall(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            Toast.makeText(getApplicationContext(), getString(R.string.phone_number_unavailable), Toast.LENGTH_SHORT).show();
            return;
        }
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + Uri.encode(phoneNumber.trim())));
        startActivity(callIntent);
    }

    private String getRiderIdForCall() {
        String riderId = rideData.get(Constants.TAG_USER_ID);
        if (riderId == null || riderId.trim().isEmpty()) {
            riderId = rideData.get("rideuserid");
        }
        if (riderId == null || riderId.trim().isEmpty()) {
            riderId = rideData.get("rider_id");
        }
        if (riderId == null || riderId.trim().isEmpty()) {
            riderId = rideData.get("receiver_id");
        }
        return riderId;
    }

}
