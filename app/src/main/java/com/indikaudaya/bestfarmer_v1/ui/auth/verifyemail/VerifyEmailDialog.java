package com.indikaudaya.bestfarmer_v1.ui.auth.verifyemail;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.indikaudaya.bestfarmer_v1.R;

public class VerifyEmailDialog extends Dialog {

    public VerifyEmailDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_verify_email);
        setValuesToEmail();
    }

    private void setValuesToEmail() {

    }


}