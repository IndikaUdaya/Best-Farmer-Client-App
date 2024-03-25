package com.indikaudaya.bestfarmer_v1.model;

import java.io.Serializable;

public class CategoryModel implements Serializable {
    private long id;
    private String cateName;

    public CategoryModel(long id, String cateName) {
        this.id = id;
        this.cateName = cateName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getCateName() {
        return cateName;
    }

    public void setCateName(String cateName) {
        this.cateName = cateName;
    }
}
