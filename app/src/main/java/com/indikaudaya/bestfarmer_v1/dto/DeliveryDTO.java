package com.indikaudaya.bestfarmer_v1.dto;

import java.time.LocalDateTime;

public class DeliveryDTO {

    private Long id;

    private String address;
    private LocalDateTime deliveryDate;
    private double deliveryCostPerKm;

    private ProductDTO product;

    private UserDTO buyer;

    private UserDTO seller;

    public DeliveryDTO() {
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(LocalDateTime deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public double getDeliveryCostPerKm() {
        return deliveryCostPerKm;
    }

    public void setDeliveryCostPerKm(double deliveryCostPerKm) {
        this.deliveryCostPerKm = deliveryCostPerKm;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    public UserDTO getBuyer() {
        return buyer;
    }

    public void setBuyer(UserDTO buyer) {
        this.buyer = buyer;
    }

    public UserDTO getSeller() {
        return seller;
    }

    public void setSeller(UserDTO seller) {
        this.seller = seller;
    }
}
