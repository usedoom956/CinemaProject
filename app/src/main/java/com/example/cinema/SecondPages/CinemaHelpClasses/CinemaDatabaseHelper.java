package com.example.cinema.SecondPages.CinemaHelpClasses;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class CinemaDatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "cinema_db";
    private static final String TABLE_CINEMA = "cinema";

    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_VENUE = "venue";
    private static final String KEY_FREE_SEATS = "free_seats";
    private static final String KEY_DATE = "date";
    private static final String KEY_IMAGE_PATH = "image_path";

    public CinemaDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CINEMA_TABLE = "CREATE TABLE " + TABLE_CINEMA + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_NAME + " TEXT,"
                + KEY_VENUE + " TEXT,"
                + KEY_FREE_SEATS + " INTEGER,"
                + KEY_DATE + " TEXT,"
                + KEY_IMAGE_PATH + " TEXT" + ")";
        db.execSQL(CREATE_CINEMA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CINEMA);
        onCreate(db);
    }

    public long addCinema(CinemaDataClass cinema) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, cinema.getName());
        values.put(KEY_VENUE, cinema.getVenue());
        values.put(KEY_FREE_SEATS, cinema.getFreeSeats());
        values.put(KEY_DATE, cinema.getDate());
        values.put(KEY_IMAGE_PATH, cinema.getImagePath());

        long result = db.insertWithOnConflict(TABLE_CINEMA, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();

        return result;
    }

    public void deleteDuplicateCinemas() {
        SQLiteDatabase db = this.getWritableDatabase();

        String tempTable = "tempTable";
        String createTempTableQuery = "CREATE TEMPORARY TABLE " + tempTable +
                " AS SELECT MIN(" + KEY_ID + ") AS " + KEY_ID +
                " FROM " + TABLE_CINEMA +
                " GROUP BY " + KEY_NAME;

        db.execSQL(createTempTableQuery);

        String deleteQuery = "DELETE FROM " + TABLE_CINEMA +
                " WHERE " + KEY_ID + " NOT IN (SELECT " + KEY_ID +
                " FROM " + tempTable + ")";

        db.execSQL(deleteQuery);

        String dropTempTableQuery = "DROP TABLE IF EXISTS " + tempTable;
        db.execSQL(dropTempTableQuery);

        db.close();
    }


    public List<CinemaDataClass> getAllCinema() {
        List<CinemaDataClass> cinemaList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CINEMA, null);

        if (cursor.moveToFirst()) {
            do {
                CinemaDataClass cinema = new CinemaDataClass();
                cinema.setName(cursor.getString(cursor.getColumnIndex(KEY_NAME)));
                cinema.setVenue(cursor.getString(cursor.getColumnIndex(KEY_VENUE)));
                cinema.setFreeSeats(cursor.getInt(cursor.getColumnIndex(KEY_FREE_SEATS)));
                cinema.setDate(cursor.getString(cursor.getColumnIndex(KEY_DATE)));
                cinema.setImagePath(cursor.getString(cursor.getColumnIndex(KEY_IMAGE_PATH)));

                cinemaList.add(cinema);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return cinemaList;
    }

    public void loadCinemaFromFirebase(List<CinemaDataClass> cinemaList) {
        for (CinemaDataClass cinema : cinemaList) {
            addCinema(cinema);
        }
    }

    public boolean isCinemaUnique(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CINEMA + " WHERE " + KEY_NAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name});
        boolean isUnique = (cursor.getCount() == 0);
        cursor.close();
        db.close();
        return isUnique;
    }
}


