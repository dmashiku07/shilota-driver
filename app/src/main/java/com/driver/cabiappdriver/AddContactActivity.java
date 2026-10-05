package com.driver.cabiappdriver;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.databinding.DataBindingUtil;
import com.driver.helper.LocaleManager;
import com.driver.cabiappdriver.databinding.ActivityAddContactBinding;

import com.driver.model.ProfileData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;



public class AddContactActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = AddContactActivity.class.getSimpleName();
    public static ProfileData data = null;
    ActivityAddContactBinding binding;
    ImageView backBtn;
    int PICK_CONTACT = 100;
    ApiInterface apiInterface;
    int updatePosition;
    private String from = "";

    RelativeLayout customToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_contact);
        backBtn = findViewById(R.id.backBtn);
        binding.name.setFilters(new InputFilter[]{ApplicationClass.EMOJI_FILTER, new InputFilter.LengthFilter(30)});

        customToolbar = findViewById(R.id.custom_toolbar);

        ImageView backBtn = customToolbar.findViewById(R.id.backBtn);
        backBtn.setVisibility(View.VISIBLE); // Make it visible if default is invisible
        backBtn.setOnClickListener(this);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        from = getIntent().getStringExtra("from");

        if (from.equals("update")) {
            updatePosition = getIntent().getIntExtra("updatedPosition", 0);
            binding.name.setText(getIntent().getStringExtra("oldName"));
            binding.phoneNumber.setText(getIntent().getStringExtra("oldPhno"));
        }
        if (LocaleManager.isRTL()) {
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }

        backBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        binding.save.setOnClickListener(this);
        binding.phoneContactlay.setOnClickListener(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_CONTACT) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    String cNumber = "";
                    Uri contactData = data.getData();
                    Cursor cursor = managedQuery(contactData, null, null, null, null);
                    while (cursor.moveToNext()) {
                        String id
                                = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                        String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                        String hasPhone = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));
                        if (hasPhone.equalsIgnoreCase("1")) {
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = " + id, null, null);
                            phones.moveToFirst();
                            cNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        }
                        binding.phoneNumber.setText(cNumber);
                        binding.name.setText(name);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(AddContactActivity.this, "Unsupported Format", Toast.LENGTH_SHORT).show();

                }
            }
        }

    }

    private JSONArray listToJSONArray(ArrayList<ProfileData.EmergencyContact> list, String keyName) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < list.size(); i++) {
            ProfileData.EmergencyContact emergencyContact = list.get(i);
            HashMap hashMap = new HashMap();
            hashMap.put("name", emergencyContact.name);
            hashMap.put("phone_no", emergencyContact.phone_no);
            JSONObject jsonObject = new JSONObject(hashMap);
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    /*API Integration*/

    /**
     * Function for add an Emergency Contact
     */

    private void emergencyContact(JSONArray jsonObject) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_EMERGENCY_CONTACT, jsonObject.toString());

        Log.v(TAG, "emergencyContactParams:" + map);
        Call<ProfileData> call3 = apiInterface.updateProfile(GetSet.getToken(), map);
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                if (response.isSuccessful()) {
                    ProfileData map = response.body();
                    if (map.status.equals("true")) {
                        Toast.makeText(AddContactActivity.this, getString(R.string.user_updated_success), Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(AddContactActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddContactActivity.this, getString(R.string.something_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function for Onclick Event
     */

//    @Override
//    public void onClick(View v) {
//        switch (v.getId()) {
//            case R.id.backBtn:
//                onBackPressed();
//                break;
//            case R.id.save:
//                binding.namelay.setError(null);
//                binding.phoneNumberLay.setError(null);
//                if (binding.name.getText().toString().equals("")) {
//                    binding.namelay.setError(getString(R.string.fullname_error));
//                    ApplicationClass.requestFocus(AddContactActivity.this, binding.name, true);
//                } else if (binding.phoneNumber.getText().toString().equals("")) {
//                    binding.namelay.setErrorEnabled(false);
//                    binding.phoneNumberLay.setError(getString(R.string.phonenumber_error));
//                    ApplicationClass.requestFocus(AddContactActivity.this, binding.phoneNumber, true);
//                } else {
//                    binding.phoneNumberLay.setErrorEnabled(false);
//                    ProfileData.EmergencyContact emergencyContact = new ProfileData.EmergencyContact();
//                    emergencyContact.name = binding.name.getText().toString();
//                    emergencyContact.phone_no = binding.phoneNumber.getText().toString();
//
//                    if (from.equals("update"))
//                        ProfileActivity.emergencyList.set(updatePosition, emergencyContact);
//                    else
//                        ProfileActivity.emergencyList.add(emergencyContact);
//
//                    JSONArray jsonObject = listToJSONArray(ProfileActivity.emergencyList, "emergency_contact");
//                    emergencyContact(jsonObject);
//                    Log.v(TAG, "jsonObject=" + jsonObject);
//                }
//                break;
//            case R.id.phoneContactlay:
//                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
//                startActivityForResult(intent, PICK_CONTACT);
//        }
//    }
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.backBtn) {
            onBackPressed();
        } else if (id == R.id.save) {
            binding.namelay.setError(null);
            binding.phoneNumberLay.setError(null);
            if (binding.name.getText().toString().equals("")) {
                binding.namelay.setError(getString(R.string.fullname_error));
                ApplicationClass.requestFocus(AddContactActivity.this, binding.name, true);
            } else if (binding.phoneNumber.getText().toString().equals("")) {
                binding.namelay.setErrorEnabled(false);
                binding.phoneNumberLay.setError(getString(R.string.phonenumber_error));
                ApplicationClass.requestFocus(AddContactActivity.this, binding.phoneNumber, true);
            } else if (!canSaveEmergencyContact(binding.phoneNumber.getText().toString())) {
                ApplicationClass.requestFocus(AddContactActivity.this, binding.phoneNumber, true);
            } else {
                binding.phoneNumberLay.setErrorEnabled(false);
                ProfileData.EmergencyContact emergencyContact = new ProfileData.EmergencyContact();
                emergencyContact.name = binding.name.getText().toString();
                emergencyContact.phone_no = binding.phoneNumber.getText().toString();

                if (from.equals("update"))
                    ProfileActivity.emergencyList.set(updatePosition, emergencyContact);
                else
                    ProfileActivity.emergencyList.add(emergencyContact);

                JSONArray jsonObject = listToJSONArray(ProfileActivity.emergencyList, "emergency_contact");
                emergencyContact(jsonObject);
                Log.v(TAG, "jsonObject=" + jsonObject);
            }
        } else if (id == R.id.phoneContactlay) {
            Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, PICK_CONTACT);
        }
    }

    private boolean canSaveEmergencyContact(String phoneNumber) {
        boolean isUpdate = "update".equals(from);
        if (!isUpdate && ProfileActivity.emergencyList.size() >= 3) {
            Toast.makeText(this, getString(R.string.emergency_contact_limit), Toast.LENGTH_SHORT).show();
            return false;
        }

        String normalizedPhone = normalizePhone(phoneNumber);
        for (int i = 0; i < ProfileActivity.emergencyList.size(); i++) {
            if (isUpdate && i == updatePosition) {
                continue;
            }
            ProfileData.EmergencyContact contact = ProfileActivity.emergencyList.get(i);
            if (contact != null && normalizedPhone.equals(normalizePhone(contact.phone_no))) {
                Toast.makeText(this, getString(R.string.emergency_contact_duplicate), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        return true;
    }

    private String normalizePhone(String phoneNumber) {
        return phoneNumber == null ? "" : phoneNumber.replaceAll("[^0-9+]", "");
    }

}
