package com.example.barcodereader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
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
import java.util.List;

public class InventoryRegisterActivity extends AppCompatActivity {

    private ListView listViewItems;
    private ArrayList<String> itemList;
    private CustomListAdapter adapter;
    private EditText editTextDocumentRef, editTextDocumentComment, editTextBarcode, editTextQuantity;
    private long currentDocumentId = -1;
    private AlertDialog alertDialog;
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

        btnNewDocument.setOnClickListener(v -> resetUI());
        btnOpenDocument.setOnClickListener(v -> openDocument());
        btnSaveToDatabase.setOnClickListener(v -> saveDataToDatabase());
    }

    private void setupEditTexts() {
        // Set up the barcode EditText.
        editTextBarcode.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (editTextQuantity.getVisibility() != View.VISIBLE) {
                    editTextQuantity.setVisibility(View.VISIBLE);
                }
                editTextQuantity.postDelayed(() -> {
                    editTextQuantity.requestFocus(); // Delayed focus to ensure it sticks
                }, 100); // Adjust delay as necessary
                return true;
            }
            return false;
        });

        // Set up the quantity EditText.
        editTextQuantity.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                String barcode = editTextBarcode.getText().toString().trim();
                String quantityStr = editTextQuantity.getText().toString().trim();
                if (!barcode.isEmpty() && !quantityStr.isEmpty()) {
                    try {
                        int quantity = Integer.parseInt(quantityStr);
                        updateItemList(barcode, quantity); // Process the data
                        adapter.notifyDataSetChanged(); // Notify the adapter for data changes
                        editTextBarcode.setText("");
                        editTextQuantity.setText("");
                        editTextQuantity.setVisibility(View.GONE);

                        editTextBarcode.post(() -> {
                            editTextBarcode.requestFocus(); // Focus back on barcode input immediately
                        });
                    } catch (NumberFormatException e) {
                        Log.e("SetupEditTexts", "Error parsing quantity", e);
                    }
                    return true; // Handle the key event
                }
                return false; // Do not intercept the key event
            }
            return false; // Do not intercept the key event
        });
    }



    private void updateItemList(String barcode, int additionalQuantity) {
        boolean found = false;
        for (int i = 0; i < itemList.size(); i++) {
            String item = itemList.get(i);
            String[] parts = item.split(" - ");
            if (parts[0].equals(barcode)) {
                int currentQuantity = Integer.parseInt(parts[1]);
                currentQuantity += additionalQuantity;
                itemList.set(i, barcode + " - " + currentQuantity);
                found = true;
                break;
            }
        }
        if (!found) {
            itemList.add(barcode + " - " + additionalQuantity);
        }
    }


    private void openDocument() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_custom_layout, null);
        builder.setView(dialogView);

        ListView listViewDocuments = dialogView.findViewById(R.id.listViewDocuments);
        Button btnDeleteDocument = dialogView.findViewById(R.id.btnDeleteDocument);

        // Disable the delete button initially
        btnDeleteDocument.setEnabled(false);

        Cursor cursor = dbHelper.getAllDocuments();
        List<String> documents = new ArrayList<>();
        List<Long> documentIds = new ArrayList<>();

        int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOCUMENT_ID);
        int refIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_REFERENCE);
        int commentIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_COMMENT);

        while (cursor.moveToNext()) {
            long docId = cursor.getLong(idIndex);
            String ref = cursor.getString(refIndex);
            String comment = cursor.getString(commentIndex);
            documents.add(ref + " - ID: " + docId);
            documentIds.add(docId);
        }
        cursor.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, documents);
        listViewDocuments.setAdapter(adapter);

        listViewDocuments.setOnItemClickListener((parent, view, position, id) -> {
            long selectedDocumentId = documentIds.get(position);
            InventoryDocument selectedDocument = dbHelper.getDocumentById(selectedDocumentId);
            currentDocumentId = selectedDocument.id;
            editTextDocumentRef.setText(selectedDocument.reference);
            editTextDocumentComment.setText(selectedDocument.comment);
            loadItemsForDocument(selectedDocumentId);

            // Enable the delete button when a document is selected
            btnDeleteDocument.setEnabled(true);
        });

        btnDeleteDocument.setOnClickListener(view -> {
            if (currentDocumentId != 0) { // Verifikimi i vlerës së currentDocumentId
                deleteDocument(currentDocumentId);
            }
            alertDialog.dismiss(); // Mbyllja e AlertDialog
        });

        alertDialog = builder.create(); // Inicializimi i AlertDialog në nivel global
        alertDialog.show();
    }







    private void loadItemsForDocument(long docId) {
        Cursor cursor = dbHelper.getBarcodesForDocument(docId);
        itemList.clear();
        int barcodeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BARCODE);
        int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY);

        while (cursor.moveToNext()) {
            String barcode = cursor.getString(barcodeIndex);
            int quantity = cursor.getInt(quantityIndex);
            itemList.add(barcode + " - " + quantity);
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }

    private boolean barcodeExists(SQLiteDatabase db, String barcode, long documentId) {
        String[] columns = {DatabaseHelper.COLUMN_ID};
        String selection = DatabaseHelper.COLUMN_BARCODE + " = ? AND " + DatabaseHelper.COLUMN_DOCUMENT_ID + " = ?";
        String[] selectionArgs = {barcode, String.valueOf(documentId)};
        Cursor cursor = db.query(DatabaseHelper.TABLE_BARCODES, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    private void saveDataToDatabase() {
        String reference = editTextDocumentRef.getText().toString();
        String comment = editTextDocumentComment.getText().toString();
        if (reference.isEmpty()) {
            Toast.makeText(this, "Ju lutem shkruani një referencë për të ruajtur.", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try {
            db.beginTransaction();
            Log.d("Database", "Starting transaction...");

            // Manage document entry or update
            manageDocumentEntry(db, reference, comment);

            // Process each barcode entry
            processBarcodeEntries(db);

            db.setTransactionSuccessful();
            Toast.makeText(this, "Të dhënat u ruajtën me sukses", Toast.LENGTH_SHORT).show();
            Log.d("Database", "Transaction successful");

            resetUI();
        } catch (Exception e) {
            Log.e("Database", "Gabim në ruajtjen e të dhënave", e);
            Toast.makeText(this, "Gabim në ruajtjen e të dhënave", Toast.LENGTH_SHORT).show();
        } finally {
            db.endTransaction();
            db.close();
            Log.d("Database", "Transaction ended");
        }

    }

    private void manageDocumentEntry(SQLiteDatabase db, String reference, String comment) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_REFERENCE, reference);
        values.put(DatabaseHelper.COLUMN_COMMENT, comment);
        if (currentDocumentId == -1) {
            currentDocumentId = db.insert(DatabaseHelper.TABLE_DOCUMENTS, null, values);
            if (currentDocumentId == -1) {
                Log.e("Database", "Error inserting new document");
                throw new IllegalStateException("Error creating new document");
            }
        } else {
            int rows = db.update(DatabaseHelper.TABLE_DOCUMENTS, values, DatabaseHelper.COLUMN_DOCUMENT_ID + " = ?", new String[]{String.valueOf(currentDocumentId)});
            if (rows < 1) {
                Log.e("Database", "Error updating document");
                throw new IllegalStateException("Error updating document");
            }
        }
    }

    private void processBarcodeEntries(SQLiteDatabase db) {
        for (String item : itemList) {
            String[] parts = item.split(" - ");
            if (parts.length == 2) {
                String barcode = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                if (!updateBarcodeIfExists(db, barcode, quantity)) {
                    addNewBarcode(db, barcode, quantity);
                }
            }
        }
    }

    private boolean updateBarcodeIfExists(SQLiteDatabase db, String barcode, int quantity) {
        Cursor cursor = dbHelper.getBarcodesForDocument(currentDocumentId);
        boolean barcodeExists = false;

        int barcodeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BARCODE);
        int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY);

        if (barcodeIndex == -1 || quantityIndex == -1) {
            Log.e("Database", "Necessary column index not found");
            cursor.close();
            return false; // or throw an exception if you prefer
        }

        while (cursor.moveToNext()) {
            if (barcode.equals(cursor.getString(barcodeIndex))) {
                int existingQuantity = cursor.getInt(quantityIndex);
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_QUANTITY, existingQuantity + quantity);
                int rows = db.update(DatabaseHelper.TABLE_BARCODES, values, DatabaseHelper.COLUMN_BARCODE + " = ? AND " + DatabaseHelper.COLUMN_DOCUMENT_ID + " = ?", new String[]{barcode, String.valueOf(currentDocumentId)});
                if (rows < 1) {
                    Log.e("Database", "Error updating barcode quantity");
                    throw new IllegalStateException("Error updating barcode quantity");
                }
                barcodeExists = true;
                break;
            }
        }
        cursor.close();
        return barcodeExists;
    }


    private void addNewBarcode(SQLiteDatabase db, String barcode, int quantity) {
        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COLUMN_BARCODE, barcode);
        values.put(DatabaseHelper.COLUMN_QUANTITY, quantity);
        values.put(DatabaseHelper.COLUMN_DOCUMENT_ID, currentDocumentId);
        long rowId = db.insert(DatabaseHelper.TABLE_BARCODES, null, values);
        if (rowId == -1) {
            Log.e("Database", "Error inserting new barcode");
            throw new IllegalStateException("Error inserting new barcode");
        }
    }



    private void resetUI() {
        Log.d("UIReset", "Resetting UI components now");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                itemList.clear();
                editTextDocumentRef.setText("");
                editTextDocumentComment.setText("");
                editTextBarcode.setText("");
                editTextQuantity.setText("");
                editTextQuantity.setVisibility(View.GONE);
                currentDocumentId = -1;
            }
        });
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

