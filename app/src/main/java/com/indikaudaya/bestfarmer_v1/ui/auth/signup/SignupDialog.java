package com.indikaudaya.bestfarmer_v1.ui.auth.signup;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.model.SignupModel;
import com.indikaudaya.bestfarmer_v1.ui.auth.sendemailveification.SendVerificationEmailDialog;
import com.indikaudaya.bestfarmer_v1.ui.auth.verifymobile.VerifyMobileDialog;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;
import com.indikaudaya.bestfarmer_v1.util.TextInputListener;
import com.indikaudaya.bestfarmer_v1.util.Validator;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class SignupDialog extends Dialog implements OnMapReadyCallback {

    private static final String TAG = SignupDialog.class.getName();

    FirebaseAuth mAuth;
    SweetAlertDialog sweetAlertDialog;

    private TextInputEditText emailAddress, firstName, lastName, password, shopName, shopAddress;
    private TextInputLayout emailAddressLayout, mobileLayout, firstNameLayout, lastNameLayout, passwordLayout, shopNameLayout, shopAddressLayout;
    public TextInputEditText mobile;
    private RadioButton seller, buyer;
    private Map<String, Double> shopLocationLatLong;
    SupportMapFragment mapFragment;
    private Marker selectedMarker;
    private CheckBox agreeCondition;
    private ImageButton signupButton;
    public Button sendCodeVerification;

    Context context;

    public SignupDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_signup);

        //* Initialize method
        initializeMethod();
        //! lock fields
        lockDownFields(false);
        //? back button Calling
        backButton();
        //? Change UI for seller and buyer
        changeUI();
        //? Check all filed valid
        conditionCheck();
        pressVerificationButton();
        keyType();
    }

    private void keyType() {
        new TextInputListener().inputListener(emailAddress, emailAddressLayout);
        new TextInputListener().inputListener(firstName, firstNameLayout);
        new TextInputListener().inputListener(mobile, mobileLayout);
        new TextInputListener().inputListener(password, passwordLayout);
//        new TextInputListener().inputListener(lastName, lastNameLayout);
    }

    private void registerType() {
        SignupModel signupModel;

        if (seller.isChecked()) {
            signupModel = new SignupModel(
                    String.valueOf(emailAddress.getText()),
                    String.valueOf(firstName.getText()),
                    String.valueOf(lastName.getText()),
                    String.valueOf(mobile.getText()),
                    String.valueOf(password.getText()),
                    shopLocationLatLong,
                    String.valueOf(shopName.getText()),
                    String.valueOf(shopAddress.getText()),
                    true
            );
        } else {
            signupModel = new SignupModel(
                    String.valueOf(emailAddress.getText()),
                    String.valueOf(firstName.getText()),
                    String.valueOf(lastName.getText()),
                    String.valueOf(mobile.getText()),
                    String.valueOf(password.getText()),
                    true
            );

        }

        new SweetAlertDialogCustomize()
                .confirmationAlert(context, "Are you sure fill information is correct?", "Sure!")
                .setConfirmClickListener(sweetAlertDialog -> {
                    signupButton.setEnabled(false);
                    SendVerificationEmailDialog emailDialog = new SendVerificationEmailDialog(getContext(), signupModel);
                    emailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent, context.getTheme())));
                    emailDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    emailDialog.getWindow().setGravity(Gravity.CENTER);
                    emailDialog.setCancelable(false);
                    emailDialog.show();
                    sweetAlertDialog.dismiss();
                }).show();
    }

    private void changeUI() {
        LinearLayout linearLayout = findViewById(R.id.layoutSeller);
        linearLayout.setVisibility(View.GONE);
        seller.setOnClickListener(v -> {
            linearLayout.setVisibility(View.VISIBLE);
        });

        buyer.setOnClickListener(v -> {
            linearLayout.setVisibility(View.GONE);
        });
    }

    private void initializeMethod() {
        //* Initialize firebase
        mAuth = FirebaseAuth.getInstance();
        //* Google Map
        mapFragment = (SupportMapFragment) ((AppCompatActivity) context).getSupportFragmentManager().findFragmentById(R.id.map);
        mapConfigure();

        //* Initialize fields
        emailAddress = findViewById(R.id.email);
        emailAddressLayout = findViewById(R.id.emailMain);

        firstName = findViewById(R.id.fName);
        firstNameLayout = findViewById(R.id.fNameMain);

        lastName = findViewById(R.id.lName);
        lastNameLayout = findViewById(R.id.lNameMain);

        mobile = findViewById(R.id.mobile);
        mobileLayout = findViewById(R.id.mobileMain);

        password = findViewById(R.id.password);
        passwordLayout = findViewById(R.id.passwordMain);

        seller = findViewById(R.id.radioButton);
        buyer = findViewById(R.id.radioButton2);
        agreeCondition = findViewById(R.id.agreeCondition);
        //* initialize imagebutton
        signupButton = findViewById(R.id.imageButton4);
        //* initialize button
        sendCodeVerification = findViewById(R.id.button6);
    }

    private void conditionCheck() {

        signupButton.setOnClickListener(v -> {

            if (!Validator.isValidEmail(emailAddress.getText())) {
                new SweetAlertDialogCustomize().errorAlert(context, "Please enter valid email address");
            } else if (!Validator.isTextValidate(firstName.getText())) {
                new SweetAlertDialogCustomize().errorAlert(context, "Please enter your first name");
            } else if (!Validator.isPhoneValidate(mobile.getText())) {
                new SweetAlertDialogCustomize().errorAlert(context, "Please enter your mobile number");
            } else if (!Validator.isPasswordValidate(password.getText())) {
                new SweetAlertDialogCustomize().errorAlert(context, "Please enter password and minimum character count is 8");
            } else {
                if (seller.isChecked()) {
                    shopName = findViewById(R.id.shopName);
                    shopNameLayout = findViewById(R.id.shopNameMain);
                    new TextInputListener().inputListener(shopName, shopNameLayout);

                    shopAddress = findViewById(R.id.shopAddress);
                    shopAddressLayout = findViewById(R.id.shopAddressMain);
                    new TextInputListener().inputListener(shopAddress, shopAddressLayout);

                    if (!Validator.isTextValidate(shopName.getText())) {
                        new SweetAlertDialogCustomize().errorAlert(context, "Please enter your shop name");
                    } else if (!Validator.isTextValidate(shopAddress.getText())) {
                        new SweetAlertDialogCustomize().errorAlert(context, "Please enter your shop address");
                    } else {
                        if (agreeCondition.isChecked()) {
//                            SweetAlertDialog sweetAlertDialog1 = new SweetAlertDialogCustomize().confirmationAlert(context, "Have you selected the shop/ farm location correctly?", "Yes");
//                            sweetAlertDialog1.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                                @Override
//                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                            registerType();
//                                }
//                            });
//                            sweetAlertDialog1.show();
                        } else {
                            new SweetAlertDialogCustomize().errorAlert(context, "Please agree to term of service!");
                        }
                    }

                } else {
                    if (agreeCondition.isChecked()) {
                        registerType();
                    } else {
                        new SweetAlertDialogCustomize().errorAlert(context, "Please agree to term of service!");
                    }
                }
            }
        });
    }

    private void backButton() {
        findViewById(R.id.imageView21).setOnClickListener(v -> {
            this.dismiss();
        });
    }

    private void pressVerificationButton() {
        sendCodeVerification.setOnClickListener(v -> {
            if (!Validator.isPhoneValidate(mobile.getText())) {
                new SweetAlertDialogCustomize().errorAlert(context, "Please enter valid mobile number");
            } else {
                VerifyMobileDialog activity = new VerifyMobileDialog(getContext(), String.valueOf(mobile.getText()), this);
                Objects.requireNonNull(activity.getWindow()).setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(android.R.color.transparent, context.getTheme())));
                activity.getWindow().setGravity(Gravity.CENTER);
                activity.setCancelable(true);
                activity.show();
            }
        });
    }

    public void lockDownFields(boolean setEnableFields) {
        password.setEnabled(setEnableFields);
        seller.setEnabled(setEnableFields);
        buyer.setEnabled(setEnableFields);
        signupButton.setEnabled(setEnableFields);
    }

    private void mapConfigure() {
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        Log.d(TAG, "onMapReady: ");

        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        googleMap.getUiSettings().setMapToolbarEnabled(true);
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        LatLng defaultLocation = new LatLng(6.928647920309498, 79.8678730550909);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 8));

        // Add a marker at the default location
        selectedMarker = googleMap.addMarker(new MarkerOptions().position(defaultLocation).title("Shop Location"));

        // Set up map click listener
        googleMap.setOnMapClickListener(latLng -> {
            // Update the marker position when the map is clicked
            selectedMarker.setPosition(latLng);

            // You can also update other UI elements or store the selected location
            double selectedLatitude = latLng.latitude;
            double selectedLongitude = latLng.longitude;
            // You can also update other UI elements or store the selected location

            // For example, log the coordinates
            Log.d("SelectedLocation", "Latitude: " + selectedLatitude + ", Longitude: " + selectedLongitude);
            shopLocationLatLong = new HashMap<>();
            shopLocationLatLong.put("latitude", selectedLatitude);
            shopLocationLatLong.put("longitude", selectedLongitude);
        });

    }

}