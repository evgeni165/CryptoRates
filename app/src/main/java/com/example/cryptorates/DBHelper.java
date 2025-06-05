package com.example.cryptorates;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "database.db";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "favourites";
    private static final String COLUMN_NUMBER = "number";
    private static final String COLUMN_TICKER = "ticker";
    private static final String COLUMN_NAME = "name";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_NUMBER + " INTEGER PRIMARY KEY, " +
                    COLUMN_TICKER + " TEXT, " +
                    COLUMN_NAME + " TEXT)";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertData(String ticker, String name) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TICKER, ticker);
        values.put(COLUMN_NAME, name);

        long id = db.insert(TABLE_NAME, null, values);
        db.close();
        return id;
    }

    public String[] getAllTickers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_TICKER},
                null, null, null, null, null);

        String[] tickers = new String[cursor.getCount()];

        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                tickers[i] = cursor.getString(0);
                i++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return tickers;
    }

    public String[] getAllNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_NAME},
                null, null, null, null, null);

        String[] names = new String[cursor.getCount()];

        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                names[i] = cursor.getString(0);
                i++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return names;
    }

    public String[] getAllNumbers() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_NUMBER},
                null, null, null, null, null);

        String[] numbers = new String[cursor.getCount()];

        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                numbers[i] = cursor.getString(0);
                i++;
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return numbers;
    }

    public boolean isNameExists(String nameToCheck) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_TICKER + " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_TICKER + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{nameToCheck});

        boolean exists = (cursor.getCount() > 0);

        cursor.close();
        db.close();

        return exists;
    }

    public void deleteData(String ticker) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT number FROM favourites WHERE ticker = ?", new String[]{ticker});
        cursor.moveToFirst();
        int deletePosition = cursor.getInt(0);
        cursor.close();
        db.delete(TABLE_NAME, COLUMN_TICKER + " = ?", new String[]{String.valueOf(ticker)});
        db.execSQL("UPDATE favourites SET number = number - 1 WHERE number > ?", new Object[]{deletePosition});
        db.close();
    }
}