package com.indikaudaya.bestfarmer_v1.model;


public class BuyerHistoryModel {

    private long productId;
    private long orderId;
    private PopularFood popularFood;
    private double orderQty;
    private String orderDate;


    public BuyerHistoryModel(long orderId, long productId, double orderQty, String orderDate, PopularFood popularFood) {
        this.orderId = orderId;
        this.productId = productId;
        this.orderQty = orderQty;
        this.orderDate = orderDate;
        this.popularFood = popularFood;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public double getOrderQty() {
        return orderQty;
    }

    public void setOrderQty(double orderQty) {
        this.orderQty = orderQty;
    }

    public long getProductId() {
        return productId;
    }

    public void setProductId(long productId) {
        this.productId = productId;
    }

    public PopularFood getPopularFood() {
        return popularFood;
    }

    public void setPopularFood(PopularFood popularFood) {
        this.popularFood = popularFood;
    }

}
