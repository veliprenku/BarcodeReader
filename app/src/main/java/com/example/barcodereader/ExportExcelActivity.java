package com.example.barcodereader;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class ExportExcelActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);

        Button btnExportExcel = findViewById(R.id.btnOpenExport);
        showDocumentSelectionDialog();  // Call the dialog method directly here
    }


    private void showDocumentSelectionDialog() {
        Cursor cursor = dbHelper.getAllDocuments();
        LinkedHashMap<String, String> documentMap = new LinkedHashMap<>();
        try {
            int idIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DOCUMENT_ID);
            int refIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REFERENCE);
            while (cursor.moveToNext()) {
                String documentId = cursor.getString(idIndex);
                String documentReference = cursor.getString(refIndex);
                documentMap.put(documentReference, documentId);
            }
        } catch (IllegalArgumentException e) {
            Toast.makeText(this, "Gabim në skemën e databazës: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            cursor.close();
        }

        List<String> documentReferences = new ArrayList<>(documentMap.keySet());
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice, documentReferences);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Zgjidhni dokumentin");
        builder.setCancelable(true);
        builder.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String reference = arrayAdapter.getItem(which);
                String documentId = documentMap.get(reference);
                exportDataToExcel(documentId);
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                finish();  // Close this activity when the dialog dismisses
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }






    private void exportDataToExcel(String documentId) {
        String sanitizedDocumentId = documentId.replaceAll("[^a-zA-Z0-9]", "_");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Barcode_Data_for_DocID_" + sanitizedDocumentId);

        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("DocumentID");
        headerRow.createCell(1).setCellValue("Barkodi");
        headerRow.createCell(2).setCellValue("Sasia");
        headerRow.createCell(3).setCellValue("Data e Regjistrimit");
        headerRow.createCell(4).setCellValue("Referenca");
        headerRow.createCell(5).setCellValue("Komenti");

        Cursor cursor = dbHelper.getBarcodesByDocumentId(documentId);

        if (cursor != null && cursor.moveToFirst()) {
            int documentIdIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DOCUMENT_ID);
            int barcodeIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_BARCODE);
            int quantityIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_QUANTITY);
            int dateRegIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATE_REGISTERED);
            int referenceIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_REFERENCE);
            int commentIndex = cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COMMENT);


            do {

                Row row = sheet.createRow(sheet.getLastRowNum() + 1);
                row.createCell(0).setCellValue(cursor.getLong(documentIdIndex));
                row.createCell(1).setCellValue(cursor.getString(barcodeIndex));
                row.createCell(2).setCellValue(cursor.getString(quantityIndex));
                row.createCell(3).setCellValue(cursor.getString(dateRegIndex));
                row.createCell(4).setCellValue(cursor.getString(referenceIndex));
                row.createCell(5).setCellValue(cursor.getString(commentIndex));

            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "BarcodeData_" + documentId + ".xlsx");
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        Uri uri = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            uri = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        }

        try {
            if (uri != null) {
                try (OutputStream outputStream = getContentResolver().openOutputStream(uri)) {
                    workbook.write(outputStream);
                }
                Toast.makeText(this, "File saved to Downloads", Toast.LENGTH_LONG).show();
            } else {
                throw new IOException("Failed to create new MediaStore record.");
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to save file: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                Toast.makeText(this, "Error closing workbook: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
