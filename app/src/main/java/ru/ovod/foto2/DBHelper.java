package ru.ovod.foto2;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 2; // версия
    private static final String DB_Name = "OvodOrders";  // имя локаьной базы данных
    private static final String TAGDB = "DATABASE_OPERATION";

    public static final String INSPECTION = "inspection";  // таблица актов осмотра
    public static final String INSPECTION_ID = "_inspectionid";  // id
    public static final String INSPECTION_NUMBER = "number";  // номер ЗН
    public static final String INSPECTION_ORDERID = "orderid";  // ORderID
    public static final String INSPECTION_ISSYNC = "issync";  // пометка, что синхронизировано

    public static final String PHOTO = "photo";  // таблица актов осмотра
    public static final String PHOTO_ID = "_photoid";  // id
    public static final String PHOTO_PATH = "path";  // пусть
    public static final String PHOTO_NAME = "name";  // имя файла
    public static final String PHOTO_INSPECTION = "inspectionid";  // ссылка на ID инспекции
    public static final String PHOTO_ISSYNC = "issync";  // признак, что фото залито на сервер

    public DBHelper() {
        super(null, DB_Name, null, DB_VERSION);
    }


    public DBHelper(Context context) {
        super(context, DB_Name, null, DB_VERSION);
        Log.e(TAGDB,"DBHelper Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.e(TAGDB,"Begin table create.");


        try {

//            db.execSQL("drop table if exists " + INSPECTION);
//            db.execSQL("drop table if exists " + PHOTO);

            String sql = "create table IF NOT EXISTS " + INSPECTION + "(" + INSPECTION_ID
                    + " integer primary key AUTOINCREMENT," + INSPECTION_NUMBER + " text," + INSPECTION_ORDERID + " integer," + INSPECTION_ISSYNC + " integer" + ")";

            Log.e(TAGDB, sql);
            db.execSQL(sql);

            sql = "create table IF NOT EXISTS " + PHOTO + "(" + PHOTO_ID
                    + " integer primary key AUTOINCREMENT," + PHOTO_PATH + " text," + PHOTO_NAME + " text," + PHOTO_ISSYNC + " integer," + PHOTO_INSPECTION + " integer )";

            Log.e(TAGDB, sql);
            db.execSQL(sql);

            Log.e(TAGDB, "Table Created");
        }
        catch (Exception e)
            {
            // This will catch any exception, because they are all descended from Exception
                Log.e(TAGDB, "Error " + e.getMessage());
            }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists " + INSPECTION);
        db.execSQL("drop table if exists " + PHOTO);

        onCreate(db);

    }


    /*public void AddInspection(String num)
    {
        ContentValues contentValues = new ContentValues();
        contentValues.put(INSPECTION_NUMBER,num);
        db.insert(SCORE_TABLE_NAME, null , contentValues);
        Log.e(TAG,"One row is inserted");
    }*/

}
