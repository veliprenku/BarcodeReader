package com.example.barcodereader;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class Reports extends AppCompatActivity {

    private PieChart pieChart;
    private DatabaseHelper databaseHelper;
    private Spinner documentSpinner;
    private ArrayAdapter<String> documentAdapter;
    private List<Long> documentIds = new ArrayList<>();
    private RecyclerView recyclerViewBarcodes;
    private BarcodeAdapter barcodeAdapter;
    private List<BarcodeItem> barcodeItems = new ArrayList<>();
    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_bar_chart);

        databaseHelper = new DatabaseHelper(this);

        pieChart = findViewById(R.id.pieChart);
        documentSpinner = findViewById(R.id.documentSpinner);
        recyclerViewBarcodes = findViewById(R.id.recyclerViewBarcodes);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setupDocumentSpinner();
        setupRecyclerView();
    }

    private void setupDocumentSpinner() {
        List<String> documentReferences = new ArrayList<>();
        try (Cursor cursor = databaseHelper.getAllDocuments()) {
            while (cursor.moveToNext()) {
                int referenceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_REFERENCE);
                int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOCUMENT_ID);
                if (referenceIndex != -1 && idIndex != -1) {
                    String reference = cursor.getString(referenceIndex);
                    long documentId = cursor.getLong(idIndex);
                    documentReferences.add(reference);
                    documentIds.add(documentId);
                } else {
                    Log.e("DatabaseError", "COLUMN_REFERENCE ose COLUMN_DOCUMENT_ID not found in Cursor.");
                }
            }
        } catch (Exception e) {
            Log.e("DatabaseError", "Error while fetching documents.", e);
        }

        documentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, documentReferences);
        documentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        documentSpinner.setAdapter(documentAdapter);
        documentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long documentId = documentIds.get(position);
                setupPieChart(documentId);
                loadBarcodeData(documentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupRecyclerView() {
        barcodeAdapter = new BarcodeAdapter(barcodeItems);
        recyclerViewBarcodes.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewBarcodes.setAdapter(barcodeAdapter);
    }

    private void loadBarcodeData(long documentId) {
        barcodeItems.clear();
        Cursor cursor = databaseHelper.getBarcodesForDocument(documentId);
        while (cursor.moveToNext()) {
            int barcodeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BARCODE);
            int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY);
            if (barcodeIndex != -1 && quantityIndex != -1) {
                String barcode = cursor.getString(barcodeIndex);
                int quantity = cursor.getInt(quantityIndex);
                barcodeItems.add(new BarcodeItem(barcode, quantity));
            }
        }
        cursor.close();
        barcodeAdapter.notifyDataSetChanged();
    }

    private void setupPieChart(long documentId) {
        Map<String, Integer> barcodeQuantities = new HashMap<>();
        Cursor cursor = databaseHelper.getBarcodesForDocument(documentId);

        while (cursor.moveToNext()) {
            int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY);
            int barcodeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BARCODE);

            if (quantityIndex != -1 && barcodeIndex != -1) {
                int quantity = cursor.getInt(quantityIndex);
                String barcode = cursor.getString(barcodeIndex);
                barcodeQuantities.put(barcode, barcodeQuantities.getOrDefault(barcode, 0) + quantity);
            }
        }
        cursor.close();

        ArrayList<PieEntry> entries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : barcodeQuantities.entrySet()) {
            entries.add(new PieEntry(entry.getValue(), entry.getKey()));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        int[] colors = getColors(entries.size());
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        PieData data = new PieData(dataSet);
        data.setValueTextSize(10f);
        data.setValueTextColor(Color.BLACK);

        pieChart.setData(data);
        pieChart.setUsePercentValues(true);
        pieChart.setEntryLabelColor(Color.BLACK);
        pieChart.setEntryLabelTextSize(12f);
        pieChart.setDescription(null);
        pieChart.setDrawEntryLabels(false);
        pieChart.invalidate();
    }

    private int[] getColors(int count) {
        int[] colors = new int[count];
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            colors[i] = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        }
        return colors;
    }
}
