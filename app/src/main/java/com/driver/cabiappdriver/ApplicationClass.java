package com.driver.cabiappdriver;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.os.Build;
import android.os.Handler;
import android.text.InputFilter;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.WindowMetrics;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;
import androidx.multidex.MultiDex;

import com.driver.external.FontsOverride;
import com.driver.helper.LocaleManager;
import com.driver.helper.NetworkReceiver;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;
import com.google.android.material.snackbar.Snackbar;


import org.acra.annotation.ReportsCrashes;

import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Created by hitasoft on 24/1/18.
 */

@ReportsCrashes(mailTo = "crashlog@hitasoft.com")
public class ApplicationClass extends Application {
    private static final String TAG = ApplicationClass.class.getSimpleName();
    private static final TimeZone TANZANIA_TIME_ZONE = TimeZone.getTimeZone("Africa/Dar_es_Salaam");
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;
    public static IntentFilter intentFilter;
    private static Snackbar snackbar = null;
    private static ApplicationClass mInstance;
    private static Toast toast = null;
    private Socket mSocket;
    private static boolean activityVisible;
    private static Activity mCurrentActivity = null;


    public static synchronized ApplicationClass getInstance() {
        return mInstance;
    }

    // Showing network status in Snackbar
    public static void showSnack(final Context context, View view, boolean isConnected) {
        if (snackbar == null) {
            snackbar = Snackbar
                    .make(view, context.getString(R.string.network_failure), Snackbar.LENGTH_INDEFINITE)
                    .setAction("SETTINGS", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent intent = new Intent(android.provider.Settings.ACTION_SETTINGS);
                            context.startActivity(intent);
                        }
                    });
            View sbView = snackbar.getView();
            TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
        }

        if (!isConnected && !snackbar.isShown()) {
            snackbar.show();
        } else {
            snackbar.dismiss();
            snackbar = null;
        }
    }


    public static void showToast(final Context context, String text, int duration) {

        if (toast == null || toast.getView().getWindowVisibility() != View.VISIBLE) {
            toast = Toast.makeText(context, text, duration);
            toast.show();
        } else toast.cancel();
    }

    /**
     * To convert the given dp value to pixel
     **/
    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    public static String getDateTimeFromISO(String time) {
        try {
            DateFormat df1 = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSS'Z'");
            Date value = df1.parse(time);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            simpleDateFormat.setTimeZone(TANZANIA_TIME_ZONE);
            return simpleDateFormat.format(value);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static void hideSoftKeyboard(Activity context, View view) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException npe) {
        } catch (Exception e) {
        }
    }

    public static float pxToDp(Context context, float px) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }



    public static void activityResumed() {
        activityVisible = true;
    }

    public static void activityPaused() {
        activityVisible = false;
    }


    public static void setCurrentActivity(Activity activity) {
        mCurrentActivity = activity;
    }

    public static Activity getCurrentActivity() {
        return mCurrentActivity;
    }


    public static void defaultSnack(View view, String message, String type) {
        if (snackbar == null) {
            snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            View sbView = snackbar.getView();
            TextView textView = sbView.findViewById(com.google.android.material.R.id.snackbar_text);
            if ("alert".equals(type))
                textView.setTextColor(Color.WHITE);
            if ("error".equals(type))
                textView.setTextColor(Color.RED);
        }
        if (!snackbar.isShown()) {
            snackbar.show();
            snackbar = null;
        } else {
            snackbar.dismiss();
            snackbar = null;
        }
    }


    public static void requestFocus(Activity activity, View view, boolean isEnabled) {
        if (view.requestFocus()) {
            if (isEnabled)
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            else
                activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }
    }

    public static void showStatusDialog(final Activity context, boolean status, String postedMessage, final Class destinationClass) {
        final Dialog dialog = new Dialog(context, android.R.style.Theme_Black_NoTitleBar);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.default_dialog_lay);
        int height = ViewGroup.LayoutParams.MATCH_PARENT;
        int width = ViewGroup.LayoutParams.MATCH_PARENT;
        dialog.getWindow().setLayout(width, height);
        ImageView close = dialog.findViewById(R.id.close);
        TextView tick_posted = dialog.findViewById(R.id.dialog_msg);
        tick_posted.setText(postedMessage);
        if (status) {
            dialog.show();
            close.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.finish();
                    dialog.dismiss();
                }
            });
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent intent = new Intent(context, destinationClass);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    context.startActivity(intent);
                    dialog.dismiss();
                }
            }, Constants.DIALOG_DELAY);
        }
    }

    public static String getDateTimeFromStamp(String timeStamp) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
            simpleDateFormat.setTimeZone(TANZANIA_TIME_ZONE);
            Date netDate = (new Date(Long.parseLong(timeStamp) * 1000));
            return simpleDateFormat.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }
    }

    public static String getDateTimeFromStamp(long timeStamp) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("EEE");
            sdf.setTimeZone(TANZANIA_TIME_ZONE);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM dd yyyy' 'hh:mm a");
            simpleDateFormat.setTimeZone(TANZANIA_TIME_ZONE);
            Date netDate = (new Date(timeStamp * 1000));
            String dayOfTheWeek = sdf.format(netDate);
            return dayOfTheWeek + " " + simpleDateFormat.format(netDate);
        } catch (Exception ex) {
            ex.printStackTrace();
            return "";
        }

    }

    public static String getRideTimeFromTimeStamp(Context context, long timeStamp) {
        try {
            Date date = (new Date(timeStamp * 1000));
            SimpleDateFormat dayMonthFormat = new SimpleDateFormat("EEE MMM", LocaleManager.getLocale(context.getResources()));
            dayMonthFormat.setTimeZone(TANZANIA_TIME_ZONE);
            String dayMonth = dayMonthFormat.format(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd yyyy' 'hh:mm aaa", Locale.US);
            simpleDateFormat.setTimeZone(TANZANIA_TIME_ZONE);
            String time = simpleDateFormat.format(date);
            return dayMonth + " " + time + " " ;
        } catch (Exception ex) {
            ex.printStackTrace();
            Log.e(TAG, "getRideTimeFromTimeStamp: " + ex.getMessage());
            return "";
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
        pref = getApplicationContext().getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = pref.edit();
    //    ACRA.init(this);

        intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.CONNECTIVITY_ACTION);
        FontsOverride.setDefaultFont(this, "DEFAULT", "font_regular.ttf");
        FontsOverride.setDefaultFont(this, "SANS_SERIF", "font_regular.ttf");
        FontsOverride.setDefaultFont(this, "MONOSPACE", "font_regular.ttf");
        if (pref.getBoolean("isLogged", false)) {
            GetSet.setLogged(true);
            GetSet.setUserId(pref.getString("userId", null));
            GetSet.setUserName(pref.getString("userName", null));
            GetSet.setFullName(pref.getString("fullName", null));
            GetSet.setImageUrl(pref.getString("userImage", null));
            GetSet.setToken(pref.getString("token", null));
            GetSet.setWallet(pref.getString("wallet", null));
        }

        {
            try {
                mSocket = IO.socket(Constants.SOCKET_URL);
                com.driver.helper.AcceptedOfferHandler.initialize(this, mSocket);
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Socket getSocket() {
        return mSocket;
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LocaleManager.setLocale(context));
        MultiDex.install(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    public void setConnectivityListener(NetworkReceiver.ConnectivityReceiverListener listener) {
        NetworkReceiver.connectivityReceiverListener = listener;
    }

    /**
     * function for avoiding emoji typing in keyboard
     **/
    public static InputFilter EMOJI_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL) {
                    return "";
                }
            }
            return null;
        }
    };

    /**
     * function for avoiding emoji typing in keyboard
     **/
    public static InputFilter NUMBER_FILTER = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int index = start; index < end; index++) {

                int type = Character.getType(source.charAt(index));

                if (type == Character.DECIMAL_DIGIT_NUMBER) {
                    return "";
                }
            }
            return null;
        }
    };


    public static int getAdWidth(FragmentActivity activity) {
        float widthPixels;
        float density;
        int adWidth;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets()
                    .getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            widthPixels = windowMetrics.getBounds().width() - insets.left - insets.right;
            Display display = activity.getDisplay();
            DisplayMetrics displayMetrics = activity.getResources().getDisplayMetrics();
            display.getRealMetrics(displayMetrics);
            density = displayMetrics.densityDpi;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            widthPixels = displayMetrics.widthPixels;
            density = displayMetrics.density;
        }
        adWidth = (int) (widthPixels / density);
//        Log.d(TAG, "getAdWidth: " + widthPixels);
//        Log.d(TAG, "getAdWidth: " + density);
//        Log.d(TAG, "getAdWidth: " + adWidth);
        return adWidth;
    }
    public static void preventMultipleClick(final View view) {
        view.setEnabled(false);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                view.setEnabled(true);
            }
        }, 1000);
    }
}
