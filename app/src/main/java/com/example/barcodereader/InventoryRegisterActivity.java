package com.example.barcodereader;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class InventoryRegisterActivity extends AppCompatActivity {

    private ListView listViewItems;
    private ArrayList<String> itemList;
    private CustomListAdapter adapter;
    private View headerLayout;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_register);

        listViewItems = findViewById(R.id.listViewItems);
        headerLayout = findViewById(R.id.headerLayout);
        dbHelper = new DatabaseHelper(this);

        itemList = new ArrayList<>();
        adapter = new CustomListAdapter(this, itemList);
        listViewItems.setAdapter(adapter);

        setupEditTexts();

        listViewItems.setOnItemClickListener((parent, view, position, id) -> editQuantityForItem(position));  // Set item click listener for editing quantity

        Button btnSaveToDatabase = findViewById(R.id.btnSaveToDatabase);
        btnSaveToDatabase.setOnClickListener(v -> saveDataToDatabase());

        headerLayout.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupEditTexts() {
        EditText editTextBarcode = findViewById(R.id.editTextBarcode);
        EditText editTextQuantity = findViewById(R.id.editTextQuantity);

        editTextBarcode.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                editTextQuantity.setVisibility(View.VISIBLE);
                editTextQuantity.requestFocus();
                return true;
            }
            return false;
        });

        editTextQuantity.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                String barcode = editTextBarcode.getText().toString();
                String quantity = editTextQuantity.getText().toString();
                if (!barcode.isEmpty() && !quantity.isEmpty()) {
                    itemList.add(barcode + " - " + quantity);
                    adapter.notifyDataSetChanged();
                    editTextBarcode.setText("");
                    editTextQuantity.setText("");
                    editTextQuantity.setVisibility(View.GONE);
                    editTextBarcode.requestFocus();
                    return true;
                }
                return true;
            }
            return false;
        });
    }

    private void saveDataToDatabase() {
        for (String item : itemList) {
            String[] parts = item.split(" - ");
            if (parts.length == 2) {
                String barcode = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                dbHelper.addBarcode(barcode, quantity);
            }
        }
        itemList.clear();
        adapter.notifyDataSetChanged();
    }

    private void editQuantityForItem(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Quantity");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String newQuantityStr = input.getText().toString();
            if (!newQuantityStr.isEmpty()) {
                int newQuantity = Integer.parseInt(newQuantityStr);
                String item = itemList.get(position);
                String[] parts = item.split(" - ");
                String updatedItem = parts[0] + " - " + newQuantity;
                itemList.set(position, updatedItem);
                adapter.notifyDataSetChanged();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private class CustomListAdapter extends ArrayAdapter<String> {
        private ArrayList<String> itemList;

        public CustomListAdapter(Context context, ArrayList<String> itemList) {
            super(context, 0, itemList);
            this.itemList = itemList;
        }

        @NonNull
        @Override
        public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            String currentItem = itemList.get(position);
            String[] parts = currentItem.split(" - ");
            String barcode = parts[0];
            String quantity = parts[1];

            TextView textViewBarcode = convertView.findViewById(R.id.textViewBarcode);
            textViewBarcode.setText(barcode);

            TextView textViewQuantity = convertView.findViewById(R.id.textViewQuantity);
            textViewQuantity.setText(quantity);
            // Set an OnClickListener specifically for the quantity TextView
            textViewQuantity.setOnClickListener(v -> editQuantityForItem(position));

            Button btnDelete = convertView.findViewById(R.id.btnEditQuantity);
            btnDelete.setText("Delete");
            btnDelete.setOnClickListener(v -> {
                itemList.remove(position);
                notifyDataSetChanged();
            });

            return convertView;
        }
    }

}
