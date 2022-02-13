package org.techtown.diary.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.techtown.diary.data.AppConstants;

public class NoteDatabase {
    private static final String TAG = "NoteDatabase";

    private static NoteDatabase database;
    public static String TABLE_NOTE = "NOTE";
    public static int DATABASE_VERSION = 1;

    private DatabaseHelper dbHelper;
    private SQLiteDatabase db;
    private Context context;

    private NoteDatabase(Context context){
        this.context = context;
    }

    public static NoteDatabase getInstance(Context context){
        if(database == null)
            database = new NoteDatabase(context);

        return database;
    }

    public boolean open(){
        println("opening database [" + AppConstants.DATABASE_NAME + "]");

        dbHelper = new DatabaseHelper(context);
        db = dbHelper.getWriteableDatabase();
    }

    public void close(){
        println("closing database [" + AppConstants.DATABASE_NAME + "]" );
        db.close();

        database = null;
    }

    public Cursor rawQuery(String SQL){
        println("\nexevuteQuery called.\n");

        Cursor cursor = null;
        try{
            cursor = db.rawQuery(SQL,null);
            println("cursor count : " + cursor.getCount());
        }catch (Exception ex){
            Log.e(TAG,"Exception in executeQuery", ex);
        }

        return cursor;
    }

    public boolean execSQL(String SQL){
        println("\nexecute called.\n");

        try{
            Log.d(TAG,"SQL : " + SQL);
            db.execSQL(SQL);
        }catch (Exception ex){
            Log.e(TAG,"Exception in executeQuery", ex);
            return false;
        }

        return true;
    }

}
