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
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
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
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;
import android.util.Log;



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
    private TableRow SelectedTableRow;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private TableLayout tablelayout;
    private TableLayout photoLayout;

    ProgressDialog dialog = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyImage= findViewById(R.id.mImageView);
        OrderEdit= findViewById(R.id.editText);
        filepath= findViewById(R.id.filepath);
        path = Environment.getExternalStorageDirectory().toString();
        tablelayout = findViewById(R.id.tablelayout);
        photoLayout = findViewById(R.id.phototablelayout);

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
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    // Функция клика по новой фото
    public void NewPhotoClick(View view) {

        if  (OrderEdit.getText().length() == 0) {  // Проверим, что ЗН выбран
            showToast("Укажите номер заказ-наряда.");
            return;
        }

        if (InspectionID_Number!=OrderEdit.getText().toString()) { // если предыдущий номер ЗН для сканирования был другой
            OrderID=0; // сразу сбросим OrderID
            InspectionID = GetInspectionIDByNumber(); // поищем тот что вбил мастер

            // Пока отключил проверку OrderId, чтоб время не терять
            //if (OrderID==0) // если OкderID не определён, то поищем его по номеру ЗН в БД Овода (если доступна база)
            //{
            //    GetOrderIdByNumber();
            //}

            if (InspectionID == 0) {  // Если 0, то  сгенерим новый
                InspectionID = CreateNewInspection();
            }
            InspectionID_Number = OrderEdit.getText().toString();  // запомним текущий номер ЗН
        }

        saveFullImage(GetFileName());
    }


    // сформируем новй акт по ЗН
    public  Integer CreateNewInspection() {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.INSPECTION_NUMBER, OrderEdit.getText().toString());
            if (OrderID>0)  // если известен номер ЗН
            {
                if (OrderID>0)  // то его тоже запишем в базу
                {
                    contentValues.put(DBHelper.INSPECTION_ORDERID, OrderID.toString());
                }
            }
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
        String SQL = "SELECT " + DBHelper.INSPECTION_ID + ", " + DBHelper.INSPECTION_ORDERID + " "
                    + " FROM " + DBHelper.INSPECTION + " where " + DBHelper.INSPECTION_NUMBER +" = '"+OrderEdit.getText().toString()+"'";
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            cursor.moveToFirst();
            //while (cursor.moveToNext()) {
            id = cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ID));
            OrderID = cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ORDERID));
            Log.e("DB ", "Извлекли INSPECTION_ID: " + id);
            //}
        }
        if (!cursor.isClosed()) {cursor.close();}
        return id;
    }



    // Фунция получает список Inspections из базу и заполняете TableLayout
    public  void GetPhotoList() {

        // проверим, что выбран (определён) акт осомотра
        if (!CheckInspectionId()) {return;}

        // очистикм tablelayout
        photoLayout.removeAllViewsInLayout();

        // получим из базы список Актов
        String SQL = "SELECT " + DBHelper.PHOTO_ID + ", " + DBHelper.PHOTO_INSPECTION + ", "+ DBHelper.PHOTO_NAME
                + " FROM " + DBHelper.PHOTO + " where " + DBHelper.PHOTO_INSPECTION + "="+InspectionID.toString();

        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {
                Integer PhoID = cursor.getInt(cursor.getColumnIndex(DBHelper.PHOTO_ID));
                String PhoNa = cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME));
                AddTableRowPhoto(PhoNa,PhoID);
                Log.e("DB ", "Добавилили фото в список: " + PhoNa);
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
    public void onEvent(EventModel event) {
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
                //Boolean b = Boolean.TRUE;
                if (b)
                {
                    filepath.append("Файл удалён:\n");
                    filepath.append(event.getMessage() + "\n");

                    // пометим в базе, что файл сихронизирован
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DBHelper.PHOTO_ISSYNC, 1);
                    int Inspect = database.update(DBHelper.PHOTO, contentValues, DBHelper.PHOTO_NAME+"=?", new String[] { event.getMessage() });
                    Log.e("Фото сихронизированно в базе:", event.getMessage() );

                }
            }
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
            contentValues.put(DBHelper.PHOTO_PATH, file.getPath());
            contentValues.put(DBHelper.PHOTO_NAME, file.getName());
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

    public Boolean CheckInspectionId() {

        if (InspectionID == 0) {
            InspectionID = GetInspectionIDByNumber(); // поищем тот что вбил мастер
            if (InspectionID == 0) {
                showToast("Не найден акт осмотра");
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }

    public void Sync(View view) {

        //filepath.setText(GetFileName());
        if (!isOnline())
        {
            showToast("Нет подключения к сети");
        return;
        }

        // проверим, что выбран коррретный акт
        if (!CheckInspectionId()) {return;}


        if (OrderID==0) {
            GetOrderIdByNumber(); // получим Order Id
        }

        if (OrderID==0)
        {
            showToast("Не найден заказ-наряд с указанным номером в базе Овода");
            return;
        }


        String SQL = "SELECT " + DBHelper.PHOTO_ID + ", " + DBHelper.PHOTO_INSPECTION + ", "+ DBHelper.PHOTO_NAME
                + " FROM " + DBHelper.PHOTO + " WHERE " +DBHelper.PHOTO_ISSYNC+"=0 and " + DBHelper.PHOTO_INSPECTION +"=" + InspectionID.toString()  ;
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {
                // отправка файла
                Log.e("DB ", "Начали отправку файла: " + cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME)).toString() );
                ru.ovod.foto2.NetworkRelatedClass.NetworkCall.fileUpload(path+"/"+cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME)).toString(), new ru.ovod.foto2.ModelClass.ImageSenderInfo(OrderID.toString(), OrderEdit.getText().toString() ));
            }
        }
        if (!cursor.isClosed()) {cursor.close();}

