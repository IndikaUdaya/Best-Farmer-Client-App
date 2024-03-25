package com.indikaudaya.bestfarmer_v1.ui.auth.sendemailveification;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.JsonObject;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.dto.SignupDTO;
import com.indikaudaya.bestfarmer_v1.model.SignupModel;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;
import com.indikaudaya.bestfarmer_v1.util.Validator;

import org.jetbrains.annotations.NotNull;

import cn.pedant.SweetAlert.SweetAlertDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SendVerificationEmailDialog extends Dialog {

    private static final String TAG = SendVerificationEmailDialog.class.getName();
    private static final String COLLECTION = "users";

    SweetAlertDialog sweetLoadingDialog;

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    EditText email;
    Button sendVerificationCodeButton;
    Context context;

    SignupModel signupModel;

    public SendVerificationEmailDialog(@NonNull Context context, @NotNull SignupModel model) {
        super(context);
        this.context = context;
        this.signupModel = model;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_send_verification_email);
        //? Data Set
        setEmailAddressToField();
        //? initialize
        initialize();
        //* Send Verification code
        sendCodeAndRegister();

    }

    private void sendCodeAndRegister() {
        Log.d(TAG, "sendCodeAndRegister: Press");
        sendVerificationCodeButton.setOnClickListener(v -> {
            sweetLoadingDialog = new SweetAlertDialogCustomize().loadingAlert(getContext(), false);
            sweetLoadingDialog.show();
            isAlreadyExistEmail();
        });
    }

    private void register() {
        SignupModel previousDataBundle = signupModel;
        if (!Validator.isValidEmail(email.getText())) {
            new SweetAlertDialogCustomize().errorAlert(context, "Please enter valid email address");
        } else {
            previousDataBundle.setEmail(String.valueOf(email.getText()));

            db.collection(COLLECTION)
                    .document(String.valueOf(email.getText()))
                    .set(previousDataBundle)
                    .addOnSuccessListener(documentReference -> {
                        sendVerificationEmail();
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            sweetLoadingDialog.dismiss();
                            new SweetAlertDialogCustomize().errorAlert(context, "Error adding document");
                        }
                    });
        }
    }

    private void apiRegister(String email, String mobile, String password) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BestFarmerApiService service = retrofit.create(BestFarmerApiService.class);
        String type = (signupModel.getShopAddress() == null) ? "seller" : "buyer";
        SignupDTO signupDTO = new SignupDTO(email, mobile, password, true,type);

        Call<JsonObject> signUp = service.signUp(signupDTO);
        signUp.request().newBuilder().header("Content-Type", "application/json").build();

        signUp.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                sweetLoadingDialog.dismiss();
                if (response.isSuccessful()) {
                    new SweetAlertDialog(context, SweetAlertDialog.SUCCESS_TYPE)
                            .setTitleText("We will send verification link into your email address. Please check it!")
                            .setConfirmButton("Ok", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    dismiss();
                                }
                            })
                            .showCancelButton(false)
                            .show();
                } else {
                    new SweetAlertDialogCustomize().errorAlert(getContext(), "User Register Failed!");
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e(TAG, "Error :" + t.getMessage());
            }
        });
    }

    private void isAlreadyExistEmail() {
        DocumentReference docRef = db.collection(COLLECTION).document(String.valueOf(email.getText()));
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    sweetLoadingDialog.dismiss();
                    new SweetAlertDialogCustomize().errorAlert(getContext(), "Already existing this email address, please try another email address!");
                } else {
                    register();
                }
            } else {
                Log.d(TAG, "get failed with ", task.getException());
            }
        }).addOnFailureListener(e -> Log.e(TAG, "onFailure: " + e.getMessage()));
    }

    private void sendVerificationEmail() {
        String email = signupModel.getEmail();
        String password = signupModel.getPassword();
        String mobile = signupModel.getMobileNumber();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            user.sendEmailVerification();
                            //! Register for API access
                            apiRegister(email, mobile, password);
                        }
                    } else {
                        Log.w(TAG, "signInWithEmail:failure", task.getException());
                        new SweetAlertDialogCustomize().errorAlert(context, "Authentication failed.");
                    }
                });
    }

    private void initialize() {
        //* Firebase Initialize
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        //* Fields initialize
        sendVerificationCodeButton = findViewById(R.id.button3);
    }

    private void setEmailAddressToField() {
        email = findViewById(R.id.editTextPhone1);
        email.setText(signupModel.getEmail());
    }


}