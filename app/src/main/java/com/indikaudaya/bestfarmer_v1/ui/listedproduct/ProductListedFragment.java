package com.indikaudaya.bestfarmer_v1.ui.listedproduct;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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
import com.indikaudaya.bestfarmer_v1.adapter.ProductListedAdapter;
import com.indikaudaya.bestfarmer_v1.adapter.SearchAdapter;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.dto.WatchlistDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.model.ProductListedModel;
import com.indikaudaya.bestfarmer_v1.model.SearchModel;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.home.HomeFragment;
import com.indikaudaya.bestfarmer_v1.ui.watchlist.WatchlistFragment;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductListedFragment extends Fragment {

    private static final String TAG = ProductListedFragment.class.getName();
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_product_listed, container, false);
        backButton();
        loadSellerProduct();
        return root;
    }
    private void loadSellerProduct() {
        long sellerId = new LoginDetails().getUserDTO().getId() ;
        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        Call<List<ProductDTO>> products = apiService.getProductsBySellerId(sellerId);

        products.enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDTO>> call, @NonNull Response<List<ProductDTO>> response) {
                if (response.isSuccessful()) {
                    List<ProductDTO> body = response.body();
                    ArrayList<ProductListedModel> models = new ArrayList<>();
                    for (ProductDTO productDTO : body) {

                        PopularFood popularFood = new PopularFood(
                                productDTO.getId(),
                                productDTO.getName(),
                                productDTO.getDescription(),
                                productDTO.getPrice(),
                                productDTO.getReviewCount(),
                                productDTO.getRatingScore(),
                                productDTO.getCartCount(),
                                productDTO.getProductImages(),
                                productDTO.getSeller(),
                                productDTO.getQty()
                        );
                        popularFood.setWatchlistCount(productDTO.getWatchlistCount());
                        popularFood.setType(productDTO.getType());
                        popularFood.setDeliveryOption(productDTO.isDeliveryAvailable());
                        popularFood.setUnit(productDTO.getUnit());
                        popularFood.setQty(productDTO.getQty());
                        popularFood.setCategoryNameForUpdate(productDTO.getCategory().getName());

                        Log.e(TAG, "onResponse: "+popularFood.toString() );
                        models.add(
                                new ProductListedModel(
                                        productDTO.getId(),
                                        popularFood
                                )
                        );
                    }
                    initListedRecycler(models);
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Log.e(TAG, "onFailure: no Product found - " + t.getMessage());
            }
        });
    }

    private void initListedRecycler(ArrayList<ProductListedModel> models) {
        RecyclerView recyclerView = root.findViewById(R.id.ListedRecycleView);
        ProductListedAdapter listed = new ProductListedAdapter(getContext(), models);
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