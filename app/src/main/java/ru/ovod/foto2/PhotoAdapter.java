package ru.ovod.foto2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;

import static ru.ovod.foto2.MainActivity.calculateInSampleSize;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private ArrayList<CreateListPhoto> galleryList;
    private Context context;
    private String path; // путь к фото
    private ImageView bigimageview; // ссылка на ImageView, куда надо положить большое фото при клике


    public PhotoAdapter(ArrayList<CreateListPhoto> galleryList, Context context, String path, ImageView bigimageview) {
        this.galleryList = galleryList;
        this.context = context;
        this.path = path;
        this.bigimageview = bigimageview;
    }

    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_preview, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoAdapter.ViewHolder viewHolder, int i) {
        //viewHolder.title.setText(galleryList.get(i).getImage_title());
        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        //viewHolder.img.setImageResource((galleryList.get(i).getImage_id()));

        int px = 85;
        File file = new File(path,galleryList.get(i).getFilename_thumdnail());
        Bitmap bitmap = decodeSampledBitmapFromResource(file.getAbsolutePath(), px, px);
        //Log.d("log", String.format("Required size = %s, bitmap size = %sx%s, byteCount = %s",
//                px, bitmap.getWidth(), bitmap.getHeight(), bitmap.getByteCount()));
        viewHolder.img.setTag(i);
        viewHolder.img.setImageBitmap(bitmap);

        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* int px = 300;
                File file = new File(path,galleryList.get( (int) v.getTag()).getFilename());
                Bitmap bitmap = decodeSampledBitmapFromResource(file.getAbsolutePath(), px, px);
                Log.d("log", String.format("Required size = %s, bitmap size = %sx%s, byteCount = %s",
                        px, bitmap.getWidth(), bitmap.getHeight(), bitmap.getByteCount()));
                bigimageview.setImageBitmap(bitmap);*/

                File file = new File(path,galleryList.get( (int) v.getTag()).getFilename());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                //Log.d("log", String.format("bitmap size = %sx%s, byteCount = %s",
//                        bitmap.getWidth(), bitmap.getHeight(),
  //                      (int) (bitmap.getByteCount() / 1024)));

                if (bitmap==null)  // если большого изображения нет, то подсунем Preview (такой моежет быть, если изображение ещё сохраняется)
                {
                    file = new File(path,galleryList.get( (int) v.getTag()).getFilename_thumdnail());
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                }


                bigimageview.setImageBitmap(bitmap);
                //bigimageview.setRotation(90);
            }
        });


    }

    /*private static Bitmap rotateImage(Bitmap img, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedImg = Bitmap.createBitmap(img, 0, 0, img.getWidth(), img.getHeight(), matrix, true);
        img.recycle();
        return rotatedImg;
    }*/

    @Override
    public int getItemCount() {
        return galleryList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        //private TextView title;
        private ImageView img;

        public ViewHolder(View view) {
            super(view);

            //title = (TextView) view.findViewById(R.id.title);
            img = (ImageView) view.findViewById(R.id.img);
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


}
