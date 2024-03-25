package com.indikaudaya.bestfarmer_v1.dto;

import java.io.Serializable;

public class WatchlistDTO implements Serializable {

    private long id;

    private UserDTO user;

    private ProductDTO products;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    public ProductDTO getProducts() {
        return products;
    }

    public void setProducts(ProductDTO products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return "WatchlistDTO{" +
                "id=" + id +
                ", user=" + user +
                ", products=" + products +
                '}';
    }
}
