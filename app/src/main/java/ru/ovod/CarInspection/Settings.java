package ru.ovod.CarInspection;

import android.app.Activity;

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
    private ImageView iv;

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
        versionText.setText("Версия "+BuildConfig.VERSION_NAME);

        iv = findViewById(R.id.imageViewOvod);
        iv.setImageResource(R.drawable.ovod);

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
        try
        {
            settingsHelper.setCounter_cols( Integer.parseInt( editcolumns.getText().toString()));
        }
        catch(NumberFormatException nfe) // если ошибка парсинга
        {
            settingsHelper.setCounter_cols(4);  // то поставим 4
        }
    }

    // метод читает установленные настройки
    @Override
    protected void onResume() {
        super.onResume();
        checkBox.setChecked(settingsHelper.getShow_synchronized_acts());
        editcolumns.setText(settingsHelper.getCounter_cols().toString());
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
