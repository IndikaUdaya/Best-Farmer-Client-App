package com.indikaudaya.bestfarmer_v1.ui.productdetail;

import static android.R.color.transparent;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.HeroCarouselStrategy;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.indikaudaya.bestfarmer_v1.R;
import com.indikaudaya.bestfarmer_v1.adapter.CarouselAdapter;
import com.indikaudaya.bestfarmer_v1.dto.CartDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductImageDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductOrderItemDTO;
import com.indikaudaya.bestfarmer_v1.dto.SellerReviewDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.dto.WatchlistDTO;
import com.indikaudaya.bestfarmer_v1.interceptor.RequestInterceptor;
import com.indikaudaya.bestfarmer_v1.model.CarouselModel;
import com.indikaudaya.bestfarmer_v1.model.InvoiceModel;
import com.indikaudaya.bestfarmer_v1.model.LoginDetails;
import com.indikaudaya.bestfarmer_v1.model.PopularFood;
import com.indikaudaya.bestfarmer_v1.service.AdapterCallBack;
import com.indikaudaya.bestfarmer_v1.service.BestFarmerApiService;
import com.indikaudaya.bestfarmer_v1.ui.sellerreview.SellerReviewListDialog;
import com.indikaudaya.bestfarmer_v1.util.SweetAlertDialogCustomize;
import com.indikaudaya.bestfarmer_v1.util.TextInputListener;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import lk.payhere.androidsdk.model.InitRequest;
import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ProductDetailDialog extends Dialog {

    private static final String TAG = ProductDetailDialog.class.getName();
    Context context;
    PopularFood popularFood;
    boolean isExistingOnWatchlist;
    boolean isExistingOnCart;
    long watchlistId;
    long cartId;

    Dialog dialog;

    TextInputLayout orderQtyInputLayout;
    TextInputEditText orderQty;

    AdapterCallBack callBack;

    public ProductDetailDialog(Context context, PopularFood popularFood, AdapterCallBack callBack) {
        super(context);
        this.context = context;
        this.popularFood = popularFood;
        this.callBack = callBack;
    }

    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_product_detail);

        initMethod();
        setValuesToFields(popularFood);
        clickReviewButton();
        backButton();
        watchlistManagement();
        cartManagement();
        getWatchlistExisting();
        getCartExisting();
        getSellerReview(false);

        pressBuyNow();
    }

    private void initMethod() {
        orderQtyInputLayout = findViewById(R.id.orderQtyInputLayout);
        orderQty = findViewById(R.id.orderQty);
        orderQty.setText("0");
        new TextInputListener().inputListener(orderQty, orderQtyInputLayout);
    }

    private void pressBuyNow() {
        Button buyButtonProduct = findViewById(R.id.button);
        buyButtonProduct.setOnClickListener(v -> {
            if (!LoginDetails.isSigning) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please signing to your account");
            } else {
                if (Double.parseDouble(String.valueOf(orderQty.getText())) <= popularFood.getQty()) {
                    UserDTO userDTO = new LoginDetails().getUserDTO();
                    String orderId = popularFood.getProductId() + "-" + userDTO.getId() + "-" + System.currentTimeMillis();
                    double amount = Double.parseDouble(String.valueOf(orderQty.getText())) * popularFood.getPrice();

                    InitRequest req = new InitRequest();
                    req.setMerchantId("1222716");       // Merchant ID
                    req.setCurrency("LKR");             // Currency code LKR/USD/GBP/EUR/AUD
                    req.setAmount(amount);             // Final Amount to be charged
                    req.setOrderId(orderId);        // Unique Reference ID
                    req.setItemsDescription(popularFood.getTitle());  // Item description title
                    req.setCustom1("This is the custom message 1");
                    req.setCustom2("This is the custom message 2");
                    req.getCustomer().setFirstName(userDTO.getEmail());
                    req.getCustomer().setLastName(" ");
                    req.getCustomer().setEmail(userDTO.getEmail());
                    req.getCustomer().setPhone("077");
                    req.getCustomer().getAddress().setAddress("99, embaraluwa, weliweriya");
                    req.getCustomer().getAddress().setCity("Gampaha");
                    req.getCustomer().getAddress().setCountry("Sri Lanka");

                    int uniqueId = (int) System.currentTimeMillis();

                    if (callBack != null) {
                        ArrayList<ProductOrderItemDTO> productList = new ArrayList<>();

                        ProductOrderItemDTO dto = new ProductOrderItemDTO(popularFood.getProductId(), Double.parseDouble(String.valueOf(orderQty.getText())));

                        productList.add(dto);

                        callBack.onDataItemClicked(req, uniqueId, new InvoiceModel(orderId, userDTO, amount, productList), this);
                    }
                }
                else {
                    new SweetAlertDialogCustomize().errorAlert(getContext(), "Please check order quantity less than or equal product quantity.");
                }
            }
        });
    }

    //! Cart
    private void cartManagement() {
        findViewById(R.id.addToCart).setOnClickListener(v1 -> {
            if (LoginDetails.isSigning) {
                if (isExistingOnCart) {
                    removeCart();
                } else {
                    saveCart();
                }
            } else {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please sign in!");
            }
        });
    }

    private void getCartExisting() {
        if (LoginDetails.isSigning) {

            long productId = popularFood.getProductId();
            long uid = new LoginDetails().getUserDTO().getId();

            String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getContext().getString(R.string.base_url))
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
                        Log.e(TAG, "onResponse: " + body.toString());
                        if (body.getId() != 0) {
                            ((ImageView) findViewById(R.id.addToCart)).setImageResource(R.drawable.cart_fill);
                            cartId = body.getId();
                            isExistingOnCart = true;
                        } else {
                            ((ImageView) findViewById(R.id.addToCart)).setImageResource(R.drawable.cart);
                            isExistingOnCart = false;
                        }
                    } else {
                        Log.d(TAG, "onResponse: no watchlist product");
                    }
                }

                @Override
                public void onFailure(Call<CartDTO> call, Throwable t) {
                    Log.d(TAG, "onFailure: no cart found - " + t.getMessage());
                }
            });
        }
    }

    private void removeCart() {

        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<Boolean> booleanCall = apiService.deleteCartById(cartId);
        booleanCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    new SweetAlertDialogCustomize().successAlert(getContext(), "Removed successfully!..");
                    ((ImageView) findViewById(R.id.addToCart)).setImageResource(R.drawable.cart);
                    cartId = 0;
                    isExistingOnCart = false;
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
            }
        });
    }

    private void saveCart() {
        CartDTO cartDTO = new CartDTO();
        cartDTO.setProducts(new ProductDTO(popularFood.getProductId()));
        cartDTO.setUser(new LoginDetails().getUserDTO());

        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<CartDTO> cartDTOCall = apiService.saveOrUpdateCart(cartDTO);
        cartDTOCall.enqueue(new Callback<CartDTO>() {
            @Override
            public void onResponse(Call<CartDTO> call, Response<CartDTO> response) {
                if (response.isSuccessful()) {
                    Log.e(TAG, "onResponse: " + response);
                    CartDTO body = response.body();
                    if (body.getId() == 0) {
                        new SweetAlertDialogCustomize().errorAlert(getContext(), "Cart add failed! ");
                    } else {
                        cartId = body.getId();
                        isExistingOnCart = true;
                        new SweetAlertDialogCustomize().successAlert(getContext(), "Cart added successfully!.");
                        ((ImageView) findViewById(R.id.addToCart)).setImageResource(R.drawable.cart_fill);
                    }
                }
            }

            @Override
            public void onFailure(Call<CartDTO> call, Throwable t) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Product add to cart failed! " + t.getMessage());
            }
        });
    }


    //!Watchlist
    private void watchlistManagement() {
        findViewById(R.id.addToWatchlist).setOnClickListener(v1 -> {
            if (LoginDetails.isSigning) {
                if (isExistingOnWatchlist) {
                    removeWatchlist();
                } else {
                    saveWatchlist();
                }
            } else {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please sign in!");
            }
        });
    }

    private void getWatchlistExisting() {
        if (LoginDetails.isSigning) {

            long productId = popularFood.getProductId();
            Long uid = new LoginDetails().getUserDTO().getId();

            String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(getContext().getString(R.string.base_url))
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
                            ((ImageView) findViewById(R.id.addToWatchlist)).setImageResource(R.drawable.watchlist_fill);
                            watchlistId = body.getId();
                            isExistingOnWatchlist = true;
                        } else {
                            ((ImageView) findViewById(R.id.addToWatchlist)).setImageResource(R.drawable.watchlist_home);
                            isExistingOnWatchlist = false;
                        }
                    } else {
                        Log.d(TAG, "onResponse: no watchlist product");
                    }
                }

                @Override
                public void onFailure(Call<WatchlistDTO> call, Throwable t) {
                    Log.d(TAG, "onFailure: no watchlist found - " + t.getMessage());
                }
            });
        }
    }

    private void removeWatchlist() {

        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<Boolean> booleanCall = apiService.deleteWatchlistById(watchlistId);
        booleanCall.enqueue(new Callback<Boolean>() {
            @Override
            public void onResponse(Call<Boolean> call, Response<Boolean> response) {
                if (response.isSuccessful()) {
                    new SweetAlertDialogCustomize().successAlert(getContext(), "Removed successfully!..");
                    ((ImageView) findViewById(R.id.addToWatchlist)).setImageResource(R.drawable.watchlist_home);
                    watchlistId = 0;
                    isExistingOnWatchlist = false;
                }
            }

            @Override
            public void onFailure(Call<Boolean> call, Throwable t) {
            }
        });
    }

    private void saveWatchlist() {
        WatchlistDTO watchlistDTO = new WatchlistDTO();
        watchlistDTO.setProducts(new ProductDTO(popularFood.getProductId()));
        watchlistDTO.setUser(new LoginDetails().getUserDTO());

        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
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
                        new SweetAlertDialogCustomize().errorAlert(getContext(), "Product watchlist add failed! ");
                    } else {
                        watchlistId = body.getId();
                        isExistingOnWatchlist = true;
                        new SweetAlertDialogCustomize().successAlert(getContext(), "Product watchlist added successfully!.");
                        ((ImageView) findViewById(R.id.addToWatchlist)).setImageResource(R.drawable.watchlist_fill);
                    }
                }
            }

            @Override
            public void onFailure(Call<WatchlistDTO> call, Throwable t) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Product watchlist add failed! " + t.getMessage());
            }
        });
    }


    //! Seller Review
    private void clickReviewButton() {
        findViewById(R.id.imageView5).setOnClickListener(v -> {
            if (!LoginDetails.isSigning) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "Please signing to your account");
            } else {
                getSellerReview(true);
            }
        });
    }

    private void getSellerReview(boolean click) {
        long sellerId = popularFood.getSeller().getId();
        String token = getContext().getSharedPreferences(getContext().getString(R.string.security_api_store), getContext().MODE_PRIVATE).getString("token", "");

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(getContext().getString(R.string.base_url))
                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient().newBuilder().addInterceptor(new RequestInterceptor(token)).build())
                .build();

        BestFarmerApiService apiService = retrofit.create(BestFarmerApiService.class);
        Call<List<SellerReviewDTO>> review = apiService.getAllSellerReviewById(sellerId);

        review.enqueue(new Callback<List<SellerReviewDTO>>() {
            @Override
            public void onResponse(Call<List<SellerReviewDTO>> call, Response<List<SellerReviewDTO>> response) {
                if (response.isSuccessful()) {
                    List<SellerReviewDTO> body = response.body();

                    int totalReview = 0;
                    float ratingByBuyer = 0.0f;

                    for (SellerReviewDTO sr : body) {
                        totalReview++;
                        ratingByBuyer += sr.getRating();
                    }
                    if (totalReview != 0) {
                        ((TextView) findViewById(R.id.sellerRating)).setText(String.valueOf(ratingByBuyer / totalReview));
                    } else {
                        ((TextView) findViewById(R.id.sellerRating)).setText(String.valueOf(0));

                    }

                    ((TextView) findViewById(R.id.reviewCount)).setText(String.valueOf(totalReview));

                    if (click) {
                        viewSellerReviewDetails(body);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<SellerReviewDTO>> call, Throwable throwable) {
                new SweetAlertDialogCustomize().errorAlert(getContext(), "no found seller review - " + throwable.getMessage());
            }
        });
    }

    private void viewSellerReviewDetails(List<SellerReviewDTO> reviewDTOS) {
        SellerReviewListDialog reviewSellerDialog = new SellerReviewListDialog(context, reviewDTOS);
        reviewSellerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(context.getResources().getColor(transparent, context.getTheme())));
        reviewSellerDialog.setCancelable(true);
        reviewSellerDialog.getWindow().setGravity(Gravity.CENTER);
        reviewSellerDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        reviewSellerDialog.show();
    }


    //! Other
    private void setValuesToFields(PopularFood popularFood) {
        if (popularFood != null) {
            ((TextView) findViewById(R.id.productTitle)).setText(popularFood.getTitle());
            ((TextView) findViewById(R.id.food_price)).setText(context.getString(R.string.price_unit).concat(" ").concat(String.valueOf(BigDecimal.valueOf(popularFood.getPrice()).setScale(2, RoundingMode.HALF_UP).doubleValue())));
            ((TextView) findViewById(R.id.sellerRating)).setText(String.valueOf(popularFood.getRatingScore()));
            ((TextView) findViewById(R.id.reviewCount)).setText(String.valueOf(popularFood.getReviewCount()));
            ((TextView) findViewById(R.id.productDescription)).setText(popularFood.getDescription());
            ((TextView) findViewById(R.id.pQty)).setText(String.valueOf(popularFood.getQty()));

            loadImageFromServer(popularFood.getProductImageList());
        }
    }

    private void loadImageFromServer(List<ProductImageDTO> productImageList) {
        ArrayList<CarouselModel> carouselModels = new ArrayList<>();
        String[] split = productImageList.get(0).getPath().split("/");

        String newPath = split[0] + "/" + split[1];
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storage.getReference("/product-image/" + newPath)
                .listAll().addOnCompleteListener(task -> {
                    for (StorageReference item : task.getResult().getItems()) {
                        item.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    carouselModels.add(new CarouselModel(uri));
                                    if (task.getResult().getItems().size() == carouselModels.size()) {
                                        setImageToCarousel(carouselModels);
                                    }
                                });
                    }
                });
    }

    private void setImageToCarousel(ArrayList<CarouselModel> productImageList) {
        RecyclerView recyclerView = findViewById(R.id.carousel_recycler_view);
        CarouselAdapter carouselAdapter = new CarouselAdapter(context, productImageList);
        recyclerView.setLayoutManager(new CarouselLayoutManager(new HeroCarouselStrategy(), CarouselLayoutManager.HORIZONTAL));
        recyclerView.setAdapter(carouselAdapter);
    }

    private void backButton() {
        ((ImageView) findViewById(R.id.imageView)).setOnClickListener(v -> {
            dismiss();
        });
    }

}