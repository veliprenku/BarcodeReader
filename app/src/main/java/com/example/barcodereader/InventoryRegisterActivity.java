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
        btnNewDocument.setOnClickListener(v -> createNewDocument());

        Button btnOpenDocument = findViewById(R.id.btnOpenDocument);
        btnOpenDocument.setOnClickListener(v -> openDocument());

        Button btnSaveToDatabase = findViewById(R.id.btnSaveToDatabase);
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

    private void createNewDocument() {
        String reference = editTextDocumentRef.getText().toString();
        String comment = editTextDocumentComment.getText().toString();
        if (!reference.isEmpty()) {
            currentDocumentId = dbHelper.addDocument(reference, comment);
            editTextDocumentRef.setText("");
            editTextDocumentComment.setText("");
            itemList.clear();
            adapter.notifyDataSetChanged();
        }
    }

    private void openDocument() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select a Document");

        Cursor cursor = dbHelper.getAllDocuments();
        List<String> documents = new ArrayList<>();
        int indexDocId = cursor.getColumnIndex("DocumentID");
        int indexRef = cursor.getColumnIndex("Reference");

        if (indexDocId != -1 && indexRef != -1) {  // Check if columns exist
            while (cursor.moveToNext()) {
                long docId = cursor.getLong(indexDocId);
                String ref = cursor.getString(indexRef);
                documents.add(ref + " - ID: " + docId);
            }
        } else {
            // Log error or inform the user that necessary columns are missing
            Toast.makeText(this, "Error: Necessary columns are missing in the database.", Toast.LENGTH_LONG).show();
        }
        cursor.close();

        CharSequence[] items = documents.toArray(new CharSequence[0]);
        builder.setItems(items, (dialog, which) -> {
            String selection = documents.get(which);
            currentDocumentId = Long.parseLong(selection.split(" - ID: ")[1]);
            editTextDocumentRef.setText(selection.split(" - ID: ")[0]);
            itemList.clear();  // Clear current items as we are opening a new document context
            adapter.notifyDataSetChanged();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void saveDataToDatabase() {
        if (currentDocumentId == -1) {
            // Prompt user to create or select a document first
            new AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("Please create or open a document first.")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        for (String item : itemList) {
            String[] parts = item.split(" - ");
            if (parts.length == 2) {
                String barcode = parts[0];
                int quantity = Integer.parseInt(parts[1]);
                dbHelper.addBarcode(barcode, quantity, currentDocumentId);
            }
        }
        itemList.clear();
        adapter.notifyDataSetChanged();
        // Optionally show a success message or update the UI
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
