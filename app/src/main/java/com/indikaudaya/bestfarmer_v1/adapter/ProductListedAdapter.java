package com.indikaudaya.bestfarmer_v1.adapter;

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
import com.indikaudaya.bestfarmer_v1.dto.CartDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.WatchlistDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.model.ProductListedModel;
import com.indikaudaya.bestfarmer_v1.model.SearchModel;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.productdetail.ProductDetailDialog;
import com.indikaudaya.bestfarmer_v1.ui.productedit.ProductEditDialog;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.ArrayList;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductListedAdapter extends RecyclerView.Adapter<ProductListedAdapter.ProductListedAdapterViewHolder> {

    ArrayList<ProductListedModel> arrayList;
    Context context;

    public ProductListedAdapter(Context context, ArrayList<ProductListedModel> productListedModels) {
        this.context = context;
        this.arrayList = productListedModels;
    }

    @NonNull
    @Override
    public ProductListedAdapter.ProductListedAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.product_listed_layout, parent, false);
        return new ProductListedAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductListedAdapter.ProductListedAdapterViewHolder holder, int position) {

        holder.cartCount.setText(String.valueOf(arrayList.get(position).getPopularFood().getCartCount()));
        holder.watchlistCount.setText(String.valueOf(arrayList.get(position).getPopularFood().getWatchlistCount()));
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
            ProductEditDialog editDialog = new ProductEditDialog(context, arrayList.get(position).getPopularFood());
            editDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            editDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            editDialog.getWindow().setGravity(Gravity.CENTER);
            editDialog.setCancelable(false);
            editDialog.show();
        });
    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class ProductListedAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView productName, price, description, cartCount, watchlistCount;
        ImageView productImage;

        public ProductListedAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            description = itemView.findViewById(R.id.productDescription);
            cartCount = itemView.findViewById(R.id.cartCount);
            watchlistCount = itemView.findViewById(R.id.watchlistCount);
        }
    }

}
