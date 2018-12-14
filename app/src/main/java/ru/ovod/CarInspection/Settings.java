package ru.ovod.CarInspection;

import android.app.Activity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class Settings extends Activity {

    private SQLiteDatabase database;
    private DBHelper dbhelper;
    TextView versionText;
    private CheckBox checkBox;
    private EditText editcolumns;
    private  EditText editdays;
    private ImageView iv;
    private  String path;

    private SettingsHelper settingsHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // инициализируем БД
        dbhelper = new DBHelper(getApplicationContext());
        database = dbhelper.getWritableDatabase();
        settingsHelper = new SettingsHelper(getApplicationContext());

        checkBox = findViewById(R.id.checkBox);
        editcolumns = findViewById(R.id.editcolumns);
        editdays = findViewById(R.id.editdays);

        versionText=findViewById(R.id.VersionText);
        versionText.setText("Версия "+BuildConfig.VERSION_NAME);

        iv = findViewById(R.id.imageViewOvod);
        iv.setImageResource(R.drawable.ovod);

        path = Environment.getExternalStorageDirectory().toString();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        dbhelper.close();
    }


    // метож записывает установленные настройки
    @Override
    protected void onPause() {
        super.onPause();

        // запись Показывать синхронизированный акты
        settingsHelper.setShow_synchronized_acts(checkBox.isChecked());

        // запись счётчика столбцов
        try
        {
            settingsHelper.setCounter_cols( Integer.parseInt( editcolumns.getText().toString()));
        }
        catch(NumberFormatException nfe) // если ошибка парсинга
        {
            settingsHelper.setCounter_cols(4);  // то поставим 4
        }


        // запись счётчика дней для удаления
        try
        {
            settingsHelper.setDays_to_delete ( Integer.parseInt( editdays.getText().toString()));
        }
        catch(NumberFormatException nfe) // если ошибка парсинга
        {
            settingsHelper.setDays_to_delete(2);  // то поставим 4
        }
    }

    // метод читает установленные настройки
    @Override
    protected void onResume() {
        super.onResume();
        checkBox.setChecked(settingsHelper.getShow_synchronized_acts());
        editcolumns.setText(settingsHelper.getCounter_cols().toString());
        editdays.setText(settingsHelper.getDays_to_delete().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    public void ClickClearFileAndDB(View view) {

        ClearInspectionByDays(settingsHelper.getDays_to_delete());


        // почистим файлы в директории
        // старый вариант - просто чистит файлы в папке
        // пока лучше не удалять - может, когда-то захотим почистить папку
        /*String path;
        path = Environment.getExternalStorageDirectory().toString();

        File directory = new File(path);
        File[] files = directory.listFiles();

        for (int i = 0; i < files.length; i++)
        {
            if (files[i].getName().contains("jpg")) {
                Log.e("File operaton:",  "Удаление файла:"+files[i].getName()+"\n");
                File file = new File(path+"/"+files[i].getName());
                Boolean b = file.delete();
            };
        }

        // почистим базу
        database.execSQL("delete from  " + dbhelper.INSPECTION);
        database.execSQL("delete from  " + dbhelper.PHOTO);*/
    }


    // функция поиска и удаление Inspections
    public void ClearInspectionByDays(Integer i) {

        Integer id;
        String SQL = "SELECT " + DBHelper.INSPECTION_ID + ", " + DBHelper.INSPECTION_ORDERID + " "
                + " FROM " + DBHelper.INSPECTION + " where " + DBHelper.INSPECTION_CREATEDATE + " >= date('now','-"+i.toString()+" day')";
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {
                id = cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ID));
                DeletePhotosByInspectionId(id); // почистим фото

                database.delete(DBHelper.INSPECTION, DBHelper.INSPECTION_ID+"="+id.toString(), null); // удалим в базе
                Log.e("DB ", "Удалили INSPECTION_ID: " + id);
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }
    }



    // функция поиска и удаления фото
    public void DeletePhotosByInspectionId(Integer InsId)
    {

        // получим из базы список Фото
        String SQL = "SELECT " + DBHelper.PHOTO_ID + ", " + DBHelper.PHOTO_INSPECTION + ", " + DBHelper.PHOTO_NAME_THUMBNAIL + ", " + DBHelper.PHOTO_NAME
                + " FROM " + DBHelper.PHOTO + " where " + DBHelper.PHOTO_INSPECTION + "=" + InsId.toString();

        Cursor cursor = database.rawQuery(SQL, null);

        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {  // цикл по списку фото

                Integer ph_id = (Integer) cursor.getInt(cursor.getColumnIndex(DBHelper.PHOTO_ID));
                // удалим основное фото
                File file = new File(path+"/"+ (String) cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME)));
                file.delete();

                // удалим Preview
                file = new File(path+"/"+ (String) cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME_THUMBNAIL)));
                file.delete();

                // почистим информацию в базе данных
                database.delete(DBHelper.PHOTO, DBHelper.PHOTO_ID+"="+InsId.toString(), null);
                Log.e("DB ", "Удалили фото : " + ph_id.toString() );
            }
        }
        if (!cursor.isClosed()) {
            cursor.close();
        }

    }

}
