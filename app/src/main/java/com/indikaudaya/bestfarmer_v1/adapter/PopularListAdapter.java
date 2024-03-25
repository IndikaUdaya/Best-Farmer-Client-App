package com.indikaudaya.bestfarmer_v1.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.google.firebase.storage.FirebaseStorage;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.service.AdapterCallBack;
import com.indikaudaya.bestfarmer_v1.ui.auth.login.LoginDialog;
import com.indikaudaya.bestfarmer_v1.ui.productdetail.ProductDetailDialog;

import java.util.ArrayList;
import java.util.Objects;


public class PopularListAdapter extends RecyclerView.Adapter<PopularListAdapter.ViewHolder> {

    private static final String TAG = PopularListAdapter.class.getName();
    ArrayList<PopularFood> foodArrayList;
    Context context;
    Dialog dialog;
    AdapterCallBack callBack;

    public PopularListAdapter(Context context, ArrayList<PopularFood> foodArrayList, AdapterCallBack callBack, Dialog dilaog) {
        this.foodArrayList = foodArrayList;
        this.context = context;
        this.callBack = callBack;
        this.dialog = dilaog;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_view_list, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.title.setText(foodArrayList.get(position).getTitle());
        holder.reviewRating.setText(String.valueOf(foodArrayList.get(position).getRatingScore()));
        holder.price.setText(context.getString(R.string.price_unit).concat(String.valueOf(foodArrayList.get(position).getPrice())));
        holder.reviewCount.setText(String.valueOf(foodArrayList.get(position).getReviewCount()));

        Glide.with(holder.itemView.getContext())
                .asGif()
                .load(R.drawable.loading)
                .into(holder.foodImage);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference("product-image/" + foodArrayList.get(position).getProductImageList().get(0).getPath())
                .getDownloadUrl()
                .addOnSuccessListener(uri -> {
                    Glide.with(holder.itemView.getContext())
                            .load(uri)
                            .transform(new GranularRoundedCorners(35, 35, 0, 0))
                            .into(holder.foodImage);
                });

        holder.itemView.setOnClickListener(v -> {
            ProductDetailDialog productDetailDialog = new ProductDetailDialog(v.getContext(), (PopularFood) foodArrayList.get(position), callBack);
            productDetailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            productDetailDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            productDetailDialog.getWindow().setGravity(Gravity.CENTER);
            productDetailDialog.setCancelable(false);
            productDetailDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return foodArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView reviewRating, title, price, reviewCount;
        ImageView foodImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            reviewRating = itemView.findViewById(R.id.textView29);
            title = itemView.findViewById(R.id.textView30);
            price = itemView.findViewById(R.id.textView31);
            reviewCount = itemView.findViewById(R.id.textView32);
            foodImage = itemView.findViewById(R.id.imageView10);
        }
    }
}
