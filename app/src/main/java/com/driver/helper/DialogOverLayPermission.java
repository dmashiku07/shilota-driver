package com.driver.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.DialogFragment;




import static android.content.Context.MODE_PRIVATE;

import com.driver.cabiappdriver.R;

public class DialogOverLayPermission extends DialogFragment {

    private static final String TAG = DialogOverLayPermission.class.getSimpleName();
    private AppCompatTextView txtTitle;
    private ImageView dimenImage;
    private AppCompatTextView txtDescription;
    private TextView btnAllow;
    private TextView btnDeny;
    private CheckBox btnDontAsk;
    private Context context;
    private OkCallback callBack;
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(R.color.transparent)));
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            Window window = dialog.getWindow();
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.CENTER;
            // wlp.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
            window.setAttributes(wlp);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup
            container, @Nullable Bundle savedInstanceState) {
        View itemView = inflater.inflate(R.layout.dialog_overlay_permission, container, false);
        preferences = context.getSharedPreferences("SavedPref", MODE_PRIVATE);
        editor = preferences.edit();
        findViews(itemView);
        btnDontAsk.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
//                    btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_white));
                    btnAllow.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    btnAllow.setEnabled(false);
                } else {
//                    btnAllow.setBackground(context.getDrawable(R.drawable.btn_bg_primary));
                    btnAllow.setTextColor(context.getResources().getColor(R.color.colorPrimary));
                    btnAllow.setEnabled(true);
                }
            }
        });
        return itemView;

    }

    private void findViews(View itemView) {
      /*  txtTitle = (AppCompatTextView) itemView.findViewById(R.id.txtTitle);
        dimenImage = (ImageView) itemView.findViewById(R.id.dimenImage);*/
        txtDescription = (AppCompatTextView) itemView.findViewById(R.id.txtDescription);
        btnAllow = (TextView) itemView.findViewById(R.id.btnAllow);
        btnDeny = (TextView) itemView.findViewById(R.id.btnDeny);
        btnDontAsk = (CheckBox) itemView.findViewById(R.id.btnDontAsk);

        btnAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callBack.onOkClicked(true);
            }
        });
        btnDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor.putBoolean("popup_window_permission", false);
                editor.apply();
                callBack.onOkClicked(false);
            }
        });
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setCallBack(OkCallback callBack) {
        this.callBack = callBack;
    }
}
