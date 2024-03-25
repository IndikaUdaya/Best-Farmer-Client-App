package com.indikaudaya.bestfarmer_v1.ui.main;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.auth.FirebaseAuth;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.databinding.ActivityMainBinding;
import com.indikaudaya.bestfarmer_v1.dto.CategoryDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.auth.login.LoginDialog;
import com.indikaudaya.bestfarmer_v1.ui.buyerhistory.BuyerHistoryFragment;
import com.indikaudaya.bestfarmer_v1.ui.cart.CartFragment;
import com.indikaudaya.bestfarmer_v1.ui.home.HomeFragment;
import com.indikaudaya.bestfarmer_v1.ui.listedproduct.ProductListedFragment;
import com.indikaudaya.bestfarmer_v1.ui.order.OrderFragment;
import com.indikaudaya.bestfarmer_v1.ui.productlisting.ProductListingFragment;
import com.indikaudaya.bestfarmer_v1.ui.profile.ProfileFragment;
import com.indikaudaya.bestfarmer_v1.ui.watchlist.WatchlistFragment;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.List;
import java.util.Objects;
import java.util.zip.Inflater;

import cn.pedant.SweetAlert.SweetAlertDialog;
import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new HomeFragment());

        binding.bottomNavigationView.setBackground(null);

        binding.bottomNavigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.home1) {
                replaceFragment(new HomeFragment());
            } else if (item.getItemId() == R.id.cart) {
                if (!LoginDetails.isSigning) {
                    new SweetAlertDialogCustomize().errorAlert(this, "Please signing");
                } else {
                    replaceFragment(new CartFragment());
                }
            } else if (item.getItemId() == R.id.watchlist) {
                if (!LoginDetails.isSigning) {
                    new SweetAlertDialogCustomize().errorAlert(this, "Please signing");
                } else {
                    replaceFragment(new WatchlistFragment());
                }
            } else if (item.getItemId() == R.id.signing) {
                if (!LoginDetails.isSigning) {
                    userLogin();
                } else {
                    signOut();
//                    replaceFragment(new ProfileFragment());
                }
            }
            return true;
        });

        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomDialog();
            }
        });
    }

    private void signOut() {
        new SweetAlertDialogCustomize()
                .confirmationAlert(this, "Sign out and exit the app", "Sure")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        LoginDetails.isSigning = false;
                        new LoginDetails(new UserDTO());
                        finish();
                    }
                }).show();
    }

    private void userLogin() {
        LoginDialog loginActivity = new LoginDialog(MainActivity.this);
        loginActivity.getWindow().setBackgroundDrawable(new ColorDrawable(getResources().getColor(android.R.color.transparent, getTheme())));
        loginActivity.getWindow().getAttributes().windowAnimations = R.style.DialogAnimationCenter;
        loginActivity.getWindow().setGravity(Gravity.CENTER);
        loginActivity.setCancelable(true);
        loginActivity.show();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

    private void showBottomDialog() {

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.bottomdialolayout);

        LinearLayout productListingLayout = dialog.findViewById(R.id.layoutProductListing);
        LinearLayout orderLayout = dialog.findViewById(R.id.layoutOrder);
        LinearLayout productListed = dialog.findViewById(R.id.layoutProductListed);
        LinearLayout buyHistory = dialog.findViewById(R.id.layoutBuyHistory);
        ImageView cancelButton = dialog.findViewById(R.id.cancelButton);
        ImageView lineClose = dialog.findViewById(R.id.lineClose);

        productListingLayout.setOnClickListener(v -> {
            dialog.dismiss();
            if (LoginDetails.isSigning) {
                if (LoginDetails.isSeller) {
                    replaceFragment(new ProductListingFragment());
                } else {
                    new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "This feature only for seller!");
                }
            } else {
                new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "Please sign in!");
            }
        });

        orderLayout.setOnClickListener(v -> {
            dialog.dismiss();
            if (LoginDetails.isSigning) {
                if (LoginDetails.isSeller) {
                    replaceFragment(new OrderFragment());
                } else {
                    new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "This feature only for seller!");
                }
            } else {
                new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "Please sign in!");
            }
        });

        productListed.setOnClickListener(v -> {
            dialog.dismiss();
            if (LoginDetails.isSigning) {
                if (LoginDetails.isSeller) {
                    replaceFragment(new ProductListedFragment());
                } else {
                    new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "This feature only for seller!");
                }
            } else {
                new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "Please sign in!");
            }
        });

        buyHistory.setOnClickListener(v -> {
            dialog.dismiss();
            if (LoginDetails.isSigning) {
                if (!LoginDetails.isSeller) {
                    replaceFragment(new BuyerHistoryFragment());
                } else {
                    new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "This feature only for buyers!");
                }
            } else {
                new SweetAlertDialogCustomize().errorAlert(MainActivity.this, "Please sign in!");
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        lineClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        dialog.getWindow().setGravity(Gravity.BOTTOM);

    }

}