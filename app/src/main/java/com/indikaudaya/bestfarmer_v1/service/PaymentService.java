package com.indikaudaya.bestfarmer_v1.service;

import android.app.Dialog;
import android.content.Context;
import android.widget.ImageView;

import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.dto.InvoiceDTO;
import com.indikaudaya.bestfarmer_v1.dto.OrderDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductOrderItemDTO;
import com.indikaudaya.bestfarmer_v1.dto.WatchlistDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.InvoiceModel;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class PaymentService {

    private InvoiceModel invoiceModel;
    Context context;

    Dialog dialog;

    public PaymentService(Context context, InvoiceModel invoiceModel, Dialog dialog) {
        this.context = context;
        this.invoiceModel = invoiceModel;
        this.dialog = dialog;
    }

    public void saveDb() {

        OrderDTO orderDTO = new OrderDTO(false,
                invoiceModel.getUserDTO(),
                new InvoiceDTO(invoiceModel.getInvoiceNumber()),
                invoiceModel.getProductList());

        String token = context.getSharedPreferences(context.getString(R.string.security_api_store), context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<OrderDTO> saveOrder = apiService.saveOrder(orderDTO);
        saveOrder.enqueue(new Callback<OrderDTO>() {
            @Override
            public void onResponse(Call<OrderDTO> call, Response<OrderDTO> response) {
                if (response.isSuccessful()) {
                    OrderDTO body = response.body();
                    if (body == null | body.getOrderId() == 0) {
                        new SweetAlertDialogCustomize().errorAlert(context, "Product order failed! ");
                    } else {
                        new SweetAlertDialogCustomize().successAlert(context, "Product order successfully!.");
                        dialog.dismiss();
                    }
                }
            }

            @Override
            public void onFailure(Call<OrderDTO> call, Throwable t) {
                new SweetAlertDialogCustomize().errorAlert(context, "Product order failed! " + t.getMessage());
            }
        });
    }

}
