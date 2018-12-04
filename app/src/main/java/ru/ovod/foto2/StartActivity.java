package ru.ovod.foto2;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static ru.ovod.foto2.MainActivity.verifyStoragePermissions;
import ru.ovod.foto2.ModelClass.EventModel;

public class StartActivity extends AppCompatActivity {

    TableLayout tableInspections;
    SQLiteDatabase database;  // база данных SQLite - с ней работаем в данном классе
    DBHelper dbhelper; // класс,  в котором задана структура нашей базы
    Integer selectedInspectionId = 0; // выбранная в таблице InspectionId

    ItemsAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        verifyStoragePermissions(this);
        dbhelper = new DBHelper(getApplicationContext());
        database = dbhelper.getWritableDatabase();


        adapter = new ItemsAdapter();
        final ListView items = (ListView) findViewById(R.id.items);
        items.setAdapter(adapter);
        items.setClickable(true);
        items.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Inspection item = (Inspection) parent.getItemAtPosition(position);
                        startAddCarInspectionActivity(item.get_inspectionid());
                    }
                }
        );

        RefreshList(); // обновим список


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_neworder);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this,  MainActivity.class);
                intent.putExtra("InsId", selectedInspectionId);
                startActivity(intent);

                // Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //         .setAction("Action", null).show();
            }
        });

      /*  tableInspections = findViewById(R.id.tableInspections);




        GetInspectionListFromDB();*/
    }


    // обновим список при возврате из дочерних Activity
    @Override
    protected void onRestart() {
        super.onRestart();
        RefreshList();
    }

    // подцепим меню
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mainmenu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // получим идентификатор выбранного пункта меню
        int id = item.getItemId();


        // Операции для выбранного пункта меню
        switch (id) {
            case R.id.Menu_Settings:
                Intent intent = new Intent(StartActivity.this,  Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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


                try {
                        for (int i = 0; i < tableInspections.getChildCount() ; i++) {
                            View row = tableInspections.getChildAt(i);
                            if (row == v) {
                                row.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                            } else {
                                //Change this to your normal background color.
                                row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        }
                        }
                }
                catch (Exception e)
                {
                    selectedInspectionId =0;
                }
                Intent intent = new Intent(StartActivity.this,  MainActivity.class);
                intent.putExtra("InsId", selectedInspectionId);
                startActivity(intent);
                //ClearInspection();
                //SetInspectionId((Integer) v.getTag());
                // GetPhotoList(); // !!! Внимание ! Временно отключил формирвоание списка, чтоб быстрее работало. готовлю демо-версию к запуску



            }
        });

        tableRow.setPadding(5,9,5,9);

        tableInspections.addView(tableRow);
    }



    // Determines if Action bar item was selected. If true then do corresponding action.
 /*   @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        //handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.acti:
                startAddCarInspectionActivity(0);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }*/

    private void startAddCarInspectionActivity(int id){
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("InsId", id);
        startActivity(intent);
    }

    private class ItemsAdapter extends ArrayAdapter<Inspection> {
        public ItemsAdapter() {
            super(StartActivity.this, R.layout.list_item_inspection);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            final View view = getLayoutInflater().inflate(R.layout.list_item_inspection, null);
            final Inspection item = getItem(position);

            if (item.getPhotoCo() > 0) { ((TextView) view.findViewById(R.id.viewPhotoCo)).setText(String.valueOf(item.getPhotoCo())); }
            ((TextView) view.findViewById(R.id.viewNumber)).setText(String.valueOf(item.getNumber()));
            /*if (item.getDate().getTime() == 0) {
                ((TextView) view.findViewById(R.id.viewDate)).setText("");
            }else {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.mm.yyyy");
                ((TextView) view.findViewById(R.id.viewDate)).setText(dateFormat.format(item.getDate()));
            }*/
            ((TextView) view.findViewById(R.id.viewDate)).setText(item.getDate());
            ((TextView) view.findViewById(R.id.viewModel)).setText(item.getModel());
            ((TextView) view.findViewById(R.id.viewVIN)).setText(item.getVin());
            ((CheckBox) view.findViewById(R.id.chbIsSynced)).setChecked(item.getIssync() == 1);

            return view;
        }

    }


    // Фунция получает список Inspections из базу
    public void RefreshList() {

        // сначала почистим ListView
        //ListView items = (ListView) findViewById(R.id.items);
        //items.removeAllViewsInLayout();
        //adapter.notifyDataSetChanged();

        // получим из базы список Актов
        ArrayList<Inspection> inspectionList;

        //inspectionList.clear();
        //adapter.notifyDataSetChanged();

        inspectionList = dbhelper.getInspectionList();

        for (Inspection item: inspectionList) {
            adapter.add(item);
        }

        return;
    }



}
