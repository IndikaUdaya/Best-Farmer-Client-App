package com.indikaudaya.bestfarmer_v1.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.google.firebase.storage.FirebaseStorage;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.CartAdapterModel;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.model.WatchlistModel;
import com.indikaudaya.bestfarmer_v1.service.AdapterCallBack;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.productdetail.ProductDetailDialog;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WatchlistAdapter extends RecyclerView.Adapter<WatchlistAdapter.WatchlistViewHolder> {

    ArrayList<WatchlistModel> arrayList;
    Context context;
    AdapterCallBack callBack;

    public WatchlistAdapter(Context context, ArrayList<WatchlistModel> cartAdapterModels, AdapterCallBack callBack) {
        this.context = context;
        this.arrayList = cartAdapterModels;
        this.callBack = callBack;
    }

    @NonNull
    @Override
    public WatchlistAdapter.WatchlistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.watchlist_recycleview_layout, parent, false);
        return new WatchlistViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull WatchlistAdapter.WatchlistViewHolder holder, int position) {
        //        holder.cartCount.setText(String.valueOf(arrayList.get(position).getPopularFood().getCartCount()));
        holder.productName.setText(arrayList.get(position).getPopularFood().getTitle());
        holder.description.setText(arrayList.get(position).getPopularFood().getDescription());
        holder.price.setText(context.getString(R.string.price_unit).concat(" ").concat(String.valueOf(arrayList.get(position).getPopularFood().getPrice())));

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference("product-image/" + arrayList.get(position).getPopularFood().getProductImageList().get(0).getPath())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .transform(new GranularRoundedCorners(35, 35, 0, 0))
                            .into(holder.productImage);
                });

        holder.itemView.setOnClickListener(v -> {
            ProductDetailDialog productDetailDialog = new ProductDetailDialog(v.getContext(), arrayList.get(position).getPopularFood(), callBack);
            productDetailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            productDetailDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            productDetailDialog.getWindow().setGravity(Gravity.CENTER);
            productDetailDialog.setCancelable(false);
            productDetailDialog.setDialog(productDetailDialog);
            productDetailDialog.show();
        });

        holder.recyclebin.setOnClickListener(v -> {

            String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(context.getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                    .build();

            BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
            Call<Boolean> booleanCall = apiService.deleteWatchlistById(arrayList.get(position).getWatchlistId());
            booleanCall.enqueue(new Callback<Boolean>() {
                @Override
                public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                    if (response.isSuccessful()) {
                        holder.setIsRecyclable(true);
                        arrayList.remove(position);
                        notifyItemRemoved(position);
                        new SweetAlertDialogCustomize().successAlert(context, "Watchlist remove successfully!.");
                    }
                }

                @Override
                public void onFailure(Call<Boolean> call, Throwable t) {
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class WatchlistViewHolder extends RecyclerView.ViewHolder {

        TextView productName, price, description;//, cartCount;
        ImageView productImage, recyclebin;

        public WatchlistViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            description = itemView.findViewById(R.id.productDescription);
//            cartCount = itemView.findViewById(R.id.cartCount);
            recyclebin = itemView.findViewById(R.id.recyclebin);
        }
    }
}
