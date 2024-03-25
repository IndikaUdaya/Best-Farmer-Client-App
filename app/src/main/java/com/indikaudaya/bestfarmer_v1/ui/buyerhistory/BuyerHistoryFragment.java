package com.indikaudaya.bestfarmer_v1.ui.buyerhistory;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.adapter.BuyerHistoryAdapter;
import com.indikaudaya.bestfarmer_v1.adapter.OrderAdapter;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.BuyerHistoryModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.OrderModel;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.home.HomeFragment;
import com.indikaudaya.bestfarmer_v1.ui.home.search.SearchFragment;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class BuyerHistoryFragment extends Fragment {

    private static final String TAG = SearchFragment.class.getName();

    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_buyer_history, container, false);
        loadBuyerHistoryProduct();
        backButton();
        return root;
    }

    private void loadBuyerHistoryProduct() {
        long buyerId = new LoginDetails().getUserDTO().getId();
        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        Call<List<ProductDTO>> products = apiService.getProductOrderByBuyerId(buyerId);

        products.enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDTO>> call, @NonNull Response<List<ProductDTO>> response) {
                if (response.isSuccessful()) {
                    List<ProductDTO> body = response.body();

                    ArrayList<BuyerHistoryModel> models = new ArrayList<>();

                    for (ProductDTO orderDTO : body) {

                        PopularFood popularFood = new PopularFood(
                                orderDTO.getId(),
                                orderDTO.getName(),
                                orderDTO.getDescription(),
                                orderDTO.getPrice(),
                                orderDTO.getReviewCount(),
                                orderDTO.getRatingScore(),
                                orderDTO.getCartCount(),
                                orderDTO.getProductImages(),
                                orderDTO.getSeller(),
                                orderDTO.getQty()
                        );
                        popularFood.setWatchlistCount(orderDTO.getWatchlistCount());
                        popularFood.setType(orderDTO.getType());
                        popularFood.setDeliveryOption(orderDTO.isDeliveryAvailable());
                        popularFood.setUnit(orderDTO.getUnit());
                        popularFood.setQty(orderDTO.getQty());
                        popularFood.setCategoryNameForUpdate(orderDTO.getCategory().getName());
                        popularFood.setDeliveryProduct(orderDTO.isProductDelivered());

                        models.add(
                                new BuyerHistoryModel(
                                        orderDTO.getOrderId(),
                                        orderDTO.getId(),
                                        orderDTO.getOrderQty(),
                                        orderDTO.getOrderDate(),
                                        popularFood
                                )
                        );
                    }
                    initBuyerHistoryRecycler(models);
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Log.e(TAG, "onFailure: no order found - " + t.getMessage());
            }
        });
    }

    private void initBuyerHistoryRecycler(ArrayList<BuyerHistoryModel> models) {
        RecyclerView recyclerView = root.findViewById(R.id.sellerReviewRecycleView);
        BuyerHistoryAdapter listed = new BuyerHistoryAdapter(getContext(), models);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(listed);
    }

    private void backButton() {
        root.findViewById(R.id.backButtonListed).setOnClickListener(v -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.frame_layout, new HomeFragment());
            fragmentTransaction.commit();
        });
    }


}