package com.indikaudaya.bestfarmer_v1.ui.productlisting;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.ProductImage;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.home.HomeFragment;
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

public class ProductListingFragment extends Fragment {

    private static final String TAG = ProductListingFragment.class.getName();
    private static final String STORAGE_BASE_PRODUCT_IMAGE_PATH = "product-image/";
    private SweetAlertDialog sweetAlertDialog;
    private List<ProductImage> productImagePath;
    private ImageView backButton, productImage1, productImage2, productImage3, productImage4, productImage5;
    private TextInputEditText productName, description, price, productQty;
    private TextInputLayout productNameLayout, descriptionLayout, priceLayout, productQtyLayout;
    private RadioButton retail, wholesale;
    private CheckBox delivery;
    private FirebaseStorage storage;
    private final long currentTimeMillis = System.currentTimeMillis();

    private Spinner category, units;
    View root;

    UserDTO loginDetails;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_product_listing, container, false);

        //! Initialize
        initMethod();
        //! load Listener
        setListener();
        loadCategory();
        loadUnit();
        pressBackButton();
        clickProductAddButton();
        //! image set
        setProductImage1();

        return root;
    }

    private void setListener() {
        new TextInputListener().inputListener(productName, productNameLayout);
        new TextInputListener().inputListener(productQty, productQtyLayout);
        new TextInputListener().inputListener(description, descriptionLayout);
        new TextInputListener().inputListener(price, priceLayout);
    }

    private void setProductImage1() {
        productImage1.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(1);
        });

        productImage2.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(2);
        });
        productImage3.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(3);
        });
        productImage4.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(4);
        });
        productImage5.setOnClickListener(v -> {
            ImagePicker.with(this)
                    .crop()
                    .compress(1024)
                    .maxResultSize(1080, 1080)
                    .start(5);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            String imageId = UUID.randomUUID().toString();
            if (requestCode == 1) {
                Uri uri = data.getData();
                if (uri != null) {
                    productImage1.setImageURI(uri);
                    productImagePath.add(new ProductImage(loginDetails.getEmail() + "/" + currentTimeMillis + "/" + imageId, uri));
                }
            }
            if (requestCode == 2) {
                Uri uri = data.getData();
                if (uri != null) {
                    productImage2.setImageURI(uri);
                    productImagePath.add(new ProductImage(loginDetails.getEmail() + "/" + currentTimeMillis + "/" + imageId, uri));
                }
            }
            if (requestCode == 3) {
                Uri uri = data.getData();
                if (uri != null) {
                    productImage3.setImageURI(uri);
                    productImagePath.add(new ProductImage(loginDetails.getEmail() + "/" + currentTimeMillis + "/" + imageId, uri));
                }
            }
            if (requestCode == 4) {
                Uri uri = data.getData();
                if (uri != null) {
                    productImage4.setImageURI(uri);
                    productImagePath.add(new ProductImage(loginDetails.getEmail() + "/" + currentTimeMillis + "/" + imageId, uri));
                }
            }
            if (requestCode == 5) {
                Uri uri = data.getData();
                if (uri != null) {
                    productImage5.setImageURI(uri);
                    productImagePath.add(new ProductImage(loginDetails.getEmail() + "/" + currentTimeMillis + "/" + imageId, uri));
                }
            }
        }

    }

    private void uploadImageToServer(Uri uri, String imageNameUnique) {
        StorageReference storageRef = storage.getReference(STORAGE_BASE_PRODUCT_IMAGE_PATH).child(imageNameUnique);

        storageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Log.i(TAG, "onSuccess: product image upload");
                        // new SweetAlertDialogCustomize().successAlert(ProductListingActivity.this, "Upload successfully");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        new SweetAlertDialogCustomize().errorAlert(getContext(), e.getMessage());
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    }
                });

    }

    private void pressBackButton() {
        backButton.setOnClickListener(v -> {
            FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, new HomeFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        });
    }

    private void clickProductAddButton() {
        root.findViewById(R.id.buttonSubmit).setOnClickListener(v -> {
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
        } else if (productImagePath == null) {
            new SweetAlertDialogCustomize().errorAlert(getContext(), "Please upload least one product image");
        } else {
            pleaseWaitProgress(true);
            saveOnDb();
        }
    }

    private void saveOnDb() {

        String token = getActivity().getSharedPreferences("SECURITY_API", Context.MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient.Builder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);

        Call<CategoryDTO> categoryByName = apiService.getCategoryByName(category.getSelectedItem().toString());
        categoryByName.enqueue(new Callback<CategoryDTO>() {
            @Override
            public void onResponse(Call<CategoryDTO> call, Response<CategoryDTO> response) {
                if (response.isSuccessful()) {
                    saveOnDbAfterCategory(response.body());
                } else {
                    pleaseWaitProgress(false);
                    new SweetAlertDialogCustomize().errorAlert(getContext(), "Please select valid category!");
                }
            }
            private void saveOnDbAfterCategory(CategoryDTO category) {
                ArrayList<ProductImageDTO> pathNew = new ArrayList<>();

                productImagePath.forEach(p -> {
                    pathNew.add(new ProductImageDTO(p.getPath()));
                });

                ProductDTO product = new ProductDTO(
                        String.valueOf(productName.getText()),
                        String.valueOf(description.getText()),
                        Double.parseDouble(String.valueOf(price.getText())),
                        Double.parseDouble(String.valueOf(productQty.getText())),
                        units.getSelectedItem().toString(),
                        (retail.isChecked()) ? "retail" : "wholesale",
                        delivery.isChecked(),
                        category,
                        pathNew,
                        loginDetails
                );

                Call<ProductDTO> productCall = apiService.saveProduct(product);
                productCall.enqueue(new Callback<ProductDTO>() {
                    @Override
                    public void onResponse(Call<ProductDTO> call, Response<ProductDTO> response) {
                        if (response.isSuccessful()) {
                            //  new SweetAlertDialogCustomize().successAlert(ProductListingActivity.this, "Product save successfully!");
                            productImageSave();
                        }
                    }

                    @Override
                    public void onFailure(Call<ProductDTO> call, Throwable t) {
                        pleaseWaitProgress(false);
                        new SweetAlertDialogCustomize().errorAlert(getContext(), "Product save unsuccessfully :" + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Call<CategoryDTO> call, Throwable t) {
            }
        });

    }

    private void productImageSave() {
        if (productImagePath != null) {
            productImagePath.forEach(productImage -> {
                uploadImageToServer(productImage.getUri(), productImage.getPath());
            });
            pleaseWaitProgress(false);
            new SweetAlertDialogCustomize().successAlert(getContext(), "Product save successfully!");
            clearAllFields();
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

    private void loadUnit() {
        ArrayList<String> uni = new ArrayList<>();
        uni.add("Select unit");
        uni.add("Pack");
        uni.add("Kg");
        uni.add("L");
        uni.add("Nos");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, uni);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        units.setAdapter(adapter);

    }

    private void setCategoryToSpinner(List<String> categories) {

        ArrayAdapter adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,
                categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);
    }

    private void initMethod() {
        storage = FirebaseStorage.getInstance();
        category = root.findViewById(R.id.spinnerCategory);
        units = root.findViewById(R.id.spinnerUnit);
        backButton = root.findViewById(R.id.backButton);

        productName = root.findViewById(R.id.productName);
        productNameLayout = root.findViewById(R.id.productNameLayout);
        description = root.findViewById(R.id.productDescription);
        descriptionLayout = root.findViewById(R.id.productDescriptionLayout);
        price = root.findViewById(R.id.productPrice);
        priceLayout = root.findViewById(R.id.productPriceLayout);
        productQty = root.findViewById(R.id.productQty);
        productQtyLayout = root.findViewById(R.id.productQtyLayout);

        retail = root.findViewById(R.id.radioButtonRetail);
        wholesale = root.findViewById(R.id.radioButtonWholesale);
        delivery = root.findViewById(R.id.checkBoxDelivery);
        productImage1 = root.findViewById(R.id.imageView1);
        productImage2 = root.findViewById(R.id.imageView2);
        productImage3 = root.findViewById(R.id.imageView3);
        productImage4 = root.findViewById(R.id.imageView4);
        productImage5 = root.findViewById(R.id.imageView5);
        productImagePath = new ArrayList<>();
        if (LoginDetails.isSigning) {
            loginDetails = new LoginDetails().getUserDTO();
        }
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

    private void clearAllFields() {
        loadCategory();
        loadUnit();
        productImagePath = null;

        productName.setText("");
        description.setText("");
        price.setText("");
        retail.setSelected(false);
        wholesale.setSelected(false);
        delivery.setChecked(false);
        productQty.setText("");
        productImage1.setImageResource(android.R.drawable.ic_menu_gallery);
        productImage2.setImageResource(android.R.drawable.ic_menu_gallery);
        productImage3.setImageResource(android.R.drawable.ic_menu_gallery);
        productImage4.setImageResource(android.R.drawable.ic_menu_gallery);
        productImage5.setImageResource(android.R.drawable.ic_menu_gallery);
    }


}