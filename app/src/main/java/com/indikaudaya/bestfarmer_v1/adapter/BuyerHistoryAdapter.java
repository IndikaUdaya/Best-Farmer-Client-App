package com.indikaudaya.bestfarmer_v1.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.google.firebase.storage.FirebaseStorage;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.dto.OrderDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.SellerReviewDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.BuyerHistoryModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.OrderModel;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.rateseller.RateSellerDialog;
import com.indikaudaya.bestfarmer_v1.ui.sellerdetails.SellerDetailsDialog;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BuyerHistoryAdapter extends RecyclerView.Adapter<BuyerHistoryAdapter.BuyerHistoryViewHolder> {

    ArrayList<BuyerHistoryModel> arrayList;
    Context context;

    public BuyerHistoryAdapter(Context context, ArrayList<BuyerHistoryModel> orderModels) {
        this.context = context;
        this.arrayList = orderModels;
    }

    @NonNull
    @Override
    public BuyerHistoryAdapter.BuyerHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.buyer_history_recycleview_layout, parent, false);
        return new BuyerHistoryViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull BuyerHistoryAdapter.BuyerHistoryViewHolder holder, int position) {
        holder.orderQty.setText(String.valueOf(arrayList.get(position).getOrderQty()));
        holder.productName.setText(arrayList.get(position).getPopularFood().getTitle());
        holder.description.setText(arrayList.get(position).getPopularFood().getDescription());
        holder.productPrice.setText(String.valueOf(arrayList.get(position).getPopularFood().getPrice()));

        if (arrayList.get(position).getPopularFood().isDeliveryProduct()) {
            holder.delivered.setImageResource(R.drawable.ok_icon);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference("product-image/" + arrayList.get(position).getPopularFood().getProductImageList().get(0).getPath())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .transform(new GranularRoundedCorners(35, 35, 0, 0))
                            .into(holder.productImage);
                });


        checkReviewWrite(arrayList.get(position).getPopularFood().getProductId(), holder.reviewSeller);

        holder.reviewSeller.setOnClickListener(v -> {
            RateSellerDialog sellerDialog = new RateSellerDialog(context, arrayList.get(position).getPopularFood());
            sellerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            sellerDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            sellerDialog.getWindow().setGravity(Gravity.CENTER);
            sellerDialog.setCancelable(false);
            sellerDialog.show();
        });

        holder.rateSellerLayout.setOnClickListener(v -> {

            UserDTO seller = arrayList.get(position).getPopularFood().getSeller();

            SellerDetailsDialog sellerDetailsDialog = new SellerDetailsDialog(context, seller);
            sellerDetailsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            sellerDetailsDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            sellerDetailsDialog.getWindow().setGravity(Gravity.CENTER);
            sellerDetailsDialog.setCancelable(true);
            sellerDetailsDialog.show();
        });

    }

    private void checkReviewWrite(long productId, Button button) {
        long buyerId = new LoginDetails().getUserDTO().getId();

        String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        Call<SellerReviewDTO> review = apiService.getSellerReviewByProductIdAndBuyerId(productId, buyerId);

        review.enqueue(new Callback<SellerReviewDTO>() {
            @Override
            public void onResponse(@NonNull Call<SellerReviewDTO> call, @NonNull Response<SellerReviewDTO> response) {
                if (response.isSuccessful()) {
                    SellerReviewDTO body = response.body();
                    if (body.getId() == 0) {
                        button.setEnabled(false);
                    }
                }
            }

            @Override
            public void onFailure(Call<SellerReviewDTO> call, Throwable t) {
            }
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class BuyerHistoryViewHolder extends RecyclerView.ViewHolder {

        TextView productName, description, orderQty, productPrice;
        ImageView productImage, delivered;
        Button reviewSeller;
        LinearLayout rateSellerLayout;

        public BuyerHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            description = itemView.findViewById(R.id.productDescription);
            orderQty = itemView.findViewById(R.id.orderQty);
            delivered = itemView.findViewById(R.id.delivered);
            reviewSeller = itemView.findViewById(R.id.reviewSeller);
            rateSellerLayout = itemView.findViewById(R.id.rateSellerLayout);
            productPrice = itemView.findViewById(R.id.productPrice);
        }
    }
}
