package com.driver.cabiappdriver;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import androidx.databinding.DataBindingUtil;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.driver.helper.LocaleManager;
import com.driver.helper.NetworkReceiver;

import com.driver.model.ProfileData;
import com.driver.cabiappdriver.databinding.ActivityEmergencyContactBinding;
import com.driver.cabiappdriver.databinding.EmergencyContactItemBinding;
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

public class EmergencyContactActivity extends BaseActivity implements
        View.OnClickListener, NetworkReceiver.ConnectivityReceiverListener {

    static final String TAG = EmergencyContactActivity.class.getSimpleName();
    ImageView backBtn;
    ActivityEmergencyContactBinding binding;
    Display display;
    ProgressDialog progressDialog;

    RelativeLayout nullLay;
    TextView nullText;
    ImageView nullImage;
    RecyclerAdapter recyclerAdapter;
    LinearLayoutManager linearLayoutManager;
    ProfileData data = null;
    ApiInterface apiInterface;
    NetworkReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_contact);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_emergency_contact);
        backBtn = findViewById(R.id.backBtn);
        nullLay = findViewById(R.id.nullLay);
        nullText = findViewById(R.id.nullText);
        nullImage = findViewById(R.id.nullImage);
        display = this.getWindowManager().getDefaultDisplay();

        receiver = new NetworkReceiver();
        ApplicationClass.getInstance().setConnectivityListener(this);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.pleasewait));
        backBtn.setVisibility(View.VISIBLE);

        if (LocaleManager.isRTL()){
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }

        linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.recyclerView.setLayoutManager(linearLayoutManager);

        backBtn.setOnClickListener(this);
        binding.addContact.setOnClickListener(this);

        recyclerAdapter = new RecyclerAdapter(EmergencyContactActivity.this, ProfileActivity.emergencyList);
        binding.recyclerView.setAdapter(recyclerAdapter);

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.v(TAG, "adapterValues=" + ProfileActivity.emergencyList);
        if (recyclerAdapter != null)
            recyclerAdapter.notifyDataSetChanged();
        if (ProfileActivity.emergencyList.size() <= 0) {
            nullLay.setVisibility(View.VISIBLE);
            nullText.setText(getString(R.string.no_contacts));
        } else {
            nullLay.setVisibility(View.GONE);
        }

        registerReceiver(receiver, ApplicationClass.intentFilter);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_emergency_contact), NetworkReceiver.isConnected());
    }

    @Override
    public void onNetworkConnectionChanged(boolean isConnected) {
        ApplicationClass.showSnack(this, findViewById(R.id.activity_emergency_contact), isConnected);
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        ApplicationClass.showSnack(this, findViewById(R.id.activity_emergency_contact), true);
    }

    private JSONArray listToJSONArray(ArrayList<ProfileData.EmergencyContact> list) {
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
     * Function to Edit/Delete Emergency Contact
     */

    private void emergencyContact(JSONArray jsonObject) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_EMERGENCY_CONTACT, jsonObject.toString());
        progressDialog.show();
        Log.v(TAG, "emergencyContactParams:" + map);
        Call<ProfileData> call3 = apiInterface.updateProfile(GetSet.getToken(), map);
        call3.enqueue(new Callback<ProfileData>() {
            @Override
            public void onResponse(Call<ProfileData> call, Response<ProfileData> response) {
                ProfileData map = response.body();
                if (response.isSuccessful() && map.status.equals("true")) {
                    progressDialog.dismiss();
                    Toast.makeText(EmergencyContactActivity.this, getString(R.string.contact_deleted_success), Toast.LENGTH_SHORT).show();
                    recyclerAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(EmergencyContactActivity.this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    /**
     * Function for OnClick Event
     */

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.backBtn) {
            onBackPressed();
        } else if (id == R.id.addContact) {
            Intent i = new Intent(this, AddContactActivity.class);
            i.putExtra("from", "add");
            startActivity(i);
        }
    }


    /**
     * Adapter for Emergency Contacts
     */

    private class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.EmContactViewHolder> {
        ArrayList<ProfileData.EmergencyContact> phoneNumberList;
        Context context;

        public RecyclerAdapter(Context context, ArrayList<ProfileData.EmergencyContact> items) {
            phoneNumberList = items;
            this.context = context;
        }

        @Override
        public EmContactViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            EmergencyContactItemBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.emergency_contact_item, parent, false);
            return new EmContactViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(EmContactViewHolder viewHolder, final int position) {


            viewHolder.getBinding().name.setText(phoneNumberList.get(position).name);
            viewHolder.getBinding().phoneNumber.setText(phoneNumberList.get(position).phone_no);
            viewHolder.getBinding().more.setOnClickListener(new View.OnClickListener() {
                @SuppressLint("NewApi")
                @Override
                public void onClick(View v) {
                    TextView edit, delete;
                    LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(LAYOUT_INFLATER_SERVICE);
                    final View popupView = layoutInflater.inflate(R.layout.address_popup_window, null);
                    final PopupWindow popup = new PopupWindow(context);
                    popup.setContentView(popupView);
                    popup.setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
                    popup.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
                    popup.setFocusable(true);
                    try {
                        popup.setElevation(10);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    popup.showAsDropDown(v, -((display.getWidth() * 18 / 100)), -30);
                    edit = popupView.findViewById(R.id.edit);
                    delete = popupView.findViewById(R.id.delete);
                    edit.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            popup.dismiss();
                            Intent i = new Intent(getApplicationContext(), AddContactActivity.class);
                            i.putExtra("from", "update");
                            i.putExtra("updatedPosition", position);
                            i.putExtra("oldName", phoneNumberList.get(position).name);
                            i.putExtra("oldPhno", phoneNumberList.get(position).phone_no);
                            startActivity(i);
                        }
                    });
                    delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ProfileActivity.emergencyList.remove(position);
                            popup.dismiss();
                            JSONArray jsonObject = listToJSONArray(ProfileActivity.emergencyList);
                            emergencyContact(jsonObject);
                            if (ProfileActivity.emergencyList.size() <= 0) {
                                nullLay.setVisibility(View.VISIBLE);
                                nullText.setText(getString(R.string.no_contacts));
                            }
                        }
                    });
                }
            });
        }

        @Override
        public int getItemCount() {
            return phoneNumberList.size();
        }

        public class EmContactViewHolder extends RecyclerView.ViewHolder {
            EmergencyContactItemBinding emContactItemBinding;

            public EmContactViewHolder(EmergencyContactItemBinding layoutBinding) {
                super(layoutBinding.getRoot());
                emContactItemBinding = layoutBinding;
            }

            public EmergencyContactItemBinding getBinding() {
                return emContactItemBinding;
            }
        }
    }
}