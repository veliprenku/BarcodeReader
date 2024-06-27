package com.example.barcodereader;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BarcodeAdapter extends RecyclerView.Adapter<BarcodeAdapter.BarcodeViewHolder> {

    private List<BarcodeItem> barcodeItems;

    public BarcodeAdapter(List<BarcodeItem> barcodeItems) {
        this.barcodeItems = barcodeItems;
    }

    @NonNull
    @Override
    public BarcodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_barcode, parent, false);
        return new BarcodeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BarcodeViewHolder holder, int position) {
        BarcodeItem item = barcodeItems.get(position);
        holder.barcodeTextView.setText(item.getBarcode());
        holder.quantityTextView.setText(String.valueOf(item.getQuantity()));
    }

    @Override
    public int getItemCount() {
        return barcodeItems.size();
    }

    static class BarcodeViewHolder extends RecyclerView.ViewHolder {
        TextView barcodeTextView;
        TextView quantityTextView;

        BarcodeViewHolder(@NonNull View itemView) {
            super(itemView);
            barcodeTextView = itemView.findViewById(R.id.barcodeTextView);
            quantityTextView = itemView.findViewById(R.id.quantityTextView);
        }
    }
}
