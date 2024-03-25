package com.indikaudaya.bestfarmer_v1.dto;

import java.io.Serializable;
import java.util.List;

public class ProductImageDTO implements Serializable {
    private String path;

    public ProductImageDTO() {
    }

    public ProductImageDTO(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
