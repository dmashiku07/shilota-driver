package com.driver.cabiappdriver;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.ImageView;

import com.driver.helper.LocaleManager;
import com.driver.cabiappdriver.databinding.ActivityAddBraintreeBinding;




public class AddBraintreeActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = AddBraintreeActivity.class.getSimpleName();
    ImageView backBtn;
    ActivityAddBraintreeBinding braintreeBinding;

//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_add_braintree);
//        braintreeBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_braintree);
//        backBtn = findViewById(R.id.backBtn);
//
//        braintreeBinding.address.setFilters(new InputFilter[]{ApplicationClass.EMOJI_FILTER});
//        if (PaymentActivity.routing_no != null && !PaymentActivity.routing_no.equals("")) {
//            braintreeBinding.routingNo.setText(PaymentActivity.routing_no);
//            braintreeBinding.bankAcc.setText(PaymentActivity.bank_ac);
//            braintreeBinding.name.setText(PaymentActivity.name);
//            braintreeBinding.address.setText(PaymentActivity.address);
//        }
//
//        if (LocaleManager.isRTL()){
//            backBtn.setRotation(180);
//        } else {
//            backBtn.setRotation(0);
//        }
//
//        backBtn.setVisibility(View.VISIBLE);
//        backBtn.setOnClickListener(this);
//        braintreeBinding.saveBtn.setOnClickListener(this);
//        /*TextInputLayout which displays additional padding once an error is displayed and isn't removed when the error is cleared or setErrorEnabled(false) is called.
//         * It can be solved by following code*/
//        if (braintreeBinding.routinglay.getChildCount() == 2)
//            braintreeBinding.routinglay.getChildAt(1).setVisibility(View.GONE);
//        if (braintreeBinding.bankAcclay.getChildCount() == 2)
//            braintreeBinding.bankAcclay.getChildAt(1).setVisibility(View.GONE);
//        if (braintreeBinding.namelay.getChildCount() == 2)
//            braintreeBinding.namelay.getChildAt(1).setVisibility(View.GONE);
//        if (braintreeBinding.addresslay.getChildCount() == 2)
//            braintreeBinding.addresslay.getChildAt(1).setVisibility(View.GONE);
//
//    }
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_add_braintree);
    braintreeBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_braintree);
    backBtn = findViewById(R.id.backBtn);

    // Remove routing number input filters and text
    // braintreeBinding.routingNo.setFilters(new InputFilter[]{ApplicationClass.EMOJI_FILTER});
    // Note: Do not set any text for routing number since it is removed

    // Populate existing data without routing number
    braintreeBinding.bankAcc.setText(PaymentActivity.bank_ac);
    braintreeBinding.name.setText(PaymentActivity.name);
    braintreeBinding.address.setText(PaymentActivity.address);

    if (LocaleManager.isRTL()){
        backBtn.setRotation(180);
    } else {
        backBtn.setRotation(0);
    }

    backBtn.setVisibility(View.VISIBLE);
    backBtn.setOnClickListener(this);
    braintreeBinding.saveBtn.setOnClickListener(this);

    // Hide error views for the removed routing number field
    if (braintreeBinding.bankAcclay.getChildCount() == 2)
        braintreeBinding.bankAcclay.getChildAt(1).setVisibility(View.GONE);
    if (braintreeBinding.namelay.getChildCount() == 2)
        braintreeBinding.namelay.getChildAt(1).setVisibility(View.GONE);
    if (braintreeBinding.addresslay.getChildCount() == 2)
        braintreeBinding.addresslay.getChildAt(1).setVisibility(View.GONE);
}

