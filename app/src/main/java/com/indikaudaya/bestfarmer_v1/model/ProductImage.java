package com.indikaudaya.bestfarmer_v1.model;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class ProductImage implements Serializable {
    private String path;
    private Uri uri;

    public ProductImage() {
    }

    public ProductImage(String path, Uri uri) {
        this.path = path;
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public ProductImage(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @NonNull
    @Override
    public String toString() {
        return "ProductImage{" +
                "path='" + path + '\'' +
                ", uri=" + uri +
                '}';
    }
}
