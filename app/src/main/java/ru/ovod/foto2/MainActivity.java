package ru.ovod.foto2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import pub.devrel.easypermissions.EasyPermissions;

import ru.ovod.foto2.ModelClass.EventModel;


public class MainActivity extends AppCompatActivity {


    private ImageView MyImage;
    private EditText OrderEdit;
    private String mCurrentPhotoPath;
    private Uri photoURI;
    private Uri outputFileUri;
    private TextView filepath;
    private File file;


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

 /*   private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_GALLERY_CODE = 200;
    private static final int READ_REQUEST_CODE = 300;
    private static final String SERVER_PATH = "Path_to_your_server";
    private Uri uri; */


 //   int serverResponseCode = 0;
    ProgressDialog dialog = null;
 /*   String upLoadServerUri = null;
    final String uploadFilePath = "/mnt/sdcard/";
    final String uploadFileName = "service_lifecycle.png"; */


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyImage=(android.widget.ImageView)findViewById(R.id.mImageView);
        OrderEdit=(android.widget.EditText)findViewById(R.id.editText);
        filepath=(android.widget.TextView)findViewById(R.id.filepath);

        verifyStoragePermissions(this);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, MainActivity.this);
    }

    public void MyClick(View view) {
//        android.content.Intent takePictureIntent = new android.content.Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
//        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
//        }
        //dispatchTakePictureIntent();
        saveFullImage(GetFileName());
    }

    public void NewOrder(View view) {
        OrderEdit.setText("");
        }



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
            filepath.append(responseMessage+"\n");
        }
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
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }



    private void dispatchTakePictureIntent() {
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
    }


    private void getThumbnailPicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }


    private void saveFullImage(String fn) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        file = new File(Environment.getExternalStorageDirectory(),
                fn);
//                "Test.jpg");
        filepath.setText(file.getPath()+file.getName());
        outputFileUri =  Uri.fromFile(file);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
    }



    public void Sync(View view) {

        //filepath.setText(GetFileName());

        filepath.setText("");
        String path = Environment.getExternalStorageDirectory().toString();
        //filepath.append("Path: " + path+"\n" );
        File directory = new File(path);
        File[] files = directory.listFiles();
        //filepath.append("Size: "+ files.length+"\n" );
        for (int i = 0; i < files.length; i++)
        {
            if (files[i].getName().contains("jpg")) {
                filepath.append("Начало отправки файла:\n");
                filepath.append(files[i].getName() + "\n");
                String name = "gfdgfd"; // сопроводительная информация
                int age = 45; // сопроводительная информация
                ru.ovod.foto2.NetworkRelatedClass.NetworkCall.fileUpload(path+"/"+files[i].getName(), new ru.ovod.foto2.ModelClass.ImageSenderInfo(name, age));
                filepath.append("Файл отправлен\n");
                //break;

            };
        }


    }

    /*@Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }*/


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

}
