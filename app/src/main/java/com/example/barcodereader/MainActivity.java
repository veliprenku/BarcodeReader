package com.example.barcodereader;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnOpenInventory;
    private Button btnOpenExport;
    private Button btnReports;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find buttons by their IDs
        btnOpenInventory = findViewById(R.id.btnOpenInventory);
        btnOpenExport = findViewById(R.id.btnOpenExport);
        btnReports = findViewById(R.id.btnReports);

        // Set click listeners for the buttons
        btnOpenInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Inventory Register Activity
                Intent intent = new Intent(MainActivity.this, InventoryRegisterActivity.class);
                startActivity(intent);
            }
        });

        btnOpenExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Export Excel Activity
                Intent intent = new Intent(MainActivity.this, ExportExcelActivity.class);
                startActivity(intent);
            }
        });
        btnReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Export Excel Activity
                Intent intent = new Intent(MainActivity.this, Reports.class);
                startActivity(intent);
            }
        });
    }
}
