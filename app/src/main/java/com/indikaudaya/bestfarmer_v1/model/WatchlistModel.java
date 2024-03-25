package com.indikaudaya.bestfarmer_v1.model;


public class WatchlistModel {

    private long watchlistId;
    private PopularFood popularFood;

    public WatchlistModel(long watchlistId, PopularFood popularFood) {
        this.watchlistId = watchlistId;
        this.popularFood = popularFood;
    }

    public long getWatchlistId() {
        return watchlistId;
    }

    public void setWatchlistId(long watchlistId) {
        this.watchlistId = watchlistId;
    }

    public PopularFood getPopularFood() {
        return popularFood;
    }

    public void setPopularFood(PopularFood popularFood) {
        this.popularFood = popularFood;
    }
}
