package com.indikaudaya.bestfarmer_v1.ui.auth.login;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.dto.AuthRequestDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.FirebaseUserModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.auth.signup.SignupDialog;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;
import com.indikaudaya.bestfarmer_v1.util.TextInputListener;
import com.indikaudaya.bestfarmer_v1.util.Validator;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginDialog extends Dialog {

    private static final String USERTYPE = "users";
    private static final String TAG = LoginDialog.class.getName();

    SweetAlertDialog sweetAlertDialog;

    FirebaseAuth mAuth;
    FirebaseFirestore db;

    private TextInputEditText email, pass;

    Context context;

    public LoginDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        keyType();
        moveToSignup();
        signing();
//        ((TextInputEditText) findViewById(R.id.email)).setText("indikauwmr@gmail.com");
//        ((TextInputEditText) findViewById(R.id.password)).setText("123456789");

    }

    private void keyType() {
        email = findViewById(R.id.email);
        pass = findViewById(R.id.password);
        new TextInputListener().inputListener(email, findViewById(R.id.emailMain));
        new TextInputListener().inputListener(pass, findViewById(R.id.passwordMain));
    }

    private void pleaseWait(boolean visibility) {
        if (sweetAlertDialog == null) {
            sweetAlertDialog = new SweetAlertDialogCustomize().loadingAlert(getContext(), false);
        }
        if (visibility) {
            sweetAlertDialog.show();
        } else {
            sweetAlertDialog.dismiss();
        }
    }

    private void apiCalling(UserDTO dataModel) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BestFarmerApiService service = retrofit.create(BestFarmerApiService.class);
        Call<JsonObject> request = service.auth(new AuthRequestDTO(dataModel.getEmail(), dataModel.getPassword()));

        request.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {

                if (response.isSuccessful()) {
                    String to = response.body().get("jwtToken").getAsString();

                    SharedPreferences.Editor securityApi = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), Context.MODE_PRIVATE).edit();
                    securityApi.putString("token", to);
                    securityApi.apply();

                    getLoginUserDetailFromApi(to);
                } else {
                    new SweetAlertDialogCustomize().errorAlert(getContext(), "User not found");
                }
            }

            private void getLoginUserDetailFromApi(String to) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(context.getString(R.string.base_url))
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(to)).build())
                        .build();

                BestFarmerApiService service = retrofit.create(BestFarmerApiService.class);

                Call<UserDTO> userDetail = service.getUserDetail(dataModel.getEmail());

                userDetail.enqueue(new Callback<UserDTO>() {
                    @Override
                    public void onResponse(Call<UserDTO> call, Response<UserDTO> response) {
                        if (response.isSuccessful()) {
                            UserDTO u = response.body();
                            if (u.isStatus()) {
                                LoginDetails.isSigning = true;

                                new LoginDetails(u);
                                dismiss();
                                new SweetAlertDialogCustomize().successAlert(getContext(), "Login Successfully..");

                            }else{
                                mAuth.signOut();
                                SharedPreferences.Editor securityApi = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), Context.MODE_PRIVATE).edit();
                                securityApi.putString("token", "");
                                securityApi.apply();
                                new SweetAlertDialogCustomize().errorAlert(getContext(), "Your account is deactivated by system, please contact support team!.");

                            }
                            pleaseWait(false);
                        } else {
                            Log.i(TAG, "onResponse: no data user from api");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<UserDTO> call, @NonNull Throwable t) {
                        Log.e(TAG, "onFailure API calling login : " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable error) {
                Log.e(TAG, "Error :" + error.getMessage());
                if (mAuth != null) {
                    mAuth.signOut();
                    new SweetAlertDialogCustomize().errorAlert(getContext(), "Service currently unavailable!");
                }
            }
        });
    }

    private void moveToSignup() {
        findViewById(R.id.textView39).setOnClickListener(v -> {
            SignupDialog signupDialog = new SignupDialog(context);
            signupDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            signupDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            signupDialog.getWindow().setGravity(Gravity.CENTER);
            signupDialog.show();
        });
    }

    private void signing() {
        findViewById(R.id.imageButton).setOnClickListener(v -> {

            TextInputEditText emailFiled = email;
            TextInputEditText passwordField = pass;

            String email = String.valueOf(emailFiled.getText());
            String password = String.valueOf(passwordField.getText());

            if (!Validator.isValidEmail(emailFiled.getText())) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please enter valid email address");
            } else if (!Validator.isPasswordValidate(passwordField.getText())) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please enter password and minimum character count is 8");
            } else {
                pleaseWait(true);
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(command -> {
                            if (command.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();
                                updateUI(user);
                            } else {
                                pleaseWait(false);
                                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please check email address and password");
                            }
                        });
            }
        });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            if (!user.isEmailVerified()) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please verify your email");
                sendVerification(user);
            } else {
                getUserDetailsFromDB(user);
//                dismiss();
            }
            pleaseWait(false);
        }
    }

    private void getUserDetailsFromDB(FirebaseUser user) {
        Log.d(TAG, "getUserDetailsFromDB: ");
        if (user != null) {
            db.collection(USERTYPE)
                    .document(String.valueOf(user.getEmail()))
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Document found in the offline cache
                            FirebaseUserModel dataModel = task.getResult().toObject(FirebaseUserModel.class);
                            if (dataModel != null && dataModel.isStatus()) {

                                UserDTO userDTO = new UserDTO();
                                userDTO.setEmail(dataModel.getEmail());
                                userDTO.setPassword(dataModel.getPassword());
                                userDTO.setFirstName(dataModel.getFirstName());
                                userDTO.setLastName(dataModel.getLastName());
                                userDTO.setAddress(dataModel.getAddress());
                                userDTO.setShopAddress(dataModel.getShopAddress());
                                userDTO.setStatus(true);

                                LoginDetails.setFirebaseUserModel(dataModel);

                                if (dataModel.getShopAddress() == null || dataModel.getShopName() == null) {
                                    LoginDetails.isSeller = false;
                                } else {
                                    LoginDetails.isSeller = true;
                                }

                                apiCalling(userDTO);
                            }else{
                                new SweetAlertDialogCustomize().errorAlert(getContext(), "Account is deactivated by system, Please contact support center!.");
                            }
                        } else {
                            Log.e(TAG, "Cached get failed: ", task.getException());
                            new SweetAlertDialogCustomize().errorAlert(getContext(), String.valueOf(task.getException()));
                        }
                    });
        }
    }

    private void sendVerification(FirebaseUser user) {
        user.sendEmailVerification();
        new SweetAlertDialogCustomize().successAlert(getContext(), "We have been sent verification code in your email. Please check your email ");
    }

}