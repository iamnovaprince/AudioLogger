package com.novaapps.audiologger.database;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DBHandler extends SQLiteOpenHelper {

    // creating a constant variables for our database.
    // below variable is for our database name.
    private static final String DB_NAME = "AudioLogger";

    // below int is our database version
    private static final int DB_VERSION = 1;

    // below variable is for our table name.
    private static final String TABLE_NAME = "Mapping";

    // below variable is for our id column.
    private static final String ID_COL = "id";

    // below variable is for our course name column
    private static final String FILE_HASH_COL = "hash";

    private static final String DIRECTORY_HASH_COL = "parent";

    // below variable id for our course duration column.
    private static final String DATE_COL = "date";

    // below variable for our course description column.
    private static final String TIME_COL = "time";


    // creating a constructor for our database handler.
    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // below method is for creating a database by running a sqlite query
    @Override
    public void onCreate(SQLiteDatabase db) {
        // on below line we are creating
        // an sqlite query and we are
        // setting our column names
        // along with their data types.
        String query = "CREATE TABLE " + TABLE_NAME + " ("
                + ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + FILE_HASH_COL + " TEXT,"
                + DIRECTORY_HASH_COL + " TEXT,"
                + DATE_COL + " TEXT,"
                + TIME_COL + " TEXT)";

        // at last we are calling a exec sql
        // method to execute above sql query
        db.execSQL(query);
    }

    // this method is use to add new course to our sqlite database.
    public void addNewRecord(String recordHash, String recordParentHash, String recordDate, String recordTime) {

        Log.d("TAG","Data inserted with hash " + recordHash);

        // on below line we are creating a variable for
        // our sqlite database and calling writable method
        // as we are writing data in our database.
        SQLiteDatabase db = this.getWritableDatabase();

        // on below line we are creating a
        // variable for content values.
        ContentValues values = new ContentValues();

        // on below line we are passing all values
        // along with its key and value pair.
        values.put(FILE_HASH_COL, recordHash);
        values.put(DIRECTORY_HASH_COL, recordParentHash);
        values.put(DATE_COL, recordDate);
        values.put(TIME_COL, recordTime);

        // after adding all values we are passing
        // content values to our table.
        db.insert(TABLE_NAME, null, values);

        // at last we are closing our
        // database after adding database.
        db.close();
    }

    @SuppressLint("Range")
    public String getLastFileHash(String todayHash){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT *  FROM " + TABLE_NAME + " WHERE " + DIRECTORY_HASH_COL + " = '" + todayHash + "' ;",null);
        if(cursor != null){
            if(cursor.moveToFirst()){
                return cursor.getString(cursor.getColumnIndex("hash"));
            }
        }
        return null;
    }

    public void updateHash(String newHash, String todayHash){
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "UPDATE " + TABLE_NAME +" SET " + FILE_HASH_COL + " = '"+ newHash + "' WHERE "+DIRECTORY_HASH_COL+" = '"+todayHash+"';";
        db.execSQL(query);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // this method is called to check if the table exists already.
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
