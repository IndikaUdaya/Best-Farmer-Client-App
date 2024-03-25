package com.indikaudaya.bestfarmer_v1.model;


public class CartAdapterModel {

    private long cartId;
    private PopularFood popularFood;


    public CartAdapterModel(long cartId, PopularFood popularFood) {
        this.cartId = cartId;
        this.popularFood = popularFood;
    }

    public long getCartId() {
        return cartId;
    }

    public void setCartId(long cartId) {
        this.cartId = cartId;
    }

    public PopularFood getPopularFood() {
        return popularFood;
    }

    public void setPopularFood(PopularFood popularFood) {
        this.popularFood = popularFood;
    }

}
