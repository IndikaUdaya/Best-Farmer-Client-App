package com.indikaudaya.bestfarmer_v1.ui.watchlist;

import android.app.Dialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.adapter.CartAdapter;
import com.indikaudaya.bestfarmer_v1.adapter.WatchlistAdapter;
import com.indikaudaya.bestfarmer_v1.dto.CartDTO;
import com.indikaudaya.bestfarmer_v1.dto.WatchlistDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.CartAdapterModel;
import com.indikaudaya.bestfarmer_v1.model.InvoiceModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.model.WatchlistModel;
import com.indikaudaya.bestfarmer_v1.service.AdapterCallBack;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.productdetail.ProductDetailDialog;

import java.util.ArrayList;
import java.util.List;

import lk.payhere.androidsdk.model.InitRequest;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class WatchlistFragment extends Fragment implements AdapterCallBack {

    private static final String TAG = WatchlistFragment.class.getName();
    View root;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_watchlist, container, false);
        loadAllWatchlistOnThisUser();
        return root;
    }

    private void loadAllWatchlistOnThisUser() {
        long uid = new LoginDetails().getUserDTO().getId();

        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<List<WatchlistDTO>> cartDTOCall = apiService.getWatchlistByUserId(uid);

        cartDTOCall.enqueue(new Callback<List<WatchlistDTO>>() {
            @Override
            public void onResponse(Call<List<WatchlistDTO>> call, Response<List<WatchlistDTO>> response) {
                if (response.isSuccessful()) {
                    List<WatchlistDTO> body = response.body();
                    ArrayList<WatchlistModel> models = new ArrayList<>();

                    Log.e(TAG, "onResponse: " + body);
                    for (WatchlistDTO watchlist : body) {
                        Log.e(TAG, "onResponse: " + watchlist.toString());
                        models.add(
                                new WatchlistModel(
                                        watchlist.getId(),
                                        new PopularFood(
                                                watchlist.getProducts().getId(),
                                                watchlist.getProducts().getName(),
                                                watchlist.getProducts().getDescription(),
                                                watchlist.getProducts().getPrice(),
                                                watchlist.getProducts().getReviewCount(),
                                                watchlist.getProducts().getRatingScore(),
                                                watchlist.getProducts().getCartCount(),
                                                watchlist.getProducts().getProductImages(),
                                                watchlist.getProducts().getSeller(),
                                                watchlist.getProducts().getQty()
                                        )
                                )
                        );
                    }
                    initWatchlistRecycler(models);
                }
            }

            @Override
            public void onFailure(Call<List<WatchlistDTO>> call, Throwable t) {
                Log.e(TAG, "onFailure: no watchlist found - " + t.getMessage());
            }
        });
    }

    private void initWatchlistRecycler(ArrayList<WatchlistModel> models) {
        RecyclerView recyclerView = root.findViewById(R.id.watchlistRecycleView);
        WatchlistAdapter watchlistAdapter = new WatchlistAdapter(getContext(), models,this);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(watchlistAdapter);
    }

    @Override
    public void onDataItemClicked(InitRequest req, int uniqueId, InvoiceModel invoiceModel, Dialog dialog) {

    }
}