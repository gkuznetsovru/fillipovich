package ru.ovod.foto2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.Menu;
import android.widget.TextView;


import pub.devrel.easypermissions.EasyPermissions;

import ru.ovod.foto2.ModelClass.EventModel;


public class MainActivity extends AppCompatActivity {

    // Объявление глобальных переменных
    private Integer InspectionID =0; // InspectionID - текущий (активный) акт осмотра
    private String InspectionID_Number = ""; // Номер заказ-наряда, который привязан к выбранному InspectionID (объявлен выше)
    private Integer OrderID = 0;  // OrderID
    private EditText OrderEdit; //поле Edit с номером ЗН. Инициализируется OnCreate.


    DBHelper dbhelper; // класс,  в котором задана структура нашей базы
    SQLiteDatabase database;  // база данных SQLite - с ней работаем в данном классе
    DataSet dataset = new DataSet(); // общий класс для доступа к базе овода


    private ImageView MyImage;
    private Uri photoURI;
    private Uri outputFileUri;
    private TextView filepath;
    private File file;
    private String path;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private TableLayout tablelayout;

    ProgressDialog dialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyImage=(android.widget.ImageView)findViewById(R.id.mImageView);
        OrderEdit=(android.widget.EditText)findViewById(R.id.editText);
        filepath=(android.widget.TextView)findViewById(R.id.filepath);
        path = Environment.getExternalStorageDirectory().toString();
        tablelayout = (TableLayout)findViewById(R.id.tablelayout);

        verifyStoragePermissions(this);
        dbhelper = new DBHelper(getApplicationContext());
        database = dbhelper.getWritableDatabase();

        // Allow application use internet
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

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
            case R.id.Menu_ListInspections:
                SetListInspection();
                return true;
            case R.id.Menu_OrdersFromServer:
              // infoTextView.setText("Вы выбрали кошку!");
                return true;
            case R.id.Menu_Settings:
              //  infoTextView.setText("Вы выбрали котёнка!");
                Intent intent = new Intent(MainActivity.this,  Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void SetListInspection()
    {
        GetInspectionListFromDB();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);
    }

