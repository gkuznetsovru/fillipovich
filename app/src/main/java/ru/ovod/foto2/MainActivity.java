package ru.ovod.foto2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;


import android.util.Log;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;


import pub.devrel.easypermissions.EasyPermissions;

import ru.ovod.foto2.ModelClass.EventModel;


public class MainActivity extends AppCompatActivity {

    // Объявление глобальных переменных
    private Integer InspectionID =0; // InspectionID - текущий (активный) акт осмотра
    private String InspectionID_Number = ""; // Номер заказ-наряда, который привязан к выбранному InspectionID (объявлен выше)
    private Integer OrderID = 0;  // OrderID. Внимание !! Устанавливать через SetOrderID
    private EditText OrderEdit; //поле Edit с номером ЗН. Инициализируется OnCreate.


    DBHelper dbhelper; // класс,  в котором задана структура нашей локальной базы
    SQLiteDatabase database;  // база данных SQLite - с ней работаем в данном классе
    DataSet dataset = new DataSet(); // общий класс для доступа к базе овода
    SettingsHelper settingshelper; // класс по работе с настройками

    private Integer count_sending_images = 0 ; // счётчик отправляемых в текущий момент фто на сервер. Нужно для управление внешним видом кнопки Upload.
                                               // устанавливать только через setCount_sending_images !!
    private ImageView MyImage;
    private Uri photoURI;
    private Uri outputFileUri;
    private TextView operationlog;
    private File file;
    private File thumbnaul_file;
    private String path;
    private Button uploadbutton;
    private String Tmp_Number = "";


    TextView model;
    TextView vin;
    TextView dateorder;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static final int REQUEST_IMAGE_CAPTURE = 3374;  // зададим случайный код для активности получения фото
    static final int REQUEST_ORDERID = 3477;  // зададим случайный код для OderId

    //private TableLayout tablelayout;
    private TableLayout photoLayout;

    ProgressDialog dialog = null;


