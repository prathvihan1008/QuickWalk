package com.prathvihan1008.quickwalk;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class MyDBHelper extends SQLiteOpenHelper{

    private final static String DATABASE_NAME ="DataDB"; //this needs to be static because super() methods is
    // called first and it requires the DATABASE_NAME to be referenced before calling the super() meethod ,this is only possible if the field is made static
    private static final int DATABASE_VERSION =4;
    private static final String TABLE_DATA ="Data";

    //COLUMN CONTAINTS
    private static final String KEY_ID ="id";
    private static final String KEY_TIME="time";
    private static final String KEY_DISTANCE="distance";
    private static final String KEY_CAL ="cal";
    private static final String KEY_STEPS ="steps";
    private static final String KEY_DATE ="date";
    private static final String KEY_savingTime ="savingTime";

    public MyDBHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_DATA + " (" +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                KEY_TIME + " TEXT, " +
                KEY_STEPS + " TEXT, " +
                KEY_DISTANCE + " TEXT, " +
                KEY_CAL + " TEXT, " +  // Add space before TEXT
                KEY_DATE + " TEXT, " +  // Add space before TEXT
                KEY_savingTime + " TEXT" +  // Add space before TEXT and remove extra comma
                ")");
    }

    //this may look like:CREATE TABLE data(id INTEGER PRIMARY KEY AUTO INCREAMENT,time text,steps text,distance text,cal text)

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS "+TABLE_DATA);//space after EXISTS IS ALSO NECESSARY since u are writing a query
        onCreate(db);

    }

    public void addData(String time, String steps, String distance, String cal,String date,String savingTime) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(KEY_TIME, time);
        if (steps != null) {
            values.put(KEY_STEPS, steps);
        } else {
            values.put(KEY_STEPS, "0");
        }
        values.put(KEY_DISTANCE, distance);
        values.put(KEY_CAL, cal);
      values.put(KEY_DATE, date);  // Add date
    values.put(KEY_savingTime, savingTime);

        // Inserting Row
        // Inserting Row
        long newRowId = db.insert(TABLE_DATA, null, values);

        // Check if the insertion was successful
        if (newRowId != -1) {
            Log.d("MyDBHelper", "Data added successfully. Row ID: " + newRowId);
         //    Toast.makeText(MainActivity.this, "Data stored successfully", Toast.LENGTH_LONG).show();
        } else {
            Log.e("MyDBHelper", "Failed to add data.");
        }

       // db.close(); // Closing database connection
    }

    public ArrayList<DataModel> fetchDataForDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {KEY_ID, KEY_TIME, KEY_STEPS, KEY_DISTANCE, KEY_CAL, KEY_DATE};

        // Use a WHERE clause to filter data based on the provided date
        String selection = KEY_DATE + " = ?";
        String[] selectionArgs = {date};

        // ORDER BY both date and savingTime in descending order
        String orderBy = KEY_DATE + " DESC, " + KEY_savingTime + " DESC";

        Cursor cursor = db.query(TABLE_DATA, columns, selection, selectionArgs, null, null, orderBy);

        ArrayList<DataModel> arrData = new ArrayList<>();

        while (cursor.moveToNext()) {
            DataModel model = new DataModel();
            model.id = cursor.getInt(0);
            model.time = cursor.getString(1);
            model.steps = cursor.getString(2);
            model.distance = cursor.getString(3);
            model.calories = cursor.getString(4);
            model.date = cursor.getString(5);

            arrData.add(model);
        }

        cursor.close();
        return arrData;
    }

}
