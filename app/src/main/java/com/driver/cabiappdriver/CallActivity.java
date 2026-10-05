package com.driver.cabiappdriver;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SdpObserver;
import org.webrtc.SessionDescription;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.engineio.client.transports.WebSocket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CallActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "ShilotaCall";
    public static boolean isInCall = false;
    public String roomId = "";

    private static final String AUDIO_TRACK_ID = "CABIAudioTrack";
    private static final int AUDIO_PERMISSION_REQUEST = 701;

    private ApiInterface apiInterface;
    private Socket socket;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private AudioSource localAudioSource;
    private AudioTrack localAudioTrack;
    private AudioManager audioManager;
    private boolean isCaller;
    private String partnerId = "";
    private String partnerName = "";
    private String onrideId = "";
    private boolean callStarted = false;
    private boolean offerSent = false;
    private boolean remoteDescriptionSet = false;
    private boolean accepted = false;
    private boolean isMuted = false;
    private boolean speakerOn = true;
    private long connectedAt = 0L;
    private final List<IceCandidate> pendingRemoteCandidates = new ArrayList<>();
    private final List<PeerConnection.IceServer> iceServers = new ArrayList<>();
    private final Handler durationHandler = new Handler(Looper.getMainLooper());
    private final Runnable durationRunnable = new Runnable() {
        @Override
        public void run() {
            updateCallDuration();
            durationHandler.postDelayed(this, 1000);
        }
    };

    private TextView userName;
    private TextView callType;
    private TextView callTime;
    private LinearLayout acceptLay;
    private LinearLayout callControlsLay;
    private ImageView btnAccept;
    private ImageView btnDecline;
    private TextView btnMute;
    private TextView btnSpeaker;
    private TextView btnHangup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);

        roomId = getIntent().getStringExtra(Constants.TAG_ROOM_ID);
        partnerId = getIntent().getStringExtra(Constants.TAG_USER_ID);
        partnerName = getIntent().getStringExtra(Constants.TAG_FULL_NAME);
        onrideId = getIntent().getStringExtra(Constants.TAG_ONRIDE_ID);
        isCaller = getIntent().getBooleanExtra(Constants.TAG_SEND, false);
        if (roomId == null || roomId.trim().isEmpty()) {
            roomId = "call_" + System.currentTimeMillis();
        }

        userName = findViewById(R.id.userName);
        callType = findViewById(R.id.callType);
        callTime = findViewById(R.id.callTime);
        acceptLay = findViewById(R.id.acceptLay);
        callControlsLay = findViewById(R.id.callControlsLay);
        btnAccept = findViewById(R.id.btnAccept);
        btnDecline = findViewById(R.id.btnDecline);
        btnMute = findViewById(R.id.btnMute);
        btnSpeaker = findViewById(R.id.btnSpeaker);
        btnHangup = findViewById(R.id.btnHangup);

        userName.setText(partnerName == null || partnerName.isEmpty() ? getString(R.string.app_name) : partnerName);
        TextView avatarInitial = findViewById(R.id.userImage);
        String avatarName = partnerName == null || partnerName.trim().isEmpty()
                ? getString(R.string.app_name)
                : partnerName.trim();
        avatarInitial.setText(avatarName.substring(0, 1).toUpperCase(Locale.getDefault()));
        btnAccept.setOnClickListener(this);
        btnDecline.setOnClickListener(this);
        btnMute.setOnClickListener(this);
        btnSpeaker.setOnClickListener(this);
        btnHangup.setOnClickListener(this);

        isInCall = true;
        setupAudioMode();

        if (hasAudioPermission()) {
            initCall();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, AUDIO_PERMISSION_REQUEST);
        }
    }

    private boolean hasAudioPermission() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == AUDIO_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initCall();
        } else {
            Toast.makeText(this, getString(R.string.read_phone_state_permission_forandroid12), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void initCall() {
        initPeerFactory();
        iceServers.clear();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer());
        iceServers.add(PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer());
        if (isCaller) {
            acceptLay.setVisibility(View.GONE);
            showCallControls();
            callType.setText(R.string.calling_rider);
        } else {
            acceptLay.setVisibility(View.VISIBLE);
            callType.setText(R.string.ringing);
        }
        loadIceServersAndConnect();
    }

    @SuppressWarnings("unchecked")
    private void loadIceServersAndConnect() {
        apiInterface.getCallIceServers(GetSet.getToken(), GetSet.getUserId())
                .enqueue(new Callback<HashMap<String, Object>>() {
                    @Override
                    public void onResponse(Call<HashMap<String, Object>> call, Response<HashMap<String, Object>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            Object configured = response.body().get("ice_servers");
                            if (configured instanceof List) {
                                for (Object entry : (List<?>) configured) {
                                    if (!(entry instanceof Map)) continue;
                                    Map<String, Object> server = (Map<String, Object>) entry;
                                    Object rawUrls = server.get("urls");
                                    List<String> urls = new ArrayList<>();
                                    if (rawUrls instanceof List) {
                                        for (Object url : (List<?>) rawUrls) urls.add(String.valueOf(url));
                                    } else if (rawUrls != null) {
                                        urls.add(String.valueOf(rawUrls));
                                    }
                                    if (urls.isEmpty()) continue;
                                    PeerConnection.IceServer.Builder builder = PeerConnection.IceServer.builder(urls);
                                    Object username = server.get("username");
                                    Object credential = server.get("credential");
                                    if (username != null) builder.setUsername(String.valueOf(username));
                                    if (credential != null) builder.setPassword(String.valueOf(credential));
                                    iceServers.add(builder.createIceServer());
                                }
                            }
                        }
                        initSocket();
                    }

                    @Override
                    public void onFailure(Call<HashMap<String, Object>> call, Throwable throwable) {
                        Log.w(TAG, "TURN configuration unavailable; using STUN fallback", throwable);
                        initSocket();
                    }
                });
    }

    private void initPeerFactory() {
        PeerConnectionFactory.initialize(
                PeerConnectionFactory.InitializationOptions.builder(this).createInitializationOptions()
        );
        peerConnectionFactory = PeerConnectionFactory.builder().createPeerConnectionFactory();
    }

    private void initSocket() {
        try {
            if (isCaller) startPeer();
            IO.Options options = new IO.Options();
            options.transports = new String[]{WebSocket.NAME};
            options.reconnection = true;
            options.forceNew = true;
            socket = IO.socket(Constants.SOCKET_URL, options);
            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.i(TAG, "Signaling connected room=" + roomId);
                joinCall();
                if (accepted) emitReadyCall();
            });
            socket.on(Socket.EVENT_CONNECT_ERROR, args -> runOnUiThread(() -> {
                Log.e(TAG, "Signaling connect error: " + (args.length > 0 ? args[0] : "unknown"));
                callType.setText(R.string.connecting);
            }));
            socket.on(Socket.EVENT_DISCONNECT, args -> Log.w(TAG, "Signaling disconnected"));
            socket.on("call-ready", args -> runOnUiThread(() -> {
                if (isCaller && peerConnection != null) {
                    createOffer();
                }
            }));
            socket.on("rtcmessage", args -> runOnUiThread(() -> {
                Log.d(TAG, "RTC message received: " + (args.length > 0 ? args[0] : "empty"));
                handleRtcMessage(args.length > 0 ? args[0] : null);
            }));
            socket.on("bye", args -> runOnUiThread(this::finish));
            socket.connect();
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.tryagain), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void joinCall() {
        if (socket == null || roomId == null) {
            return;
        }
        try {
            JSONObject data = new JSONObject();
            data.put(Constants.TAG_ROOM_ID, roomId);
            data.put(Constants.TAG_USER_ID, GetSet.getUserId());
            socket.emit("join-call", data);
        } catch (Exception ignored) {
        }
    }

    private void emitReadyCall() {
        if (socket == null || !socket.connected()) return;
        try {
            JSONObject data = new JSONObject();
            data.put(Constants.TAG_ROOM_ID, roomId);
            data.put(Constants.TAG_USER_ID, GetSet.getUserId());
            socket.emit("ready-call", data);
        } catch (Exception error) {
            Log.e(TAG, "Unable to emit ready-call", error);
        }
    }

    private void startPeer() {
        if (callStarted) {
            return;
        }
        callStarted = true;
        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);
        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new PeerConnection.Observer() {
            @Override public void onSignalingChange(PeerConnection.SignalingState signalingState) {}
            @Override public void onIceConnectionChange(PeerConnection.IceConnectionState state) {
                Log.i(TAG, "ICE state=" + state);
                runOnUiThread(() -> {
                    if (state == PeerConnection.IceConnectionState.CONNECTED || state == PeerConnection.IceConnectionState.COMPLETED) {
                        markConnected();
                    } else if (state == PeerConnection.IceConnectionState.DISCONNECTED || state == PeerConnection.IceConnectionState.FAILED) {
                        callType.setText(R.string.call_cancelled);
                    }
                });
            }
            @Override public void onIceConnectionReceivingChange(boolean b) {}
            @Override public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {}
            @Override public void onIceCandidate(IceCandidate candidate) { sendIceCandidate(candidate); }
            @Override public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {}
            @Override public void onAddStream(MediaStream mediaStream) {}
            @Override public void onRemoveStream(MediaStream mediaStream) {}
            @Override public void onDataChannel(DataChannel dataChannel) {}
            @Override public void onRenegotiationNeeded() {}
            @Override public void onAddTrack(RtpReceiver rtpReceiver, MediaStream[] mediaStreams) {}
        });

        localAudioSource = peerConnectionFactory.createAudioSource(new MediaConstraints());
        localAudioTrack = peerConnectionFactory.createAudioTrack(AUDIO_TRACK_ID, localAudioSource);
        localAudioTrack.setEnabled(true);
        List<String> streamIds = new ArrayList<>();
        streamIds.add("CABIAudioStream");
        peerConnection.addTrack(localAudioTrack, streamIds);
    }

    private void createOffer() {
        if (peerConnection == null || offerSent) return;
        offerSent = true;
        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override public void onSetSuccess() { sendSessionDescription(sessionDescription); }
                    @Override public void onSetFailure(String error) {
                        offerSent = false;
                        Log.e(TAG, "Failed to set local offer: " + error);
                    }
                }, sessionDescription);
            }
            @Override public void onCreateFailure(String error) {
                offerSent = false;
                Log.e(TAG, "Failed to create offer: " + error);
            }
        }, new MediaConstraints());
    }

    private void createAnswer() {
        peerConnection.createAnswer(new SimpleSdpObserver() {
            @Override public void onCreateSuccess(SessionDescription sessionDescription) {
                peerConnection.setLocalDescription(new SimpleSdpObserver() {
                    @Override public void onSetSuccess() { sendSessionDescription(sessionDescription); }
                }, sessionDescription);
            }
        }, new MediaConstraints());
    }

    private void handleRtcMessage(Object payload) {
        try {
            JSONObject data = payload instanceof JSONObject ? (JSONObject) payload : new JSONObject(String.valueOf(payload));
            String messageType = data.optString("message_type");
            if ("offer".equals(messageType) || "answer".equals(messageType)) {
                startPeer();
                SessionDescription.Type type = "offer".equals(messageType) ? SessionDescription.Type.OFFER : SessionDescription.Type.ANSWER;
                SessionDescription description = new SessionDescription(type, data.optString("sdp"));
                peerConnection.setRemoteDescription(new SimpleSdpObserver() {
                    @Override public void onSetSuccess() {
                        remoteDescriptionSet = true;
                        flushPendingRemoteCandidates();
                        if ("offer".equals(messageType)) {
                            createAnswer();
                        }
                    }
                    @Override public void onSetFailure(String error) {
                        Log.e(TAG, "Failed to set remote " + messageType + ": " + error);
                    }
                }, description);
            } else if ("candidate".equals(messageType)) {
                JSONArray candidateArray = data.optJSONArray("candidate");
                if (candidateArray != null) {
                    IceCandidate candidate = new IceCandidate(
                            candidateArray.optString(0),
                            candidateArray.optInt(1),
                            candidateArray.optString(2)
                    );
                    if (peerConnection == null || !remoteDescriptionSet) {
                        pendingRemoteCandidates.add(candidate);
                    } else {
                        peerConnection.addIceCandidate(candidate);
                    }
                }
            }
        } catch (Exception error) {
            Log.e(TAG, "Invalid RTC message", error);
        }
    }

    private void flushPendingRemoteCandidates() {
        if (peerConnection == null || !remoteDescriptionSet) return;
        for (IceCandidate candidate : pendingRemoteCandidates) {
            peerConnection.addIceCandidate(candidate);
        }
        pendingRemoteCandidates.clear();
    }

    private void sendSessionDescription(SessionDescription description) {
        try {
            JSONObject data = new JSONObject();
            data.put(Constants.TAG_ROOM_ID, roomId);
            data.put("message_type", description.type.canonicalForm());
            data.put("sdp", description.description);
            socket.emit("rtcmessage", data);
        } catch (Exception ignored) {
        }
    }

    private void sendIceCandidate(IceCandidate candidate) {
        try {
            JSONObject data = new JSONObject();
            JSONArray candidateArray = new JSONArray();
            candidateArray.put(candidate.sdpMid);
            candidateArray.put(candidate.sdpMLineIndex);
            candidateArray.put(candidate.sdp);
            data.put(Constants.TAG_ROOM_ID, roomId);
            data.put("message_type", "candidate");
            data.put("candidate", candidateArray);
            socket.emit("rtcmessage", data);
        } catch (Exception ignored) {
        }
    }

    private void setupAudioMode() {
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
            audioManager.setSpeakerphoneOn(speakerOn);
        }
    }

    private void showCallControls() {
        if (callControlsLay != null) {
            callControlsLay.setVisibility(View.VISIBLE);
        }
        if (callTime != null) {
            callTime.setVisibility(View.VISIBLE);
        }
    }

    private void markConnected() {
        callType.setText(R.string.connected);
        showCallControls();
        if (connectedAt == 0L) {
            connectedAt = System.currentTimeMillis();
            durationHandler.post(durationRunnable);
        }
    }

    private void updateCallDuration() {
        if (callTime == null || connectedAt == 0L) {
            return;
        }
        long elapsed = Math.max(0L, (System.currentTimeMillis() - connectedAt) / 1000L);
        callTime.setText(String.format(Locale.US, "%02d:%02d", elapsed / 60, elapsed % 60));
    }

    private void toggleMute() {
        isMuted = !isMuted;
        if (localAudioTrack != null) {
            localAudioTrack.setEnabled(!isMuted);
        }
        btnMute.setText(isMuted ? R.string.unmute : R.string.mute);
    }

    private void toggleSpeaker() {
        speakerOn = !speakerOn;
        if (audioManager == null) {
            audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        }
        if (audioManager != null) {
            audioManager.setSpeakerphoneOn(speakerOn);
        }
        btnSpeaker.setText(speakerOn ? R.string.speaker_on : R.string.speaker_off);
    }

    private void endCall() {
        try {
            if (socket != null) {
                JSONObject data = new JSONObject();
                data.put(Constants.TAG_ROOM_ID, roomId);
                socket.emit("bye", data);
            }
        } catch (Exception ignored) {
        }

        HashMap<String, String> map = new HashMap<>();
        map.put("sender_id", GetSet.getUserId());
        map.put("receiver_id", partnerId == null ? "" : partnerId);
        map.put("type", "audio");
        map.put("user_type", "driver");
        map.put("room_id", roomId);
        map.put(Constants.TAG_ONRIDE_ID, onrideId == null ? "" : onrideId);
        map.put("platform", "android");
        map.put("timestamp", String.valueOf(System.currentTimeMillis()));
        apiInterface.endCall(GetSet.getToken(), map).enqueue(new Callback<Map<String, String>>() {
            @Override public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {}
            @Override public void onFailure(Call<Map<String, String>> call, Throwable t) { call.cancel(); }
        });
        finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnAccept) {
            if (accepted) return;
            accepted = true;
            acceptLay.setVisibility(View.GONE);
            showCallControls();
            callType.setText(R.string.connecting);
            startPeer();
            emitReadyCall();
        } else if (view.getId() == R.id.btnDecline) {
            endCall();
        } else if (view.getId() == R.id.btnMute) {
            toggleMute();
        } else if (view.getId() == R.id.btnSpeaker) {
            toggleSpeaker();
        } else if (view.getId() == R.id.btnHangup) {
            endCall();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isInCall = false;
        durationHandler.removeCallbacks(durationRunnable);
        if (localAudioTrack != null) {
            localAudioTrack.dispose();
        }
        if (localAudioSource != null) {
            localAudioSource.dispose();
        }
        if (peerConnection != null) {
            peerConnection.close();
            peerConnection.dispose();
        }
        if (socket != null) {
            socket.off();
            socket.disconnect();
            socket.close();
        }
        if (peerConnectionFactory != null) {
            peerConnectionFactory.dispose();
        }
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }
    }

    private static class SimpleSdpObserver implements SdpObserver {
        @Override public void onCreateSuccess(SessionDescription sessionDescription) {}
        @Override public void onSetSuccess() {}
        @Override public void onCreateFailure(String s) {}
        @Override public void onSetFailure(String s) {}
    }
}
