package com.indikaudaya.bestfarmer_v1.service;

import com.google.gson.JsonObject;
import com.indikaudaya.bestfarmer_v1.dto.AuthRequestDTO;
import com.indikaudaya.bestfarmer_v1.dto.CartDTO;
import com.indikaudaya.bestfarmer_v1.dto.CategoryDTO;
import com.indikaudaya.bestfarmer_v1.dto.DeliveryDTO;
import com.indikaudaya.bestfarmer_v1.dto.OrderDTO;
import com.indikaudaya.bestfarmer_v1.dto.ProductDTO;
import com.indikaudaya.bestfarmer_v1.dto.SignupDTO;
import com.indikaudaya.bestfarmer_v1.dto.SellerReviewDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;
import com.indikaudaya.bestfarmer_v1.dto.WatchlistDTO;


import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface BestFarmerApiService {

    @POST("authenticate")
//    Call<AuthResponse> auth(@Body AuthRequest authRequest);
    Call<JsonObject> auth(@Body AuthRequestDTO authRequest);

    @POST("sign-up")
    Call<JsonObject> signUp(@Body SignupDTO authRequest);

    @GET("api/user/{email}")
    Call<UserDTO> getUserDetail(@Path("email") String email);

    //! Product
    @GET("api/products")
    Call<List<ProductDTO>> getAllProducts();

    @GET("default/product/{cateName}/{searchWord}")
    Call<List<ProductDTO>> getAllProductsBySearching(@Path("cateName") String cateName, @Path("searchWord") String searchWord);

    @GET("default/product/search-word/{searchWord}")
    Call<List<ProductDTO>> getAllProductsByProductName(@Path("searchWord") String searchWord);

    @GET("default/product/category-name/{cateName}")
    Call<List<ProductDTO>> getAllProductsByCategoryName(@Path("cateName") String cateName);

    @GET("default/product")
    Call<List<ProductDTO>> getAllProductsNotSigning();

    @GET("default/product/{product-id}")
    Call<ProductDTO> getProductByIdNotSigning(@Path("product-id") long id);

    @GET("api/products/{id}")
    Call<ProductDTO> getProductById(@Path("id") long id);

    @GET("api/products/seller/{id}")
    Call<List<ProductDTO>> getProductsBySellerId(@Path("id") long id);

    @POST("api/products")
    Call<ProductDTO> saveProduct(@Body ProductDTO product);

    @PUT("api/products/{id}")
    Call<ProductDTO> updateProduct(@Path("id") long id, @Body ProductDTO productDTO);

    @DELETE("api/products/{id}")
    void deleteProductById(@Path("id") int id);

    //! Category
    @GET("api/categories")
    Call<List<CategoryDTO>> getAllCategories();

    @GET("default/category")
    Call<List<CategoryDTO>> getAllCategorySigning();

    @GET("api/categories/category-name/{name}")
    Call<CategoryDTO> getCategoryByName(@Path("name") String name);

    //! Cart
    @GET("api/cart")
    Call<List<CartDTO>> getAllCarts(@Header("Authorization") String token);

    @GET("api/cart/{id}")
    Call<CartDTO> getCartById(@Path("id") int id);

    @GET("api/cart/user/{uid}")
    Call<List<CartDTO>> getCartByUserId(@Path("uid") long uid);

    @POST("api/cart")
    Call<CartDTO> saveOrUpdateCart(@Body CartDTO cart);

    @GET("api/cart/{uid}/{pid}")
    Call<CartDTO> getCartByproductIdAndUserId(@Path("uid") long uid, @Path("pid") long pid);

    @DELETE("api/cart/{id}")
    Call<Boolean> deleteCartById(@Path("id") long id);

    //! Delivery
    @GET("delivery")
    Call<List<DeliveryDTO>> getAllDelivery(@Header("Authorization") String token);

    @GET("delivery/{id}")
    Call<DeliveryDTO> getDeliveryById(@Path("id") int id, @Header("Authorization") String token);

    @POST("delivery")
    Call<DeliveryDTO> saveDelivery(@Body DeliveryDTO delivery, @Header("Authorization") String token);

//    @DELETE("delivery/{id}")
//    void deleteDeliveryById(@Path("id") int id, @Header("Authorization") String token);

    //! Seller Review
    @GET("api/seller-review")
    Call<List<SellerReviewDTO>> getAllSellerReview();

    @GET("api/seller-review/{id}")
    Call<List<SellerReviewDTO>> getSellerReviewById(@Path("id") long id);

    @GET("api/seller-review/review-all/{id}")
    Call<List<SellerReviewDTO>> getAllSellerReviewById(@Path("id") long id);

    @GET("api/seller-review/{uid}/{pid}")
    Call<SellerReviewDTO> getSellerReviewByProductIdAndBuyerId(@Path("uid") long uid,@Path("pid") long pid);

    @POST("api/seller-review")
    Call<SellerReviewDTO> saveSellerReview(@Body SellerReviewDTO sellerReview);


    //! Watchlist
    @GET("api/watchlist")
    Call<List<WatchlistDTO>> getAllWatchlist(@Header("Authorization") String token);

    @GET("api/watchlist/{uid}/{pid}")
    Call<WatchlistDTO> getWatchlistByproductIdAndUserId(@Path("uid") long uid, @Path("pid") long pid);

    @GET("api/watchlist/user/{uid}")
    Call<List<WatchlistDTO>> getWatchlistByUserId(@Path("uid") long uid);

    @POST("api/watchlist")
    Call<WatchlistDTO> saveWatchlist(@Body WatchlistDTO watchlist);

    @DELETE("api/watchlist/{id}")
    Call<Boolean> deleteWatchlistById(@Path("id") long id);

    //! Order
    @POST("api/order")
    Call<OrderDTO> saveOrder(@Body OrderDTO orderDTO);

    @PUT("api/order/{id}")
    Call<OrderDTO> updateOrderStatus(@Path("id") long oid, @Body OrderDTO orderDTO);

    @GET("api/order/buyer/{id}")
    Call<List<ProductDTO>> getProductOrderByBuyerId(@Path("id") long id);

    //!ProductOrderItem
    @GET("api/product-order/all/{id}")
    Call<List<ProductDTO>> getProductOrderItems(@Path("id") long id);


}
