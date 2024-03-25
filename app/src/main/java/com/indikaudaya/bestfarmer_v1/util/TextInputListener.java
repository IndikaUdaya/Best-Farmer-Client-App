package com.indikaudaya.bestfarmer_v1.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class TextInputListener {

    public void inputListener(TextInputEditText textInputEditText, TextInputLayout textInputLayout) {
        textInputEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if ((count == 1 && s.length() == 0) || s.length() == 0) {
                    textInputLayout.setHelperText("Required*");
                } else {
                    textInputLayout.setHelperText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }
}