//    @Override
//    public void onClick(View view) {
//        switch (view.getId()) {
//            case R.id.backBtn:
//                onBackPressed();
//                break;
//            case R.id.saveBtn:
//                if (braintreeBinding.routingNo.getText().toString().equals("")) {
//                    braintreeBinding.routinglay.getChildAt(1).setVisibility(View.VISIBLE);
//                    braintreeBinding.routinglay.setError(getString(R.string.routing_error));
//                    ApplicationClass.requestFocus(AddBraintreeActivity.this, braintreeBinding.routingNo, true);
//                }
//                 if (braintreeBinding.bankAcc.getText().toString().equals("")) {
//                    braintreeBinding.routinglay.setErrorEnabled(false);
//                    braintreeBinding.routinglay.getChildAt(1).setVisibility(View.GONE);
//                    braintreeBinding.bankAcclay.getChildAt(1).setVisibility(View.VISIBLE);
//                    braintreeBinding.bankAcclay.setError(getString(R.string.bankacc_error));
//                    ApplicationClass.requestFocus(AddBraintreeActivity.this, braintreeBinding.bankAcc, true);
//                } else if (braintreeBinding.name.getText().toString().equals("")) {
//                    braintreeBinding.bankAcclay.setErrorEnabled(false);
//                    braintreeBinding.bankAcclay.getChildAt(1).setVisibility(View.GONE);
//                    braintreeBinding.namelay.getChildAt(1).setVisibility(View.VISIBLE);
//                    braintreeBinding.namelay.setError(getString(R.string.recipientname_error));
//                    ApplicationClass.requestFocus(AddBraintreeActivity.this, braintreeBinding.name, true);
//                } else if (braintreeBinding.address.getText().toString().equals("")) {
//                    braintreeBinding.namelay.setErrorEnabled(false);
//                    braintreeBinding.namelay.getChildAt(1).setVisibility(View.GONE);
//                    braintreeBinding.addresslay.getChildAt(1).setVisibility(View.VISIBLE);
//                    braintreeBinding.addresslay.setError(getString(R.string.address_error));
//                    ApplicationClass.requestFocus(AddBraintreeActivity.this, braintreeBinding.address, true);
//                } else {
//                    PaymentActivity.routing_no = braintreeBinding.routingNo.getText().toString();
//                    PaymentActivity.bank_ac = braintreeBinding.bankAcc.getText().toString();
//                    PaymentActivity.name = braintreeBinding.name.getText().toString();
//                    PaymentActivity.address = braintreeBinding.address.getText().toString();
//                    finish();
//                }
//                break;
//        }
//    }
@Override
public void onClick(View view) {
    int id = view.getId();

    if (id == R.id.backBtn) {
        onBackPressed();
    } else if (id == R.id.saveBtn) {
        if (braintreeBinding.bankAcc.getText().toString().equals("")) {
            braintreeBinding.bankAcclay.getChildAt(1).setVisibility(View.VISIBLE);
            braintreeBinding.bankAcclay.setError(getString(R.string.bankacc_error));
            ApplicationClass.requestFocus(AddBraintreeActivity.this, braintreeBinding.bankAcc, true);
        } else if (braintreeBinding.name.getText().toString().equals("")) {
            braintreeBinding.bankAcclay.setErrorEnabled(false);
            braintreeBinding.bankAcclay.getChildAt(1).setVisibility(View.GONE);
            braintreeBinding.namelay.getChildAt(1).setVisibility(View.VISIBLE);
            braintreeBinding.namelay.setError(getString(R.string.recipientname_error));
            ApplicationClass.requestFocus(AddBraintreeActivity.this, braintreeBinding.name, true);
        } else if (braintreeBinding.address.getText().toString().equals("")) {
            braintreeBinding.namelay.setErrorEnabled(false);
            braintreeBinding.namelay.getChildAt(1).setVisibility(View.GONE);
            braintreeBinding.addresslay.getChildAt(1).setVisibility(View.VISIBLE);
            braintreeBinding.addresslay.setError(getString(R.string.address_error));
            ApplicationClass.requestFocus(AddBraintreeActivity.this, braintreeBinding.address, true);
        } else {
            // Save only the bank account information
            PaymentActivity.bank_ac = braintreeBinding.bankAcc.getText().toString();
            PaymentActivity.name = braintreeBinding.name.getText().toString();
            PaymentActivity.address = braintreeBinding.address.getText().toString();
            finish();
        }
    }
}

}


