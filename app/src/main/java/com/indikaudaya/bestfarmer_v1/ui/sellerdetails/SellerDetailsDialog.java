package com.indikaudaya.bestfarmer_v1.ui.sellerdetails;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.skydoves.progressview.ProgressView;

public class SellerDetailsDialog extends Dialog {

    private TextView sellerName, sellerEmail, sellerMobile;
    ImageView profilePic;

    UserDTO userDTO;

    public SellerDetailsDialog(@NonNull Context context, UserDTO userDTO) {
        super(context);
        this.userDTO = userDTO;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_seller_details);

        initMethod();
        setDataToFields();
    }

    private void setDataToFields() {
        sellerName.setText(userDTO.getEmail());
        sellerEmail.setText(userDTO.getEmail());
        sellerMobile.setText(userDTO.getMobile());
        profilePic.setImageResource(R.drawable.buyer_icon);
    }

    private void initMethod() {
        profilePic = findViewById(R.id.imageView25);
        sellerEmail = findViewById(R.id.sellerEmail);
        sellerMobile = findViewById(R.id.mobile);
        sellerName = findViewById(R.id.sellerName);
    }

}
