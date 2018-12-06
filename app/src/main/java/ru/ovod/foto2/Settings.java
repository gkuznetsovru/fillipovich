package ru.ovod.foto2;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Environment;
import android.os.TestLooperManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.io.File;

import static ru.ovod.foto2.DBHelper.INSPECTION;

public class Settings extends Activity {

    private SQLiteDatabase database;
    private DBHelper dbhelper;
    TextView versionText;
    private CheckBox checkBox;
    private EditText editcolumns;

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

        versionText=findViewById(R.id.VersionText);
        versionText.setText(" ФотоОвод. Версия "+BuildConfig.VERSION_NAME);

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
        settingsHelper.setShow_synchronized_acts(checkBox.isChecked());
    }

    // метод читает установленные настройки
    @Override
    protected void onResume() {
        super.onResume();
        checkBox.setChecked(settingsHelper.getShow_synchronized_acts());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    public void ClickClearFileAndDB(View view) {

        // почистим файлы в директории
        String path;
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
        database.execSQL("delete from  " + dbhelper.PHOTO);
    }
}
