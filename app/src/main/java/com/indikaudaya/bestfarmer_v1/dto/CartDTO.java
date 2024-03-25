package com.indikaudaya.bestfarmer_v1.dto;

import java.io.Serializable;

public class CartDTO implements Serializable {

    private Long id;
    private ProductDTO productDTO;
    private UserDTO userDTO;

    public CartDTO() {
    }

    public CartDTO(long id, ProductDTO products, UserDTO user) {
        this.id = id;
        this.productDTO = products;
        this.userDTO = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        id = id;
    }

    public ProductDTO getProducts() {
        return productDTO;
    }

    public void setProducts(ProductDTO products) {
        this.productDTO = products;
    }

    public UserDTO getUser() {
        return userDTO;
    }

    public void setUser(UserDTO user) {
        this.userDTO = user;
    }

    @Override
    public String toString() {
        return "CartDTO{" +
                "Id=" + id +
                ", products=" + productDTO +
                ", user=" + userDTO +
                '}';
    }
}
