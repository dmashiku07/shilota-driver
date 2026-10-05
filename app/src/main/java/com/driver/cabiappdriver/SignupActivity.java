package com.driver.cabiappdriver;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.driver.countrypicker.Country_picker;
import com.driver.helper.LocaleManager;
import com.driver.helper.PlaceAutocompleteAdapter;
import com.driver.helper.SharedPrefManager;
import com.driver.cabiappdriver.databinding.ActivitySignupBinding;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import com.driver.model.UserData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends BaseActivity implements View.OnClickListener, PlaceAutocompleteAdapter.PlaceAutoCompleteInterface {
    private static final String TAG = SignupActivity.class.getSimpleName();
    public static int APP_REQUEST_CODE = 99;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    static ApiInterface apiInterface;
    private static LatLngBounds bounds = new LatLngBounds(new LatLng(-0, 0), new LatLng(0, 0));
    ImageView backBtn;
    ActivitySignupBinding binding;
    PlaceAutocompleteAdapter mAdapter;
    Dialog dialog;
    LinearLayoutManager llm;
    String phoneNumber = "", countryCode = "";
    double radiusDegrees = 1.0, lat, lng;
    LatLng center;
    ProgressDialog progressDialog;
    private RecyclerView mRecyclerView;
    private FusedLocationProviderClient mFusedLocationClient;
    private PlacesClient placesClient;
    private AutocompleteSessionToken placeSessionToken;

    private static final String GOOGLE_PAY_PACKAGE_NAME = "com.google.android.apps.nbu.paisa.user";

    RequestQueue queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_signup);
        Places.initialize(this, BuildConfig.API_KEY);

        placesClient = Places.createClient(this);

        backBtn = findViewById(R.id.backBtn);
        binding.name.setFilters(new InputFilter[]{ApplicationClass.EMOJI_FILTER, ApplicationClass.NUMBER_FILTER, new InputFilter.LengthFilter(30)});
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getResources().getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();

        queue = Volley.newRequestQueue(this);

        backBtn.setVisibility(View.VISIBLE);
        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        backBtn.setOnClickListener(this);
        binding.signin.setOnClickListener(this);
        binding.next.setOnClickListener(this);
        binding.location.setOnClickListener(this);

        requestLocationAccessIfNeeded();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == APP_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String countrycode = data.getStringExtra("countrycode");
                String phonenumber = data.getStringExtra("phonenumber");
                Log.e(TAG,"PHONE NUMBER : " + countrycode +"," + phonenumber);
                signin(phonenumber, countrycode);
                // Successfully signed in
              /*  FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null && user.getPhoneNumber() != null) {
                    PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                    Log.d(TAG, "onActivityResult: " + user.getPhoneNumber());
                    try {
                        Phonenumber.PhoneNumber numberProto = phoneUtil.parse(user.getPhoneNumber(), null);

                        signin("" + numberProto.getNationalNumber(), "" + numberProto.getCountryCode());

                    } catch (NumberParseException e) {
                        Log.d(TAG, "NumberParseException: " + e.getMessage());
                        ApplicationClass.showToast(SignupActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT);
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
    }

    private void requestLocationAccessIfNeeded() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fetchLastKnownLocation();
            return;
        }

        ActivityCompat.requestPermissions(
                this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    private void fetchLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            lat = location.getLatitude();
                            lng = location.getLongitude();
                            LatLng northEast = new LatLng(lat, lng);
                            LatLng southWest = new LatLng(lat, lng);
                            bounds = LatLngBounds.builder()
                                    .include(northEast)
                                    .include(southWest)
                                    .build();
                        }
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLastKnownLocation();
            } else {
                Toast.makeText(this, getString(R.string.enable_location_permission), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void signup() {
        progressDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_FULL_NAME, binding.name.getText().toString());
        map.put(Constants.TAG_EMAIL, binding.email.getText().toString());
        map.put(Constants.TAG_PASSWORD, binding.password.getText().toString());
        map.put(Constants.TAG_LOCATION, binding.location.getText().toString());
        map.put(Constants.TAG_LATITUDE, String.valueOf(lat));
        map.put(Constants.TAG_LONGITUDE, String.valueOf(lng));

        Log.v(TAG, "signUpParams" + map);
        Call<HashMap<String, String>> call3 = apiInterface.signup(map);
        call3.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                try {
                    HashMap<String, String> data = response.body();
                    if (data.get(Constants.TAG_STATUS).equals("true")) {
                      //  verifyMobileNo();
                        Intent intent = new Intent(SignupActivity.this, ActivityEnterPhoneNumber.class);
                        startActivityForResult(intent, APP_REQUEST_CODE);
                    } else if (data.get(Constants.TAG_STATUS).equals("false")) {
                        Toast.makeText(SignupActivity.this, data.get(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {

                    e.printStackTrace();
                }
                progressDialog.dismiss();

            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                progressDialog.dismiss();
                call.cancel();
            }
        });
    }

    public void signin(String phoneNumber, String countryCode) {
        progressDialog.show();
        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_EMAIL, binding.email.getText().toString());
        map.put(Constants.TAG_PASSWORD, binding.password.getText().toString());
        map.put(Constants.TAG_COUNTRY_CODE, countryCode);
        map.put(Constants.TAG_PHONE_NUMBER, phoneNumber);
        Log.v(TAG, "signInParams:" + map);
        Call<UserData> call3 = apiInterface.signin(map);
        call3.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                try {
                    UserData userdata = response.body();
                    progressDialog.dismiss();


                    if (userdata.status.equals("true")) {

                        // A newly registered driver cannot have completed vehicle details yet.
                        // Do not reuse a stale static/persisted status from a previous session.
                        VehicleInspectionActivity.vehicledetail = "InComplete";
                        VehicleInspectionActivity.paymentdetail = userdata.payment_details;
                        VehicleInspectionActivity.securitydeposit = userdata.security_deposit;
                        VehicleInspectionActivity.vehicleinspection = userdata.vehicle_inspection;

                        GetSet.Logingin(userdata.user_id, binding.email.getText().toString(), userdata.full_name, userdata.imageurl, userdata.token, userdata.walletmoney);
                        editor.putBoolean("isLogged", true);
                        editor.putString("userId", userdata.user_id);
                        editor.putString("email", binding.email.getText().toString());
                        editor.putString("userImage", userdata.imageurl);
                        editor.putString("fullName", userdata.full_name);
                        editor.putString("token", userdata.token);
                        editor.putString("wallet", userdata.walletmoney);
                        editor.putString("online", "0");
                        editor.putString("password", userdata.password);
                        editor.putString("admin_approved", userdata.admin_approved);
                        editor.putString("vehicle_approval", userdata.vehicle_approval);
                        editor.putString("vehicle_details", "InComplete");
                        editor.putString("payment_details", userdata.payment_details);
                        editor.putString("security_deposit", userdata.security_deposit);
                        editor.putString("vehicle_inspection", userdata.vehicle_inspection);
                        editor.commit();
                        GetSet.setPassword(userdata.password);
                        GetSet.setToken(userdata.token);
                        GetSet.setWallet(userdata.walletmoney);
                        addDeviceId(SignupActivity.this);

                    } else if (userdata.status.equals("false")) {
                        Toast.makeText(getApplicationContext(), userdata.message, Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {
                    progressDialog.dismiss();
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                Log.e(TAG, "signInError: " + t.getMessage());
                progressDialog.dismiss();
                call.cancel();
            }
        });
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
                /*Intent nextIntent = new Intent(SignupActivity.this, VehicleDetailsActivity.class);
                nextIntent.putExtra("from", "signup");
                startActivity(nextIntent);*/
                Intent nextIntent = new Intent(getApplicationContext(), VehicleInspectionActivity.class);
                startActivity(nextIntent);
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                progressDialog.dismiss();
            }
        });

    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.backBtn) {
            onBackPressed();

        } else if (id == R.id.signin) {
            Intent b = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(b);

        } else if (id == R.id.next) {

            // Clear previous errors
            binding.passwordlay.setError(null);
            binding.emaillay.setError(null);
            binding.namelay.setError(null);
            binding.repasswordlay.setError(null);
            binding.locationlay.setError(null);

            if (binding.name.getText().toString().equals("") || binding.name.getText().toString().length() < 3) {
                binding.namelay.setError(getString(R.string.fullname_error));
                requestFocus(binding.name);

            } else if (binding.email.getText().toString().equals("")) {
                binding.emaillay.setError(getString(R.string.emailempty_error));
                requestFocus(binding.email);

            } else if (!binding.email.getText().toString().matches(Constants.EMAIL_PATTERN)) {
                binding.emaillay.setError(getString(R.string.email_error));
                requestFocus(binding.email);

            } else if (binding.password.getText().toString().equals("")) {
                binding.passwordlay.setError(getString(R.string.passwordempty_error));
                requestFocus(binding.password);

            } else if (binding.password.getText().toString().length() < 6) {
                binding.passwordlay.setError(getString(R.string.password_error));
                requestFocus(binding.password);

            } else if (!binding.repassword.getText().toString().equals(binding.password.getText().toString())) {
                binding.repasswordlay.setError(getString(R.string.repassword_error));
                requestFocus(binding.repassword);

            } else if (binding.repassword.getText().toString().length() < 6) {
                binding.repasswordlay.setError(getString(R.string.password_error));
                requestFocus(binding.repassword);

            } else if (binding.location.getText().toString().equals("")) {
                binding.locationlay.setError(getString(R.string.chose_location));
                requestFocus(binding.location);

            } else {
                signup();
            }

        } else if (id == R.id.location) {
            placeSessionToken = AutocompleteSessionToken.newInstance();
            final EditText locationText;
            final ImageView clear, dbackBtn;
            dialog = new Dialog(this, R.style.DialogStyle);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.location_dialog_lay);

            dialog.setCancelable(true);
            dbackBtn = dialog.findViewById(R.id.backBtn);
            dbackBtn.setVisibility(View.VISIBLE);
            if (LocaleManager.isRTL()) {
                dbackBtn.setRotation(180);
            } else {
                dbackBtn.setRotation(0);
            }
            locationText = dialog.findViewById(R.id.locationText);
            clear = dialog.findViewById(R.id.clear);
            mRecyclerView = dialog.findViewById(R.id.list_search);

            mRecyclerView.setHasFixedSize(true);
            llm = new LinearLayoutManager(this);
            mRecyclerView.setLayoutManager(llm);
            mAdapter = new PlaceAutocompleteAdapter(SignupActivity.this, R.layout.view_placesearch,
                    placesClient, bounds);
            mRecyclerView.setAdapter(mAdapter);

            clear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clear.setVisibility(View.GONE);
                    locationText.setText("");
                }
            });

            dbackBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });

            locationText.requestFocus();
            locationText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mRecyclerView.setVisibility(View.VISIBLE);
                    if (count > 0) {
                        clear.setVisibility(View.VISIBLE);
                        if (mAdapter != null) {
                            mAdapter.notifyDataSetChanged();
                        }
                    } else {
                        clear.setVisibility(View.GONE);
                    }
                    if (s.length() > 2)
                        mAdapter.setAutoSuggestions("" + s, placeSessionToken);
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (s.length() == 0) {
                        mAdapter.clearList();
                    }
                }
            });

            if (!binding.location.getText().toString().equals(""))
                locationText.setText(binding.location.getText().toString());

            dialog.show();
        }
    }


    private boolean isGooglePayInstalled() {
        // Check if Google Pay package exists on the device
        Intent intent = getPackageManager().getLaunchIntentForPackage("com.google.android.apps.walletnfcrel");
        return intent != null;
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

    @Override
    public void onPlaceClick(ArrayList<PlaceAutocompleteAdapter.PlaceAutocomplete> mResultList, int position) {
        if (mResultList != null) {
            try {
                final String placeId = String.valueOf(mResultList.get(position).placeId);
                // Specify the fields to return (in this example all fields are returned).
                List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS);

                // Construct a request object, passing the place ID and fields array.
                FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).setSessionToken(placeSessionToken).build();

                placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
                    Place place = response.getPlace();
                    Log.i(TAG, "Place found: " + place.getName());
                    binding.location.setText(place.getAddress());
                    if (place.getLatLng() != null) {
                        lat = place.getLatLng().latitude;
                        lng = place.getLatLng().longitude;
                    }
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        int statusCode = apiException.getStatusCode();
                        // Handle error with given status code.
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                        Toast.makeText(getApplicationContext(), getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                });

                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            } catch (Exception e) {
                Log.e(TAG, "onPlaceClick:Exception " + e.getMessage());
            }

        }
    }


    private void Activate_Account() {

        String url = "https://apiotp.beem.africa/v1/request";
//        String encode_value = encodeToBase64("03895cbc0d4af385:Mjg0MDFhMzIzZjU3MThiZGM3MjY3NzRlMzk4NjQ4NzU2MmQ3OWJiMDNlYmEzMWM1NmMxYzdmNTgyNWNlMTc4MA==\n" +
        String encode_value = encodeToBase64("d76e3f89b7b1d075:NzE1MzZjZjJiYzM2YTgzNDUwYmU1MjY4NTNjNTljOGYwNDkzNjRjYWU2MzVkZmUxZTc5YjU4ZmMzYzU2NTM0ZA==");

        StringRequest req = new StringRequest(Request.Method.POST, url,
                new com.android.volley.Response.Listener<String>() {
                    @Override
                    public void onResponse(String res) {
                      Log.d("onResponsesss",res);
                    }
                }, new com.android.volley.Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
//                VolleyLog.d(TAG, "Error: " + error.getMessage());
//                Log.d("onErrorResponse",error.getMessage());


                NetworkResponse response = error.networkResponse;
                if (error instanceof ServerError && response != null) {
                    try {
                        String res = new String(response.data,
                                HttpHeaderParser.parseCharset(response.headers, "utf-8"));
                        // Now you can use any deserializer to make sense of data
                        Log.d(TAG, "onErrorResponse1: " + res);
                        JSONObject obj = new JSONObject(res);
                        Log.d(TAG, "onErrorResponse2: " + obj);
                    } catch (UnsupportedEncodingException e1) {
                        // Couldn't properly decode data to string
                        e1.printStackTrace();
                    } catch (JSONException e2) {
                        // returned data is not JSONObject?
                        e2.printStackTrace();
                    }
                }
                VolleyLog.d(TAG, "payBraintreeError:3 " + error.getMessage());


            }

        }) {

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> map = new HashMap<String, String>();
//                map.put(Constants.TAG_AUTHORIZATION, pref.getString(Constants.TAG_AUTHORIZATION, ""));


                map.put("Authorization", "Basic " + encode_value);


//                map.put("Content-Type", "application/json");
//                map.put("Authorization", "Bearer " + "03895cbc0d4af385");

//                "Content-Type": "application/json",
//                        "Authorization": "Bearer YOUR_API_KEY"

//                Log.v(TAG, "Authorization=" + map);
                return map;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("appId", "1728");
                map.put("msisdn", "919659606838");
//                Log.v(TAG, "createChatParams=" + map);
                return map;
            }
        };

