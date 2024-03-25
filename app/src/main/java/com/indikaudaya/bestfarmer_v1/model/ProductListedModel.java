package com.indikaudaya.bestfarmer_v1.model;


public class ProductListedModel {
    private long pid;
    private PopularFood popularFood;

    public ProductListedModel(long productId, PopularFood popularFood) {
        this.pid = productId;
        this.popularFood = popularFood;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public PopularFood getPopularFood() {
        return popularFood;
    }

    public void setPopularFood(PopularFood popularFood) {
        this.popularFood = popularFood;
    }
}
