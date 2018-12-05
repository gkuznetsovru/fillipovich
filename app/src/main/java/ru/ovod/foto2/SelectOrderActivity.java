package ru.ovod.foto2;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class SelectOrderActivity extends AppCompatActivity {

    DataSet dataset = new DataSet(); // Инициализируем класс доступа к базе Овода
    TableLayout tableorders;
    Integer orderId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_order);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ReturnOrder();
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tableorders = (TableLayout) findViewById(R.id.TableOrders);
        GetOrderFromWebServer();

        //setTitle("Поймал мышек: ");
    }

    // фунция ищет ЗН на сервере Овода по номеру
    public void GetOrderFromWebServer() {


        dataset.GetJSONFromWEB("select orderid, number, date, vin, model from TechnicalCentre.dbo.V_ActualOrderForOrderPhotos with(NoLock) order by number ");
        if (dataset.RecordCount() > 0) {
            for (int i = 0; i < dataset.RecordCount(); i++) {
                dataset.GetRowByNumber(i);

                AddTableRow(
                        dataset.FieldByName_AsString("number"),
                        dataset.FieldByName_AsString("date"),
                        dataset.FieldByName_AsInteger("orderid"),
                        dataset.FieldByName_AsString("model"),
                        dataset.FieldByName_AsString("vin")
                );

            }

        }
    }



        // функция доабвление строки
        private void  AddTableRow(String Numd, String dat, Integer OrderId, String Mod, String Vi)
        {

            TextView col1= new TextView(this);


            col1.setText( Html.fromHtml("№ <b><font size='3'>"+Numd +"</font></b> от  "+dat.substring(0, 10)+" Модель: <b>" +Mod+"</b>"));
            TableRow tableRow = new TableRow(this);

            tableRow.addView(col1);
            tableRow.setTag(OrderId);


            tableRow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    /*Intent answerIntent = new Intent();
                    answerIntent.putExtra("OrdId", (Integer) v.getTag());
                    //answerIntent.putExtra("Num", 344);
                    setResult(RESULT_OK, answerIntent);
                    finish();*/
                    for (int i = 0; i < tableorders.getChildCount(); i++) {
                        View row = tableorders.getChildAt(i);
                        if (row == v) {
                            row.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                        } else {
                            //Change this to your normal background color.
                            row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                        }
                    }
                    orderId = (Integer) v.getTag();

                }
            });


            tableRow.setPadding(5,9,5,9);

            tableorders.addView(tableRow);
        }


    public void ReturnOrder() {
        if (orderId>0) {
            Intent answerIntent = new Intent();
            answerIntent.putExtra("OrdId", orderId);
            //answerIntent.putExtra("Num", 344);
            setResult(RESULT_OK, answerIntent);
            finish();
        }
    }
}

