package com.indikaudaya.bestfarmer_v1.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
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
import com.indikaudaya.bestfarmer_v1.model.SearchModel;
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

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SearchAdapterViewHolder> {

    ArrayList<SearchModel> arrayList;
    Context context;
    Activity activity;

    boolean isExistingOnWatchlist;
    boolean isExistingOnCart;

    long watchlistId;
    long cartId;

    AdapterCallBack callBack;

    public SearchAdapter(Context context, ArrayList<SearchModel> searchModels, AdapterCallBack callBack) {
        this.context = context;
        this.arrayList = searchModels;
    }

    @NonNull
    @Override
    public SearchAdapter.SearchAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(context).inflate(R.layout.search_product_layout, parent, false);
        return new SearchAdapterViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchAdapter.SearchAdapterViewHolder holder, int position) {

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
            ProductDetailDialog productDetailDialog = new ProductDetailDialog(context, arrayList.get(position).getPopularFood(), callBack);
            productDetailDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            productDetailDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            productDetailDialog.getWindow().setGravity(Gravity.CENTER);
            productDetailDialog.setCancelable(false);
            productDetailDialog.setDialog(productDetailDialog);
            productDetailDialog.show();
        });

        if (LoginDetails.isSigning) {
            getCartExisting(arrayList.get(position).getPopularFood().getProductId(), new LoginDetails().getUserDTO().getId(), holder.cartButton);
            getWatchlistExisting(arrayList.get(position).getPopularFood().getProductId(), new LoginDetails().getUserDTO().getId(), holder.watchlistButton);
        }
            holder.cartButton.setOnClickListener(v -> {
                if (LoginDetails.isSigning) {
                    CartDTO cartDTO = new CartDTO();
                    cartDTO.setProducts(new ProductDTO(arrayList.get(position).getPopularFood().getProductId()));
                    cartDTO.setUser(new LoginDetails().getUserDTO());
                    cartManagement(holder.cartButton, cartDTO);
                } else {
                    new SweetAlertDialogCustomize().errorAlert(context, "Please sign in!.");
                }
            });

            holder.watchlistButton.setOnClickListener(v -> {
                if (LoginDetails.isSigning) {
                    WatchlistDTO watchlistDTO = new WatchlistDTO();
                    watchlistDTO.setProducts(new ProductDTO(arrayList.get(position).getPopularFood().getProductId()));
                    watchlistDTO.setUser(new LoginDetails().getUserDTO());
                    watchlistManagement(holder.watchlistButton, watchlistDTO);
                } else {
                    new SweetAlertDialogCustomize().errorAlert(context, "Please sign in!.");
                }
            });

    }

    @Override
    public int getItemCount() {
        return arrayList.size();
    }

    public class SearchAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView productName, price, description, cartCount, watchlistCount;
        ImageView productImage, watchlistButton, cartButton;

        public SearchAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            price = itemView.findViewById(R.id.productPrice);
            description = itemView.findViewById(R.id.productDescription);
            cartCount = itemView.findViewById(R.id.cartCount);
            watchlistCount = itemView.findViewById(R.id.watchlistCount);
            cartButton = itemView.findViewById(R.id.cartButton);
            watchlistButton = itemView.findViewById(R.id.watchlistButton);
        }
    }


    //! Cart
    private void cartManagement(ImageView imageView, CartDTO cartDTO) {
        if (LoginDetails.isSigning) {
            if (isExistingOnCart) {
                removeCart(imageView);
            } else {
                saveCart(imageView, cartDTO);
            }
        }
    }

    private void getCartExisting(long productId, long uid, ImageView imageView) {
        if (LoginDetails.isSigning) {

            String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(context.getString(R.string.base_url))
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                    .build();

            BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
            Call<CartDTO> cartDTOCall = apiService.getCartByproductIdAndUserId(uid, productId);

            cartDTOCall.enqueue(new Callback<CartDTO>() {
                @Override
                public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                    if (response.isSuccessful()) {
                        CartDTO body = response.body();
                        if (body.getId() != 0) {
                            imageView.setImageResource(R.drawable.cart_fill);
                            cartId = body.getId();
                            isExistingOnCart = true;
                        } else {
                            imageView.setImageResource(R.drawable.cart);
                            isExistingOnCart = false;
                        }
                    }
                }

                @Override
                public void onFailure(Call<CartDTO> call, Throwable t) {
                }
            });
        }
    }

    private void removeCart(ImageView imageView) {

        String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<Boolean> booleanCall = apiService.deleteCartById(cartId);
        booleanCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    new SweetAlertDialogCustomize().successAlert(context, "Removed successfully!..");
                    imageView.setImageResource(R.drawable.cart);
                    cartId = 0;
                    isExistingOnCart = false;
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
            }
        });
    }

    private void saveCart(ImageView imageView, CartDTO cartDTO) {

        String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<CartDTO> cartDTOCall = apiService.saveOrUpdateCart(cartDTO);
        cartDTOCall.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                if (response.isSuccessful()) {
                    CartDTO body = response.body();
                    if (body.getId() == 0) {
                    } else {
                        cartId = body.getId();
                        isExistingOnCart = true;
                        new SweetAlertDialogCustomize().successAlert(context, "Cart added successfully!.");
                        imageView.setImageResource(R.drawable.cart_fill);
                    }
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
            }
        });
    }


    //!Watchlist
    private void watchlistManagement(ImageView imageView, WatchlistDTO watchlistDTO) {
        if (LoginDetails.isSigning) {
            if (isExistingOnWatchlist) {
                removeWatchlist(imageView);
            } else {
                saveWatchlist(imageView, watchlistDTO);
            }
        }
    }

    private void getWatchlistExisting(long productId, long uid, ImageView imageView) {
        String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<WatchlistDTO> watchlistDTOCall = apiService.getWatchlistByproductIdAndUserId(uid, productId);

        watchlistDTOCall.enqueue(new Callback<WatchlistDTO>() {
            @Override
            public void onResponse(Call<WatchlistDTO> call, Response<WatchlistDTO> response) {
                if (response.isSuccessful()) {
                    WatchlistDTO body = response.body();
                    if (body.getId() != 0) {
                        imageView.setImageResource(R.drawable.watchlist_fill);
                        watchlistId = body.getId();
                        isExistingOnWatchlist = true;
                    } else {
                        imageView.setImageResource(R.drawable.watchlist_home);
                        isExistingOnWatchlist = false;
                    }
                }
            }

            @Override
            public void onFailure(Call<WatchlistDTO> call, Throwable t) {
            }
        });
    }

    private void removeWatchlist(ImageView imageView) {

        String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<Boolean> booleanCall = apiService.deleteWatchlistById(watchlistId);
        booleanCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    imageView.setImageResource(R.drawable.watchlist_home);
                    watchlistId = 0;
                    isExistingOnWatchlist = false;
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
            }
        });
    }

    private void saveWatchlist(ImageView imageView, WatchlistDTO watchlistDTO) {
        String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<WatchlistDTO> watchlistDTOCall = apiService.saveWatchlist(watchlistDTO);
        watchlistDTOCall.enqueue(new Callback<WatchlistDTO>() {
            @Override
            public void onResponse(Call<WatchlistDTO> call, Response<WatchlistDTO> response) {
                if (response.isSuccessful()) {
                    WatchlistDTO body = response.body();
                    if (body == null | body.getId() == 0) {
                    } else {
                        watchlistId = body.getId();
                        isExistingOnWatchlist = true;
                        imageView.setImageResource(R.drawable.watchlist_fill);
                    }
                }
            }

            @Override
            public void onFailure(Call<WatchlistDTO> call, Throwable t) {
            }
        });
    }


}
