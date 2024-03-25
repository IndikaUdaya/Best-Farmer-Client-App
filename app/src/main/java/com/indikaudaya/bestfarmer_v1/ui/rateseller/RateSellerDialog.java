package com.indikaudaya.bestfarmer_v1.ui.rateseller;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.textfield.TextInputEditText;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.SellerReviewDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.BuyerHistoryModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.home.search.SearchFragment;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RateSellerDialog extends Dialog {

    private static final String TAG = RateSellerDialog.class.getName();
    private float buyerRating;

    Button remindMeLater;
    PopularFood popularFood;

    TextInputEditText comment;
    RatingBar ratingBar;
    TextView sellerEmail;


    public RateSellerDialog(@NonNull Context context, PopularFood popularFood) {
        super(context);
        this.popularFood = popularFood;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_seller_dialog_layout);

        initMethod();
        ratingBarChange();
        rateNowClick();
        pressRemindMeLater();

    }

    private void initMethod() {
        remindMeLater = findViewById(R.id.button4);
        comment = findViewById(R.id.comment);
        ratingBar = findViewById(R.id.ratingBar);
        sellerEmail = findViewById(R.id.textView46);
    }

    private void pressRemindMeLater() {
        remindMeLater.setOnClickListener(v -> {
            this.dismiss();
        });
    }

    private void rateNowClick() {
        findViewById(R.id.button5).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                SellerReviewDTO sellerReviewDTO = new SellerReviewDTO();
                sellerReviewDTO.setComment(String.valueOf(comment.getText()));

                sellerReviewDTO.setRating(ratingBar.getRating());
                sellerReviewDTO.setProduct(new ProductDTO(popularFood.getProductId()));
                sellerReviewDTO.setSeller(popularFood.getSeller());
                sellerReviewDTO.setBuyer(new LoginDetails().getUserDTO());

                Log.e(TAG, "onClick: " + sellerReviewDTO.toString());

                String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(getContext().getString(R.string.base_url))
                        .addConverterFactory(GsonConverterFactory.create())
                        .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(token)).build())
                        .build();

                BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

                Call<SellerReviewDTO> products = apiService.saveSellerReview(sellerReviewDTO);

                products.enqueue(new Callback<SellerReviewDTO>() {
                    @Override
                    public void onResponse(@NonNull Call<SellerReviewDTO> call, @NonNull Response<SellerReviewDTO> response) {
                        if (response.isSuccessful()) {
                            SellerReviewDTO body = response.body();
                            if (body == null && body.getId() == 0) {
                                new SweetAlertDialogCustomize().errorAlert(getContext(), "Save Failed!");
                            } else {
                                new SweetAlertDialogCustomize().successAlert(getContext(), "Thanks for rating the seller!🥰🥰🥰");
                                dismiss();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<SellerReviewDTO> call, Throwable t) {
                        Log.e(TAG, "onFailure: Seller Review save failed - " + t.getMessage());
                    }
                });
            }
        });
    }


    private void ratingBarChange() {
        RatingBar ratingBar = findViewById(R.id.ratingBar);
        ImageView ratingFace = findViewById(R.id.imageView23);

        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {

                if (rating >= 0.5f && rating <= 1.0f) {
                    ratingFace.setImageResource(R.drawable.one_star);
                } else if (rating > 1.0f && rating <= 2.0f) {
                    ratingFace.setImageResource(R.drawable.two_star);
                } else if (rating > 2.0f && rating <= 3.0f) {
                    ratingFace.setImageResource(R.drawable.three_star);
                } else if (rating > 3.0f && rating <= 4.0f) {
                    ratingFace.setImageResource(R.drawable.four_star);
                } else if (rating > 4.0f && rating <= 5.0f) {
                    ratingFace.setImageResource(R.drawable.five_star);
                }

                RateSellerDialog.this.animateRatingFaceImage(ratingFace);
                buyerRating = rating;

            }
        });

    }

    private void animateRatingFaceImage(ImageView imageView) {
        ScaleAnimation animation = new ScaleAnimation(0, 1f, 0, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setFillAfter(true);
        animation.setDuration(300);
        imageView.startAnimation(animation);
    }
}
