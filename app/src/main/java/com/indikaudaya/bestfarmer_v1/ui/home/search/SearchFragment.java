package com.indikaudaya.bestfarmer_v1.ui.home.search;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.DocumentsContract;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.textfield.TextInputEditText;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.adapter.SearchAdapter;
import com.indikaudaya.bestfarmer_v1.adapter.WatchlistAdapter;
import com.indikaudaya.bestfarmer_v1.dto.CategoryDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.WatchlistDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.InvoiceModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.model.SearchModel;
import com.indikaudaya.bestfarmer_v1.model.WatchlistModel;
import com.indikaudaya.bestfarmer_v1.service.AdapterCallBack;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.service.PaymentService;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.util.ArrayList;
import java.util.List;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class SearchFragment extends Fragment implements AdapterCallBack {

    private static final String TAG = SearchFragment.class.getName();
    View root;
    TextInputEditText search;
    Spinner spinnerCategory;

    InvoiceModel model;
    Dialog dialog;
    Button searchButton;
    private int uniqueId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_search, container, false);
        backButton();
        initMethod();
        loadCategory();
        pressSearchButton();
        loadAllProduct("Select Category", "");
        return root;
    }

    private void pressSearchButton() {
        searchButton.setOnClickListener(v -> {
            String categoryName = spinnerCategory.getSelectedItem().toString();
            String searchWord = String.valueOf(search.getText()).trim();

            if (categoryName.equalsIgnoreCase("Select Category")) {
                loadAllProduct(categoryName, searchWord);
            }
            loadAllProduct(categoryName, searchWord);
        });
    }

    private void loadAllProduct(String categoryName, String searchProductKeyword) {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        Call<List<ProductDTO>> cartDTOCall;
        if (!categoryName.equalsIgnoreCase("Select Category") && !searchProductKeyword.isEmpty()) {
            cartDTOCall = apiService.getAllProductsBySearching(categoryName, searchProductKeyword);
        } else if (!searchProductKeyword.isEmpty()) {
            cartDTOCall = apiService.getAllProductsByProductName(searchProductKeyword);
        } else if (!categoryName.equalsIgnoreCase("Select Category")) {
            cartDTOCall = apiService.getAllProductsByCategoryName(categoryName);
        } else {
            cartDTOCall = apiService.getAllProductsNotSigning();
        }

        cartDTOCall.enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(Call<List<ProductDTO>> call, Response<List<ProductDTO>> response) {
                if (response.isSuccessful()) {
                    List<ProductDTO> body = response.body();

                    ArrayList<SearchModel> models = new ArrayList<>();

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
                        if (productDTO.getWatchlistList() != null) {
                            popularFood.setWatchlistCount(productDTO.getWatchlistList().size());
                        }
                        models.add(
                                new SearchModel(
                                        productDTO.getId(),
                                        popularFood
                                )
                        );
                    }
                    initSearchRecycler(models);
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Log.e(TAG, "onFailure: no Product found - " + t.getMessage());
            }
        });
    }

    private void initSearchRecycler(ArrayList<SearchModel> models) {
        RecyclerView recyclerView = root.findViewById(R.id.searchRecycleView);
        SearchAdapter searchAdapter = new SearchAdapter(getContext(), models, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(root.getContext(), LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(searchAdapter);
    }

    private void initMethod() {
        search = root.findViewById(R.id.search);
        spinnerCategory = root.findViewById(R.id.spinnerCategory);
        searchButton = root.findViewById(R.id.button2);
    }

    private void loadCategory() {

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<List<CategoryDTO>> allCategories = apiService.getAllCategorySigning();
        Request request = allCategories.request();
        request.newBuilder().header("Content-Type", "application/json");

        allCategories.enqueue(new Callback<List<CategoryDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<CategoryDTO>> call, @NonNull Response<List<CategoryDTO>> response) {
                if (response.isSuccessful()) {
                    List<CategoryDTO> categoryList = response.body();
                    ArrayList<String> cate = new ArrayList<>();
                    cate.add("Select Category");

                    if (categoryList != null && !categoryList.isEmpty()) {

                        categoryList.forEach(c -> {
                            cate.add(c.getName());
                        });

                        setCategoryToSpinner(cate);

                    } else {
                        cate.add("No Category Found");
                        setCategoryToSpinner(cate);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CategoryDTO>> call, @NonNull Throwable t) {
                List<String> list = new ArrayList<>();
                list.add("No Category Found");
                setCategoryToSpinner(list);
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Server side error : " + t.getMessage());
            }
        });
    }

    private void setCategoryToSpinner(List<String> categories) {
        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(adapter);
    }

    private void backButton() {
        root.findViewById(R.id.backButtonSearch).setOnClickListener(v -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            if (fragmentManager.getBackStackEntryCount() > 0) {
                fragmentManager.popBackStack();
            } else {
                getActivity().getOnBackPressedDispatcher().onBackPressed();
            }
        });
    }

    @Override
    public void onDataItemClicked(InitRequest req, int uniqueId, InvoiceModel model, Dialog dialog) {
        this.model = model;
        this.uniqueId = uniqueId;
        this.dialog = dialog;
        Intent intent = new Intent(getContext(), PHMainActivity.class);
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