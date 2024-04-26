package com.example.barcodereader;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;

public class ExportExcelActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_excel);

        dbHelper = new DatabaseHelper(this);
        Button exportButton = findViewById(R.id.btnExportExcel);
        exportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                exportDatabaseToExcel();
            }
        });

    }

    private void exportDatabaseToExcel() {
        try {
            Workbook workbook = new HSSFWorkbook(); // Use HSSFWorkbook for .xls format
            Sheet sheet = workbook.createSheet("Barcode Data");

            // Create header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("BarcodeID");
            header.createCell(1).setCellValue("Barcode");
            header.createCell(2).setCellValue("Quantity");
            header.createCell(3).setCellValue("Date Registered");

            // Fill data
            Cursor cursor = dbHelper.getAllBarcodes();
            int rowIndex = 1;
            int idIndex = cursor.getColumnIndex("BarcodeID");
            int barcodeIndex = cursor.getColumnIndex("Barcode");
            int quantityIndex = cursor.getColumnIndex("Quantity");
            int dateRegisteredIndex = cursor.getColumnIndex("DateRegistered");

            if (idIndex != -1 && barcodeIndex != -1 && quantityIndex != -1 && dateRegisteredIndex != -1) {
                while (cursor.moveToNext()) {
                    Row row = sheet.createRow(rowIndex++);
                    row.createCell(0).setCellValue(cursor.getInt(idIndex));
                    row.createCell(1).setCellValue(cursor.getString(barcodeIndex));
                    row.createCell(2).setCellValue(cursor.getInt(quantityIndex));
                    row.createCell(3).setCellValue(cursor.getString(dateRegisteredIndex));
                }
            } else {
                // Handle the error or log it
                Log.e("ExportExcel", "One or more columns are not found in the database.");
            }
            cursor.close();

            // Save the workbook to file
            String fileName = "BarcodeData.xls"; // Change file extension to .xls
            File file = new File(this.getExternalFilesDir(null), fileName);
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
            outputStream.close();

            // Share the file
            shareExcelFile(file);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void shareExcelFile(File file) {
        Uri path = Uri.fromFile(file);
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_STREAM, path);
        shareIntent.setType("application/vnd.ms-excel"); // Change MIME type to match .xls files
        startActivity(Intent.createChooser(shareIntent, "Share Excel File"));
    }
}
