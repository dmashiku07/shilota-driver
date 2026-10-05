package com.driver.cabiappdriver;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;

import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;

import com.driver.helper.LocaleManager;
import com.driver.model.HelpData;
import com.driver.cabiappdriver.databinding.ActivityHelpBinding;
import com.driver.cabiappdriver.databinding.HelpItemLayBinding;
import com.driver.cabiappdriver.databinding.UsrContactLayoutBinding;
import com.driver.utils.ApiClient;
import com.driver.utils.ApiInterface;
import com.driver.utils.Constants;
import com.driver.utils.GetSet;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by hitasoft on 22/3/18.
 */

public class HelpActivity extends BaseActivity implements View.OnClickListener {
    static final String TAG = HelpActivity.class.getSimpleName();
    public static HelpData data = null;
    ActivityHelpBinding helpBinding;
    UsrContactLayoutBinding binding;
    BottomSheetDialog dialog;
    ImageView backBtn;

    FrameLayout mAdView;

    ApiInterface apiInterface;
    HelpAdapter helpAdapter;
    ArrayList<HelpData.Result> helpAry = new ArrayList<>();
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        helpBinding = DataBindingUtil.setContentView(this, R.layout.activity_help);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        backBtn = findViewById(R.id.backBtn);
        mAdView = findViewById(R.id.adLay);
        helpBinding.helpList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        helpBinding.helpList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        helpAry.clear();
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.pleasewait));
        helpAdapter = new HelpAdapter(this, helpAry);
        helpBinding.helpList.setAdapter(helpAdapter);
        if (LocaleManager.isRTL()){
            backBtn.setRotation(180);
        } else {
            backBtn.setRotation(0);
        }
        getHelpData();

        backBtn.setVisibility(View.VISIBLE);

        backBtn.setOnClickListener(this);
        helpBinding.mailFab.setOnClickListener(this);


        RelativeLayout.LayoutParams banerparams = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        banerparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        if (Constants.GOOGLE_ADS){
            loadAd();
        }


    }

    private void getHelpData() {
        Call<HelpData> call3 = apiInterface.getHelp();
        call3.enqueue(new Callback<HelpData>() {
            @Override
            public void onResponse(Call<HelpData> call, Response<HelpData> response) {
                try {
                    data = response.body();
                    Log.v(TAG, "getHelpData=" + data.result);
                    if (data.status.equals("true")) {
                        helpAry.addAll(data.result);
                        helpAdapter.notifyDataSetChanged();
                        /*progressLay.setVisibility(View.GONE);
                        scrollView.setVisibility(View.VISIBLE);
                        setProfileData();*/
                    } else if (data.status.equals("false")) {
                        /*if (data.message.equals("blocked")) {
                            Intent b = new Intent(getActivity(), BlockStatus.class);
                            startActivity(b);
                        }*/
                    } else if (data.status.equalsIgnoreCase("error")) {
                        GetSet.logout();
                       Intent intent=new Intent(HelpActivity.this,WelcomeActivity.class);
                        Toast.makeText(getApplicationContext(), getString(R.string.accountdeleted), Toast.LENGTH_SHORT).show();
                       startActivity(intent);

                    }


                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(Call<HelpData> call, Throwable t) {
                call.cancel();
            }
        });
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.backBtn) {
            onBackPressed();
        } else if (id == R.id.mail_fab) {
            binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.usr_contact_layout, null, false);
            dialog = new BottomSheetDialog(this);
            dialog.setContentView(binding.getRoot());
            dialog.setCanceledOnTouchOutside(false);
            dialog.setCancelable(false);
            dialog.show();

            if (binding.usrnamelay.getChildCount() == 2)
                binding.usrnamelay.getChildAt(1).setVisibility(View.GONE);
            if (binding.emaillay.getChildCount() == 2)
                binding.emaillay.getChildAt(1).setVisibility(View.GONE);
            if (binding.subjectlay.getChildCount() == 2)
                binding.subjectlay.getChildAt(1).setVisibility(View.GONE);
            if (binding.messagelay.getChildCount() == 2)
                binding.messagelay.getChildAt(1).setVisibility(View.GONE);

            binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ApplicationClass.requestFocus(HelpActivity.this, binding.usrname, false);
                    ApplicationClass.requestFocus(HelpActivity.this, binding.email, false);
                    ApplicationClass.requestFocus(HelpActivity.this, binding.subject, false);
                    ApplicationClass.requestFocus(HelpActivity.this, binding.message, false);
                    dialog.dismiss();
                }
            });

            binding.sendBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (binding.usrname.getText().toString().equals("")) {
                        binding.usrnamelay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.usrnamelay.setError(getString(R.string.name_error));
                        ApplicationClass.requestFocus(HelpActivity.this, binding.usrname, true);
                    } else if (binding.email.getText().toString().equals("")) {
                        binding.usrnamelay.setErrorEnabled(false);
                        binding.usrnamelay.getChildAt(1).setVisibility(View.GONE);
                        binding.emaillay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.emaillay.setError(getString(R.string.emailempty_error));
                        ApplicationClass.requestFocus(HelpActivity.this, binding.email, true);
                    } else if (!binding.email.getText().toString().matches(Constants.EMAIL_PATTERN)) {
                        binding.emaillay.setErrorEnabled(false);
                        binding.emaillay.getChildAt(1).setVisibility(View.GONE);
                        binding.emaillay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.emaillay.setError(getString(R.string.email_error));
                        ApplicationClass.requestFocus(HelpActivity.this, binding.email, true);
                    } else if (binding.subject.getText().toString().equals("")) {
                        binding.emaillay.setErrorEnabled(false);
                        binding.emaillay.getChildAt(1).setVisibility(View.GONE);
                        binding.subjectlay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.subjectlay.setError(getString(R.string.subject_error));
                        ApplicationClass.requestFocus(HelpActivity.this, binding.subject, true);
                    } else if (binding.message.getText().toString().equals("")) {
                        binding.subjectlay.setErrorEnabled(false);
                        binding.subjectlay.getChildAt(1).setVisibility(View.GONE);
                        binding.messagelay.getChildAt(1).setVisibility(View.VISIBLE);
                        binding.messagelay.setError(getString(R.string.message_error));
                        ApplicationClass.requestFocus(HelpActivity.this, binding.message, true);
                    } else {
                        contactus();
                    }
                }
            });
        }
    }

