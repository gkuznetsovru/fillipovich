package ru.ovod.CarInspection;

import android.app.LauncherActivity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.ArrayList;
import static ru.ovod.CarInspection.MainActivity.verifyStoragePermissions;

public class StartActivity extends AppCompatActivity  implements SearchView.OnQueryTextListener  {

    //TableLayout tableInspections;
    SQLiteDatabase database;  // база данных SQLite - с ней работаем в данном классе
    DBHelper dbhelper; // класс,  в котором задана структура нашей базы
    Integer selectedInspectionId = 0; // выбранная в таблице InspectionId

    ItemsAdapter adapter;

    ArrayList<Inspection> inspectionList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        verifyStoragePermissions(this);
        dbhelper = new DBHelper(getApplicationContext());
        database = dbhelper.getWritableDatabase();

        adapter = new ItemsAdapter();
        ListView items = findViewById(R.id.items);
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

        //items.setTextFilterEnabled(true); // активируем фильтр


        RefreshList(); // обновим список


        FloatingActionButton fab = findViewById(R.id.fab_neworder);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StartActivity.this,  MainActivity.class);
                intent.putExtra("InsId", selectedInspectionId);
                startActivity(intent);

            }
        });

      /*  tableInspections = findViewById(R.id.tableInspections);
        GetInspectionListFromDB();*/
    }

    // функции реакции на ввод текста в строке поиска
    @Override
    public boolean onQueryTextChange(String s) {

        if (inspectionList.size()>0)  // если список не пуст, то производим поиск
        {
            // очистим ListView
            adapter.clear();
            adapter.notifyDataSetChanged();

            if (TextUtils.isEmpty(s))  // если строка пустая, то быстро обратно весь список закинем
            {
                for (Inspection item : inspectionList) {
                    adapter.add(item);
                }
            } else  // иначе фильтруем список
            {
                for (Inspection item : inspectionList) {
                    if (item.getNumber().toString().toUpperCase().contains(s.toUpperCase())
                        |  item.getModel().toUpperCase().contains(s.toUpperCase())
                        | item.getVin().toUpperCase().contains(s.toUpperCase())  )
                    {
                        adapter.add(item);
                    }
                }
            }
        }
        return false;
    }

    // функция реакции на нажатия на кнопку поиска (обязательно должна быть определа)
    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
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
        getMenuInflater().inflate(R.menu.mainmenu, menu);  // добавим главное меню

        // далее идёт блок для определания кнопки контектсного оиска
        getMenuInflater().inflate(R.menu.menu_search, menu); // добавил меню с кнопкой поиска
        MenuItem searchItem = menu.findItem(R.id.search);  // определим кнопку поиска
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem); // привяжем к кнопке формирование сроки поиска
        searchView.setOnQueryTextListener(this); // и зададим реакцию на поиск

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


    /*// Фунция получает список Inspections из базу
    public  void GetInspectionListFromDB() {

        // очистикм tablelayout
        tableInspections.removeAllViewsInLayout();

        // получим из базы список Актов

        String SQL = "SELECT " + DBHelper.INSPECTION_ID + ", " + DBHelper.INSPECTION_NUMBER + ", "+ DBHelper.INSPECTION_ORDERID + ", "
                + " (SELECT count(*) from  "+ DBHelper.PHOTO + " where "+DBHelper.PHOTO_ISSYNC+"=0 and "
                + DBHelper.PHOTO+"."+DBHelper.PHOTO_INSPECTION+" = " + DBHelper.INSPECTION+"."+DBHelper.INSPECTION_ID + ") as coun"
                // + " 10 as coun"
                + " FROM " + DBHelper.INSPECTION
//                + actsparam
                + " Order by "+ DBHelper.INSPECTION_ID;
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
    */


    // функция вызова MainActivity
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
        if (inspectionList!=null) {
            inspectionList.clear();
            adapter.clear();
            adapter.notifyDataSetChanged();
        }

        // получим из базы список Актов
        inspectionList = dbhelper.getInspectionList();
        for (Inspection item: inspectionList) {
            adapter.add(item);
        }

        return;
    }



}
