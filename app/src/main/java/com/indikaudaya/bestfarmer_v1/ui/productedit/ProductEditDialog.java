package com.indikaudaya.bestfarmer_v1.ui.productedit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentTransaction;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.github.dhaval2404.imagepicker.ImagePicker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.dto.CategoryDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductImageDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.CategoryModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.model.ProductImage;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.home.HomeFragment;
import com.indikaudaya.bestfarmer_v1.ui.productlisting.ProductListingFragment;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;
import com.indikaudaya.bestfarmer_v1.util.TextInputListener;
import com.indikaudaya.bestfarmer_v1.util.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import cn.pedant.SweetAlert.SweetAlertDialog;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductEditDialog extends Dialog {

    private static final String TAG = ProductEditDialog.class.getName();
    private SweetAlertDialog sweetAlertDialog;
    private ImageView backButton;
    private TextInputEditText productName, description, price, productQty;
    private TextInputLayout productNameLayout, descriptionLayout, priceLayout, productQtyLayout;
    private RadioButton retail, wholesale;
    private CheckBox delivery;
    private Spinner category, units;

    Context context;
    PopularFood popularFood;

    public ProductEditDialog(@NonNull Context context, PopularFood popularFood) {
        super(context);
        this.context = context;
        this.popularFood = popularFood;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_product_edit);

        initMethod();
        setListener();
        pressBackButton();

        loadUnit();
        loadCategory();
        clickProductUpdateButton();
        setExistedData();

    }

    private void setExistedData() {
        if (popularFood != null) {
            productName.setText(popularFood.getTitle());
            description.setText(popularFood.getDescription());
            price.setText(String.valueOf(popularFood.getPrice()));
            productQty.setText(String.valueOf(popularFood.getQty()));

            if (popularFood.getType().equalsIgnoreCase("wholesale")) {
                wholesale.setChecked(true);
            } else {
                retail.setChecked(true);
            }
            delivery.setChecked(popularFood.isDeliveryOption());
        }
    }

    private void setListener() {
        new TextInputListener().inputListener(productName, productNameLayout);
        new TextInputListener().inputListener(productQty, productQtyLayout);
        new TextInputListener().inputListener(description, descriptionLayout);
        new TextInputListener().inputListener(price, priceLayout);
    }

    private void pressBackButton() {
        backButton.setOnClickListener(v -> {
            dismiss();
        });
    }

    private void clickProductUpdateButton() {
        findViewById(R.id.buttonUpdate).setOnClickListener(v -> {
            checkInputField();
        });
    }

    private void checkInputField() {
        if (!Validator.isTextValidate(productName.getText())) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please enter product/food name");
        } else if (!Validator.isTextValidate(description.getText())) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please enter product/food description");
        } else if (!Validator.isTextValidate(productQty.getText())) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please enter product/food quantity");
        } else if (String.valueOf(units.getSelectedItem()).equalsIgnoreCase("Select unit")) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please enter product/food unit");
        } else if (Double.parseDouble(String.valueOf(price.getText())) <= 0.0) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please enter product/food price");
        } else if (!retail.isChecked() && !wholesale.isChecked()) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please select Retail or Wholesale");
        } else if (category.getSelectedItem().toString().equalsIgnoreCase("Select Category")) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please select product category");
        } else {
            pleaseWaitProgress(true);
            updateOnDbAfterCategory();
        }
    }

    private void updateOnDbAfterCategory() {
        String token = context.getSharedPreferences("SECURITY_API", Context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        Call<CategoryDTO> categoryByName = apiService.getCategoryByName(category.getSelectedItem().toString());
        categoryByName.enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                if (response.isSuccessful()) {
                    updateOnDbAfterCategory(response.body());
                }
            }

            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {

            }
        });
    }

    private void updateOnDbAfterCategory(CategoryDTO categoryDTO) {

        String token = context.getSharedPreferences("SECURITY_API", Context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        ProductDTO product = new ProductDTO(popularFood.getProductId());

        product.setCategory(categoryDTO);
        product.setName(String.valueOf(productName.getText()));
        product.setDescription(String.valueOf(description.getText()));
        product.setPrice(Double.parseDouble(String.valueOf(price.getText())));
        product.setQty(Double.parseDouble(String.valueOf(productQty.getText())));
        product.setUnit(units.getSelectedItem().toString());
        product.setType((retail.isChecked()) ? "retail" : "wholesale");
        product.setDeliveryAvailable(delivery.isChecked());
        product.setSeller(new LoginDetails().getUserDTO());
        product.setProductImages(popularFood.getProductImageList());

        Call<ProductDTO> productCall = apiService.updateProduct(popularFood.getProductId(), product);
        productCall.enqueue(new Callback<ProductDTO>() {
            @Override
            public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                if (response.isSuccessful()) {
                    new SweetAlertDialogCustomize().successAlert(context, "Product Update successfully!");
                    pleaseWaitProgress(false);
                    dismiss();
                }
            }

            @Override
            public void onFailure(Call<ProductDTO> call, Throwable t) {
                pleaseWaitProgress(false);
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Product save unsuccessfully :" + t.getMessage());
            }
        });
    }

    private void loadCategory() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(context.getString(R.string.base_url))
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
//                    ArrayList<CategoryModel> cate = new ArrayList<>();
//                    cate.add(new CategoryModel(0, "Select Category"));
                    ArrayList<String> cate = new ArrayList<>();
                    cate.add("Select Category");

                    if (categoryList != null && !categoryList.isEmpty()) {
                        categoryList.forEach(c -> {
//                            cate.add(new CategoryModel(c.getId(), c.getName()));
                            cate.add(c.getName());
                        });
                        setCategoryToSpinner(cate);
                    } else {
//                        cate.add(new CategoryModel(0, "No Category Found"));
                        cate.add("No Category Found");
                        setCategoryToSpinner(cate);
                    }
                    for (int i = 0; i < cate.size(); i++) {
                        if (category.getItemAtPosition(i).toString().equalsIgnoreCase(popularFood.getCategoryNameForUpdate())) {
                            category.setSelection(i);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CategoryDTO>> call, @NonNull Throwable t) {
//                List<CategoryModel> list = new ArrayList<>();
//                list.add(new CategoryModel(0, "No Category Found"));
                List<String> list = new ArrayList<>();
                list.add("No Category Found");
                setCategoryToSpinner(list);
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Server side error : " + t.getMessage());
            }
        });
    }

    private void loadUnit() {
        ArrayList<String> uni = new ArrayList<>();
        uni.add("Select unit");
        uni.add("Pack");
        uni.add("Kg");
        uni.add("L");
        uni.add("Nos");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, uni);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        units.setAdapter(adapter);

        for (int i = 0; i < uni.size(); i++) {
            if (units.getItemAtPosition(i).toString().equalsIgnoreCase(popularFood.getUnit())) {
                units.setSelection(i);
                break;
            }
        }

    }

    private void setCategoryToSpinner(List<String> categories) {
        ArrayAdapter adapter = new ArrayAdapter(context, android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);
    }

    private void pleaseWaitProgress(boolean show) {
        if (sweetAlertDialog == null) {
            sweetAlertDialog = new SweetAlertDialogCustomize().loadingAlert(getContext(), false);
        }
        if (show) {
            sweetAlertDialog.show();
        } else {
            sweetAlertDialog.dismiss();
        }
    }

    private void initMethod() {
        category = findViewById(R.id.spinnerCategoryPUpdate);
        units = findViewById(R.id.spinnerUnitPUpdate);
        backButton = findViewById(R.id.backButtonPUpdate);

        productName = findViewById(R.id.productNamePUpdate);
        productNameLayout = findViewById(R.id.productNameLayoutPUpdate);
        description = findViewById(R.id.productDescriptionPUpdate);
        descriptionLayout = findViewById(R.id.productDescriptionLayoutPUpdate);
        price = findViewById(R.id.productPricePUpdate);
        priceLayout = findViewById(R.id.productPriceLayoutPUpdate);
        productQty = findViewById(R.id.productQtyPUpdate);
        productQtyLayout = findViewById(R.id.productQtyLayoutPUpdate);

        retail = findViewById(R.id.radioButtonRetailPUpdate);
        wholesale = findViewById(R.id.radioButtonWholesalePUpdate);
        delivery = findViewById(R.id.checkBoxDeliveryPUpdate);
    }
}