//        getApplication().getInstance().addToRequestQueue(req, TAG);
        queue.add(req);
    }


//    private void Activate_Account() {
//        RequestQueue queue = Volley.newRequestQueue(this);
//        String url = "https://apiotp.beem.africa/v1/request";
//        String encode_value = encodeToBase64("03895cbc0d4af385:Mjg0MDFhMzIzZjU3MThiZGM3MjY3NzRlMzk4NjQ4NzU2MmQ3OWJiMDNlYmEzMWM1NmMxYzdmNTgyNWNlMTc4MA==");
//
//        // Request a string response from the provided URL
//        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
//                new com.android.volley.Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//
//                    }
//                }, new com.android.volley.Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//
//            }
//        }) {
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
////                String encode_value = encodeToBase64("01626feb88290f0c:ZDJhMmMzMmUxZGY3YmZkZTk5YTA5MmQyNmUwZmE1ZDk5NTRmZTg5YzI5MjM0MjIzZTk0NzU3YzYwY2Q0OTRhMA==");
//
////
////                map.put("api_key", "03895cbc0d4af385");
////                map.put("secret_key", "Mjg0MDFhMzIzZjU3MThiZGM3MjY3NzRlMzk4NjQ4NzU2MmQ3OWJiMDNlYmEzMWM1NmMxYzdmNTgyNWNlMTc4MA==");
////                map.put("Content-Type", "Application/json");
//                map.put("Authorization", "Basic " + encode_value);
//
//                Log.d("encodeToBase64",encode_value);
//
//                return map;
//            }
//
//            @Nullable
//            @Override
//            protected Map<String, String> getParams() throws AuthFailureError {
//                Map<String, String> map = new HashMap<String, String>();
//                map.put("appId", "1715");
//                map.put("msisdn", "919659606838");
//                return map;
//            }
//        };
//
//
//        // Add the request to the RequestQueue
//        queue.add(stringRequest);
//    }

    public static String encodeToBase64(String originalString) {
        // Encode the string to Base64
//        byte[] bytesEncoded = Base64.encode(originalString.getBytes(), Base64.DEFAULT);
        String bytesEncoded = Base64.encodeToString(originalString.getBytes(), Base64.NO_WRAP);
        return bytesEncoded;
    }
}
