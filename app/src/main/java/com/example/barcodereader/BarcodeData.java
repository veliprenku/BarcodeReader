package com.example.barcodereader;

public class BarcodeData {
    private String barcode;
    private int quantity;
    private int color; // This holds the color associated with the barcode in the chart

    public BarcodeData(String barcode, int quantity, int color) {
        this.barcode = barcode;
        this.quantity = quantity;
        this.color = color;
    }

    public String getBarcode() {
        return barcode;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getColor() {
        return color;
    }
}