//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.backBtn:
//                onBackPressed();
//                break;
//            case R.id.mail_fab:
//                binding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.usr_contact_layout, null, false);
//                dialog = new BottomSheetDialog(this);
//                dialog.setContentView(binding.getRoot());
//                dialog.setCanceledOnTouchOutside(false);
//                dialog.setCancelable(false);
//                dialog.show();
//
//                /*TextInputLayout which displays additional padding once an error is displayed and isn't removed when the error is cleared or setErrorEnabled(false) is called.
//                 * It can be solved by following code*/
//                if (binding.usrnamelay.getChildCount() == 2)
//                    binding.usrnamelay.getChildAt(1).setVisibility(View.GONE);
//                if (binding.emaillay.getChildCount() == 2)
//                    binding.emaillay.getChildAt(1).setVisibility(View.GONE);
//                if (binding.subjectlay.getChildCount() == 2)
//                    binding.subjectlay.getChildAt(1).setVisibility(View.GONE);
//                if (binding.messagelay.getChildCount() == 2)
//                    binding.messagelay.getChildAt(1).setVisibility(View.GONE);
//
//                binding.cancelBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//
//                        ApplicationClass.requestFocus(HelpActivity.this, binding.usrname, false);
//                        ApplicationClass.requestFocus(HelpActivity.this, binding.email, false);
//                        ApplicationClass.requestFocus(HelpActivity.this, binding.subject, false);
//                        ApplicationClass.requestFocus(HelpActivity.this, binding.message, false);
//
//                        dialog.dismiss();
//                    }
//                });
//                binding.sendBtn.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        if (binding.usrname.getText().toString().equals("")) {
//                            binding.usrnamelay.getChildAt(1).setVisibility(View.VISIBLE);
//                            binding.usrnamelay.setError(getString(R.string.name_error));
//                            ApplicationClass.requestFocus(HelpActivity.this, binding.usrname, true);
//                        } else if (binding.email.getText().toString().equals("")) {
//                            binding.usrnamelay.setErrorEnabled(false);
//                            binding.usrnamelay.getChildAt(1).setVisibility(View.GONE);
//                            binding.emaillay.getChildAt(1).setVisibility(View.VISIBLE);
//                            binding.emaillay.setError(getString(R.string.emailempty_error));
//                            ApplicationClass.requestFocus(HelpActivity.this, binding.email, true);
//                        } else if (!binding.email.getText().toString().matches(Constants.EMAIL_PATTERN)) {
//                            binding.emaillay.setErrorEnabled(false);
//                            binding.emaillay.getChildAt(1).setVisibility(View.GONE);
//                            binding.emaillay.getChildAt(1).setVisibility(View.VISIBLE);
//                            binding.emaillay.setError(getString(R.string.email_error));
//                            ApplicationClass.requestFocus(HelpActivity.this, binding.email, true);
//                        } else if (binding.subject.getText().toString().equals("")) {
//                            binding.emaillay.setErrorEnabled(false);
//                            binding.emaillay.getChildAt(1).setVisibility(View.GONE);
//                            binding.subjectlay.getChildAt(1).setVisibility(View.VISIBLE);
//                            binding.subjectlay.setError(getString(R.string.subject_error));
//                            ApplicationClass.requestFocus(HelpActivity.this, binding.subject, true);
//                        } else if (binding.message.getText().toString().equals("")) {
//                            binding.subjectlay.setErrorEnabled(false);
//                            binding.subjectlay.getChildAt(1).setVisibility(View.GONE);
//                            binding.messagelay.getChildAt(1).setVisibility(View.VISIBLE);
//                            binding.messagelay.setError(getString(R.string.message_error));
//                            ApplicationClass.requestFocus(HelpActivity.this, binding.message, true);
//                        } else {
//                            contactus();
//                        }
//                    }
//                });
//                break;
//        }
//    }

    private void contactus() {
        progressDialog.show();
        Map<String, String> map = new HashMap<>();
        map.put(Constants.TAG_USER_ID, GetSet.getUserId());
        map.put("name", binding.usrname.getText().toString());
        map.put("email", binding.email.getText().toString());
        map.put("subject", binding.subject.getText().toString());
        map.put("message", binding.message.getText().toString());

        Log.v(TAG, "contactUsParams:" + map);
        Call<Map<String, String>> call3 = apiInterface.contactus(GetSet.getToken(), map);
        call3.enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                Map<String, String> data = response.body();
                Log.v(TAG, "contactUsResponse=" + data);
                if (data.get(Constants.TAG_STATUS).equals("true")) {

                    if (dialog.isShowing())
                        dialog.dismiss();
                }
                Toast.makeText(HelpActivity.this, data.get(Constants.TAG_MESSSAGE), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                call.cancel();
                progressDialog.dismiss();
            }
        });
    }

    public class HelpAdapter extends RecyclerView.Adapter<HelpAdapter.HelpViewHolder> {
        ArrayList<HelpData.Result> helpTitleLists;
        Context context;

        public HelpAdapter(Context context, ArrayList<HelpData.Result> helpTitleLists) {
            this.helpTitleLists = helpTitleLists;
            this.context = context;
        }

        @Override
        public HelpViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            HelpItemLayBinding binding = DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.help_item_lay, parent, false);
            return new HelpViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(final HelpViewHolder viewHolder, int position) {
            Log.v(TAG, "helpList=" + helpTitleLists);
            viewHolder.getBinding().helpTitle.setText(helpTitleLists.get(position).title);
            // final String content = ;


            viewHolder.getBinding().mainLay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Dialog helpdialog;
                    final ImageView dbackBtn;
                    helpdialog = new Dialog(HelpActivity.this, R.style.DialogStyle);
                    helpdialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    helpdialog.setContentView(R.layout.help_dialog);

                    dbackBtn = helpdialog.findViewById(R.id.backBtn);
                    dbackBtn.setVisibility(View.VISIBLE);
                    helpdialog.setCancelable(true);

                    final WebView webview = helpdialog.findViewById(R.id.webView);
                    final TextView title = helpdialog.findViewById(R.id.title1);
                    title.setText(helpTitleLists.get(viewHolder.getAdapterPosition()).title);
                    String pish = "<html><head><style type=\"text/css\">@font-face {font-family: MyFont;src: url(\"file:///android_asset/font_regular.ttf\")}body {font-family: MyFont;font-size: medium;text-align: justify;}</style></head><body>";
                    String pas = "</body></html>";
                    String myHtmlString = pish + helpTitleLists.get(viewHolder.getAdapterPosition()).content + pas;
                    webview.loadDataWithBaseURL(null, myHtmlString, "text/html", "UTF-8", null);
                    dbackBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            helpdialog.dismiss();
                        }
                    });

                    if (!helpdialog.isShowing()) {
                        helpdialog.show();
                    }

                }
            });
        }

        @Override
        public int getItemCount() {
            return helpTitleLists.size();
        }

        public class HelpViewHolder extends RecyclerView.ViewHolder {
            HelpItemLayBinding helpItemLayBinding;

            public HelpViewHolder(HelpItemLayBinding layoutBinding) {
                super(layoutBinding.getRoot());
                helpItemLayBinding = layoutBinding;
            }

            public HelpItemLayBinding getBinding() {
                return helpItemLayBinding;
            }
        }
    }
    private void loadAd() {
        //Addon for googleads
            //BannerAdUtils.loadBannerAd(HelpActivity.this, mAdView);
    }
}