    @Override
    protected void onRestart() {
        super.onRestart();

     /*   if ( (OrderEdit.getText().toString()=="") && (Tmp_Number.toString()!="") ) // если номер пуст, а был ранее указан, то опять его заполним. Эта функция нужна для возврат из формы выбора ЗН кнопкой Back
        {
            OrderEdit.setText(Tmp_Number);
        }*/

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // инициализируем все наши переменные
        setContentView(R.layout.activity_main);
        MyImage= findViewById(R.id.mImageView);
        OrderEdit= findViewById(R.id.editText);
        operationlog= findViewById(R.id.filepath);
        path = Environment.getExternalStorageDirectory().toString();
        //tablelayout = findViewById(R.id.tablelayout);
        photoLayout = findViewById(R.id.phototablelayout);
        uploadbutton = findViewById(R.id.uploadbutton);

        verifyStoragePermissions(this);
        dbhelper = new DBHelper(getApplicationContext());
        database = dbhelper.getWritableDatabase();

        model = findViewById(R.id.model);
        vin = findViewById(R.id.vin);
        dateorder = findViewById(R.id.dateorder);

        settingshelper = new SettingsHelper(getApplicationContext());

        // Allow application use internet
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        FloatingActionButton fabPhoto = (FloatingActionButton) findViewById(R.id.fabPhoto);
        fabPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NewPhotoClick(view);

            }
        });




        OrderEdit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.setFocusable(true);
                v.setFocusableInTouchMode(true);
                return false;
            }
        });


        // проверим полученнные парметры при активации формы
        Intent intent = getIntent();
        InspectionID  = intent.getIntExtra("InsId",0);
        if (InspectionID>0)  // если передан параметр, то извлечём вс из базы данных
          {SetInspectionId(InspectionID);
           MyImage.requestFocus();
          }
           else
               {NewOrder();} // иначе готовим форму для нового акта


        GetPhotoList();
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
                Intent intent = new Intent(MainActivity.this,  Settings.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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
            setOrderID(0); // сразу сбросим OrderID
            InspectionID = GetInspectionIDByNumber(); // поищем тот что вбил мастер

            // Пока отключил проверку OrderId, чтоб время не терять на проверказ. Не знаю, верно это или нет
            //if (OrderID==0) // если OкderID не определён, то поищем его по номеру ЗН в БД Овода (если доступна база)
            //{
            //    GetOrderIdByNumber();
            //}

            if (InspectionID == 0) {  // Если 0, то  сгенерим новый
                InspectionID = CreateNewInspection();
            }
            InspectionID_Number = OrderEdit.getText().toString();  // запомним текущий номер ЗН
            //return;
        }

        saveFullImage(GetFileName());
    }


    // сформируем новый акт по ЗН
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
            setOrderID(cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ORDERID)));
            Log.e("DB ", "Извлекли INSPECTION_ID: " + id);
            //}
        }
        if (!cursor.isClosed()) {cursor.close();}
        return id;
    }



    // Фунция получает список Фото из базы
    public  void GetPhotoList() {

        // проверим, что выбран (определён) акт осомотра
        if (!CheckInspectionId(true)) {return;}


        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.imagegallery);
        recyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(),settingshelper.getCounter_cols()); // тут задаём количество фото в колонке
        recyclerView.setLayoutManager(layoutManager);

        ArrayList<CreateListPhoto> createLists = new ArrayList<>();;  // объявим массив фото


        // получим из базы список Фото
        String SQL = "SELECT " + DBHelper.PHOTO_ID + ", " + DBHelper.PHOTO_INSPECTION + ", "+ DBHelper.PHOTO_NAME_THUMBNAIL + ", "+ DBHelper.PHOTO_NAME  + ", "+ DBHelper.PHOTO_ISSYNC
                + " FROM " + DBHelper.PHOTO + " where " + DBHelper.PHOTO_INSPECTION + "="+InspectionID.toString();

        Cursor cursor = database.rawQuery(SQL, null);

        if (!cursor.isAfterLast()) {
            while (cursor.moveToNext()) {  // цикл по списку фото

                CreateListPhoto createList = new CreateListPhoto(); // объявим эксземпляр (фото)

                Integer ph_id = (Integer) cursor.getInt(cursor.getColumnIndex(DBHelper.PHOTO_ID));
                createList.setImage_id(ph_id);
                createList.setFilename_thumdnail(cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME_THUMBNAIL)));
                createList.setFilename(cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME)));
                createList.setImage_title(ph_id.toString()); // пока имени картинки дадим Photo_ID. Оно нигде не выводится
                createList.setIsSync((Integer) cursor.getInt(cursor.getColumnIndex(DBHelper.PHOTO_ISSYNC))); // пока имени картинки дадим Photo_ID. Оно нигде не выводится
                createLists.add(createList);
                Log.e("DB ", "Добавилили фото в список: " + cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME_THUMBNAIL)));
            }
        }
        if (!cursor.isClosed()) {cursor.close();}

        PhotoAdapter adapter = new PhotoAdapter(createLists, getApplicationContext(), path, MyImage);
        recyclerView.setAdapter(adapter);

        return;
    }



    // функци подготовки системы к вводу нового акта
    public void ClearInspection() {

        // почистим переменные
        OrderEdit.setText("");
        InspectionID = 0;
        setOrderID(0);
        InspectionID_Number = "";
        model.setText(getString(R.string.modelbaseline));
        vin.setText(getString(R.string.vinbaseline));
    }

    //  подготовим форму для формирование нового акта
    public void NewOrder() {
        ClearInspection();

        OrderEdit.setFocusable(true);
        OrderEdit.setFocusableInTouchMode(true);
        OrderEdit.requestFocus();
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
                   operationlog.append(responseMessage+"\n");
               }
               else {
                operationlog.append("Файл загружен на сервер:\n");
                operationlog.append(event.getMessage() + "\n");
                File file = new File(path+"/"+event.getMessage());
                Boolean b = true; // затычка вместо удаления файла
                // Boolean b = file.delete();  // удаление файлов отключено

                if (b)
                {
                    //operationlog.append("Файл удалён:\n");
                    //operationlog.append(event.getMessage() + "\n");

                    // пометим в базе, что файл сихронизирован
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DBHelper.PHOTO_ISSYNC, 1);
                    int Inspect = database.update(DBHelper.PHOTO, contentValues, DBHelper.PHOTO_NAME+"=?", new String[] { event.getMessage() });
                    Log.e("Фото сихронизированно в базе:", event.getMessage() );

                }
                setCount_sending_images(count_sending_images-1); // уменьшим счётчик загружаемых изображений
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


    // генерация уникального имени файла
    String generateUniqueFileName() {
        String filename = "";
        long millis = System.currentTimeMillis();
        String datetime = ""; //new Date().toGMTString();


        filename ="_" + millis;
        return filename;
    }


    // генерацияи случайного имени файла
    private String GetFileName() {
        String logFileName="";

        String gUF = generateUniqueFileName();  // получим случайную строку
        logFileName=OrderEdit.getText()+"_"+gUF+".jpg";

        // сгенерим сразу File для Thumbnail
        thumbnaul_file = new File(path+"/"+OrderEdit.getText()+"_"+gUF+"_small.jpg");

        return logFileName;
    }


    // функция записи Bitmap (Thumbnail) в файл
    private void saveBitmap(Bitmap bitmap,String path){
        if(bitmap!=null){
            try {
                FileOutputStream outputStream = null;
                try {
                    outputStream = new FileOutputStream(path); //here is set your file path where you want to save or also here you can set file object directly

                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream); // bitmap is your Bitmap instance, if you want to compress it you can compress reduce percentage
                    // PNG is a lossless format, the compression factor (100) is ignored
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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



    // функция возвращает угол для поворота изображения
    private static Integer angleToReturn(Context context, Uri selectedImage) throws IOException {

        InputStream input = context.getContentResolver().openInputStream(selectedImage);
        ExifInterface ei;
        if (Build.VERSION.SDK_INT > 23)
            ei = new ExifInterface(input);
        else
            ei = new ExifInterface(selectedImage.getPath());

        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:
                return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:
                return 270;
            default:
                return 0;
        }
    }


    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }



    // определим функцию получени резултатов (обращение к другим формам или активностя)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        // обработаем получение фото
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {


            // запишем информацию о фото в базу
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.PHOTO_PATH, file.getPath());
            contentValues.put(DBHelper.PHOTO_NAME, file.getName());
            contentValues.put(DBHelper.PHOTO_NAME_THUMBNAIL, thumbnaul_file.getName());
            contentValues.put(DBHelper.PHOTO_INSPECTION, InspectionID);
            contentValues.put(DBHelper.PHOTO_ISSYNC, 0);
            Long Inspect = database.insert(DBHelper.PHOTO, null, contentValues);
            Integer id =  Inspect !=null ? Inspect.intValue() :null;
            Log.e("ID добавленной фото:", id.toString());
            Log.e("Preview:", thumbnaul_file.getPath());

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
                //MyImage.setImageURI(outputFileUri);

                // сформируем сразу Preview
                int px = 85;
                Bitmap myBitmap = decodeSampledBitmapFromResource( file.getAbsolutePath(), px, px);

                Integer angle = 0; // по умолчанию не поворачиваем
                try {
                    angle = angleToReturn( getApplicationContext(),Uri.fromFile(file));
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (angle>0) {
                    myBitmap = RotateBitmap(myBitmap, angle);  // перевернём Preview

                    // заодно перевернём большое изображение в асинхронном потоке
                    PhotoAsyncTask photoAsync = new PhotoAsyncTask(angle,file);
                    photoAsync.execute();
                }
                saveBitmap(myBitmap, thumbnaul_file.getPath());

                MyImage.setImageURI(Uri.fromFile(thumbnaul_file));
            }

            operationlog.append("Файл сформирован:\n");
            operationlog.append(file.getName() + "\n");

            GetPhotoList(); // обновим список фото
        }

        // обработаем получение OrderID
        if (requestCode == REQUEST_ORDERID && resultCode == RESULT_OK)
        {
            OrderID = (Integer) data.getIntExtra("OrdId",0);
            if (OrderID>0) { // если получен OrderID
                OrderEdit.setText(GetNumberByOrderId(OrderID));  // получим OrderId

                GetOrderIdByNumber(); // заполним модель и VIN
                SaveOrderInfoToInspection();

            }

        }
    }


    // запись в локальную базу информации об Inspection
    private  void SaveOrderInfoToInspection() {
        // зальём в нашу БД выбранную информаци по PY.
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.INSPECTION_NUMBER, OrderEdit.getText().toString());
        contentValues.put(DBHelper.INSPECTION_MODEL, model.getText().toString());
        contentValues.put(DBHelper.INSPECTION_VIN, vin.getText().toString());
        contentValues.put(DBHelper.INSPECTION_DATE, dateorder.getText().toString());
        contentValues.put(DBHelper.INSPECTION_ORDERID, OrderID);
        int Inspect = database.update(DBHelper.INSPECTION, contentValues, DBHelper.INSPECTION_ID + "=?", new String[]{InspectionID.toString()});
        Log.e("Изменили в локальной базе Inspection:", InspectionID.toString());
    }


    /*private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        return image;
    } */


    private void saveFullImage(String fn) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(path,
                fn);
        outputFileUri =  Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        //intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        intent.putExtra(MediaStore.EXTRA_SCREEN_ORIENTATION, ActivityInfo.SCREEN_ORIENTATION_FULL_USER); // фигня какая-то.. Не работает этот параметр
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);

    }

    // функция проверяет, определён ли InspectionId и вызвает функции формирование InspectionId
    public Boolean CheckInspectionId(Boolean silent) {  // silent - не показывать сообщение

        if (InspectionID == 0) {
            InspectionID = GetInspectionIDByNumber(); // поищем тот что вбил мастер
            if (InspectionID == 0) {
                if (!silent) {showToast("Не найден акт осмотра");}
                return Boolean.FALSE;
            }
        }
        return Boolean.TRUE;
    }


    // функция синхронизации
    public void Sync(View view) {

        if (!isOnline())
        {
            showToast("Нет подключения к сети");
        return;
        }

        // проверим, что выбран коррретный акт
        if (!CheckInspectionId(false)) {return;}


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
                operationlog.append("Начало отправки файла:\n");
                operationlog.append(cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME)) + "\n");
                Log.e("DB ", "Начали отправку файла: " + cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME)).toString() );
                setCount_sending_images(count_sending_images+1);
                ru.ovod.foto2.NetworkRelatedClass.NetworkCall.fileUpload(path+"/"+cursor.getString(cursor.getColumnIndex(DBHelper.PHOTO_NAME)).toString(), new ru.ovod.foto2.ModelClass.ImageSenderInfo(OrderID.toString(), OrderEdit.getText().toString() ));
            }
        }
        else
        {
            showToast("Все изображения уже залиты на севрер.");
        }
        if (!cursor.isClosed()) {cursor.close();}

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
        dataset.GetJSONFromWEB("select orderid, number, date, vin, model from TechnicalCentre.dbo.V_ActualOrderForOrderPhotos with(NoLock) where number='"+OrderEdit.getText()+"'");
        if (dataset.RecordCount()>0)
        {
            dataset.GetRowByNumber(0);
            setOrderID(dataset.FieldByName_AsInteger("orderid"));
            model.setText(dataset.FieldByName_AsString("model"));
            vin.setText(dataset.FieldByName_AsString("vin"));
            dateorder.setText( dataset.FieldByName_AsString("date").substring(0,10));

        }

    }


    // фунция ищет Number на сервере Овода по номеру
    public String GetNumberByOrderId(Integer OrdId)
    {

        String res = "";
        if (!isOnline()) {return res;} // если не в сети, то не получаем инфу с сервера
        dataset.GetJSONFromWEB("select number from TechnicalCentre.dbo.V_ActualOrderForOrderPhotos with(NoLock) where OrderId="+OrdId.toString()+" ");
        if (dataset.RecordCount()>0)
        {
            dataset.GetRowByNumber(0);
            res = dataset.FieldByName_AsString("number");
        }
        return res;

    }


    // функция устанавливает InspectionId (излекает данные из базы)
    public void SetInspectionId (Integer InsID)
    {
        InspectionID = InsID;

        String SQL = "SELECT " + DBHelper.INSPECTION_NUMBER + ", " + DBHelper.INSPECTION_ORDERID + ", "
                + DBHelper.INSPECTION_MODEL + ", " + DBHelper.INSPECTION_VIN + ", " + DBHelper.INSPECTION_ISSYNC + ", " + DBHelper.INSPECTION_DATE + " "
                + " FROM " + DBHelper.INSPECTION + " where " + DBHelper.INSPECTION_ID +" = "+InspectionID.toString()+" ";
        Cursor cursor = database.rawQuery(SQL, null);
        if (!cursor.isAfterLast()) {
            cursor.moveToFirst();
            setOrderID(cursor.getInt(cursor.getColumnIndex(DBHelper.INSPECTION_ORDERID)));
            OrderEdit.setText( cursor.getString(cursor.getColumnIndex(DBHelper.INSPECTION_NUMBER)));
            model.setText( cursor.getString(cursor.getColumnIndex(DBHelper.INSPECTION_MODEL)));
            vin.setText( cursor.getString(cursor.getColumnIndex(DBHelper.INSPECTION_VIN)));
            dateorder.setText(cursor.getString(cursor.getColumnIndex(DBHelper.INSPECTION_DATE)));
            InspectionID_Number=OrderEdit.getText().toString();
            Log.e("DB ", "Извлекли  данные по INSPECTION_ID: " + InspectionID.toString());
        }
        if (!cursor.isClosed()) {cursor.close();}
    }


    // клик для кнопки поиска
    public void SearchClick(View view) {

        if (!isOnline())  // если не в сети, то не получаем инфу с сервера
        {
            showToast("Нет подключения к сети.");
            return;
        }


        if  (OrderEdit.getText().length() == 0) {  // Проверим, что ЗН выбран
            showToast("Укажите номер заказ-наряда.");
            return;
        }

        if (InspectionID_Number!=OrderEdit.getText().toString()) { // если предыдущий номер ЗН для сканирования был другой
            setOrderID(0); // сразу сбросим OrderID
            InspectionID = GetInspectionIDByNumber(); // поищем тот что вбил мастер

            if (InspectionID == 0) {  // Если 0, то  сгенерим новый
                InspectionID = CreateNewInspection();
            }
            InspectionID_Number = OrderEdit.getText().toString();  // запомним текущий номер ЗН
            //return;
        }


        if (InspectionID==0)  // если не определён акт осмотра (такого не текущий момент не должно быть)
        {
            showToast("Не выбран акт осмотра (или нет фотографий).");
            return;
        }

        // зачистим переменны перед поиском
        setOrderID(0);
        model.setText("");
        vin.setText("");
        dateorder.setText("");

        GetOrderIdByNumber(); // ищем данные по нммеру акта

        if (OrderID==0) // если ЗН не обнаружен
        {
            // то запросим у менеджера выбор ЗН
            Intent questionIntent = new Intent(MainActivity.this, SelectOrderActivity.class);
            startActivityForResult(questionIntent, REQUEST_ORDERID);
        }
        else
        {
            // запишем в локальную базу обновлённую инфомрацию
            SaveOrderInfoToInspection();
        }
    }


    // Setter для OrderID.  Должен отключать кнопку
    public void setOrderID(Integer orderID) {
        OrderID = orderID;

        // отключение кнопки пока убрал.. Не знаю, надо ли блокировать. Т.к. при нажатии есть проверка и сообщение.
        // uploadbutton.setEnabled(OrderID>0); // отключим кнопку, если OrderID не опередлён

    }


    // Setter для счётчкиа выгружаемых изображений. Меняем кнопку Upload
    public void setCount_sending_images(Integer count_sending_images) {
        if (count_sending_images>0)
        {
            uploadbutton.setText("Идёт отправка фотографий.");
            uploadbutton.setEnabled(false); // отключим кнопку, если идёт  отравка фото
        }
        else
        {
            uploadbutton.setText(getString(R.string.uploadtestbutton));
            uploadbutton.setEnabled(true); // отключим кнопку, если идёт  отравка фото
            if (this.count_sending_images > count_sending_images)  // если предыдущее значение было больше 0, то обновим список Preview (чтоб наложить изображение)
            {
                GetPhotoList();
            }
        }
        this.count_sending_images = count_sending_images;
    }





}
