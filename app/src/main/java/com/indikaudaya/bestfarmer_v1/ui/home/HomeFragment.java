package com.indikaudaya.bestfarmer_v1.ui.home;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.AnimationTypes;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.adapter.PopularListAdapter;
import com.indikaudaya.bestfarmer_v1.databinding.FragmentHomeBinding;
import com.indikaudaya.bestfarmer_v1.dto.CategoryDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.SellerReviewDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.model.InvoiceModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.model.ProductImage;
import com.indikaudaya.bestfarmer_v1.service.AdapterCallBack;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.service.PaymentService;
import com.indikaudaya.bestfarmer_v1.ui.home.search.SearchFragment;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import lk.payhere.androidsdk.PHConfigs;
import lk.payhere.androidsdk.PHConstants;
import lk.payhere.androidsdk.PHMainActivity;
import lk.payhere.androidsdk.PHResponse;
import lk.payhere.androidsdk.model.InitRequest;
import lk.payhere.androidsdk.model.StatusResponse;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment  implements AdapterCallBack {

    private static final String TAG = HomeFragment.class.getName();

    View root;

    InvoiceModel model;
    Dialog dialog;
    private int uniqueId;

    private ImageView ima1, ima2, ima3, ima4, ima5;
    private TextView cName1, cName2, cName3, cName4, cName5, userName;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        root = inflater.inflate(R.layout.fragment_home, container, false);
        initMethod();
        loadBannerFromFirebaseStorage();
        loadPopularFood();
        loadSearchFragment();
        loadCategory();
        isLoginUser();
        return root;
    }

    private void isLoginUser() {
        if (LoginDetails.isSigning) {
            userName.setText(new LoginDetails().getUserDTO().getEmail());
        } else {
            userName.setText("");
        }
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
                            switch (c.getName()) {
                                case "Fruits":
                                    ima1.setImageResource(R.drawable.fruits);
                                    cName1.setText(c.getName());
                                    break;
                                case "Tuberous Vegetables":
                                    ima2.setImageResource(R.drawable.tuberous_vegetables);
                                    cName2.setText(c.getName());
                                    break;
                                case "Beverage":
                                    ima3.setImageResource(R.drawable.beverage);
                                    cName3.setText(c.getName());
                                    break;
                                case "Leafy Green Vegetables":
                                    ima4.setImageResource(R.drawable.leafy_green_vegetables);
                                    cName4.setText(c.getName());
                                    break;
                                case "Nightshade Vegetables":
                                    ima5.setImageResource(R.drawable.nightshade_vegetables);
                                    cName5.setText(c.getName());
                                    break;
                            }
                        });
                    } else {
                        cate.add("No Category Found");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CategoryDTO>> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: no category found in home");
            }
        });
    }

    private void initMethod() {
        ima1 = root.findViewById(R.id.imageView3);
        ima2 = root.findViewById(R.id.imageView4);
        ima3 = root.findViewById(R.id.imageView5);
        ima4 = root.findViewById(R.id.imageView6);
        ima5 = root.findViewById(R.id.imageView7);
        cName1 = root.findViewById(R.id.textView6);
        cName2 = root.findViewById(R.id.textView7);
        cName3 = root.findViewById(R.id.textView8);
        cName4 = root.findViewById(R.id.textView9);
        cName5 = root.findViewById(R.id.textView10);
        userName = root.findViewById(R.id.userNameHome);

        ima1.setOnClickListener(v -> {
            replaceFragment();
        });
        ima2.setOnClickListener(v -> {
            replaceFragment();
        });
        ima3.setOnClickListener(v -> {
            replaceFragment();
        });
        ima4.setOnClickListener(v -> {
            replaceFragment();
        });
        ima5.setOnClickListener(v -> {
            replaceFragment();
        });
    }

    private void loadSearchFragment() {
        root.findViewById(R.id.searchHome).setOnClickListener(v -> {
            replaceFragment();
        });
    }

    private void replaceFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.frame_layout, new SearchFragment());
        transaction.addToBackStack("home");
        transaction.commit();
    }

    private void loadBannerFromFirebaseStorage() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference("/banner/")
                .listAll()
                .addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        ArrayList<SlideModel> imageList = new ArrayList<>();
                        for (StorageReference reference : listResult.getItems()) {
                            reference.getDownloadUrl()
                                    .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            SlideModel slideModel = new SlideModel(String.valueOf(uri),
                                                    "Fresh and organic Vegetable", ScaleTypes.CENTER_CROP);
                                            Log.e(TAG, "onSuccess: " + uri);
                                            imageList.add(slideModel);
                                            if (listResult.getItems().size() == imageList.size()) {
                                                sliderImage(imageList);
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.i(TAG, "onFailure: Banner image load - " + e.getMessage());
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: Load Image - " + e.getMessage());
                    }
                });
    }

    private void sliderImage(ArrayList<SlideModel> slideModels) {
        ImageSlider imageSlider = root.findViewById(R.id.image_slider);
        imageSlider.setSlideAnimation(AnimationTypes.FIDGET_SPINNER);
        imageSlider.setImageList(slideModels);
    }

    private void initRecyclerView(ArrayList<PopularFood> popularFoods) {
        RecyclerView recyclerViewPopular = root.findViewById(R.id.recyclerView);
        recyclerViewPopular.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        recyclerViewPopular.setAdapter(new PopularListAdapter(getContext(), popularFoods,this,dialog));
    }

    private void loadPopularFood() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        Call<List<ProductDTO>> allProducts = apiService.getAllProductsNotSigning();
        allProducts.request().newBuilder().header("Content-Type", "application/json").build();

        allProducts.enqueue(new Callback<List<ProductDTO>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductDTO>> call, @NonNull Response<List<ProductDTO>> response) {
                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        List<ProductDTO> resProduct = response.body();
                        ArrayList<PopularFood> items1 = new ArrayList<>();
                        resProduct.forEach(product -> {
                            List<SellerReviewDTO> buyerReview = product.getSeller().getBuyerReview();

                            int sellerReviewCount = 0;
                            double sellerRanking = 0.0;

                            if (buyerReview != null && buyerReview.size() > 0) {
                                for (SellerReviewDTO sellerReview : buyerReview) {
                                    sellerReviewCount++;
                                    sellerRanking += sellerReview.getRating();
                                }
                            }

                            double SellerRankingScore = 0.0;
                            if (sellerReviewCount != 0) {
                                SellerRankingScore = new BigDecimal(sellerRanking / sellerReviewCount).setScale(2, RoundingMode.HALF_UP).doubleValue();
                            }

                            items1.add(new PopularFood(
                                    product.getId(),
                                    product.getName(),
                                    product.getDescription(),
                                    BigDecimal.valueOf(product.getPrice()).setScale(2, RoundingMode.HALF_UP).doubleValue(),
                                    sellerReviewCount,
                                    SellerRankingScore,
                                    product.getCartCount(),
                                    product.getProductImages(),
                                    new UserDTO(product.getSeller().getId()),
                                    product.getQty()
                            ));
                        });
                        initRecyclerView(items1);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<ProductDTO>> call, Throwable t) {
                Log.e(TAG, "onFailure: load all product in main activity - " + t.getMessage());
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

