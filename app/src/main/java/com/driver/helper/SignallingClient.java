package com.driver.helper;

import android.annotation.SuppressLint;
import android.util.Log;


import com.driver.utils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.security.cert.X509Certificate;

import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Webrtc_Step3
 * Created by vivek-3102 on 11/03/17.
 */

public class SignallingClient {
    private static final String TAG = SignallingClient.class.getSimpleName();
    private static SignallingClient instance;
    private Socket socket;
    public boolean isChannelReady = false;
    public boolean isInitiator = false;
    public boolean isStarted = false;
    private SignalingInterface callback;

    //This piece of code should not go into production!!
    //This will help in cases where the node server is running in non-https server and you want to ignore the warnings
    @SuppressLint("TrustAllX509TrustManager")
    private final TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[]{};
        }

        public void checkClientTrusted(X509Certificate[] chain,
                                       String authType) {
        }

        public void checkServerTrusted(X509Certificate[] chain,
                                       String authType) {
        }
    }};

    public static SignallingClient getInstance() {
        if (instance == null) {
            instance = new SignallingClient();
        }
        return instance;
    }

    public void init(SignalingInterface signalingInterface) {
        this.callback = signalingInterface;
        try {
            /*SSLContext sslcontext = SSLContext.getInstance("TLS");
            sslcontext.init(null, trustAllCerts, null);
            IO.setDefaultHostnameVerifier((hostname, session) -> true);
            IO.setDefaultSSLContext(sslcontext);*/
            //set the socket.io url here
            socket = IO.socket(Constants.SOCKET_URL);
            socket.connect();
            Log.i(TAG, "init: Connected");

            //messages - SDP and ICE candidates are transferred through this
            listenRTCMessage();
            listenByeMessage();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void listenRTCMessage() {
        socket.on("rtcmessage", args -> {
            Log.i(TAG, "onAwayStatus: " + args[0]);
            try {
                JSONObject jsonObject = new JSONObject("" + args[0]);
                callback.onAwayStatus(jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void listenByeMessage() {
        socket.on("bye", args -> {
            Log.i(TAG, "listenByeMessage: " + args[0]);
            callback.onByeStatus("" + args[0]);
        });
    }

    public void createOrJoin(String message) {
        Log.v(TAG, "emitInitStatement() called with: event = [" + "create or join" + "], message = [" + message + "]");
        socket.emit("create or join", message);
    }

    public void emitOnlineStatus(String onlineStatus, String userId, String partnerId, String roomId) {
        try {
            JSONObject obj = new JSONObject();
            JSONObject msg = new JSONObject();
            msg.put(Constants.TAG_TYPE, onlineStatus);
            msg.put(Constants.TAG_USER_ID, userId);
            msg.put(Constants.TAG_RECEIVER_ID, partnerId);
            msg.put(Constants.TAG_ROOM_ID, roomId);
            obj.put("room", roomId);
            obj.put("message", msg);
            Log.i(TAG, "emitOnlineStatus: " + obj);
            if (socket != null)
                socket.emit("rtcmessage", obj);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void close(String roomID) {
        if (socket != null) {
            try {
            /*    JSONObject roomObject = new JSONObject();
                roomObject.put("room_id", roomID);
                socket.emit("bye", roomObject);
                    Log.e("bye", "- "+roomObject);
            */
            }catch(Exception e){
             Log.e("SocketExp","-"+e.toString());
            }
            socket.emit("bye", roomID);
            socket.disconnect();
            socket = null;
        }
        isInitiator = false;
        isChannelReady = false;
        isStarted = false;
    }

    public void disconnect() {
        if (socket != null) {
            socket.disconnect();
            instance = null;
        }
    }

    public interface SignalingInterface {
        void onAwayStatus(JSONObject online);

        void onByeStatus(String bye);
    }
}
