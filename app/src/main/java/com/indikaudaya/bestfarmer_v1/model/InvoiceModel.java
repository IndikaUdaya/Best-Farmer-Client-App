package com.indikaudaya.bestfarmer_v1.model;

import com.indikaudaya.bestfarmer_v1.dto.ProductOrderItemDTO;
import com.indikaudaya.bestfarmer_v1.dto.UserDTO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class InvoiceModel implements Serializable {

    private String invoiceNumber;
    private UserDTO userDTO;

    private double amount;

    ArrayList<ProductOrderItemDTO> productList;

    public InvoiceModel(String invoiceNumber, UserDTO userDTO, double amount, ArrayList<ProductOrderItemDTO> productList) {
        this.invoiceNumber = invoiceNumber;
        this.userDTO = userDTO;
        this.amount = amount;
        this.productList=productList;
    }

    public ArrayList<ProductOrderItemDTO> getProductList() {
        return productList;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setProductList(ArrayList<ProductOrderItemDTO> productList) {
        this.productList = productList;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}
