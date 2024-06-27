package com.example.barcodereader;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button btnOpenInventory;
    private Button btnOpenExport;
    private Button btnReports;
    private Button btnAbout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenInventory = findViewById(R.id.btnOpenInventory);
        btnOpenExport = findViewById(R.id.btnOpenExport);
        btnReports = findViewById(R.id.btnReports);
        btnAbout = findViewById(R.id.btnAbout);

        btnOpenInventory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, InventoryRegisterActivity.class);
                startActivity(intent);
            }
        });

        btnOpenExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ExportExcelActivity.class);
                startActivity(intent);
            }
        });
        btnReports.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, Reports.class);
                startActivity(intent);
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AboutActivity.class);
                startActivity(intent);
            }
        });


    }
}
