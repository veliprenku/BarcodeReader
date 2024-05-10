package com.example.barcodereader;

import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Reports extends AppCompatActivity {
    private BarChart barChart;
    private DatabaseHelper databaseHelper;
    private Spinner documentSpinner;
    private ArrayAdapter<String> documentAdapter;
    private List<Long> documentIds = new ArrayList<>();  // Store document IDs for reference

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_bar_chart);

        databaseHelper = new DatabaseHelper(this);
        barChart = findViewById(R.id.barChart);
        documentSpinner = findViewById(R.id.documentSpinner);

        setupDocumentSpinner();
    }

    private void setupDocumentSpinner() {
        List<String> documentReferences = new ArrayList<>();
        Cursor cursor = databaseHelper.getAllDocuments();
        while (cursor.moveToNext()) {
            int referenceIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_REFERENCE);
            int idIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DOCUMENT_ID);
            if (referenceIndex != -1 && idIndex != -1) {
                String reference = cursor.getString(referenceIndex);
                long documentId = cursor.getLong(idIndex);
                documentReferences.add(reference);
                documentIds.add(documentId);  // Map references to IDs for later use
            } else {
                Log.e("DatabaseError", "COLUMN_REFERENCE or COLUMN_DOCUMENT_ID not found in Cursor.");
            }
        }
        cursor.close();

        documentAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, documentReferences);
        documentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        documentSpinner.setAdapter(documentAdapter);
        documentSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                long documentId = documentIds.get(position);  // Fetch the document ID corresponding to the selected reference
                setupBarChart(documentId);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupBarChart(long documentId) {
        Map<String, Integer> barcodeQuantities = new HashMap<>();
        ArrayList<String> labels = new ArrayList<>();
        Cursor cursor = databaseHelper.getBarcodesForDocument(documentId);

        while (cursor.moveToNext()) {
            int quantityIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_QUANTITY);
            int barcodeIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_BARCODE);

            if (quantityIndex != -1 && barcodeIndex != -1) {
                int quantity = cursor.getInt(quantityIndex);
                String barcode = cursor.getString(barcodeIndex);
                barcodeQuantities.put(barcode, barcodeQuantities.getOrDefault(barcode, 0) + quantity);
            } else {
                Log.e("DatabaseError", "One or more columns not found in Cursor.");
            }
        }
        cursor.close();

        ArrayList<BarEntry> entries = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, Integer> entry : barcodeQuantities.entrySet()) {
            entries.add(new BarEntry(index, entry.getValue()));
            labels.add(entry.getKey());
            index++;
        }

        BarDataSet dataSet = new BarDataSet(entries, "Barcode Quantities");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        BarData data = new BarData(dataSet);
        barChart.setData(data);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.invalidate(); // refresh the chart
    }
}
