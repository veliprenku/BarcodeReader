package com.example.barcodereader;

public class BarcodeItem {
    private String barcode;
    private int quantity;

    public BarcodeItem(String barcode, int quantity) {
        this.barcode = barcode;
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getQuantity() {
        return quantity;
    }
}
