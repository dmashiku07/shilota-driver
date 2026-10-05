package com.driver.cabiappdriver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.driver.model.UserData;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ActivityWebView extends BaseActivity implements View.OnClickListener {
    private static final String TAG = ActivityWebView.class.getSimpleName();

    ApiInterface apiInterface;
    String webviewUrl = "", returnUrl = "", paidAmount = "", transId = "", verifyUrl = "";
    WebView webView;
    ImageView leftArrow;
    boolean verificationStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        webView = findViewById(R.id.webView);
        leftArrow = findViewById(R.id.left_arrow);
        leftArrow.setOnClickListener(this);

        Intent intent = getIntent();
        webviewUrl = intent.getStringExtra("webview_url");
        returnUrl = intent.getStringExtra("return_url");
        paidAmount = intent.getStringExtra("amount");
        transId = intent.getStringExtra("transid");
        verifyUrl = intent.getStringExtra("verify_url");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, @NonNull WebResourceRequest request) {
                String url = request.getUrl().toString();
                Log.v(TAG, "shouldOverrideUrlLoading=" + url);
                if (!verificationStarted && matchesReturnUrl(url)) {
                    verificationStarted = true;
                    verifySelcomPayment();
                }
                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        if (webviewUrl != null) {
            webView.loadUrl(webviewUrl);
        }
    }

    private boolean matchesReturnUrl(String currentUrl) {
        try {
            Uri expected = Uri.parse(returnUrl);
            Uri actual = Uri.parse(currentUrl);
            return stringEquals(expected.getScheme(), actual.getScheme())
                    && stringEquals(expected.getHost(), actual.getHost())
                    && stringEquals(normalizePath(expected.getPath()), normalizePath(actual.getPath()));
        } catch (Exception e) {
            return returnUrl != null && returnUrl.equals(currentUrl);
        }
    }

    private String normalizePath(String value) {
        return value == null ? "" : value.replaceAll("/+$", "");
    }

    private boolean stringEquals(String left, String right) {
        return left == null ? right == null : left.equalsIgnoreCase(right);
    }

    private void verifySelcomPayment() {
        if (transId == null || transId.trim().isEmpty()) {
            Toast.makeText(this, getString(R.string.something_went_to_be_wrong), Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Call<UserData> call = (verifyUrl != null && !verifyUrl.trim().isEmpty())
                ? apiInterface.getMethod(verifyUrl)
                : apiInterface.verifySelcom(transId);

        call.enqueue(new Callback<UserData>() {
            @Override
            public void onResponse(Call<UserData> call, Response<UserData> response) {
                if (response.isSuccessful() && response.body() != null && "true".equals(response.body().status)) {
                    UserData data = response.body();
                    String verifiedAmount = data.paidamount != null && !data.paidamount.trim().isEmpty() ? data.paidamount : paidAmount;
                    showResultDialog(data.message, data.transid != null ? data.transid : transId, verifiedAmount, true);
                } else {
                    UserData data = response.body();
                    showResultDialog(data != null && data.message != null ? data.message : getString(R.string.something_went_to_be_wrong), "", "", false);
                }
            }

            @Override
            public void onFailure(Call<UserData> call, Throwable t) {
                call.cancel();
                showResultDialog(getString(R.string.something_went_to_be_wrong), "", "", false);
            }
        });
    }

    private void showResultDialog(String message, String transId, String amount, boolean success) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ActivityWebView.this);
        builder.setTitle(getString(R.string.alert));
        builder.setMessage(message);
        builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (success) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra("transid", transId);
                    resultIntent.putExtra("paidamount", amount);
                    setResult(Activity.RESULT_OK, resultIntent);
                } else {
                    setResult(Activity.RESULT_CANCELED);
                }
                dialog.dismiss();
                finish();
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.left_arrow) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                finish();
            }
        }
    }
}
