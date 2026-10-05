package com.driver.helper;

import android.app.Activity;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import com.driver.cabiappdriver.MapsActivity;
import com.driver.cabiappdriver.R;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import io.socket.client.Socket;

/** Routes socket and push acceptance through the same foreground/background flow. */
public final class AcceptedOfferHandler implements Application.ActivityLifecycleCallbacks {
    private static final String CHANNEL = "accepted_offers";
    private static AcceptedOfferHandler instance;
    private final Application app;
    private final Socket socket;
    private final Handler main = new Handler(Looper.getMainLooper());
    private java.lang.ref.WeakReference<Activity> foreground = new java.lang.ref.WeakReference<>(null);

    private AcceptedOfferHandler(Application app, Socket socket) {
        this.app = app;
        this.socket = socket;
        app.registerActivityLifecycleCallbacks(this);
        socket.on(Socket.EVENT_CONNECT, args -> registerDriver());
        socket.on("offer-accepted", args -> {
            if (args.length == 0 || !(args[0] instanceof JSONObject)) return;
            JSONObject json = (JSONObject) args[0];
            Map<String, String> data = new HashMap<>();
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                data.put(key, json.optString(key, ""));
            }
            receive(data, true);
        });
    }

    public static void initialize(Application app, Socket socket) {
        instance = new AcceptedOfferHandler(app, socket);
    }

    public static void handle(Map<String, String> data) {
        if (instance != null) instance.receive(new HashMap<>(data), false);
    }

    /** Android delivers notification+data payloads to the launcher when tapped. */
    public static boolean openNotification(Activity activity) {
        Bundle extras = activity.getIntent().getExtras();
        if (!GetSet.isLogged() || extras == null || !"offer_accepted".equals(extras.getString("type"))) return false;
        Map<String, String> data = new HashMap<>();
        for (String key : extras.keySet()) {
            Object value = extras.get(key);
            if (value != null) data.put(key, String.valueOf(value));
        }
        HashMap<String, String> ride = AcceptedOfferData.navigationData(data);
        if (ride == null) return false;
        activity.startActivity(new Intent(activity, MapsActivity.class)
                .putExtra("data", ride).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
        activity.finish();
        return true;
    }

    private void registerDriver() {
        String userId = GetSet.getUserId();
        if (!GetSet.isLogged() || userId == null || userId.trim().isEmpty()) return;
        try {
            socket.emit("register-user", new JSONObject().put("user_id", userId));
        } catch (org.json.JSONException ignored) { }
    }

    private void receive(Map<String, String> data, boolean socketDelivery) {
        main.post(() -> {
            // Android renders the server's notification+data push in the background.
            // Let that delivery own the alert rather than duplicating it from the socket.
            if (socketDelivery && foreground.get() == null) return;
            if (!app.getSharedPreferences("SavedPref", Context.MODE_PRIVATE).getBoolean("isLogged", false)) return;
            HashMap<String, String> ride = AcceptedOfferData.navigationData(data);
            if (ride == null) return;
            String rideId = data.get("ride_id");
            // FCM and socket can deliver the same acceptance, including after process restart.
            android.content.SharedPreferences seen = app.getSharedPreferences("AcceptedOffers", Context.MODE_PRIVATE);
            String eventKey = GetSet.getUserId() + ":" + rideId;
            if (eventKey.equals(seen.getString("last_routed", ""))) return;
            Intent intent = new Intent(app, MapsActivity.class)
                    .putExtra("data", ride)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            Activity active = foreground.get();
            if (active != null && !active.isFinishing()) {
                active.startActivity(intent);
            } else {
                NotificationManager manager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    manager.createNotificationChannel(new NotificationChannel(CHANNEL,
                            app.getString(R.string.accepted_offer_channel), NotificationManager.IMPORTANCE_HIGH));
                }
                PendingIntent tap = PendingIntent.getActivity(app, eventKey.hashCode(), intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                try {
                    manager.notify(eventKey, 1, new NotificationCompat.Builder(app, CHANNEL)
                            .setSmallIcon(R.drawable.small_notification)
                            .setContentTitle(app.getString(R.string.accepted_offer_title))
                            .setContentText(app.getString(R.string.accepted_offer_body))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setDefaults(NotificationCompat.DEFAULT_ALL)
                            .setContentIntent(tap).setAutoCancel(true).build());
                } catch (SecurityException denied) {
                    return; // Notification permission is controlled by the driver.
                }
            }
            seen.edit().putString("last_routed", eventKey).apply();
        });
    }

    @Override public void onActivityResumed(Activity activity) {
        foreground = new java.lang.ref.WeakReference<>(activity);
        if (activity instanceof MapsActivity) {
            Object extra = activity.getIntent().getSerializableExtra("data");
            if (extra instanceof Map) {
                Object rideId = ((Map<?, ?>) extra).get(Constants.TAG_ONRIDE_ID);
                if (rideId != null) {
                    String eventKey = GetSet.getUserId() + ":" + rideId;
                    app.getSharedPreferences("AcceptedOffers", Context.MODE_PRIVATE)
                            .edit().putString("last_routed", eventKey).apply();
                    ((NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(eventKey, 1);
                }
            }
        }
        if (GetSet.isLogged() && GetSet.getUserId() != null) {
            if (!socket.connected()) socket.connect();
            else registerDriver();
        }
    }
    @Override public void onActivityPaused(Activity activity) { if (foreground.get() == activity) foreground.clear(); }
    @Override public void onActivityDestroyed(Activity activity) { if (foreground.get() == activity) foreground.clear(); }
    @Override public void onActivityCreated(Activity activity, Bundle state) { }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle state) { }
}
