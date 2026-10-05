package com.driver.helper;

import static android.Manifest.permission.READ_PHONE_STATE;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;


import com.driver.cabiappdriver.NotificationActivity;
import com.driver.cabiappdriver.R;
import com.driver.cabiappdriver.RideActivity;
import com.driver.cabiappdriver.CallActivity;
import com.driver.cabiappdriver.LostItemsActivity;
import com.driver.cabiappdriver.MapsActivity;
import com.driver.utils.Constants;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.driver.cabiappdriver.ApplicationClass;
import com.driver.cabiappdriver.TimerActivity;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.GetSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Hitasoft on 03/11/16.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = MyFirebaseMessagingService.class.getSimpleName();
    ApiInterface apiInterface;
    String type;

    @Override
    public void onNewToken(String s) {
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String refreshedToken = task.getResult();
                        Log.d(TAG, "onComplete: "+refreshedToken);
                        storeToken(refreshedToken);
                        // Log and toast
                    }
                });
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        Log.i(TAG, "onMessageReceived: " + data);
        type = data.get("type");
        String message = data.get("message");

        if ("make_audio_call".equals(message)) {
            openIncomingCall(data);
        }
        else if ("end_audio_call".equals(message)) {
            closeActiveCall(data);
        }
        else if(type==null){}
    /*    else if(type.equalsIgnoreCase("call")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

                if (ContextCompat.checkSelfPermission(getApplicationContext(), READ_PHONE_STATE) == PackageManager.PERMISSION_DENIED) {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"Please Enable Phone and Microphone Settings",Toast.LENGTH_LONG).show();
                    getApplicationContext().startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getApplicationContext().getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }}

            String roomId = data.get(Constants.TAG_ROOM_ID);
            String receiverId = data.get(Constants.TAG_RECEIVER_ID);
            String senderId = data.get(Constants.TAG_SENDER_ID);
            String userName = data.get(Constants.TAG_USER_NAME);
            String userImage = data.get(Constants.TAG_USER_IMAGE);
            String platform = data.get(Constants.TAG_PLATFORM);

            if (ApplicationClass.getCurrentActivity() instanceof CallActivity) {
                CallActivity activity = (CallActivity) ApplicationClass.getCurrentActivity();
                if (activity.roomId.equals(roomId)) {
                    activity.finish();
                }
            }

            TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();

            Log.e("CallCheck","-"+CallActivity.isInCall+" isPhoneCallone "+isPhoneCallOn);

            if (!CallActivity.isInCall && isPhoneCallOn == 0) {
                //CallActivity.isInCall = true;
                AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                Intent callIntent = appRTCUtils.connectToRoom(senderId, Constants.TAG_RECEIVE, Constants.TAG_AUDIO);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(Constants.TAG_FULL_NAME, userName);
                hashMap.put(Constants.TAG_USER_IMAGE, userImage);
                hashMap.put(Constants.TAG_USER_ID, senderId);
                callIntent.putExtra("data", hashMap);
                callIntent.putExtra(Constants.TAG_ROOM_ID, roomId);
                callIntent.putExtra(Constants.TAG_PLATFORM, platform);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(callIntent);
            }

        }else  if (type.equals("bye")) {
            String roomId = data.get(Constants.TAG_ROOM_ID);
            if(ApplicationClass.getCurrentActivity() instanceof CallActivity) {
                CallActivity activity = (CallActivity) ApplicationClass.getCurrentActivity();
                Log.i(TAG, "sendPushNotification: " + activity.roomId);
                if (activity.roomId.equals(roomId)) {
                    activity.finish();
                }
            }
        }*/
        else if (type != null && type.equals("nearby")) {
            if (isAppIsInBackground(getApplicationContext())) {
                showMissingNotification(data);
                Log.e(TAG, "Background Data Payload: " + remoteMessage.getData().toString());
            } else {
                Intent intent = new Intent(getApplicationContext(), TimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                putNearbyRideExtras(intent, data);
                startActivity(intent);
                Log.e(TAG, "Foreground Data Payload: " + remoteMessage.getData().toString());
            }
        } else if (type != null && type.equals("offer_accepted")) {
            AcceptedOfferHandler.handle(data);
        } else {
            showSmallNotification(data);
        }

        /*  Map<String, String> data = remoteMessage.getData();
        Log.i(TAG, "onMessageReceived: " + data);
        type = data.get("type");

        if(type==null){}
        //Addon for audiocall
        *//*else if(type.equalsIgnoreCase("call")){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_DENIED) {
                    Looper.prepare();
                    Toast.makeText(getApplicationContext(),"Please Enable Phone Settings",Toast.LENGTH_LONG).show();
                    getApplicationContext().startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getApplicationContext().getPackageName())).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                }else{
                    String roomId = data.get(Constants.TAG_ROOM_ID);
                    String receiverId = data.get(Constants.TAG_RECEIVER_ID);
                    String senderId = data.get(Constants.TAG_SENDER_ID);
                    String userName = data.get(Constants.TAG_USER_NAME);
                    String userImage = data.get(Constants.TAG_USER_IMAGE);
                    String platform = data.get(Constants.TAG_PLATFORM);

                    *//**//*if (ApplicationClass.getCurrentActivity() instanceof CallActivity) {
                        CallActivity activity = (CallActivity) ApplicationClass.getCurrentActivity();
                        if (activity.roomId.equals(roomId)) {
                            activity.finish();
                        }
                    }*//**//*

                    TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    int isPhoneCallOn = telephony.getCallState();

                    //Log.e("CallCheck","-"+CallActivity.isInCall+" isPhoneCallone "+isPhoneCallOn);
                     //Addon for audiocall
                    *//**//*if (!CallActivity.isInCall && isPhoneCallOn == 0) {
                        //CallActivity.isInCall = true;
                        AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                        Intent callIntent = appRTCUtils.connectToRoom(senderId, Constants.TAG_RECEIVE, Constants.TAG_AUDIO);
                        HashMap<String, String> hashMap = new HashMap<>();
                        hashMap.put(Constants.TAG_FULL_NAME, userName);
                        hashMap.put(Constants.TAG_USER_IMAGE, userImage);
                        hashMap.put(Constants.TAG_USER_ID, senderId);
                        callIntent.putExtra("data", hashMap);
                        callIntent.putExtra(Constants.TAG_ROOM_ID, roomId);
                        callIntent.putExtra(Constants.TAG_PLATFORM, platform);
                        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(callIntent);
                    }*//**//*
                }
            }else {
            String roomId = data.get(Constants.TAG_ROOM_ID);
            String receiverId = data.get(Constants.TAG_RECEIVER_ID);
            String senderId = data.get(Constants.TAG_SENDER_ID);
            String userName = data.get(Constants.TAG_USER_NAME);
            String userImage = data.get(Constants.TAG_USER_IMAGE);
            String platform = data.get(Constants.TAG_PLATFORM);
             //Addon for audiocall
            *//**//*if (ApplicationClass.getCurrentActivity() instanceof CallActivity) {
                CallActivity activity = (CallActivity) ApplicationClass.getCurrentActivity();
                if (activity.roomId.equals(roomId)) {
                    activity.finish();
                }
            }*//**//*

            TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            int isPhoneCallOn = telephony.getCallState();

            //Log.e("CallCheck","-"+CallActivity.isInCall+" isPhoneCallone "+isPhoneCallOn);

            if (!CallActivity.isInCall && isPhoneCallOn == 0) {
                //CallActivity.isInCall = true;
                AppRTCUtils appRTCUtils = new AppRTCUtils(getApplicationContext());
                Intent callIntent = appRTCUtils.connectToRoom(senderId, Constants.TAG_RECEIVE, Constants.TAG_AUDIO);
                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put(Constants.TAG_FULL_NAME, userName);
                hashMap.put(Constants.TAG_USER_IMAGE, userImage);
                hashMap.put(Constants.TAG_USER_ID, senderId);
                callIntent.putExtra("data", hashMap);
                callIntent.putExtra(Constants.TAG_ROOM_ID, roomId);
                callIntent.putExtra(Constants.TAG_PLATFORM, platform);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(callIntent);
            }

        }}else  if (type.equals("bye")) {
            String roomId = data.get(Constants.TAG_ROOM_ID);
            if(ApplicationClass.getCurrentActivity() instanceof CallActivity) {
                CallActivity activity = (CallActivity) ApplicationClass.getCurrentActivity();
                Log.i(TAG, "sendPushNotification: " + activity.roomId);
                if (activity.roomId.equals(roomId)) {
                    activity.finish();
                }
            }*//*
        //}
        else if (type != null && type.equals("nearby")) {
            if (isAppIsInBackground(getApplicationContext())) {
                showMissingNotification(data);
                Log.e(TAG, "Background Data Payload: " + remoteMessage.getData().toString());
            } else {
                Intent intent = new Intent(getApplicationContext(), TimerActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("rideid", data.get("ride_id"));
                intent.putExtra("pickup_location", data.get("pickup_location"));
                intent.putExtra("pickup_lat", data.get("pickup_lat"));
                intent.putExtra("pickup_lng", data.get("pickup_lng"));
                startActivity(intent);
                Log.e(TAG, "Foreground Data Payload: " + remoteMessage.getData().toString());
            }
        } else {
            showSmallNotification(data);
        }
        */
    }

    private void openIncomingCall(Map<String, String> data) {
        if (CallActivity.isInCall) {
            return;
        }
        Log.i(TAG, "openIncomingCall: " + data);
        if (isAppIsInBackground(getApplicationContext())) {
            showIncomingCallNotification(data);
            return;
        }
        startIncomingCallActivity(data);
    }

    private void startIncomingCallActivity(Map<String, String> data) {
        Intent callIntent = new Intent(getApplicationContext(), CallActivity.class);
        callIntent.putExtra(Constants.TAG_ROOM_ID, data.get(Constants.TAG_ROOM_ID));
        callIntent.putExtra(Constants.TAG_USER_ID, data.get(Constants.TAG_SENDER_ID));
        callIntent.putExtra(Constants.TAG_FULL_NAME, data.get("user_name"));
        callIntent.putExtra(Constants.TAG_ONRIDE_ID, data.get(Constants.TAG_ONRIDE_ID));
        callIntent.putExtra(Constants.TAG_SEND, false);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(callIntent);
    }

    private void showIncomingCallNotification(Map<String, String> data) {
        String appName = getString(R.string.app_name);
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        Intent callIntent = new Intent(getApplicationContext(), CallActivity.class);
        callIntent.putExtra(Constants.TAG_ROOM_ID, data.get(Constants.TAG_ROOM_ID));
        callIntent.putExtra(Constants.TAG_USER_ID, data.get(Constants.TAG_SENDER_ID));
        callIntent.putExtra(Constants.TAG_FULL_NAME, data.get("user_name"));
        callIntent.putExtra(Constants.TAG_ONRIDE_ID, data.get(Constants.TAG_ONRIDE_ID));
        callIntent.putExtra(Constants.TAG_SEND, false);
        callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent resultPendingIntent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, callIntent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, callIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            return;
        }

        String channelId = "SHILOTA_CALLS";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId, "Incoming calls", NotificationManager.IMPORTANCE_HIGH);
            channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
            notificationManager.createNotificationChannel(channel);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        String callerName = data.get("user_name") == null ? appName : data.get("user_name");
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.small_notification)
                .setLargeIcon(bitmap)
                .setContentTitle(callerName)
                .setContentText(getString(R.string.ringing))
                .setTicker(appName)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE)
                .setContentIntent(resultPendingIntent)
                .setFullScreenIntent(resultPendingIntent, true)
                .build();

        notificationManager.notify(uniqueInt, notification);
    }

    private void closeActiveCall(Map<String, String> data) {
        if (ApplicationClass.getCurrentActivity() instanceof CallActivity) {
            CallActivity activity = (CallActivity) ApplicationClass.getCurrentActivity();
            if (activity.roomId != null && activity.roomId.equals(data.get(Constants.TAG_ROOM_ID))) {
                activity.finish();
            }
        }
    }

    public void showMissingNotification(Map<String, String> data) {
        Intent intent = null;
        String appName = getString(R.string.app_name);
        int m = 22;
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        long when = System.currentTimeMillis();

        intent = new Intent(getApplicationContext(), TimerActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        putNearbyRideExtras(intent, data);
        PendingIntent resultPendingIntent;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }
        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    "CABSO", "MISSING RIDES", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, "CABSO");
        Notification notification = mBuilder.setSmallIcon(R.drawable.small_notification).setTicker(appName).setWhen(when)
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.missing_rides)))
                .setContentTitle(appName + ": " + getString(R.string.online_notify))
                .setSmallIcon(R.drawable.small_notification)
                .setLargeIcon(bitmap)
                .setOngoing(false)
                .setContentText(getString(R.string.missing_rides))
                .build();


        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(m, notification);
    }

    private void putNearbyRideExtras(Intent intent, Map<String, String> data) {
        intent.putExtra("rideid", data.get("ride_id"));
        intent.putExtra(Constants.TAG_ONRIDE_ID, data.get("ride_id"));
        intent.putExtra(Constants.TAG_USER_ID, data.get(Constants.TAG_USER_ID));
        intent.putExtra(Constants.TAG_USER_NAME, data.get(Constants.TAG_USER_NAME));
        intent.putExtra(Constants.TAG_PICKUP_LOCATION, data.get(Constants.TAG_PICKUP_LOCATION));
        intent.putExtra(Constants.TAG_PICKUP_LAT, data.get(Constants.TAG_PICKUP_LAT));
        intent.putExtra(Constants.TAG_PICKUP_LNG, data.get(Constants.TAG_PICKUP_LNG));
        intent.putExtra(Constants.TAG_DROP_LOCATION, data.get(Constants.TAG_DROP_LOCATION));
        intent.putExtra(Constants.TAG_DROP_LAT, data.get(Constants.TAG_DROP_LAT));
        intent.putExtra(Constants.TAG_DROP_LNG, data.get(Constants.TAG_DROP_LNG));
        intent.putExtra("service_type", data.get("service_type"));
        intent.putExtra("ride_stops", data.get("ride_stops"));
        intent.putExtra("onride_otp", data.get("onride_otp"));
        intent.putExtra("approx_price", data.get("approx_price"));
        intent.putExtra("pricing_mode", data.get("pricing_mode"));
        intent.putExtra("request_type", data.get("request_type"));
        intent.putExtra("passenger_offered_price", data.get("passenger_offered_price"));
        intent.putExtra("broadcast_radius", data.get("broadcast_radius"));
        intent.putExtra("broadcast_expires_at", data.get("broadcast_expires_at"));
    }

    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }

        return isInBackground;
    }

    public void showSmallNotification(Map<String, String> tempMap) {
        Intent intent = null;
        String appName = getString(R.string.app_name);
        Random random = new Random();
        int m = random.nextInt(9999 - 1000) + 1000;
        int uniqueInt = (int) (System.currentTimeMillis() & 0xfffffff);
        long when = System.currentTimeMillis();
        String CHANNEL_ID = getString(R.string.notification_channel_id);
        CharSequence channelName = getString(R.string.app_name);

        switch (tempMap.get("type")) {
            case "cancelride":
                Intent cancel = new Intent("finish");
                sendBroadcast(cancel);
                intent = new Intent(getApplicationContext(), RideActivity.class);
                break;
            case "lost_item":
                intent = new Intent(getApplicationContext(), LostItemsActivity.class);
                break;
            default:
                intent = new Intent(getApplicationContext(), NotificationActivity.class);
                break;
        }

        PendingIntent resultPendingIntent ;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_ONE_SHOT);
        } else {
            resultPendingIntent = PendingIntent.getActivity(this, uniqueInt, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationManager mNotifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            mNotifyManager.createNotificationChannel(notificationChannel);
        }

        Bitmap bitmap = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.mipmap.ic_launcher);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID);
        Notification notification;
        notification = mBuilder.setSmallIcon(R.drawable.small_notification).setTicker(appName).setWhen(when)
                .setContentIntent(resultPendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(tempMap.get("message")))
                .setContentTitle(appName)
                .setSmallIcon(R.drawable.small_notification)
                .setLargeIcon(bitmap)
                .setContentText(tempMap.get("message"))
                .build();

        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        // Play default notification sound
        notification.defaults |= Notification.DEFAULT_SOUND;

        // Vibrate if vibrate is enabled
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        mNotifyManager.notify(getString(R.string.app_name), m, mBuilder.build());
    }

    private void storeToken(String token) {
        //saving the token on shared preferences
        SharedPrefManager.getInstance(getApplicationContext()).saveDeviceToken(token);

        //get the logined user details from preference
        ApplicationClass.pref = getApplicationContext().getSharedPreferences("SavedPref",
                MODE_PRIVATE);
        ApplicationClass.editor = ApplicationClass.pref.edit();

        if (ApplicationClass.pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(ApplicationClass.pref.getString("userId", null));

            addDeviceId();
        }
    }

    private void addDeviceId() {
        final String token = SharedPrefManager.getInstance(getApplicationContext()).getDeviceToken();
        final String deviceId = android.provider.Settings.Secure.getString(getApplicationContext().getContentResolver(),
                android.provider.Settings.Secure.ANDROID_ID);

        Map<String, String> map = new HashMap<>();
        map.put("user_id", GetSet.getUserId());
        map.put("device_token", token);
        map.put("device_type", "1");
        map.put("device_id", deviceId);
        map.put("lang_type", LocaleManager.getLanguageCode(this));

        Log.v("addDeviceId:", "Params- " + map);

        Call<Map<String, String>> call3 = apiInterface.pushsignin(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v("addDeviceId:", "response- " + data);

            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
            }
        });

    }
}
