package com.indikaudaya.bestfarmer_v1.dto;

import com.indikaudaya.bestfarmer_v1.model.PopularFood;

import java.io.Serializable;

public class ProductOrderItemDTO implements Serializable {

    private Long id;

    private long productId;

    private OrderDTO order;

    private PopularFood product;

    private double quantity;

    public ProductOrderItemDTO(long productId, double quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public OrderDTO getOrder() {
        return order;
    }

    public void setOrder(OrderDTO order) {
        this.order = order;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
