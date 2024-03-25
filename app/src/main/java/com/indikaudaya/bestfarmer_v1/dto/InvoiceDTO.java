package com.indikaudaya.bestfarmer_v1.dto;

import java.io.Serializable;

public class InvoiceDTO implements Serializable {

    private Long id;

    private String invoiceNumber;

    private OrderDTO order;

    public InvoiceDTO(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public OrderDTO getOrder() {
        return order;
    }

    public void setOrder(OrderDTO order) {
        this.order = order;
    }
}
