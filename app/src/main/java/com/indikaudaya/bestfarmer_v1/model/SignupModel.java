package com.indikaudaya.bestfarmer_v1.model;

import java.io.Serializable;
import java.util.Map;

public class SignupModel implements Serializable {

    private String email;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private String password;
    private Map<String, Double> shopLocation;
    private String shopName;
    private String shopAddress;
    private boolean status;

    public SignupModel(String email, String firstName, String lastName, String mobileNumber, String password, Map<String, Double> shopLocation, String shopName, String shopAddress, boolean status) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
        this.password = password;
        this.shopLocation = shopLocation;
        this.shopName = shopName;
        this.shopAddress = shopAddress;
        this.status = status;
    }

    public SignupModel(String email, String firstName, String lastName, String mobileNumber, String password, boolean status) {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.mobileNumber = mobileNumber;
        this.password = password;
        this.status = status;

    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getPassword() {
        return password;
    }

    public Map<String, Double> getShopLocation() {
        return shopLocation;
    }

    public String getShopName() {
        return shopName;
    }

    public String getShopAddress() {
        return shopAddress;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setShopLocation(Map<String, Double> shopLocation) {
        this.shopLocation = shopLocation;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public void setShopAddress(String shopAddress) {
        this.shopAddress = shopAddress;
    }
}
