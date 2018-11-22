package ru.ovod.foto2;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final int DB_VERSION = 1; // версия
    private static final String DB_Name = "OvodOrders";  // имя локаьной базы данных

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

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + INSPECTION + "(" + INSPECTION_ID
                + " integer primary key," + INSPECTION_NUMBER + " text," + INSPECTION_ORDERID + " text," + INSPECTION_ISSYNC + " integer" + ")");

        db.execSQL("create table " + PHOTO + "(" + PHOTO_ID
                + " integer primary key," + PHOTO_PATH + " text," + PHOTO_NAME + " text," + PHOTO_INSPECTION + " integer,"  + PHOTO_INSPECTION + " integer"  + ")");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("drop table if exists " + INSPECTION);
        db.execSQL("drop table if exists " + PHOTO);

        onCreate(db);

    }
}
