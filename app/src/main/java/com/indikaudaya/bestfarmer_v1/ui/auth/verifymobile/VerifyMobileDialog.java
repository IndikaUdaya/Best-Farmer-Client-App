package com.indikaudaya.bestfarmer_v1.ui.auth.verifymobile;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.ui.auth.signup.SignupDialog;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.concurrent.TimeUnit;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class VerifyMobileDialog extends Dialog {

    private static final String TAG = VerifyMobileDialog.class.getName();
    private static final long TIMEOUT_PERIOD = 60L;
    FirebaseAuth mAuth;
    SweetAlertDialog sweetAlertDialog;
    String mobileNumber;
    Context context;

    SignupDialog signupDialog;
    Button sendOTpButton;

    private String verificationId;
    private PhoneAuthProvider.ForceResendingToken resendingToken;

    public VerifyMobileDialog(@NonNull Context context, String mobile, SignupDialog signupDialog) {
        super(context);
        this.context = context;
        this.mobileNumber = mobile;
        this.signupDialog = signupDialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_verify_mobile);

        //? Initialize
        initializeFields();
        //? verify code
        pressVerifyButton();
        //* for send verification code
//        startNewThread();
    }

    private String inputVerificationCodeByUser() {
        EditText editText1 = findViewById(R.id.editTextText);
        EditText editText2 = findViewById(R.id.editTextText1);
        EditText editText3 = findViewById(R.id.editTextText2);
        EditText editText4 = findViewById(R.id.editTextText3);
        EditText editText5 = findViewById(R.id.editTextText4);
        EditText editText6 = findViewById(R.id.editTextText5);

        return String.valueOf(editText1.getText())
                .concat(String.valueOf(editText2.getText()))
                .concat(String.valueOf(editText3.getText()))
                .concat(String.valueOf(editText4.getText()))
                .concat(String.valueOf(editText5.getText()))
                .concat(String.valueOf(editText6.getText()));

    }

    private void pressVerifyButton() {
        findViewById(R.id.button3).setOnClickListener(v -> {
//            checkMobileVerification();

            new SweetAlertDialogCustomize().successAlert(context, "mobile number verify Successfully..");
            unlockParentFields();
            this.dismiss();

        });
    }

    private void unlockParentFields() {
        signupDialog.lockDownFields(true);
        signupDialog.mobile.setEnabled(false);
        signupDialog.sendCodeVerification.setEnabled(false);
    }

    private void startNewThread() {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                sendOtp();
            }
        }, 1500);
    }

    private void sendOtp() {
        setInPleaseWait(true);
        PhoneAuthOptions options = PhoneAuthOptions.newBuilder(mAuth)
                .setPhoneNumber(context.getString(R.string.country_code).concat(String.valueOf(mobileNumber)))
                .setTimeout(TIMEOUT_PERIOD, TimeUnit.SECONDS)
//                .setActivity(this)
                .setCallbacks(new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                    @Override
                    public void onCodeSent(@NonNull String verificationId1,
                                           @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                        setInPleaseWait(false);
                        verificationId = verificationId1;
                        resendingToken = forceResendingToken;

                        new SweetAlertDialogCustomize().successAlert(getContext(), "OTP sent Successfully..");

                        Log.d(TAG, "onCodeSent:" + verificationId);
                    }

                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                        setInPleaseWait(false);
                        Log.i(TAG, "onComplete: " + phoneAuthCredential.getSmsCode());
                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {
                        setInPleaseWait(false);
                    }
                })
                .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }

    private void checkMobileVerification() {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, inputVerificationCodeByUser());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        unlockParentFields();
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = task.getResult().getUser();
                        Log.i(TAG, "onComplete: " + user);
                        new SweetAlertDialogCustomize().successAlert(context, "mobile number verify Successfully..");
                        this.dismiss();
                    } else {
                        new SweetAlertDialogCustomize().errorAlert(context, "your enter OTP code is wrong");
                    }
                });
    }

    private void initializeFields() {
        //* Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        //? Fields
        sendOTpButton = findViewById(R.id.button3);
    }

    private void setInPleaseWait(boolean inProgress) {
        if (sweetAlertDialog == null) {
            sweetAlertDialog = new SweetAlertDialogCustomize().loadingAlert(this.getContext(), false);
        }
        if (inProgress) {
            sweetAlertDialog.show();
        } else {
            sweetAlertDialog.dismiss();
        }
    }

}