package com.driver.cabiappdriver;

import android.annotation.SuppressLint;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Build;
import android.os.Bundle;

import com.driver.cabiappdriver.databinding.ActivityWelcomeBinding;
import com.driver.utils.Constants;
import com.google.android.material.snackbar.Snackbar;

import android.view.View;

import com.driver.cabiappdriver.R;


public class WelcomeActivity extends BaseActivity implements View.OnClickListener/*, TextureView.SurfaceTextureListener*/ {

    private static final String TAG = WelcomeActivity.class.getSimpleName();
    ActivityWelcomeBinding binding;
    Snackbar snackbar;
    int exit = 0;
    //Addon for video
    //TextureView textureView;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_welcome);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.colorSecondary));
            getWindow().setNavigationBarColor(getResources().getColor(R.color.colorSecondary));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        binding.login.setOnClickListener(this);
        binding.signup.setOnClickListener(this);
        //Addon for video
        //textureView = findViewById(R.id.textureView);
        //textureView.setSurfaceTextureListener(this);
        snackbar = Snackbar.make(findViewById(R.id.activity_welcome), getString(R.string.exit_msg), Snackbar.LENGTH_SHORT);
        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                exit = 0;
            }

            @Override
            public void onShown(Snackbar snackbar) {
                exit = 1;
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.login) {
            Intent b = new Intent(WelcomeActivity.this, LoginActivity.class);
            startActivity(b);
        } else if (id == R.id.signup) {
            Intent intent = new Intent(WelcomeActivity.this, SignupActivity.class);
            startActivity(intent);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (Constants.VIDEO_ADDON) {
            //Addon for video
            //VideoAddon.initVideo(textureView, WelcomeActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Constants.VIDEO_ADDON) {
            //Addon for video
            //VideoAddon.pauseMediaPlayer();
        }
    }

    @Override
    public void onBackPressed() {
        if (exit == 0) {
            snackbar.show();
        } else {
            WelcomeActivity.this.finishAffinity();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (Constants.VIDEO_ADDON) {
            //Addon for video
            //VideoAddon.stopMediaPlayer();
        }
    }

    /*@Override
    public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {

    }*/
}
