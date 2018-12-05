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



    public static final String APP_PREFERENCES = "ovodsettings";
    public static final String APP_PREFERENCES_COUNTER = "counter_cols";
    public static final String APP_PREFERENCES_SHOWSYNCACTS = "showsyncacts"; // показывать ли синхронизированные акты
    private SharedPreferences mSettings;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);

        // инициализируем БД
        dbhelper = new DBHelper(getApplicationContext());
        database = dbhelper.getWritableDatabase();

        checkBox = findViewById(R.id.checkBox);
        editcolumns = findViewById(R.id.editcolumns);

        versionText=findViewById(R.id.VersionText);
        versionText.setText(" ФотоОвод. Версия "+BuildConfig.VERSION_NAME);


        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        database.close();
        dbhelper.close();
    }


    @Override
    protected void onPause() {
        super.onPause();

        // Запоминаем данные
        SharedPreferences.Editor editor = mSettings.edit();
        editor.putBoolean(APP_PREFERENCES_SHOWSYNCACTS, checkBox.isChecked());
     //   editor.putInt(APP_PREFERENCES_COUNTER, Integer.parseInt(editcolumns.getText().toString()));
        editor.apply();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mSettings.contains(APP_PREFERENCES_SHOWSYNCACTS)) {
            // Получаем число из настроек
            checkBox.setChecked(mSettings.getBoolean(APP_PREFERENCES_SHOWSYNCACTS, false));
        }
       /* if (mSettings.contains(APP_PREFERENCES_COUNTER)) {
            // Получаем число из настроек
            editcolumns.setText( mSettings.getInt(APP_PREFERENCES_COUNTER, 4));        }*/
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
