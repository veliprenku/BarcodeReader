package com.example.barcodereader;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class ExportExcelActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_excel);
        dbHelper = new DatabaseHelper(this);

        Button btnExportExcel = findViewById(R.id.btnExportExcel);
        btnExportExcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDocumentSelectionDialog();
            }
        });
    }

    private void showDocumentSelectionDialog() {
        Cursor cursor = dbHelper.getAllDocuments();
        ArrayList<String> documentReferences = new ArrayList<>();
        try {
            int refIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REFERENCE);
            while (cursor.moveToNext()) {
                documentReferences.add(cursor.getString(refIndex));
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Gabim në skemën e databazës: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            cursor.close();
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, documentReferences);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zgjidhni dokumentin");
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reference = arrayAdapter.getItem(which);
                exportDataToExcel(reference);
            }
        });
        builder.show();
    }



    private void exportDataToExcel(String documentId) {
        String sanitizedDocumentId = documentId.replaceAll("[^a-zA-Z0-9]", "_");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Barcode_Data_for_DocID_" + sanitizedDocumentId);

        // Create header row
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Document ID");
        headerRow.createCell(1).setCellValue("Barkodi");
        headerRow.createCell(2).setCellValue("Sasia");
        headerRow.createCell(3).setCellValue("Data e Regjistrimit");
        headerRow.createCell(4).setCellValue("Referenca");
        headerRow.createCell(5).setCellValue("Komenti");

        // Populate the sheet with data related to the selected document ID
        Cursor cursor = dbHelper.getBarcodesByDocumentId(documentId);
        int rowIndex = 1;
        int documentIdIndex = cursor.getColumnIndexOrThrow("DocumentID");
        int barcodeIndex = cursor.getColumnIndexOrThrow("Barcode");
        int quantityIndex = cursor.getColumnIndexOrThrow("Quantity");
        int dateRegIndex = cursor.getColumnIndexOrThrow("DateRegistered");
        int referenceIndex = cursor.getColumnIndexOrThrow("Reference");
        int commentIndex = cursor.getColumnIndexOrThrow("Comment");


        while (cursor.moveToNext()) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(cursor.getLong(documentIdIndex));
            row.createCell(1).setCellValue(cursor.getString(barcodeIndex));
            row.createCell(2).setCellValue(cursor.getInt(quantityIndex));
            row.createCell(3).setCellValue(cursor.getString(dateRegIndex));
            row.createCell(4).setCellValue(cursor.getString(referenceIndex));
            row.createCell(5).setCellValue(cursor.getString(commentIndex));
        }
        cursor.close();

        // Save the workbook to storage
        try {
            File file = new File(getExternalFilesDir(null), "BarcodeData_" + documentId + ".xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();
            Toast.makeText(this, "Është ruajtur në " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Toast.makeText(this, "Gabim në eksportim : " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }



}