/*        filepath.setText("");

        File directory = new File(path);
        File[] files = directory.listFiles();
        //filepath.append("Size: "+ files.length+"\n" );
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].getName().contains("jpg")) {
                filepath.append("Начало отправки файла:\n");
                filepath.append(files[i].getName() + "\n");
                ru.ovod.foto2.NetworkRelatedClass.NetworkCall.fileUpload(path+"/"+files[i].getName(), new ru.ovod.foto2.ModelClass.ImageSenderInfo(OrderID.toString(), OrderEdit.getText().toString() ));
                //break;
            }
        }*/
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


    // проверим разрешения
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

        tableRow.setTag(inspectid);

        //tableRow.setTag(inspectid, tableRow);

        tableRow.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {


                                            for (int i = 0; i < tablelayout.getChildCount(); i++) {
                                                View row = tablelayout.getChildAt(i);
                                                if (row == v) {
                                                    row.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                                                } else {
                                                    //Change this to your normal background color.
                                                    row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                                                }
                                            }
                                            SetInspectionId((Integer) v.getTag());
                                            GetPhotoList();
                                        }
                                    });


        tableRow.setPadding(5,9,5,9);

        tablelayout.addView(tableRow);
    }



    // функция сжатия изображения из примера  https://startandroid.ru/ru/uroki/vse-uroki-spiskom/372-urok-160-risovanie-bitmap-chtenie-izobrazhenij-bolshogo-razmera.html
    public static Bitmap decodeSampledBitmapFromResource(String path,
                                                         int reqWidth, int reqHeight) {

        // Читаем с inJustDecodeBounds=true для определения размеров
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        // Вычисляем inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Читаем с использованием inSampleSize коэффициента
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(path, options);
    }

    // функция сжатия изображения из примера  https://startandroid.ru/ru/uroki/vse-uroki-spiskom/372-urok-160-risovanie-bitmap-chtenie-izobrazhenij-bolshogo-razmera.html
    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        // Реальные размеры изображения

        int height =  options.outHeight;
        int width = options.outWidth;

        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Вычисляем наибольший inSampleSize, который будет кратным двум
            // и оставит полученные размеры больше, чем требуемые
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }


    // функция доабвление строки с картинкой
    private void  AddTableRowPhoto(String photo, Integer photoid)
    {

        ImageView im= new ImageView(this);


        int px = 85;
        File file = new File(path,photo);
        Bitmap bitmap = decodeSampledBitmapFromResource(file.getAbsolutePath(), px, px);
        Log.d("log", String.format("Required size = %s, bitmap size = %sx%s, byteCount = %s",
                px, bitmap.getWidth(), bitmap.getHeight(), bitmap.getByteCount()));
        im.setImageBitmap(bitmap);

        TableRow tableRow = new TableRow(this);
        tableRow.addView(im);
        tableRow.setTag(photoid);
       /* tableRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                for (int i = 0; i < tablelayout.getChildCount(); i++) {
                    View row = tablelayout.getChildAt(i);
                    if (row == v) {
                        row.setBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
                    } else {
                        //Change this to your normal background color.
                        row.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                    }
                }
                SetInspectionId((Integer) v.getTag());
            }
        });*/


        tableRow.setPadding(2,2,2,2);
        photoLayout.addView(tableRow);
    }




    // фунция ищет ЗН на сервере Овода по номеру
    public void GetOrderIdByNumber()
    {

        if (!isOnline()) {return;} // если не в сети, то не получаем инфу с сервера
        TextView model = findViewById(R.id.model);
        TextView vin = findViewById(R.id.vin);
        dataset.GetJSONFromWEB("select orderid, number, date, vin, model from TechnicalCentre.dbo.V_ActualOrderForOrderPhotos with(NoLock) where number='"+OrderEdit.getText()+"'");
        if (dataset.RecordCount()>0)
        {
            dataset.GetRowByNumber(0);
            OrderID = dataset.FieldByName_AsInteger("orderid");
            model.setText(dataset.FieldByName_AsString("model"));
            vin.setText(dataset.FieldByName_AsString("vin"));

        }

    }


    // функция устанавливает InspectionId (излекает данные из базы)
    public void SetInspectionId (Integer InsID)
    {
        InspectionID = InsID;

        String SQL = "SELECT " + DBHelper.INSPECTION_NUMBER + ", " + DBHelper.INSPECTION_ORDERID + " "
                + " FROM " + DBHelper.INSPECTION + " where " + DBHelper.INSPECTION_ID +" = "+InspectionID.toString()+" ";
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            cursor.moveToFirst();
            OrderID = cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ORDERID));
            OrderEdit.setText( cursor.getString(cursor.getColumnIndex(DBHelper.INSPECTION_NUMBER)));
            InspectionID_Number=OrderEdit.getText().toString();
            Log.e("DB ", "Извлекли  данные по INSPECTION_ID: " + InspectionID.toString());
        }
        if (!cursor.isClosed()) {cursor.close();}
    }

    // клик для кнопки поиска
    public void SearchClick(View view) {
        GetOrderIdByNumber();
    }
}
