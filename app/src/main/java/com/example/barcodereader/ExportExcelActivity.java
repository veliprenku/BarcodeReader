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

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
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
        showDocumentSelectionDialog();
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
                finish();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }






    private void exportDataToExcel(String documentId) {
        String sanitizedDocumentId = documentId.replaceAll("[^a-zA-Z0-9]", "_");

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Barkode " + sanitizedDocumentId);

        Font headerFont = workbook.createFont();
        headerFont.setBold(true);

        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setBorderBottom(BorderStyle.THIN);
        headerStyle.setBorderTop(BorderStyle.THIN);
        headerStyle.setBorderRight(BorderStyle.THIN);
        headerStyle.setBorderLeft(BorderStyle.THIN);
        headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        Row headerRow = sheet.createRow(0);
        String[] headers = {"DocumentID", "Barkodi", "Sasia", "Data e Regjistrimit", "Referenca", "Komenti"};

        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

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

        for (int i = 0; i < headers.length; i++) {
            sheet.setColumnWidth(i, getMaxColumnWidth(sheet, i) * 256);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, "Dokumenti_" + documentId + ".xlsx");
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
                Toast.makeText(this, "Është ruajtur në Downloads", Toast.LENGTH_LONG).show();
            } else {
                throw new IOException("Ka deshtuar.");
            }
        } catch (IOException e) {
            Toast.makeText(this, "Ka deshtuar: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            try {
                workbook.close();
            } catch (IOException e) {
                Toast.makeText(this, "Ka deshtuar : " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    private int getMaxColumnWidth(Sheet sheet, int column) {
        int maxWidth = 0;
        for (Row row : sheet) {
            Cell cell = row.getCell(column);
            if (cell != null) {
                int cellWidth = cell.toString().length();
                if (cellWidth > maxWidth) {
                    maxWidth = cellWidth;
                }
            }
        }
        return maxWidth;
    }

}
