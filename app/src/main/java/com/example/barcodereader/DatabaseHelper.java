package com.example.barcodereader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BarcodeData";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_BARCODES = "Barcodes";
    private static final String TABLE_DOCUMENTS = "Documents";
    private static final String COLUMN_ID = "BarcodeID";
    public static final String COLUMN_BARCODE = "Barcode";
    public static final String COLUMN_QUANTITY = "Quantity";
    public static final String COLUMN_DATE_REGISTERED = "DateRegistered";
    private static final String COLUMN_DOCUMENT_ID = "DocumentID";
    private static final String COLUMN_DOCUMENT_ID_DOC = "DocumentID";
    private static final String COLUMN_REFERENCE = "Reference";
    private static final String COLUMN_COMMENT = "Comment";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DOCUMENTS_TABLE = "CREATE TABLE " + TABLE_DOCUMENTS + "("
                + COLUMN_DOCUMENT_ID_DOC + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_REFERENCE + " TEXT NOT NULL,"
                + COLUMN_COMMENT + " TEXT"
                + ")";
        String CREATE_BARCODES_TABLE = "CREATE TABLE " + TABLE_BARCODES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BARCODE + " TEXT NOT NULL,"
                + COLUMN_QUANTITY + " INTEGER DEFAULT 0,"
                + COLUMN_DATE_REGISTERED + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_DOCUMENT_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_DOCUMENT_ID + ") REFERENCES " + TABLE_DOCUMENTS + "(" + COLUMN_DOCUMENT_ID_DOC + ")"
                + ")";
        db.execSQL(CREATE_DOCUMENTS_TABLE);
        db.execSQL(CREATE_BARCODES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BARCODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS);
        onCreate(db);
    }

    public long addDocument(String reference, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REFERENCE, reference);
        values.put(COLUMN_COMMENT, comment);
        long id = db.insert(TABLE_DOCUMENTS, null, values);
        db.close();
        return id;
    }

    public void addBarcode(String barcode, int quantity, long documentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BARCODE, barcode);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_DOCUMENT_ID, documentId);
        db.insert(TABLE_BARCODES, null, values);
        db.close();
    }

    public Cursor getAllDocuments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_DOCUMENTS, null);
    }
}
