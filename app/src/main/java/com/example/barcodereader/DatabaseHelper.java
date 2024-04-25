package com.example.barcodereader;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BarcodeData";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "Barcodes";
    private static final String COLUMN_ID = "BarcodeID";
    public static final String COLUMN_BARCODE = "Barcode";
    public static final String COLUMN_QUANTITY = "Quantity";
    public static final String COLUMN_DATE_REGISTERED = "DateRegistered";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_BARCODES_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BARCODE + " TEXT NOT NULL,"
                + COLUMN_QUANTITY + " INTEGER DEFAULT 0,"
                + COLUMN_DATE_REGISTERED + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        db.execSQL(CREATE_BARCODES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addBarcode(String barcode, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BARCODE, barcode);
        values.put(COLUMN_QUANTITY, quantity);
        try {
            db.insert(TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }

    public Cursor getAllBarcodes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
    }
}
