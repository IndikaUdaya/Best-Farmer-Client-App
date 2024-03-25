package com.indikaudaya.bestfarmer_v1.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class OrderDTO implements Serializable {

    private long orderId;
    private String orderDate;

    private boolean deliveryStatus;

    private UserDTO buyer;

    private InvoiceDTO invoice;

    private List<ProductOrderItemDTO> orderItems;

    public OrderDTO(boolean deliveryStatus, UserDTO buyer, InvoiceDTO invoice, ArrayList<ProductOrderItemDTO> orderItems) {
        this.deliveryStatus = deliveryStatus;
        this.buyer = buyer;
        this.invoice = invoice;
        this.orderItems = orderItems;
    }

    public OrderDTO() {
    }

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public boolean isDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(boolean deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public UserDTO getBuyer() {
        return buyer;
    }

    public void setBuyer(UserDTO buyer) {
        this.buyer = buyer;
    }

    public InvoiceDTO getInvoice() {
        return invoice;
    }

    public void setInvoice(InvoiceDTO invoice) {
        this.invoice = invoice;
    }

    public List<ProductOrderItemDTO> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<ProductOrderItemDTO> orderItems) {
        this.orderItems = orderItems;
    }
}
