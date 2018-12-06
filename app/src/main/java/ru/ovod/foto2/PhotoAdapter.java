package ru.ovod.foto2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

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

        if (galleryList.get(i).getIsSync()>0) {
            bitmap = overlayBitmapToCenter(bitmap);
        }

        viewHolder.img.setTag(i);
        viewHolder.img.setImageBitmap(bitmap);

        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(path,galleryList.get( (int) v.getTag()).getFilename());
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

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




    // функция накладывает изображение (использую для залитых фото)
    public Bitmap overlayBitmapToCenter(Bitmap bitmap1) {

        Bitmap bitmap2 = BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_upload);

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
