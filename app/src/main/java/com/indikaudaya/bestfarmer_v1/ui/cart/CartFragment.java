package com.indikaudaya.bestfarmer_v1.ui.cart;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import com.indikaudaya.bestfarmer_v1.dto.CartDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.CartAdapterModel;
import com.indikaudaya.bestfarmer_v1.model.InvoiceModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.service.AdapterCallBack;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.service.PaymentService;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CartFragment extends Fragment implements AdapterCallBack {

    private final static String TAG = CartFragment.class.getName();
    View root;

    Context context;

    InvoiceModel model;

    Dialog dialog;

    private int uniqueId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_cart, container, false);
        context = getActivity();
        loadAllCartOnThisUser();
        return root;
    }

    private void loadAllCartOnThisUser() {
        long uid = new LoginDetails().getUserDTO().getId();

        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<List<CartDTO>> cartDTOCall = apiService.getCartByUserId(uid);

        cartDTOCall.enqueue(new Callback<List<CartDTO>>() {
            @Override
            public void onResponse(Call<List<CartDTO>> call, Response<List<CartDTO>> response) {
                if (response.isSuccessful()) {
                    List<CartDTO> body = response.body();
                    ArrayList<CartAdapterModel> models = new ArrayList<>();

                    Log.e(TAG, "onResponse: " + body);
                    for (CartDTO cart : body) {
                        Log.e(TAG, "onResponse: " + cart.toString());
                        models.add(
                                new CartAdapterModel(
                                        cart.getId(),
                                        new PopularFood(
                                                cart.getProducts().getId(),
                                                cart.getProducts().getName(),
                                                cart.getProducts().getDescription(),
                                                cart.getProducts().getPrice(),
                                                cart.getProducts().getReviewCount(),
                                                cart.getProducts().getRatingScore(),
                                                cart.getProducts().getCartCount(),
                                                cart.getProducts().getProductImages(),
                                                cart.getProducts().getSeller(),
                                                cart.getProducts().getQty()
                                        )
                                )
                        );
                    }
                    initCartRecycler(models);
                }
            }

            @Override
            public void onFailure(Call<List<CartDTO>> call, Throwable t) {
                Log.e(TAG, "onFailure: no cart found - " + t.getMessage());
            }
        });
    }

    private void initCartRecycler(ArrayList<CartAdapterModel> models) {
        RecyclerView recyclerView = root.findViewById(R.id.cartRecycleView);
        CartAdapter cartAdapter = new CartAdapter(getContext(), models, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(cartAdapter);
    }

    @Override
    public void onDataItemClicked(InitRequest req, int uniqueId, InvoiceModel model, Dialog dialog) {
        this.model = model;
        this.uniqueId = uniqueId;
        this.dialog = dialog;
        Intent intent = new Intent(context, PHMainActivity.class);
        intent.putExtra(PHConstants.INTENT_EXTRA_DATA, req);
        PHConfigs.setBaseUrl(PHConfigs.SANDBOX_URL);
        startActivityForResult(intent, uniqueId);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == uniqueId && data != null && data.hasExtra(PHConstants.INTENT_EXTRA_RESULT)) {
            PHResponse<StatusResponse> response = (PHResponse<StatusResponse>) data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            data.getSerializableExtra(PHConstants.INTENT_EXTRA_RESULT);
            if (resultCode == Activity.RESULT_OK) {
                String msg;
                if (response != null)
                    if (response.isSuccess())
                        msg = "Activity result:" + response.getData().toString();
                    else
                        msg = "Result:" + response.toString();
                else
                    msg = "Result: no response";

            } else if (resultCode == Activity.RESULT_CANCELED) {
                if (response != null) {
                    //! should call success status
                    paymentSuccess();
                    new SweetAlertDialogCustomize().errorAlert(getContext(), response.toString());
                } else
                    new SweetAlertDialogCustomize().errorAlert(getContext(), "User canceled the request");
            }
        }
    }

    private void paymentSuccess() {
        Log.e(TAG, "paymentSuccess: ");
        new PaymentService(getContext(), model, dialog).saveDb();
    }


}