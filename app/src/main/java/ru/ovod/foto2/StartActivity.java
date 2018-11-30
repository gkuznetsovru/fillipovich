package ru.ovod.foto2;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import static ru.ovod.foto2.MainActivity.verifyStoragePermissions;
import ru.ovod.foto2.ModelClass.EventModel;

public class StartActivity extends AppCompatActivity {

    TableLayout tableInspections;
    SQLiteDatabase database;  // база данных SQLite - с ней работаем в данном классе
    DBHelper dbhelper; // класс,  в котором задана структура нашей базы


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        tableInspections = findViewById(R.id.tableInspections);

        verifyStoragePermissions(this);
        dbhelper = new DBHelper(getApplicationContext());
        database = dbhelper.getWritableDatabase();


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_neworder);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        GetInspectionListFromDB();
    }


    // Фунция получает список Inspections из базу
    public  void GetInspectionListFromDB() {

        // очистикм tablelayout
        tableInspections.removeAllViewsInLayout();

        // получим из базы список Актов

        String SQL = "SELECT " + DBHelper.INSPECTION_ID + ", " + DBHelper.INSPECTION_NUMBER + ", "+ DBHelper.INSPECTION_ORDERID + ", "
                + " (SELECT count(*) from  "+ DBHelper.PHOTO + " where "+DBHelper.PHOTO_ISSYNC+"=0 and "
                + DBHelper.PHOTO+"."+DBHelper.PHOTO_INSPECTION+" = " + DBHelper.INSPECTION+"."+DBHelper.INSPECTION_ID + ") as coun"
                // + " 10 as coun"
                + " FROM " + DBHelper.INSPECTION + " Order by "+ DBHelper.INSPECTION_ID;
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {
                String num = cursor.getString(cursor.getColumnIndex(DBHelper.INSPECTION_NUMBER));
                Integer OrdID = cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ORDERID));
                Integer InsID = cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ID));
                Integer Coun = cursor.getInt(cursor.getColumnIndex("coun"));
                AddTableRow(num,OrdID,InsID, Coun);
                Log.e("DB ", "Извлекли INSPECTION_ID: " + InsID);
            }
        }
        if (!cursor.isClosed()) {cursor.close();}
        return;
    }



    // функция доабвление строки
    private void  AddTableRow(String Numd, Integer Int_OrderId, Integer inspectid, Integer coun)
    {

        TextView col1= new TextView(this);
        //TextView col2= new TextView(this);
        //TextView col3= new TextView(this);


        col1.setText( Html.fromHtml("№ <b>"+Numd +"</b> "+" Фото: <b>" +coun.toString()+"</b>"));
        //col3.setText(" Фото: <b>" +coun.toString()+"</b>");

        TableRow tableRow = new TableRow(this);

        tableRow.addView(col1);
        //tableRow.addView(col2);
        //tableRow.addView(col3);

        tableRow.setTag(inspectid);

        //tableRow.setTag(inspectid, tableRow);

        tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                for (int i = 0; i < tableInspections.getChildCount(); i++) {
                    View row = tableInspections.getChildAt(i);
                    if (row == v) {
                        row.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        //Change this to your normal background color.
                        row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }
                }
                //ClearInspection();
                //SetInspectionId((Integer) v.getTag());
                // GetPhotoList(); // !!! Внимание ! Временно отключил формирвоание списка, чтоб быстрее работало. готовлю демо-версию к запуску



            }
        });

        tableRow.setPadding(5,9,5,9);

        tableInspections.addView(tableRow);
    }




}
