package com.example.barcodereader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "BarcodeData";
    private static final int DATABASE_VERSION = 1;
    static final String TABLE_BARCODES = "Barcodes";
    static final String TABLE_DOCUMENTS = "Documents";
    static final String COLUMN_ID = "BarcodeID";
    public static final String COLUMN_BARCODE = "Barcode";
    public static final String COLUMN_QUANTITY = "Quantity";
    public static final String COLUMN_DATE_REGISTERED = "DateRegistered";
    public static final String COLUMN_DOCUMENT_ID = "DocumentID";
    public static final String COLUMN_REFERENCE = "Reference";
    public static final String COLUMN_COMMENT = "Comment";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DOCUMENTS_TABLE = "CREATE TABLE " + TABLE_DOCUMENTS + "("
                + COLUMN_DOCUMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_REFERENCE + " TEXT NOT NULL,"
                + COLUMN_COMMENT + " TEXT,"
                + COLUMN_DATE_REGISTERED + " DATETIME DEFAULT CURRENT_TIMESTAMP"
                + ")";
        String CREATE_BARCODES_TABLE = "CREATE TABLE " + TABLE_BARCODES + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_BARCODE + " TEXT NOT NULL,"
                + COLUMN_QUANTITY + " INTEGER DEFAULT 0,"
                + COLUMN_DATE_REGISTERED + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
                + COLUMN_DOCUMENT_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_DOCUMENT_ID + ") REFERENCES " + TABLE_DOCUMENTS + "(" + COLUMN_DOCUMENT_ID + ")"
                + ")";
        db.execSQL(CREATE_DOCUMENTS_TABLE);
        db.execSQL(CREATE_BARCODES_TABLE);
    }


    public InventoryDocument getDocumentById(long documentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DOCUMENTS, null, COLUMN_DOCUMENT_ID + " = ?",
                new String[]{String.valueOf(documentId)}, null, null, null);
        InventoryDocument document = null;

        if (cursor != null && cursor.moveToFirst()) {
            int referenceIndex = cursor.getColumnIndex(COLUMN_REFERENCE);
            int commentIndex = cursor.getColumnIndex(COLUMN_COMMENT);

            if (referenceIndex != -1 && commentIndex != -1) {
                String reference = cursor.getString(referenceIndex);
                String comment = cursor.getString(commentIndex);
                document = new InventoryDocument(documentId, reference, comment);
            }
            cursor.close();
        }
        db.close();
        return document;
    }

    public void updateBarcodeQuantity(String barcode, int quantity, long documentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QUANTITY, quantity);

        String selection = COLUMN_BARCODE + " = ? AND " + COLUMN_DOCUMENT_ID + " = ?";
        String[] selectionArgs = {barcode, String.valueOf(documentId)};

        db.update(TABLE_BARCODES, values, selection, selectionArgs);
        db.close();
    }
    public Cursor getAllBarcodes() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_BARCODES, null);
    }

    public void deleteBarcode(String barcode, long documentId) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_BARCODE + " = ? AND " + COLUMN_DOCUMENT_ID + " = ?";
        String[] selectionArgs = {barcode, String.valueOf(documentId)};

        db.delete(TABLE_BARCODES, selection, selectionArgs);
        db.close();
    }
    public void deleteDocument(long documentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DOCUMENTS, COLUMN_DOCUMENT_ID + " = ?", new String[]{String.valueOf(documentId)});

        db.delete(TABLE_BARCODES, COLUMN_DOCUMENT_ID + " = ?", new String[]{String.valueOf(documentId)});
        db.close();
    }

    public Cursor getAllDocumentIds() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT " + COLUMN_DOCUMENT_ID + COLUMN_DATE_REGISTERED + " FROM " + TABLE_DOCUMENTS, null);
    }

    public void addOrUpdateBarcode(String barcode, int quantity, long documentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BARCODE, barcode);
        values.put(COLUMN_QUANTITY, quantity);
        values.put(COLUMN_DOCUMENT_ID, documentId);

        int updated = db.update(TABLE_BARCODES, values, COLUMN_BARCODE + " = ? AND " + COLUMN_DOCUMENT_ID + " = ?", new String[]{barcode, String.valueOf(documentId)});
        if (updated == 0) {
            db.insert(TABLE_BARCODES, null, values);
        }
        db.close();
    }


    public Cursor getBarcodesByDocumentId(String documentId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                TABLE_BARCODES + "." + COLUMN_DOCUMENT_ID,
                TABLE_BARCODES + "." + COLUMN_BARCODE,
                TABLE_BARCODES + "." + COLUMN_QUANTITY,
                TABLE_BARCODES + "." + COLUMN_DATE_REGISTERED,
                TABLE_DOCUMENTS + "." + COLUMN_REFERENCE,
                TABLE_DOCUMENTS + "." + COLUMN_COMMENT
        };

        String joinStatement = " JOIN " + TABLE_DOCUMENTS +
                " ON " + TABLE_BARCODES + "." + COLUMN_DOCUMENT_ID +
                " = " + TABLE_DOCUMENTS + "." + COLUMN_DOCUMENT_ID;

        String selection = TABLE_BARCODES + "." + COLUMN_DOCUMENT_ID + " = ?";
        String[] selectionArgs = {documentId};

        return db.query(TABLE_BARCODES + joinStatement, columns, selection, selectionArgs, null, null, null);
    }





    public static int safeGetColumnIndex(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        if (index == -1) throw new IllegalArgumentException("Column '" + columnName + "' not found.");
        return index;
    }




    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BARCODES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DOCUMENTS);
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_DOCUMENTS + " ADD COLUMN " + COLUMN_DATE_REGISTERED + " DATETIME DEFAULT CURRENT_TIMESTAMP");
        }
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

    public Cursor getBarcodesForDocument(long documentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_BARCODES, new String[] {COLUMN_BARCODE, COLUMN_QUANTITY}, COLUMN_DOCUMENT_ID + " = ?", new String[] {String.valueOf(documentId)}, null, null, null);
    }
    public void updateDocument(long documentId, String reference, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_REFERENCE, reference);
        values.put(COLUMN_COMMENT, comment);
        values.put(COLUMN_DATE_REGISTERED, getDateTime());
        db.update(TABLE_DOCUMENTS, values, COLUMN_DOCUMENT_ID + " = ?", new String[]{String.valueOf(documentId)});
        db.close();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }


}

class InventoryDocument {
    long id;
    String reference;
    String comment;

    public InventoryDocument(long id, String reference, String comment) {
        this.id = id;
        this.reference = reference;
        this.comment = comment;
    }

}
