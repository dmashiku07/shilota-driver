package com.driver.cabiappdriver;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.driver.cabiappdriver.R;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DestinationBroadcastActivity extends AppCompatActivity {
    private static final int DESTINATION_BROADCAST_PAYMENT_REQUEST = 1302;

    private ApiInterface apiInterface;
    private EditText originLabelInput, originLatInput, originLngInput, destinationLabelInput,
            destinationLatInput, destinationLngInput, departureTimeInput, priceInput,
            seatsInput, radiusInput, durationInput;
    private LinearLayout broadcastsContainer, requestsContainer;
    private TextView broadcastFeeSummary;
    private String broadcastFeeLabel = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_destination_broadcast);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        originLabelInput = findViewById(R.id.originLabelInput);
        originLatInput = findViewById(R.id.originLatInput);
        originLngInput = findViewById(R.id.originLngInput);
        destinationLabelInput = findViewById(R.id.destinationLabelInput);
        destinationLatInput = findViewById(R.id.destinationLatInput);
        destinationLngInput = findViewById(R.id.destinationLngInput);
        departureTimeInput = findViewById(R.id.departureTimeInput);
        priceInput = findViewById(R.id.pricePerPassengerInput);
        seatsInput = findViewById(R.id.availableSeatsInput);
        radiusInput = findViewById(R.id.notificationRadiusInput);
        durationInput = findViewById(R.id.broadcastDurationInput);
        broadcastsContainer = findViewById(R.id.broadcastsContainer);
        requestsContainer = findViewById(R.id.requestsContainer);
        broadcastFeeSummary = findViewById(R.id.broadcastFeeSummary);

        Button createButton = findViewById(R.id.createBroadcastButton);
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createBroadcast();
            }
        });

        if (MainActivity.lat != 0 && MainActivity.lng != 0) {
            originLatInput.setText(String.format(Locale.ENGLISH, "%.6f", MainActivity.lat));
            originLngInput.setText(String.format(Locale.ENGLISH, "%.6f", MainActivity.lng));
        }
        if (MainActivity.slocation != null) {
            originLabelInput.setText(MainActivity.slocation);
        }

        loadSettings();
        loadBroadcasts();
        loadRequests();
    }

    private void loadSettings() {
        apiInterface.getDestinationBroadcastSettings(GetSet.getToken()).enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                HashMap<String, Object> body = response.body();
                if (body == null || !Constants.TRUE.equals(String.valueOf(body.get(Constants.TAG_STATUS)))) {
                    broadcastFeeSummary.setText(getString(R.string.destination_broadcast_fee_unavailable));
                    return;
                }
                Object result = body.get("result");
                if (!(result instanceof Map)) {
                    broadcastFeeSummary.setText(getString(R.string.destination_broadcast_fee_unavailable));
                    return;
                }
                @SuppressWarnings("unchecked")
                Map<String, Object> settings = (Map<String, Object>) result;
                String currency = String.valueOf(settings.get("currency") == null ? "TZS" : settings.get("currency"));
                String fee = String.valueOf(settings.get("broadcast_fee") == null ? "0" : settings.get("broadcast_fee"));
                broadcastFeeLabel = currency + " " + fee;
                broadcastFeeSummary.setText(getString(R.string.destination_broadcast_fee_summary, broadcastFeeLabel));
                if (settings.get("default_radius_km") != null && TextUtils.isEmpty(radiusInput.getText())) {
                    radiusInput.setText(String.valueOf(settings.get("default_radius_km")));
                }
                if (settings.get("expiry_duration_minutes") != null && TextUtils.isEmpty(durationInput.getText())) {
                    durationInput.setText(String.valueOf(settings.get("expiry_duration_minutes")));
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                call.cancel();
                broadcastFeeSummary.setText(getString(R.string.destination_broadcast_fee_unavailable));
            }
        });
    }

    private void createBroadcast() {
        if (TextUtils.isEmpty(originLabelInput.getText())
                || TextUtils.isEmpty(destinationLabelInput.getText())
                || TextUtils.isEmpty(departureTimeInput.getText())
                || TextUtils.isEmpty(priceInput.getText())
                || TextUtils.isEmpty(seatsInput.getText())
                || TextUtils.isEmpty(radiusInput.getText())
                || TextUtils.isEmpty(durationInput.getText())) {
            Toast.makeText(this, getString(R.string.fill_all), Toast.LENGTH_LONG).show();
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put("origin_label", originLabelInput.getText().toString().trim());
        map.put("origin_lat", originLatInput.getText().toString().trim());
        map.put("origin_lng", originLngInput.getText().toString().trim());
        map.put("destination_label", destinationLabelInput.getText().toString().trim());
        map.put("destination_lat", destinationLatInput.getText().toString().trim());
        map.put("destination_lng", destinationLngInput.getText().toString().trim());
        map.put("departure_time", departureTimeInput.getText().toString().trim());
        map.put("price_per_passenger", priceInput.getText().toString().trim());
        map.put("available_seats", seatsInput.getText().toString().trim());
        map.put("notification_radius", radiusInput.getText().toString().trim());
        map.put("broadcast_duration_minutes", durationInput.getText().toString().trim());

        apiInterface.createDestinationBroadcast(GetSet.getToken(), map).enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                HashMap<String, Object> body = response.body();
                if (body != null && Constants.TRUE.equals(String.valueOf(body.get(Constants.TAG_STATUS)))) {
                    if (Constants.TRUE.equals(String.valueOf(body.get("payment_required")))) {
                        openPaymentFlow(body);
                        return;
                    }
                    Toast.makeText(DestinationBroadcastActivity.this, getString(R.string.destination_broadcast_created), Toast.LENGTH_LONG).show();
                    loadBroadcasts();
                    loadRequests();
                } else {
                    Toast.makeText(DestinationBroadcastActivity.this, body == null ? getString(R.string.something_went_to_be_wrong) : String.valueOf(body.get(Constants.TAG_MESSSAGE)), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                call.cancel();
                Toast.makeText(DestinationBroadcastActivity.this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void openPaymentFlow(HashMap<String, Object> body) {
        String paymentUrl = String.valueOf(body.get("payment_url") == null ? "" : body.get("payment_url"));
        String returnUrl = String.valueOf(body.get("return_url") == null ? "" : body.get("return_url"));
        String transId = String.valueOf(body.get("transid") == null ? "" : body.get("transid"));
        Intent intent = new Intent(DestinationBroadcastActivity.this, ActivityWebView.class);
        intent.putExtra("webview_url", paymentUrl);
        intent.putExtra("return_url", returnUrl);
        intent.putExtra("verify_url", Constants.BASE_URL + "driverapi/destinationbroadcasts/verifyselcom/" + transId);
        intent.putExtra("transid", transId);
        intent.putExtra("amount", broadcastFeeLabel);
        startActivityForResult(intent, DESTINATION_BROADCAST_PAYMENT_REQUEST);
    }

    private void loadBroadcasts() {
        apiInterface.getDestinationBroadcasts(GetSet.getToken(), GetSet.getUserId()).enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                renderBroadcasts(response.body());
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    private void loadRequests() {
        apiInterface.getDestinationBroadcastRequests(GetSet.getToken(), GetSet.getUserId()).enqueue(new Callback<HashMap<String, Object>>() {
            @Override
            public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                renderRequests(response.body());
            }

            @Override
            public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @SuppressWarnings("unchecked")
    private void renderBroadcasts(HashMap<String, Object> body) {
        broadcastsContainer.removeAllViews();
        List<Object> items = body == null ? null : (List<Object>) body.get("result");
        if (items == null || items.isEmpty()) {
            addEmptyState(broadcastsContainer, getString(R.string.no_destination_broadcasts));
            return;
        }
        for (Object raw : items) {
            if (!(raw instanceof Map)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            LinearLayout card = buildCard(
                    String.valueOf(item.get("origin_label")) + " → " + String.valueOf(item.get("destination_label")),
                    "Seats: " + String.valueOf(item.get("remaining_seats")) + "/" + String.valueOf(item.get("available_seats")) + " • Status: " + String.valueOf(item.get("status")) + " • Payment: " + String.valueOf(item.get("payment_status"))
            );
            LinearLayout actions = new LinearLayout(this);
            actions.setOrientation(LinearLayout.HORIZONTAL);
            String paymentStatus = String.valueOf(item.get("payment_status"));
            String status = String.valueOf(item.get("status"));
            if ("PENDING".equalsIgnoreCase(paymentStatus) || "FAILED".equalsIgnoreCase(paymentStatus) || "PAYMENT_FAILED".equalsIgnoreCase(status)) {
                Button retry = new Button(this);
                retry.setText(getString(R.string.retry_payment));
                retry.setOnClickListener(new RetryPaymentClickListener(String.valueOf(item.get("_id"))));
                actions.addView(retry);
            } else {
                Button cancel = new Button(this);
                cancel.setText(getString(R.string.cancel_broadcast));
                cancel.setOnClickListener(new StatusClickListener(String.valueOf(item.get("_id")), "CANCELLED"));
                Button complete = new Button(this);
                complete.setText(getString(R.string.complete_broadcast));
                complete.setOnClickListener(new StatusClickListener(String.valueOf(item.get("_id")), "COMPLETED"));
                actions.addView(cancel);
                actions.addView(complete);
            }
            card.addView(actions);
            broadcastsContainer.addView(card);
        }
    }

    @SuppressWarnings("unchecked")
    private void renderRequests(HashMap<String, Object> body) {
        requestsContainer.removeAllViews();
        List<Object> items = body == null ? null : (List<Object>) body.get("result");
        if (items == null || items.isEmpty()) {
            addEmptyState(requestsContainer, getString(R.string.no_destination_join_requests));
            return;
        }
        for (Object raw : items) {
            if (!(raw instanceof Map)) {
                continue;
            }
            Map<String, Object> item = (Map<String, Object>) raw;
            LinearLayout card = buildCard(
                    String.valueOf(item.get("passenger_name")) + " • " + String.valueOf(item.get("requested_seats")) + " seat(s)",
                    String.valueOf(item.get("pickup_label")) + " • " + String.valueOf(item.get("status"))
            );
            if ("PENDING".equalsIgnoreCase(String.valueOf(item.get("status")))) {
                LinearLayout actions = new LinearLayout(this);
                actions.setOrientation(LinearLayout.HORIZONTAL);
                Button accept = new Button(this);
                accept.setText(getString(R.string.accept_join_request));
                accept.setOnClickListener(new DecisionClickListener(String.valueOf(item.get("_id")), "ACCEPT"));
                Button reject = new Button(this);
                reject.setText(getString(R.string.reject_join_request));
                reject.setOnClickListener(new DecisionClickListener(String.valueOf(item.get("_id")), "REJECT"));
                actions.addView(accept);
                actions.addView(reject);
                card.addView(actions);
            }
            requestsContainer.addView(card);
        }
    }

    private LinearLayout buildCard(String title, String subtitle) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        com.driver.helper.DriverUi.card(card);

        TextView titleView = new TextView(this);
        titleView.setText(title);
        com.driver.helper.DriverUi.title(titleView);
        card.addView(titleView);

        TextView subtitleView = new TextView(this);
        subtitleView.setText(subtitle);
        com.driver.helper.DriverUi.body(subtitleView);
        card.addView(subtitleView);
        return card;
    }

    private void addEmptyState(LinearLayout container, String message) {
        TextView textView = new TextView(this);
        textView.setText(message);
        container.addView(textView);
    }

    private class DecisionClickListener implements View.OnClickListener {
        private final String joinRequestId;
        private final String decision;

        DecisionClickListener(String joinRequestId, String decision) {
            this.joinRequestId = joinRequestId;
            this.decision = decision;
        }

        @Override
        public void onClick(View v) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put("join_request_id", joinRequestId);
            map.put("decision", decision);
            apiInterface.decideDestinationBroadcastRequest(GetSet.getToken(), map).enqueue(new Callback<HashMap<String, Object>>() {
                @Override
                public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                    loadBroadcasts();
                    loadRequests();
                }

                @Override
                public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                    call.cancel();
                }
            });
        }
    }

    private class RetryPaymentClickListener implements View.OnClickListener {
        private final String broadcastId;

        RetryPaymentClickListener(String broadcastId) {
            this.broadcastId = broadcastId;
        }

        @Override
        public void onClick(View v) {
            HashMap<String, String> map = new HashMap<>();
            map.put(Constants.TAG_USER_ID, GetSet.getUserId());
            map.put("broadcast_id", broadcastId);
            apiInterface.retryDestinationBroadcastPayment(GetSet.getToken(), map).enqueue(new Callback<HashMap<String, Object>>() {
                @Override
                public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                    HashMap<String, Object> body = response.body();
                    if (body != null && Constants.TRUE.equals(String.valueOf(body.get(Constants.TAG_STATUS))) && Constants.TRUE.equals(String.valueOf(body.get("payment_required")))) {
                        openPaymentFlow(body);
                        return;
                    }
                    Toast.makeText(DestinationBroadcastActivity.this, body == null ? getString(R.string.something_went_to_be_wrong) : String.valueOf(body.get(Constants.TAG_MESSSAGE)), Toast.LENGTH_LONG).show();
                    loadBroadcasts();
                    loadRequests();
                }

                @Override
                public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                    call.cancel();
                    Toast.makeText(DestinationBroadcastActivity.this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private class StatusClickListener implements View.OnClickListener {
        private final String broadcastId;
        private final String status;

        StatusClickListener(String broadcastId, String status) {
            this.broadcastId = broadcastId;
            this.status = status;
        }

        @Override
        public void onClick(View v) {
          HashMap<String, String> map = new HashMap<>();
          map.put(Constants.TAG_USER_ID, GetSet.getUserId());
          map.put("broadcast_id", broadcastId);
          map.put("status", status);
          apiInterface.updateDestinationBroadcastStatus(GetSet.getToken(), map).enqueue(new Callback<HashMap<String, Object>>() {
              @Override
              public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                  loadBroadcasts();
                  loadRequests();
              }

              @Override
              public void onFailure(Call<HashMap<String, Object>> call, Throwable t) {
                  call.cancel();
              }
          });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DESTINATION_BROADCAST_PAYMENT_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, getString(R.string.destination_broadcast_payment_success), Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, getString(R.string.destination_broadcast_payment_pending), Toast.LENGTH_SHORT).show();
            }
            loadBroadcasts();
            loadRequests();
        }
    }
}
