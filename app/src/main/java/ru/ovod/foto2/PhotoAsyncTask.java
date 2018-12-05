package ru.ovod.foto2;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PhotoAsyncTask extends AsyncTask<Void, Void, Void> {

    Integer angle;
    File file;

    public PhotoAsyncTask(Integer angle, File file) {
        this.angle = angle;
        this.file = file;
    }

    public void setFile(File file) {
        this.file = file;
    }


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
 //       tvInfo.setText("Begin");
    }

    public static Bitmap RotateBitmap(Bitmap source, float angle)
    {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }


    @Override
    protected Void doInBackground(Void... params) {
        try {
   //         TimeUnit.SECONDS.sleep(2);
            Bitmap bigbitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            bigbitmap = RotateBitmap(bigbitmap, angle);  // перевернём Preview
            saveBitmap(bigbitmap, file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
     //   tvInfo.setText("End");
    }

    public void setAngle(Integer angle) {
        this.angle = angle;
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



}