    // проверка доступности сети
    // ВНИМАНИЕ !!!
    // проверка пока просто проверяет, есть сеть или нет.
    // возможно, в будущем лучше будет доработать проверку доступности конкретного ресурса
    // примеров полно: http://qaru.site/questions/13922/how-to-check-internet-access-on-android-inetaddress-never-times-out
    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) return true;
        else return false;
    }

    // Функция клика по новой фото
    public void NewPhotoClick(View view) {

        if  (OrderEdit.getText().length() == 0) {  // Проверим, что ЗН выбран
            showToast("Укажите номер заказ-наряда.");
            return;
        }

        if (InspectionID_Number!=OrderEdit.getText().toString()) { // если предыдущий номер ЗН для сканирования был другой
            InspectionID = GetInspectionIDByNumber(); // поищем тот что вбил мастер
            if (InspectionID == 0) {  // Если 0, то  сгенерим новый
                InspectionID = CreateNewInspection();
            }
            InspectionID_Number = OrderEdit.getText().toString();
        }
        saveFullImage(GetFileName());
    }


    // сформируем новй акт по ЗН
    public  Integer CreateNewInspection() {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.INSPECTION_NUMBER, OrderEdit.getText().toString());
            Long Inspect = database.insert(DBHelper.INSPECTION, null, contentValues);
            Integer id =  Inspect !=null ? Inspect.intValue() :null;
            Log.e("ID", InspectionID.toString());
            return id;
    }


    // Фунция поиска INSPECTION_ID по номеру акта осмотра
    public  Integer GetInspectionIDByNumber() {
        //return 0;
        Integer id=0;
    //    String SQL = "SELECT " + DBHelper.INSPECTION_ID + " " + " FROM " + DBHelper.INSPECTION;
        String SQL = "SELECT " + DBHelper.INSPECTION_ID + " "
                    + " FROM " + DBHelper.INSPECTION + " where " + DBHelper.INSPECTION_NUMBER +" = '"+OrderEdit.getText().toString()+"'";
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            cursor.moveToFirst();
            //while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ID));
            Log.e("DB ", "Извлекли INSPECTION_ID: " + id);
            //}
        }
        if (!cursor.isClosed()) {cursor.close();}
        return id;
    }



    // Фунция получает список Inspections из базу и заполняете TableLayout
    public  void GetPhotoList() {

        // очистикм tablelayout
        tablelayout.removeAllViewsInLayout();

        // получим из базы список Актов
        String SQL = "SELECT " + DBHelper.PHOTO_ID + ", " + DBHelper.PHOTO_INSPECTION + ", "+ DBHelper.PHOTO_NAME
                + " FROM " + DBHelper.PHOTO;
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {
                String num = cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME));
                Integer OrdID = 0;
                Integer InsID = cursor.getInt(cursor.getColumnIndex(DBHelper.PHOTO_INSPECTION));
                Integer Coun = cursor.getInt(cursor.getColumnIndex(DBHelper.PHOTO_ID));
                AddTableRow(num,OrdID,InsID, Coun);
                Log.e("DB ", "Извлекли INSPECTION_ID: " + InsID);
            }
        }
        if (!cursor.isClosed()) {cursor.close();}
        return;
    }


    // Фунция получает список Inspections из базу
    public  void GetInspectionListFromDB() {

        // очистикм tablelayout
        tablelayout.removeAllViewsInLayout();

        // получим из базы список Актов

        String SQL = "SELECT " + DBHelper.INSPECTION_ID + ", " + DBHelper.INSPECTION_NUMBER + ", "+ DBHelper.INSPECTION_ORDERID + ", "
                + " (SELECT count(*) from  "+ DBHelper.PHOTO + " where "+ DBHelper.PHOTO+"."+DBHelper.PHOTO_INSPECTION+" = " + DBHelper.INSPECTION+"."+DBHelper.INSPECTION_ID + ") as coun"
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

    public void NewOrder(View view) {
        OrderEdit.setText(""); // Восстановить !!
        }



    // генерация случайной строки для имени файла
    public static final String DATA = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static Random RANDOM = new Random();
    public static String randomString(int len) {
            StringBuilder sb = new StringBuilder(len);

            for (int i = 0; i < len; i++) {
                sb.append(DATA.charAt(RANDOM.nextInt(DATA.length())));
            }

            return sb.toString();
        }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(EventModel event) throws ClassNotFoundException {
        if (event.isTagMatchWith("response")) {
            String responseMessage = "Response from Server:\n" + event.getMessage();
            if (event.getMessage().contains("error"))
               {
               filepath.append(responseMessage+"\n");
               }
               else {
                filepath.append("Файл отправлен:\n");
                filepath.append(event.getMessage() + "\n");
                File file = new File(path+"/"+event.getMessage());
                Boolean b = file.delete();
                if (b)
                {
                    filepath.append("Файл удалён:\n");
                    filepath.append(event.getMessage() + "\n");
                };
            };
        }
    }

    public void showToast(String mes) {
        //создаём и отображаем текстовое уведомление
        Toast toast = Toast.makeText(getApplicationContext(),
                mes,
                Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }


    String generateUniqueFileName() {
        String filename = "";
        long millis = System.currentTimeMillis();
        String datetime = ""; //new Date().toGMTString();


        filename ="_" + millis;
        return filename;
    }

    private String GetFileName() {
        String logFileName="";
        //String logFileName = new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date());
        //logFileName = OrderEdit.getText()+"_"+ logFileName+".jpg";

        logFileName=OrderEdit.getText()+"_"+generateUniqueFileName()+".jpg";
        return logFileName;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            // запишем информацию о фото в базу
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.PHOTO_PATH,  file.getPath().toString());
            contentValues.put(DBHelper.PHOTO_NAME, file.getName().toString() );
            contentValues.put(DBHelper.PHOTO_INSPECTION, InspectionID);
            contentValues.put(DBHelper.PHOTO_ISSYNC, 0);
            Long Inspect = database.insert(DBHelper.PHOTO, null, contentValues);
            Integer id =  Inspect !=null ? Inspect.intValue() :null;
            Log.e("ID добавленной фото:", InspectionID.toString());

            // Проверяем, содержит ли результат маленькую картинку
            if (data != null) {
                if (data.hasExtra("data")) {
                    Bitmap thumbnailBitmap = data.getParcelableExtra("data");
                    // Какие-то действия с миниатюрой
                    MyImage.setImageBitmap(thumbnailBitmap);
                }
            } else {
                // Какие-то действия с полноценным изображением,
                // сохраненным по адресу outputFileUri
                MyImage.setImageURI(outputFileUri);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        // Save a file: path for use with ACTION_VIEW intents
        //mCurrentPhotoPath = image.getAbsolutePath();




        return image;
    }



/*    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(this, "Error!", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.provider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }*/


/*    private void getThumbnailPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }*/


    private void saveFullImage(String fn) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(path,
                fn);
//                "Test.jpg");
        filepath.setText(file.getPath()+file.getName());
        outputFileUri =  Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }



    public void Sync(View view) {

        //filepath.setText(GetFileName());
        if (!isOnline())
        {
            showToast("Нет подключения к сети");
        return;
        };

        filepath.setText("");
        //String path = Environment.getExternalStorageDirectory().toString();
        //filepath.append("Path: " + path+"\n" );
        File directory = new File(path);
        File[] files = directory.listFiles();
        //filepath.append("Size: "+ files.length+"\n" );
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].getName().contains("jpg")) {
                filepath.append("Начало отправки файла:\n");
                filepath.append(files[i].getName() + "\n");
                String name = "gfdgfd"; // сопроводительная информация - пока оставил, потом что-нибудь полезное передавать будем
                int age = 45; // сопроводительная информация - пока оставил, потом что-нибудь полезное передавать будем
                ru.ovod.foto2.NetworkRelatedClass.NetworkCall.fileUpload(path+"/"+files[i].getName(), new ru.ovod.foto2.ModelClass.ImageSenderInfo(name, age));
                //break;
            };
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    // функция доабвление строки
    private void  AddTableRow(String Numd, Integer Int_OrderId, Integer inspectid, Integer coun)
    {

        TextView col1= new TextView(this);
        TextView col2= new TextView(this);
        TextView col3= new TextView(this);


        col1.setText("№ "+Numd +" ");
        col2.setText("ID:"+inspectid.toString());
        col3.setText(" Ф: " +coun.toString());

        TableRow tableRow = new TableRow(this);

        tableRow.addView(col1);
        tableRow.addView(col2);
        tableRow.addView(col3);
        tableRow.setPadding(5,7,5,7);

        tablelayout.addView(tableRow);
    }




    /*private String GetJSONFromWEB(String sql) throws JSONException {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("SQL","select orderid, number, date, vin, model from TechnicalCentre.dbo.V_ActualOrderForOrderPhotos");
        String JS=dbhelper.postRequest("https://smit.ovod.ru/upload/json.php",hashMap);


        JSONArray arr = new JSONArray(JS);

        String s = "";

        for (int i = 0; i < arr.length(); i++) {
            JSONObject c = arr.getJSONObject(i);
            String id = c.getString("orderid");
            s = s + '-' + id;
        }
        return  s;

    }*/



    public void TextClick(View view) {

        TextView tvWeb = (TextView) this.findViewById(R.id.filepath);


        String s = "";
        dataset.GetJSONFromWEB("select orderid, number, date, vin, model from TechnicalCentre.dbo.V_ActualOrderForOrderPhotos");


        s ="";

        for (int i = 0; i < dataset.RecordCount() ; i++) {
            if (dataset.GetRowByNumber(i))
            {
                s = s + '!' + dataset.FieldByName_AsInteger("orderid").toString();
            }
        }
        tvWeb.setText(s);

        //GetPhotoList();
    }
}
