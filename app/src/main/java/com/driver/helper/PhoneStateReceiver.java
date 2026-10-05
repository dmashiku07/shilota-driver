package com.driver.helper;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

public class PhoneStateReceiver extends BroadcastReceiver {

    public static PhoneState phoneState;

    public PhoneStateReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            String state = extras !=null ? extras.getString(TelephonyManager.EXTRA_STATE) : "";
            Log.w("DEBUG", "aa"+state);
            if (state.equals(TelephonyManager.EXTRA_STATE_RINGING) || state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                String phoneNumber = extras
                        .getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
                Log.w("DEBUG", "aa"+phoneNumber);
                if(phoneState!=null){
                    phoneState.onIncomingCall();
                }
            }
        }
    }

    public interface PhoneState{
        void onIncomingCall();
    }

}
