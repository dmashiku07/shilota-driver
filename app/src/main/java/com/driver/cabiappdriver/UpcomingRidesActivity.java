package com.driver.cabiappdriver;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.driver.model.ScheduledRideData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UpcomingRidesActivity extends BaseActivity {
    private ApiInterface apiInterface;
    private LinearLayout rideList;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_rides);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        rideList = findViewById(R.id.rideList);
        emptyText = findViewById(R.id.emptyText);
        loadUpcomingRides();
    }

    private void loadUpcomingRides() {
        apiInterface.getScheduledUpcoming(GetSet.getToken(), GetSet.getUserId()).enqueue(new Callback<ScheduledRideData>() {
            @Override
            public void onResponse(Call<ScheduledRideData> call, Response<ScheduledRideData> response) {
                ScheduledRideData body = response.body();
                rideList.removeAllViews();
                if (body == null || body.result == null || body.result.size() == 0) {
                    emptyText.setVisibility(View.VISIBLE);
                    return;
                }
                emptyText.setVisibility(View.GONE);
                for (ScheduledRideData.ScheduledRide ride : body.result) {
                    rideList.addView(buildRideRow(ride));
                }
            }

            @Override
            public void onFailure(Call<ScheduledRideData> call, Throwable t) {
                call.cancel();
                Toast.makeText(getApplicationContext(), getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private View buildRideRow(final ScheduledRideData.ScheduledRide ride) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        com.driver.helper.DriverUi.card(row);

        TextView title = new TextView(this);
        title.setText((ride.userName == null ? getString(R.string.rider) : ride.userName) + " - " + formatTime(ride.scheduleTime));
        title.setTextColor(getResources().getColor(R.color.textPrimary));
        com.driver.helper.DriverUi.title(title);
        row.addView(title);

        TextView route = new TextView(this);
        route.setText((ride.pickupLocation == null ? "" : ride.pickupLocation) + "\n" + (ride.dropLocation == null ? "" : ride.dropLocation));
        route.setTextColor(getResources().getColor(R.color.textsecondary));
        com.driver.helper.DriverUi.body(route);
        row.addView(route);

        TextView meta = new TextView(this);
        meta.setText((ride.categoryName == null ? "" : ride.categoryName) + " - " + (ride.serviceType == null ? "ride" : ride.serviceType) + " - " + ride.rideStatus);
        meta.setTextColor(getResources().getColor(R.color.textsecondary));
        com.driver.helper.DriverUi.body(meta);
        row.addView(meta);

        Button preAccept = new Button(this);
        preAccept.setText(R.string.pre_accept);
        preAccept.setEnabled(!"accepted".equalsIgnoreCase(ride.rideStatus));
        preAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                preAcceptRide(ride.onrideId);
            }
        });
        row.addView(preAccept);
        return row;
    }

    private String formatTime(long scheduleTime) {
        if (scheduleTime <= 0) {
            return "";
        }
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("Africa/Dar_es_Salaam"));
        return dateFormat.format(new Date(scheduleTime * 1000));
    }

    private void preAcceptRide(String onrideId) {
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put(Constants.TAG_ONRIDE_ID, onrideId);
        apiInterface.preAcceptSchedule(GetSet.getToken(), map).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> body = response.body();
                Toast.makeText(getApplicationContext(), body != null && body.get(Constants.TAG_MESSSAGE) != null ? body.get(Constants.TAG_MESSSAGE) : getString(R.string.pre_accept), Toast.LENGTH_SHORT).show();
                loadUpcomingRides();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                Toast.makeText(getApplicationContext(), getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
