package ru.ovod.CarInspection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import static ru.ovod.CarInspection.MainActivity.calculateInSampleSize;


// класс, в который я собрал функции по работе с изображения
public class PhotoHelper {

    private Context context;

    // Конструктор (Context - обязательный параетр)
    public PhotoHelper(Context context) {
        this.context = context;
    }


    // чтение изображения из файла
    public void LoadImageFromFile(File file, ImageView imageView)
    {
        // Штатная функции - не переворачивает избражение
        //MyIimageViewmage.setImageURI(Uri.fromFile(file));

        // Чтение через Picasso с автоповоротом
        Picasso.get().load(file).placeholder(R.drawable.ic_file_download_black_24dp).into(imageView) ;
    }



    // функция возвращает угол для поворота изображения
    public static Integer angleToReturn(Context context, Uri selectedImage) throws IOException {

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

    // поворот изображения
    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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


    // функция чтения файла в Bitmap
    public static Bitmap LoadImageIntoBitmap(String path) {

        // Читаем с inJustDecodeBounds=true для определения размеров
        final BitmapFactory.Options options = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(path, options);
    }




    // функция накладывает изображение (использую для залитых фото)
    // На bitmap1 накладывается  bitmap2
    public Bitmap overlayBitmapToCenter(Bitmap bitmap1, Bitmap bitmap2) {

//        Bitmap bitmap2 = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_upload);

        int bitmap1Width = bitmap1.getWidth();
        int bitmap1Height = bitmap1.getHeight();
        int bitmap2Width = bitmap2.getWidth();
        int bitmap2Height = bitmap2.getHeight();


        float marginLeft = (float) (bitmap1Width * 0.5 - bitmap2Width * 0.5);
        float marginTop = (float) (bitmap1Height * 0.5 - bitmap2Height * 0.5);

        //создаем пустой битмап с размерами как 1-й битмап
        Bitmap overlayBitmap = Bitmap.createBitmap(bitmap1Width, bitmap1Height, bitmap1.getConfig());
        //создаем canvas
        Canvas canvas = new Canvas(overlayBitmap);
        //наносим на canvas 1-й битмап
        canvas.drawBitmap(bitmap1, new Matrix(), null);
        //сверху наносим 2-й битмап (по центру)
        canvas.drawBitmap(bitmap2, marginLeft, marginTop, null);
        //возвращаем итоговый битмап
        return overlayBitmap;
    }



}
