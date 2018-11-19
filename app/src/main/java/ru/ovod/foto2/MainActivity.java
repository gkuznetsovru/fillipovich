package ru.ovod.foto2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Random;

import pub.devrel.easypermissions.EasyPermissions;

import  ru.ovod.foto2.ModelClass.ImageSenderInfo;




public class MainActivity extends AppCompatActivity {


    private ImageView MyImage;
    private EditText OrderEdit;
    private String mCurrentPhotoPath;
    private Uri photoURI;
    private Uri outputFileUri;
    private TextView filepath;
    private File file;


    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_GALLERY_CODE = 200;
    private static final int READ_REQUEST_CODE = 300;
    private static final String SERVER_PATH = "Path_to_your_server";
    private Uri uri;


    int serverResponseCode = 0;
    ProgressDialog dialog = null;
    String upLoadServerUri = null;
    final String uploadFilePath = "/mnt/sdcard/";
    final String uploadFileName = "service_lifecycle.png";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyImage=(android.widget.ImageView)findViewById(R.id.mImageView);
        OrderEdit=(android.widget.EditText)findViewById(R.id.editText);
        filepath=(android.widget.TextView)findViewById(R.id.filepath);
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


    String generateUniqueFileName() {
        String filename = "";
        long millis = System.currentTimeMillis();
        String datetime = ""; //new Date().toGMTString();
//        datetime = datetime.replace(" ", "");
//        datetime = datetime.replace(":", "");

        //String rndchars = randomString(16);
        //filename = rndchars + "_" + datetime + "_" + millis;

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
//        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
//            Bundle extras = data.getExtras();
//            android.graphics.Bitmap imageBitmap = (android.graphics.Bitmap) extras.get("data");
//            MyImage.setImageBitmap(imageBitmap);
//        }

/*        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            MyImage.setImageURI(photoURI);
        }*/

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

            };
        }


    }






/*    private String getRealPathFromURIPath(Uri contentURI, Activity activity) {
        Cursor cursor = activity.getContentResolver().query(contentURI, null, null, null, null);
        if (cursor == null) {
            return contentURI.getPath();
        } else {
            cursor.moveToFirst();
            int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
            return cursor.getString(idx);
        }
    }
    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        if(uri != null){
            String filePath = getRealPathFromURIPath(uri, MainActivity.this);
            File file = new File(filePath);
            RequestBody mFile = RequestBody.create(MediaType.parse("image/*"), file);
            MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("file", file.getName(), mFile);
            RequestBody filename = RequestBody.create(MediaType.parse("text/plain"), file.getName());
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_PATH)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            UploadImageInterface uploadImage = retrofit.create(UploadImageInterface.class);
            Call<UploadObject> fileUpload = uploadImage.uploadFile(fileToUpload, filename);
            fileUpload.enqueue(new Callback<UploadObject>() {
                @Override
                public void onResponse(Call<UploadObject> call, Response<UploadObject> response) {
                    Toast.makeText(MainActivity.this, "Success " + response.message(), Toast.LENGTH_LONG).show();
                    Toast.makeText(MainActivity.this, "Success " + response.body().toString(), Toast.LENGTH_LONG).show();
                }
                @Override
                public void onFailure(Call<UploadObject> call, Throwable t) {
                    Log.d(TAG, "Error " + t.getMessage());
                }
            });
        }
    }
    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d(TAG, "Permission has been denied");
    } */




}
