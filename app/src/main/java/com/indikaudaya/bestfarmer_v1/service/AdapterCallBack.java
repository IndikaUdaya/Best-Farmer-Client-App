package com.indikaudaya.bestfarmer_v1.service;

import android.app.Dialog;

import com.indikaudaya.bestfarmer_v1.model.InvoiceModel;

import lk.payhere.androidsdk.model.InitRequest;

public interface AdapterCallBack {
    void onDataItemClicked(InitRequest req, int uniqueId, InvoiceModel invoiceModel, Dialog dialog);
}
