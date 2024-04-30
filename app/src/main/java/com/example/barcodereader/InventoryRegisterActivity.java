package com.example.barcodereader;

import android.content.Context;
import android.database.Cursor;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InventoryRegisterActivity extends AppCompatActivity {

    private ListView listViewItems;
    private ArrayList<String> itemList;
    private CustomListAdapter adapter;
    private EditText editTextDocumentRef, editTextDocumentComment, editTextBarcode, editTextQuantity;
    private long currentDocumentId = -1;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory_register);

        listViewItems = findViewById(R.id.listViewItems);
        editTextDocumentRef = findViewById(R.id.editTextDocumentRef);
        editTextDocumentComment = findViewById(R.id.editTextDocumentComment);
        editTextBarcode = findViewById(R.id.editTextBarcode);
        editTextQuantity = findViewById(R.id.editTextQuantity);
        dbHelper = new DatabaseHelper(this);

        itemList = new ArrayList<>();
        adapter = new CustomListAdapter(this, itemList);
        listViewItems.setAdapter(adapter);

        setupEditTexts();

        Button btnNewDocument = findViewById(R.id.btnNewDocument);
        Button btnOpenDocument = findViewById(R.id.btnOpenDocument);
        Button btnSaveToDatabase = findViewById(R.id.btnSaveToDatabase);

        btnNewDocument.setOnClickListener(v -> {
            // Pastro elementët e UI dhe rivendos currentDocumentId në -1
            editTextDocumentRef.setText("");
            editTextDocumentComment.setText("");
            editTextBarcode.setText("");
            editTextQuantity.setText("");
            editTextQuantity.setVisibility(View.GONE);
            itemList.clear();
            adapter.notifyDataSetChanged();
            currentDocumentId = -1;
        });

        btnOpenDocument.setOnClickListener(v -> openDocument());
        btnSaveToDatabase.setOnClickListener(v -> saveDataToDatabase());
    }

    private void setupEditTexts() {
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
                return false;
            }
            return false;
        });
    }

    private void openDocument() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Document");

        Cursor cursor = dbHelper.getAllDocuments();
        List<String> documents = new ArrayList<>();
        List<Long> documentIds = new ArrayList<>(); // Store document IDs

        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOCUMENT_ID);
        int refIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_REFERENCE);
        int commentIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COMMENT);

        while (cursor.moveToNext()) {
            if (idIndex != -1 && refIndex != -1 && commentIndex != -1) {
                long docId = cursor.getLong(idIndex);
                String ref = cursor.getString(refIndex);
                String comment = cursor.getString(commentIndex);
                documents.add(ref + " - ID: " + docId);
                documentIds.add(docId); // Add document ID to the list
            }
        }
        cursor.close();

        CharSequence[] items = documents.toArray(new CharSequence[documents.size()]);
        builder.setItems(items, (dialog, which) -> {
            // Get the document ID from the list using the selected index
            long selectedDocumentId = documentIds.get(which);
            // Fetch document details using the selected document ID
            InventoryDocument selectedDocument = dbHelper.getDocumentById(selectedDocumentId);
            // Update UI with the selected document details
            currentDocumentId = selectedDocument.id;
            editTextDocumentRef.setText(selectedDocument.reference);
            editTextDocumentComment.setText(selectedDocument.comment);

            // Load barcodes associated with the selected document
            loadItemsForDocument(selectedDocumentId);

            // Create a custom layout for the dialog
            LayoutInflater inflater = getLayoutInflater();
            View dialogView = inflater.inflate(R.layout.dialog_custom_layout, null);

            // Find the delete button in the custom layout
            Button btnDeleteDocument = dialogView.findViewById(R.id.btnDeleteDocument);
            btnDeleteDocument.setOnClickListener(view -> {
                // Call a method to delete the document and its associated barcodes
                deleteDocument(selectedDocumentId);
                dialog.dismiss();
            });

            // Set the custom view to the dialog builder
            builder.setView(dialogView);

            // Create and show the dialog after setting the custom view
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        });

        // Create and show the dialog without a custom view
        AlertDialog dialog = builder.create();
        dialog.show();
    }




    private void loadItemsForDocument(long docId) {
        Cursor cursor = dbHelper.getBarcodesForDocument(docId);
        itemList.clear();
        int barcodeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BARCODE);
        int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY);

        while (cursor.moveToNext()) {
            if (barcodeIndex != -1 && quantityIndex != -1) {
                String barcode = cursor.getString(barcodeIndex);
                int quantity = cursor.getInt(quantityIndex);
                itemList.add(barcode + " - " + quantity);
            }
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private void saveDataToDatabase() {
        String reference = editTextDocumentRef.getText().toString();
        String comment = editTextDocumentComment.getText().toString();
        if (reference.isEmpty()) {
            Toast.makeText(this, "Shkruani një referencë për ta ruajtur.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentDocumentId == -1) {
            // If no current document is selected, create a new one
            currentDocumentId = dbHelper.addDocument(reference, comment);
            if (currentDocumentId != -1) {
                Toast.makeText(this, "U krijua dokument i ri me ID : " + currentDocumentId, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Gabim gjatë krijimit.", Toast.LENGTH_LONG).show();
                return;
            }
        } else {
            // If a current document is selected, update its reference and comment
            dbHelper.updateDocument(currentDocumentId, reference, comment);
        }

        // HashSet to store unique barcodes
        HashSet<String> uniqueBarcodes = new HashSet<>();

        // Save the data to the database
        for (String item : itemList) {
            String[] parts = item.split(" - ");
            if (parts.length == 2) {
                String barcode = parts[0];
                int quantity = Integer.parseInt(parts[1]);

                if (!uniqueBarcodes.contains(barcode)) {
                    // Add the barcode to the HashSet if it's not already present
                    uniqueBarcodes.add(barcode);

                    // Add the item to the list and database
                    itemList.add(barcode + " - " + quantity);
                    dbHelper.addBarcode(barcode, quantity, currentDocumentId);
                } else {
                    // Display a message that the barcode already exists
                    Toast.makeText(this, "Barcode " + barcode + " already exists.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        // Clear the list and notify adapter
        itemList.clear();
        adapter.notifyDataSetChanged();

        // Display success message
        Toast.makeText(this, "Të dhënat janë të ruajtura me sukses", Toast.LENGTH_SHORT).show();

        // Reset UI elements for a new document
        editTextDocumentRef.setText("");
        editTextDocumentComment.setText("");
        editTextBarcode.setText("");
        editTextQuantity.setText("");
        editTextQuantity.setVisibility(View.GONE);
        currentDocumentId = -1;
    }


    private void deleteDocument(long documentId) {
        // Delete the document and its associated barcodes from the database
        dbHelper.deleteDocument(documentId);

        // Refresh UI
        editTextDocumentRef.setText("");
        editTextDocumentComment.setText("");
        itemList.clear();
        adapter.notifyDataSetChanged();

        // Display success message
        Toast.makeText(this, "Dokumenti u fshi!", Toast.LENGTH_SHORT).show();
    }


    private class CustomListAdapter extends ArrayAdapter<String> {
        private ArrayList<String> itemList;

        public CustomListAdapter(Context context, ArrayList<String> itemList) {
            super(context, 0, itemList);
            this.itemList = itemList != null ? itemList : new ArrayList<>();
        }

        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item, parent, false);
            }

            String currentItem = itemList.get(position);
            String[] parts = currentItem.split(" - ");
            final String barcode = parts[0];
            String quantity = parts[1];

            TextView textViewBarcode = convertView.findViewById(R.id.textViewBarcode);
            textViewBarcode.setText(barcode);

            TextView textViewQuantity = convertView.findViewById(R.id.textViewQuantity);
            textViewQuantity.setText(quantity);


            textViewQuantity.setOnClickListener(v -> {
                // Create a dialog to edit the quantity
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Ndrysho sasinë");

                final EditText input = new EditText(getContext());
                input.setText(quantity);
                builder.setView(input);

                builder.setPositiveButton("Ruaj", (dialog, which) -> {
                    String newQuantity = input.getText().toString();
                    // Update the quantity in the list
                    String newItem = barcode + " - " + newQuantity;
                    itemList.set(position, newItem);
                    notifyDataSetChanged();

                    // Update the quantity in the database
                    dbHelper.updateBarcodeQuantity(barcode, Integer.parseInt(newQuantity), currentDocumentId);
                });

                builder.setNegativeButton("Anulo", (dialog, which) -> dialog.cancel());

                builder.show();
            });


            // Find the delete button
            Button btnDelete = convertView.findViewById(R.id.btnDelete);
            btnDelete.setVisibility(View.VISIBLE); // Show the delete button
            btnDelete.setOnClickListener(v -> {
                // Remove the item from the list
                itemList.remove(position);
                notifyDataSetChanged();

                // Remove the item from the database
                dbHelper.deleteBarcode(barcode, currentDocumentId);
            });

            return convertView;
        }



    }




}

