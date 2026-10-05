package com.driver.cabiappdriver;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Rect;
import androidx.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.OpenableColumns;

import com.driver.external.ImagePicker;
import com.driver.helper.LocaleManager;
import com.driver.helper.NetworkReceiver;
import com.driver.cabiappdriver.databinding.ActivityVehicleDetailsBinding;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.Toast;

import com.driver.cabiappdriver.R;
import com.driver.model.AdminData;
import com.driver.model.UserData;
import com.driver.model.VehicleData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * Created by hitasoft on 24/3/18.
 */

public class VehicleDetailsActivity extends BaseActivity implements View.OnClickListener, DatePickerDialog.OnDateSetListener,
        NetworkReceiver.ConnectivityReceiverListener {
    public static final int REQUEST_BODY_TYPE = 100, REQUEST_AMENITIES = 200;
    private static final String TAG = VehicleDetailsActivity.class.getSimpleName();
    public static UserData data = null;
    ActivityVehicleDetailsBinding detailsBinding;
    ApiInterface apiInterface;
    String dateselectedfor;
    ImageView backBtn;
    ProgressDialog progressDialog;
    VehicleData vehicleData;
    String uploadfor;
    DateFormat dateFormatter = new SimpleDateFormat("dd/MM/yyyy");
    Snackbar snackbar;
    int exit = 0;
    NetworkReceiver receiver;
    private boolean isUploadingDocument = false;
    private SharedPreferences vehiclePrefs;
    private String BselectedName = "", AselectedName = "", selectedbodytype;
    private ArrayList<String> selectedIds = new ArrayList<>(), selectedNames = new ArrayList<>();
    public String[] cameraPermission = new String[]{CAMERA, WRITE_EXTERNAL_STORAGE};
    String[] mimeTypes = {"application/pdf", "image/*"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        setContentView(R.layout.activity_vehicle_details);
        detailsBinding = DataBindingUtil.setContentView(this, R.layout.activity_vehicle_details);
        keepFocusedFieldAboveKeyboard();
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        vehiclePrefs = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        receiver = new NetworkReceiver();
        ApplicationClass.getInstance().setConnectivityListener(this);
        backBtn = findViewById(R.id.backBtn);
        String from = getIntent().getStringExtra("from");
        fillVehicleDetailsFromPrefs();
        getVehicleDetails();
        if (from != null && from.equals("signup")) {
            backBtn.setVisibility(View.GONE);
            detailsBinding.nextBtn.setText(getString(R.string.next));
        } else if (VehicleInspectionActivity.vehicledetail.equals("InComplete")) {
            backBtn.setVisibility(View.VISIBLE);
        } else {
            backBtn.setVisibility(View.VISIBLE);
        }
        if (LocaleManager.isRTL()){
            backBtn.setRotation(180);
            detailsBinding.icon.setRotation(90);
            detailsBinding.amenitiesIcon.setRotation(90);
        } else {
            backBtn.setRotation(0);
            detailsBinding.icon.setRotation(270);
            detailsBinding.amenitiesIcon.setRotation(270);
        }
        snackbar = Snackbar.make(findViewById(R.id.activity_vehicle_details), getString(R.string.exit_msg), Snackbar.LENGTH_SHORT);
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
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.pleasewait));
        progressDialog.setCancelable(false);
        detailsBinding.nextBtn.setOnClickListener(this);
        detailsBinding.licenceExpiryDate.setOnClickListener(this);
        detailsBinding.insuranceExpiryDate.setOnClickListener(this);
        detailsBinding.rcBookExpiryDate.setOnClickListener(this);
        detailsBinding.bodyType.setOnClickListener(this);
        detailsBinding.bodyTypeLay.setOnClickListener(this);
        detailsBinding.icon.setOnClickListener(this);
        detailsBinding.amenities.setOnClickListener(this);
        detailsBinding.amenitiesLay.setOnClickListener(this);
        detailsBinding.amenitiesIcon.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        detailsBinding.licenceDoc.setOnClickListener(this);
        detailsBinding.insuranceDoc.setOnClickListener(this);
        detailsBinding.rcBookDoc.setOnClickListener(this);
    }

    private void keepFocusedFieldAboveKeyboard() {
        View.OnFocusChangeListener listener = (field, hasFocus) -> {
            if (!hasFocus) return;
            field.postDelayed(() -> {
                if (!field.hasFocus()) return;
                Rect visibleArea = new Rect();
                field.getDrawingRect(visibleArea);
                int clearance = Math.round(96 * getResources().getDisplayMetrics().density);
                visibleArea.bottom += clearance;
                field.requestRectangleOnScreen(visibleArea, true);
            }, 250);
        };

        detailsBinding.vehicleName.setOnFocusChangeListener(listener);
        detailsBinding.noOfSeats.setOnFocusChangeListener(listener);
        detailsBinding.vehicleNumber.setOnFocusChangeListener(listener);
        detailsBinding.licenceNumber.setOnFocusChangeListener(listener);
        detailsBinding.insuranceNumber.setOnFocusChangeListener(listener);
        detailsBinding.rcBookNumber.setOnFocusChangeListener(listener);
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog;
        Calendar cal = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(this, R.style.datepickerCustom,
                VehicleDetailsActivity.this, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));

        long now = System.currentTimeMillis() - 1000;
        cal.add(Calendar.DAY_OF_YEAR, 6);
        datePickerDialog.setTitle("Schedule Date");
        datePickerDialog.getDatePicker().setMinDate(now);
        datePickerDialog.setTitle("");
        datePickerDialog.show();

    }

    private void postVehicleDetails() {
        showLoading(getString(R.string.saving_vehicle_details));
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_VEHICLE_NAME, detailsBinding.vehicleName.getText().toString());
        map.put(Constants.TAG_AVAILABLE_SEATS, detailsBinding.noOfSeats.getText().toString());
        map.put(Constants.TAG_BODY_TYPE, BselectedName);
        map.put("service_types", serviceTypesToCsv());
        map.put(Constants.TAG_AMENITITES, "" + listToJSONArray());
        map.put(Constants.TAG_VEHICLE_NUMBER, detailsBinding.vehicleNumber.getText().toString());
        map.put(Constants.TAG_LICENSE_NO, detailsBinding.licenceNumber.getText().toString());
        map.put(Constants.TAG_INSURANCE_NO, detailsBinding.insuranceNumber.getText().toString());
        map.put(Constants.TAG_BOOK_NO, detailsBinding.rcBookNumber.getText().toString());
        map.put(Constants.TAG_VEHICLE_DETAILS, "Complete");

        try {
         /*   Date rdate = dateFormatter.parse(detailsBinding.rcBookExpiryDate.getText().toString());
            map.put(Constants.TAG_BOOK_DATE, String.valueOf(rdate.getTime() / 1000));

            Date idate = dateFormatter.parse(detailsBinding.insuranceExpiryDate.getText().toString());
            map.put(Constants.TAG_INSURANCE_DATE, String.valueOf(idate.getTime() / 1000));

            Date ldate = dateFormatter.parse(detailsBinding.licenceExpiryDate.getText().toString());
            map.put(Constants.TAG_LICENSE_DATE, String.valueOf(ldate.getTime() / 1000));*/

            map.put(Constants.TAG_BOOK_DATE, detailsBinding.rcBookExpiryDate.getText().toString());
            map.put(Constants.TAG_INSURANCE_DATE, detailsBinding.insuranceExpiryDate.getText().toString());
            map.put(Constants.TAG_LICENSE_DATE, detailsBinding.licenceExpiryDate.getText().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.v(TAG, "postVehicleDetailsParams:" + map);
        Call<HashMap<String, String>> call3 = apiInterface.postVehicleDetails(GetSet.getToken(), map);
        call3.enqueue(new Callback<HashMap<String, String>>() {
            @Override
            public void onResponse(Call<HashMap<String, String>> call, Response<HashMap<String, String>> response) {
                hideLoading();
                try {
                    HashMap<String, String> data = response.body();
                    if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {
                        saveVehicleDetailsToPrefs();
                        VehicleInspectionActivity.saveSectionStatus("vehicle_details", data.get(Constants.TAG_VEHICLE_DETAILS));
                        //      Toast.makeText(VehicleDetailsActivity.this, data.getItem(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                        if (VehicleInspectionActivity.paymentdetail.equals("Complete")) {
                            finish();
                        } else {
                            Intent nextIntent = new Intent(getApplicationContext(), PaymentActivity.class);
                            startActivity(nextIntent);
                        }
                    } else if (data != null && data.get(Constants.TAG_STATUS).equals("false")) {
                        Toast.makeText(getApplicationContext(), data.get(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<HashMap<String, String>> call, Throwable t) {
                hideLoading();
                Toast.makeText(getApplicationContext(), getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                call.cancel();
            }
        });
    }

    private JSONArray listToJSONArray() {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < selectedIds.size(); i++) {
            HashMap hashMap = new HashMap();
            hashMap.put("id", selectedIds.get(i));
            hashMap.put("name", selectedNames.get(i));
            JSONObject jsonObject = new JSONObject(hashMap);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    private void getVehicleDetails() {
        Call<VehicleData> call3 = apiInterface.getVehicleDetails(GetSet.getToken(), GetSet.getUserId());
        call3.enqueue(new Callback<VehicleData>() {
            @Override
            public void onResponse(Call<VehicleData> call, Response<VehicleData> response) {
                try {
                    vehicleData = response.body();
                    if (vehicleData != null && ("true".equalsIgnoreCase(vehicleData.status) || hasAnyVehicleDetails(vehicleData))) {
                        fillVehicleDetails(vehicleData);
                        if ("true".equalsIgnoreCase(vehicleData.status)) {
                            VehicleInspectionActivity.saveSectionStatus("vehicle_details", vehicleData.vehicle_details);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<VehicleData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void fillVehicleDetails(VehicleData vehicleData) {
        setTextIfPresent(detailsBinding.vehicleName, vehicleData.vehicle_name);
        setTextIfPresent(detailsBinding.vehicleNumber, vehicleData.vehicle_number);
        setTextIfPresent(detailsBinding.noOfSeats, vehicleData.available_seats);
        setTextIfPresent(detailsBinding.licenceNumber, vehicleData.licence_no);
        setTextIfPresent(detailsBinding.licenceExpiryDate, vehicleData.licence_date);
        setTextIfPresent(detailsBinding.insuranceNumber, vehicleData.insurance_no);
        setTextIfPresent(detailsBinding.insuranceExpiryDate, vehicleData.insurance_date);
        setTextIfPresent(detailsBinding.rcBookNumber, vehicleData.rcbook_no);
        setTextIfPresent(detailsBinding.rcBookExpiryDate, vehicleData.rcbook_date);

        detailsBinding.serviceRide.setChecked(vehicleData.service_types == null || vehicleData.service_types.size() == 0 || vehicleData.service_types.contains("ride"));
        detailsBinding.serviceDelivery.setChecked(vehicleData.service_types != null && vehicleData.service_types.contains("delivery"));

        if (hasUploadedDocument(vehicleData.licence_doc)) {
            detailsBinding.licenceDoc.setText(getString(R.string.document_uploaded));
        }
        if (hasUploadedDocument(vehicleData.insurance_doc)) {
            detailsBinding.insuranceDoc.setText(getString(R.string.document_uploaded));
        }
        if (hasUploadedDocument(vehicleData.rcbook_doc)) {
            detailsBinding.rcBookDoc.setText(getString(R.string.document_uploaded));
        }

        if (vehicleData.amenitites != null) {
            selectedIds.clear();
            selectedNames.clear();
            for (VehicleData.amenitites amenity : vehicleData.amenitites) {
                if (amenity != null && hasText(amenity.id) && hasText(amenity.name)) {
                    selectedIds.add(amenity.id);
                    selectedNames.add(amenity.name);
                }
            }
            AselectedName = getStringFromList(selectedNames);
            detailsBinding.amenities.setText(AselectedName);
        }

        if (hasText(vehicleData.vehicle_type)) {
            BselectedName = vehicleData.vehicle_type.trim();
            selectedbodytype = "";
            if (SplashActivity.bodyTypeArrayList != null) {
                for (int i = 0; i < SplashActivity.bodyTypeArrayList.size(); i++) {
                    AdminData.BodyType bodyType = SplashActivity.bodyTypeArrayList.get(i);
                    if (bodyType != null && BselectedName.equals(bodyType.name)) {
                        selectedbodytype = bodyType.id;
                        break;
                    }
                }
            }
            detailsBinding.bodyType.setText(BselectedName);
        }
    }

    private void setTextIfPresent(android.widget.TextView view, String value) {
        if (hasText(value)) {
            view.setText(value);
        }
    }

    private void fillVehicleDetailsFromPrefs() {
        if (vehiclePrefs == null) {
            return;
        }
        setTextIfPresent(detailsBinding.vehicleName, vehiclePrefs.getString("vehicle_name", ""));
        setTextIfPresent(detailsBinding.vehicleNumber, vehiclePrefs.getString("vehicle_number", ""));
        setTextIfPresent(detailsBinding.noOfSeats, vehiclePrefs.getString("available_seats", ""));
        setTextIfPresent(detailsBinding.bodyType, vehiclePrefs.getString("body_type", ""));
        setTextIfPresent(detailsBinding.amenities, vehiclePrefs.getString("amenities_text", ""));
        setTextIfPresent(detailsBinding.licenceNumber, vehiclePrefs.getString("licence_no", ""));
        setTextIfPresent(detailsBinding.licenceExpiryDate, vehiclePrefs.getString("licence_date", ""));
        setTextIfPresent(detailsBinding.insuranceNumber, vehiclePrefs.getString("insurance_no", ""));
        setTextIfPresent(detailsBinding.insuranceExpiryDate, vehiclePrefs.getString("insurance_date", ""));
        setTextIfPresent(detailsBinding.rcBookNumber, vehiclePrefs.getString("book_no", ""));
        setTextIfPresent(detailsBinding.rcBookExpiryDate, vehiclePrefs.getString("book_date", ""));
        BselectedName = vehiclePrefs.getString("body_type", BselectedName);
        AselectedName = vehiclePrefs.getString("amenities_text", AselectedName);
        selectedbodytype = vehiclePrefs.getString("body_type_id", selectedbodytype);
        selectedIds = csvToList(vehiclePrefs.getString("amenities_ids", ""));
        selectedNames = csvToList(vehiclePrefs.getString("amenities_names", ""));
    }

    private void saveVehicleDetailsToPrefs() {
        if (vehiclePrefs == null) {
            return;
        }
        vehiclePrefs.edit()
                .putString("vehicle_name", detailsBinding.vehicleName.getText().toString().trim())
                .putString("vehicle_number", detailsBinding.vehicleNumber.getText().toString().trim())
                .putString("available_seats", detailsBinding.noOfSeats.getText().toString().trim())
                .putString("body_type", detailsBinding.bodyType.getText().toString().trim())
                .putString("body_type_id", selectedbodytype == null ? "" : selectedbodytype)
                .putString("amenities_text", detailsBinding.amenities.getText().toString().trim())
                .putString("amenities_ids", TextUtils.join(",", selectedIds))
                .putString("amenities_names", TextUtils.join(",", selectedNames))
                .putString("licence_no", detailsBinding.licenceNumber.getText().toString().trim())
                .putString("licence_date", detailsBinding.licenceExpiryDate.getText().toString().trim())
                .putString("insurance_no", detailsBinding.insuranceNumber.getText().toString().trim())
                .putString("insurance_date", detailsBinding.insuranceExpiryDate.getText().toString().trim())
                .putString("book_no", detailsBinding.rcBookNumber.getText().toString().trim())
                .putString("book_date", detailsBinding.rcBookExpiryDate.getText().toString().trim())
                .apply();
    }

    private ArrayList<String> csvToList(String value) {
        ArrayList<String> values = new ArrayList<>();
        if (!hasText(value)) {
            return values;
        }
        String[] parts = value.split(",");
        for (String part : parts) {
            if (hasText(part)) {
                values.add(part.trim());
            }
        }
        return values;
    }

    private String getStringFromList(ArrayList<String> selectedNames) {
        if (selectedNames.size() == 0)
            return "";
        else {
            return TextUtils.join(",", selectedNames);
        }
    }

    private String serviceTypesToCsv() {
        ArrayList<String> serviceTypes = new ArrayList<>();
        if (detailsBinding.serviceRide.isChecked()) {
            serviceTypes.add("ride");
        }
        if (detailsBinding.serviceDelivery.isChecked()) {
            serviceTypes.add("delivery");
        }
        if (serviceTypes.size() == 0) {
            serviceTypes.add("ride");
        }
        return TextUtils.join(",", serviceTypes);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && data != null) {


            detailsBinding.bodyType.setText(data.getStringExtra("selectedName"));
            BselectedName = data.getStringExtra("selectedName");
            selectedbodytype = data.getStringExtra("selectedId");
        } else if (resultCode == RESULT_OK && requestCode == 200 && data != null) {
            detailsBinding.amenities.setText(data.getStringExtra("selectedName"));
            selectedIds = data.getStringArrayListExtra("selectedId");
            selectedNames = data.getStringArrayListExtra("selectedNames");
            AselectedName = data.getStringExtra("selectedName");
        } else if (requestCode == 7 && resultCode == RESULT_OK) {
            if (data == null || data.getData() == null) {
                Toast.makeText(VehicleDetailsActivity.this, getString(R.string.unsupported_format), Toast.LENGTH_SHORT).show();
                return;
            }
            Uri uri = data.getData();
            String uriString = "" + uri;
            File myFile = new File(uriString);
            String path = myFile.getAbsolutePath();
            String displayName = null;
            Log.d(TAG, "onActivityResultFilePath: " + path);
            if (uriString.startsWith("content://")) {
                Cursor cursor = null;
                try {
                    cursor = this.getContentResolver().query(uri, null, null, null, null);
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName();
            }
            if (displayName == null || displayName.trim().equals("")) {
                displayName = uri.getLastPathSegment();
            }
            Log.d("TAG", "onActivityResultFileName: " + displayName);

            try {
                if (isSupportedDocument(displayName)) {
                    InputStream is = getContentResolver().openInputStream(uri);
                    if (is == null) {
                        Toast.makeText(VehicleDetailsActivity.this, getString(R.string.unsupported_format), Toast.LENGTH_SHORT).show();
                        return;
                    }
                    uploadFile(getBytes(is), uploadfor, displayName);
                } else {
                    Toast.makeText(VehicleDetailsActivity.this, getString(R.string.unsupported_format), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
     * Function to Upload an User Image to Server
     */

    private void uploadFile(byte[] imageBytes, String type, String fileName) {
        isUploadingDocument = true;
        showLoading(getString(R.string.uploading_document));
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeTypeFor(fileName)), imageBytes);
        MultipartBody.Part body = MultipartBody.Part.createFormData("uploaddoc", fileName, requestFile);

        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"), GetSet.getUserId());
        RequestBody typebody = RequestBody.create(MediaType.parse("multipart/form-data"), type);
        Call<Map<String, String>> call3 = apiInterface.uploadDoc(GetSet.getToken(), body, userid, typebody);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                isUploadingDocument = false;
                hideLoading();
                Map<String, String> data = response.body();

                if (data != null && data.get(Constants.TAG_STATUS).equals("true")) {
                    Toast.makeText(VehicleDetailsActivity.this, getString(R.string.file_uploaded), Toast.LENGTH_SHORT).show();
                    if (uploadfor.equals("licensedoc"))
                        detailsBinding.licenceDoc.setText(getString(R.string.document_uploaded));
                    else if (uploadfor.equals("insurancedoc"))
                        detailsBinding.insuranceDoc.setText(getString(R.string.document_uploaded));
                    else if (uploadfor.equals("bookdoc"))
                        detailsBinding.rcBookDoc.setText(getString(R.string.document_uploaded));
                } else {
                    String message = data != null && data.get(Constants.TAG_MESSSAGE) != null
                            ? data.get(Constants.TAG_MESSSAGE)
                            : getString(R.string.something_went_to_be_wrong);
                    Toast.makeText(VehicleDetailsActivity.this, message, Toast.LENGTH_SHORT).show();
                }
                Log.v(TAG, "uploadImageResponse=" + data);
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                isUploadingDocument = false;
                hideLoading();
                Toast.makeText(VehicleDetailsActivity.this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "uploadFileError=" + t.getMessage());
                call.cancel();
            }
        });
    }

    private boolean hasUploadedDocument(String value) {
        return value != null && !value.trim().equals("");
    }

    private boolean hasAnyVehicleDetails(VehicleData vehicleData) {
        return vehicleData != null
                && (hasText(vehicleData.vehicle_name)
                || hasText(vehicleData.available_seats)
                || hasText(vehicleData.vehicle_type)
                || hasText(vehicleData.vehicle_number)
                || hasText(vehicleData.licence_no)
                || hasText(vehicleData.licence_date)
                || hasText(vehicleData.insurance_no)
                || hasText(vehicleData.insurance_date));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().equals("");
    }

    private boolean isSupportedDocument(String fileName) {
        if (fileName == null) {
            return false;
        }
        String lowerName = fileName.toLowerCase(Locale.US);
        return lowerName.endsWith(".jpeg") || lowerName.endsWith(".jpg") || lowerName.endsWith(".pdf");
    }

    private String mimeTypeFor(String fileName) {
        if (fileName != null && fileName.toLowerCase(Locale.US).endsWith(".pdf")) {
            return "application/pdf";
        }
        return "image/jpeg";
    }

    private boolean isBlank(CharSequence value) {
        return value == null || value.toString().trim().equals("");
    }

    private void showLoading(String message) {
        if (progressDialog == null) {
            return;
        }
        progressDialog.setMessage(message);
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideLoading() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void openVehicleOptions(final String optionType) {
        boolean hasOptions = "body_type".equals(optionType)
                ? SplashActivity.bodyTypeArrayList != null && !SplashActivity.bodyTypeArrayList.isEmpty()
                : SplashActivity.amenitiesArrayList != null && !SplashActivity.amenitiesArrayList.isEmpty();
        if (hasOptions) {
            launchVehicleOptions(optionType);
            return;
        }

        showLoading(getString(R.string.loading_vehicle_options));
        apiInterface.adminData().enqueue(new Callback<AdminData>() {
            @Override
            public void onResponse(Call<AdminData> call, Response<AdminData> response) {
                hideLoading();
                AdminData adminData = response.body();
                if (response.isSuccessful() && adminData != null && Constants.TRUE.equals(adminData.status)) {
                    SplashActivity.bodyTypeArrayList = adminData.bodyType == null
                            ? new ArrayList<>() : new ArrayList<>(adminData.bodyType);
                    SplashActivity.amenitiesArrayList = adminData.amenities == null
                            ? new ArrayList<>() : new ArrayList<>(adminData.amenities);
                }
                boolean optionsLoaded = "body_type".equals(optionType)
                        ? !SplashActivity.bodyTypeArrayList.isEmpty()
                        : !SplashActivity.amenitiesArrayList.isEmpty();
                if (optionsLoaded) {
                    launchVehicleOptions(optionType);
                } else {
                    Toast.makeText(VehicleDetailsActivity.this,
                            R.string.vehicle_options_unavailable, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<AdminData> call, Throwable t) {
                hideLoading();
                Toast.makeText(VehicleDetailsActivity.this,
                        R.string.vehicle_options_unavailable, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void launchVehicleOptions(String optionType) {
        Bundle bundle = new Bundle();
        bundle.putString("from", optionType);
        if ("body_type".equals(optionType)) {
            bundle.putString("selectedName", BselectedName);
        } else {
            bundle.putString("selectedName", AselectedName);
            bundle.putStringArrayList("selectedNames", selectedNames);
            bundle.putStringArrayList("selectedId", selectedIds);
        }
        Intent intent = new Intent(this, TypeListActivity.class);
        intent.putExtras(bundle);
        startActivityForResult(intent,
                "body_type".equals(optionType) ? REQUEST_BODY_TYPE : REQUEST_AMENITIES);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.nextBtn) {
            if (isUploadingDocument) {
                ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), getString(R.string.uploading_document), "alert");
            } else if (isBlank(detailsBinding.vehicleName.getText())) {
                detailsBinding.vehicleName.setError(getString(R.string.enter_vehicle_name));
                detailsBinding.vehicleName.requestFocus();
            } else if (isBlank(detailsBinding.noOfSeats.getText())) {
                detailsBinding.noOfSeats.setError("Enter number of seats");
                detailsBinding.noOfSeats.requestFocus();
            } else if (isBlank(detailsBinding.bodyType.getText())) {
                ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Select vehicle body type", "alert");
            } else if (isBlank(detailsBinding.amenities.getText())) {
                ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Select amenities", "alert");
            } else if (isBlank(detailsBinding.vehicleNumber.getText())) {
                detailsBinding.vehicleNumber.setError("Enter vehicle number");
                detailsBinding.vehicleNumber.requestFocus();
            } else if (isBlank(detailsBinding.licenceNumber.getText())) {
                detailsBinding.licenceNumber.setError("Enter license number");
                detailsBinding.licenceNumber.requestFocus();
            } else if (isBlank(detailsBinding.licenceDoc.getText())) {
                ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Select license document", "alert");
            } else if (isBlank(detailsBinding.licenceExpiryDate.getText())) {
                ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Enter license expiry", "alert");
            } else if (isBlank(detailsBinding.insuranceNumber.getText())) {
                detailsBinding.insuranceNumber.setError("Enter insurance number");
                detailsBinding.insuranceNumber.requestFocus();
            } else if (isBlank(detailsBinding.insuranceDoc.getText())) {
                ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Select insurance document", "alert");
            } else if (isBlank(detailsBinding.insuranceExpiryDate.getText())) {
                ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Enter insurance expiry", "alert");
            }
//        else if (detailsBinding.rcBookNumber.getText().toString().equals("")) {
//            detailsBinding.rcBookNumber.setError("Enter RC book number");
//            detailsBinding.rcBookNumber.requestFocus();
//        } else if (detailsBinding.rcBookDoc.getText().toString().equals("")) {
//            ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Select RC book document", "alert");
//        }
//        else if (detailsBinding.rcBookExpiryDate.getText().toString().equals("")) {
//            ApplicationClass.defaultSnack(VehicleDetailsActivity.this.findViewById(R.id.activity_vehicle_details), "Enter RC expiry", "alert");
//        }
            else {
                postVehicleDetails();
            }

        } else if (id == R.id.licenceExpiryDate) {
            dateselectedfor = "licence";
            showDatePicker();

        } else if (id == R.id.insuranceExpiryDate) {
            dateselectedfor = "insurance";
            showDatePicker();

        } else if (id == R.id.rcBookExpiryDate) {
            dateselectedfor = "rcbook";
            showDatePicker();

        } else if (id == R.id.bodyType || id == R.id.bodyTypeLay || id == R.id.icon) {
            openVehicleOptions("body_type");

        } else if (id == R.id.amenities || id == R.id.amenitiesLay || id == R.id.amenitiesIcon) {
            openVehicleOptions("amenities");

        } else if (id == R.id.backBtn) {
            onBackPressed();

        } else if (id == R.id.licenceDoc) {
            uploadfor = "licensedoc";
            Intent licenseIntent = new Intent(Intent.ACTION_GET_CONTENT);
            licenseIntent.setType("*/*");
            licenseIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(licenseIntent, 7);

        } else if (id == R.id.insuranceDoc) {
            uploadfor = "insurancedoc";
            Intent insuranceIntent = new Intent(Intent.ACTION_GET_CONTENT);
            insuranceIntent.setType("*/*");
            insuranceIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(insuranceIntent, 7);

        } else if (id == R.id.rcBookDoc) {
            uploadfor = "bookdoc";
            Intent rcIntent = new Intent(Intent.ACTION_GET_CONTENT);
            rcIntent.setType("*/*");
            rcIntent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
            startActivityForResult(rcIntent, 7);
        }
    }


    @Override
    public void onBackPressed() {
        if (backBtn.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
        } else {
            if (exit == 0) {
                snackbar.show();
            } else {
                VehicleDetailsActivity.this.finishAffinity();
            }
        }
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.activity_vehicle_details), isConnected);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, ApplicationClass.intentFilter);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_vehicle_details), NetworkReceiver.isConnected());
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_vehicle_details), true);
    }

    long componentTimeToTimestamp(int year, int month, int day, int hour, int minute) {

        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.set(Calendar.HOUR, hour);
        c.set(Calendar.MINUTE, minute);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);

        return (c.getTimeInMillis() / 1000L);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
        int year, month, day;
        year = i;
        month = i1;
        day = i2;
        Calendar newDate = Calendar.getInstance();
        newDate.set(year, month, day);
        if (dateselectedfor.equals("licence")) {
            detailsBinding.licenceExpiryDate.setText(dateFormatter.format(newDate.getTime()));
        } else if (dateselectedfor.equals("insurance")) {
            detailsBinding.insuranceExpiryDate.setText(dateFormatter.format(newDate.getTime()));
        } else {
            detailsBinding.rcBookExpiryDate.setText(dateFormatter.format(newDate.getTime()));
        }
    }

    public boolean checkCameraPermission(String[] permissions) {
        return ContextCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, permissions[1]) == PackageManager.PERMISSION_GRANTED;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 100:
                boolean permissionGranted = true;
                for (String permission : permissions) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                        permissionGranted = false;
                        break;
                    }
                }
                if (permissionGranted) {
                    ImagePicker.pickImage(this, "Select your image:");
                } else {
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                        if (shouldShowRequestPermissionRationale(CAMERA)) {
                            requestPermissions(permissions, 100);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.enable_camera_storage), Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + this.getPackageName()));

                            startActivity(i);
                        }
                    }else {
                        if (shouldShowRequestPermissionRationale(CAMERA) &&
                                shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)) {
                            requestPermissions(permissions, 100);
                        } else {
                            Toast.makeText(getApplicationContext(), getString(R.string.enable_camera_storage), Toast.LENGTH_SHORT).show();
//                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                            Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.parse("package:" + this.getPackageName()));

                            startActivity(i);
                        }
                    }
                }
                break;
        }
    }
}
