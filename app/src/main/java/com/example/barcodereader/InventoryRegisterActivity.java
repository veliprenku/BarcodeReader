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
    private View headerLayout;  // Reference to the header layout
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_register);

        listViewItems = findViewById(R.id.listViewItems);
        headerLayout = findViewById(R.id.headerLayout);  // Initialize the header layout
        dbHelper = new DatabaseHelper(this);  // Initialize the DatabaseHelper

        itemList = new ArrayList<>();
        adapter = new CustomListAdapter(this, itemList);
        listViewItems.setAdapter(adapter);

        setupEditTexts(); // Setup EditTexts for barcode and quantity input

        Button btnSaveToDatabase = findViewById(R.id.btnSaveToDatabase);
        btnSaveToDatabase.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDataToDatabase();
            }
        });

        // Initially hide the header if the list is empty
        headerLayout.setVisibility(itemList.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void setupEditTexts() {
        EditText editTextBarcode = findViewById(R.id.editTextBarcode);
        EditText editTextQuantity = findViewById(R.id.editTextQuantity);

        editTextBarcode.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                editTextQuantity.setVisibility(View.VISIBLE);
                editTextQuantity.requestFocus();
                return true; // Event was handled
            }
            return false; // Event was not handled
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
                    return true; // Event was handled
                }
                return true; // Event was handled (even if fields are empty, the key event itself is considered handled)
            }
            return false; // Event was not handled
        });
    }


    private void saveDataToDatabase() {
        for (String item : itemList) {
            String[] parts = item.split(" - ");
            if (parts.length == 2) {
                String barcode = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                dbHelper.addBarcode(barcode, quantity);  // Save each item to the database
            }
        }
        itemList.clear();  // Optionally, clear the list after saving to database
        adapter.notifyDataSetChanged();
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

            Button btnEditQuantity = convertView.findViewById(R.id.btnEditQuantity);
            btnEditQuantity.setOnClickListener(v -> editQuantityForItem(position));

            return convertView;
        }

        private void editQuantityForItem(final int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Edit Quantity");

            final EditText input = new EditText(getContext());
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
                    notifyDataSetChanged();  // Notify the adapter to refresh the list
                }
            });
            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

            builder.show();
        }
    }
}
