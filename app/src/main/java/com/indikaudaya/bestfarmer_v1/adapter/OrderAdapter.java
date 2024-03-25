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
import com.indikaudaya.bestfarmer_v1.dto.OrderDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.CartAdapterModel;
import com.indikaudaya.bestfarmer_v1.model.OrderModel;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
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

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    ArrayList<OrderModel> arrayList;
    Context context;

    public OrderAdapter(Context context, ArrayList<OrderModel> orderModels) {
        this.context = context;
        this.arrayList = orderModels;
    }

    @NonNull
    @Override
    public OrderAdapter.OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.order_recycleview_layout, parent, false);
        return new OrderViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderAdapter.OrderViewHolder holder, int position) {
        holder.orderQty.setText(String.valueOf(arrayList.get(position).getOrderQty()));
        holder.productName.setText(arrayList.get(position).getPopularFood().getTitle());
        holder.description.setText(arrayList.get(position).getPopularFood().getDescription());

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

//        holder.itemView.setOnClickListener(v -> {
//            ProductDetailDialog productDetailDialog = new ProductDetailDialog(context, (PopularFood) arrayList.get(position).getPopularFood(),callBack);
//            productDetailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
//            productDetailDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
//            productDetailDialog.getWindow().setGravity(Gravity.CENTER);
//            productDetailDialog.setCancelable(false);
//            productDetailDialog.setDialog(productDetailDialog);
//            productDetailDialog.show();
//        });

        holder.delivered.setOnClickListener(v -> {

            OrderDTO orderDTO = new OrderDTO();
            orderDTO.setOrderId(arrayList.get(position).getOrderId());
            orderDTO.setDeliveryStatus(true);

            String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(context.getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                    .build();

            BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
            Call<OrderDTO> booleanCall = apiService.updateOrderStatus(arrayList.get(position).getOrderId(), orderDTO);

            booleanCall.enqueue(new Callback<OrderDTO>() {
                @Override
                public void onResponse(Call<OrderDTO> call, Response<OrderDTO> response) {
                    if (response.isSuccessful()) {
                        new SweetAlertDialogCustomize().successAlert(context, "Delivered successfully!.");
                        holder.delivered.setImageResource(R.drawable.ok_icon);
                    }
                }

                @Override
                public void onFailure(Call<OrderDTO> call, Throwable t) {
                }
            });
        });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class OrderViewHolder extends RecyclerView.ViewHolder {

        TextView productName, description, orderQty;
        ImageView productImage, delivered;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            description = itemView.findViewById(R.id.productDescription);
            orderQty = itemView.findViewById(R.id.orderQty);
            delivered = itemView.findViewById(R.id.delivered);
        }
    }
}
