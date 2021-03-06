package ru.ovod.CarInspection;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class DBHelper extends SQLiteOpenHelper {


    private SettingsHelper settingshelper; // класс по работе с настройками
    private Context context;

    private static final int DB_VERSION = 10; // версия
    private static final String DB_Name = "OvodOrders";  // имя локаьной базы данных
    private static final String TAGDB = "DATABASE_OPERATION";

    public static final String INSPECTION = "inspection";  // таблица актов осмотра
    public static final String INSPECTION_ID = "_inspectionid";  // id
    public static final String INSPECTION_NUMBER = "number";  // номер ЗН
    public static final String INSPECTION_DATE = "orderdate";  // Дата ЗН (получаем эти данные с сервера)
    public static final String INSPECTION_MODEL = "model";  // Модель (получаем эти данные с сервера)
    public static final String INSPECTION_VIN = "vin";  // VIN (получаем эти данные с сервера)
    public static final String INSPECTION_ORDERID = "orderid";  // ORderID
    public static final String INSPECTION_ISSYNC = "issync";  // пометка, что синхронизировано
    public static final String INSPECTION_CREATEDATE = "createdate";  // пометка, что синхронизировано

    public static final String PHOTO = "photo";  // таблица актов осмотра
    public static final String PHOTO_ID = "_photoid";  // id
    public static final String PHOTO_PATH = "path";  // пусть
    public static final String PHOTO_NAME = "name";  // имя файла
    public static final String PHOTO_NAME_THUMBNAIL = "thumbnail";  // файл-Preview
    public static final String PHOTO_INSPECTION = "inspectionid";  // ссылка на ID инспекции
    public static final String PHOTO_ISSYNC = "issync";  // признак, что фото залито на сервер

    public DBHelper() {
        super(null, DB_Name, null, DB_VERSION);
    }


    public DBHelper(Context context) {
        super(context, DB_Name, null, DB_VERSION);
        this.context = context;
        settingshelper = new SettingsHelper(context);
        Log.e(TAGDB,"DBHelper Created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {


        Log.e(TAGDB,"Begin table create.");


        try {

//            db.execSQL("drop table if exists " + INSPECTION);
//            db.execSQL("drop table if exists " + PHOTO);

            String sql = "create table IF NOT EXISTS " + INSPECTION + "(" + INSPECTION_ID
                    + " integer primary key AUTOINCREMENT," + INSPECTION_NUMBER + " text," + INSPECTION_DATE + " string," + INSPECTION_MODEL + " text," + INSPECTION_VIN + " text,"
                    + INSPECTION_ORDERID + " integer," + INSPECTION_ISSYNC + " integer," + INSPECTION_CREATEDATE +" DATETIME DEFAULT CURRENT_TIMESTAMP " + ")";

            Log.e(TAGDB, sql);
            db.execSQL(sql);

            sql = "create table IF NOT EXISTS " + PHOTO + "(" + PHOTO_ID
                    + " integer primary key AUTOINCREMENT," + PHOTO_PATH + " text," + PHOTO_NAME + " text,"+ PHOTO_NAME_THUMBNAIL + " text," + PHOTO_ISSYNC + " integer," + PHOTO_INSPECTION + " integer )";

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


    public ArrayList<Inspection> getInspectionList(){
        Inspection item;
        ArrayList<Inspection> inspectionList = new ArrayList<Inspection>();



        String actsparam = "";
        if (settingshelper.getShow_synchronized_acts())
        {
            actsparam = "";
        }
        else
        {
            actsparam = " WHERE  " + INSPECTION+"."+ INSPECTION_ID + " in ( select " + PHOTO_INSPECTION + " from " +  PHOTO + " where  " + PHOTO_ISSYNC + "=0) "  ;
        }

        SQLiteDatabase database = this.getReadableDatabase();
        String SQL = "SELECT " + INSPECTION_ID + ", " + INSPECTION_NUMBER + ", " + INSPECTION_ORDERID + ", "
                + INSPECTION_DATE + ", " + INSPECTION_MODEL + ", " + INSPECTION_VIN + ", " + INSPECTION_ISSYNC + ", "
                + " (SELECT count(*) from  " + PHOTO + " where " + PHOTO + "." + PHOTO_INSPECTION + " = " + INSPECTION + "." + INSPECTION_ID + ") as coun, "
                + " (SELECT count(*) from  " + PHOTO + " where " + PHOTO + "." + PHOTO_INSPECTION + " = " + INSPECTION + "." + INSPECTION_ID + " and "+PHOTO + "." + PHOTO_ISSYNC + " = 0 " +") as coun_no_sync" // количество не сихронизированных фото
                + " FROM " + INSPECTION
                + actsparam
                + " Order by " + INSPECTION_ID + " desc";
        Cursor cursor = database. rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {
                Integer InsID = cursor.getInt(cursor.getColumnIndex(INSPECTION_ID));
                Integer num = cursor.getInt(cursor.getColumnIndex(INSPECTION_NUMBER));
                Integer OrdID = cursor.getInt(cursor.getColumnIndex(INSPECTION_ORDERID));
                String dt = cursor.getString(cursor.getColumnIndex(INSPECTION_DATE));
                String model = cursor.getString(cursor.getColumnIndex(INSPECTION_MODEL));
                String vin = cursor.getString(cursor.getColumnIndex(INSPECTION_VIN));
                Integer isSynced = cursor.getInt(cursor.getColumnIndex("coun_no_sync"));
                if (isSynced>0) {isSynced=0;} else {isSynced=1;}  // если есть не сихронные фотки, то галочнку уберём
                Integer Coun = cursor.getInt(cursor.getColumnIndex("coun"));

                item = new Inspection(InsID, num, OrdID, isSynced, dt, model, vin);
                item.setPhotoCo(Coun);
                inspectionList.add(item);

                Log.e("DB ", "Извлекли INSPECTION_ID: " + InsID);
            }
        }
        cursor.close();
        database.close();

        return inspectionList;
    }


}
