package ru.ovod.CarInspection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.ArrayList;

import static ru.ovod.CarInspection.MainActivity.calculateInSampleSize;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.ViewHolder> {
    private ArrayList<CreateListPhoto> galleryList;
    private Context context;
    private String path; // путь к фото
    private ImageView bigimageview; // ссылка на ImageView, куда надо положить большое фото при клике

    private PhotoHelper photoHelper;


    public PhotoAdapter(ArrayList<CreateListPhoto> galleryList, Context context, String path, ImageView bigimageview) {
        this.galleryList = galleryList;
        this.context = context;
        this.path = path;
        this.bigimageview = bigimageview;
        photoHelper = new PhotoHelper(this.context);
    }

    @Override
    public PhotoAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.photo_preview, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(PhotoAdapter.ViewHolder viewHolder, int i) {



        viewHolder.img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        /*
        int px = 85;
        File file = new File(path,galleryList.get(i).getFilename_thumdnail());
        Bitmap bitmap = photoHelper.decodeSampledBitmapFromResource(file.getAbsolutePath(), px, px);

        if (galleryList.get(i).getIsSync()>0) {
            bitmap = overlayBitmapToCenter(bitmap);
        }

        viewHolder.img.setTag(i);
        viewHolder.img.setImageBitmap(bitmap);
        */

        File file = new File(path,galleryList.get(i).getFilename_thumdnail());
        Bitmap bitmap = photoHelper.LoadImageIntoBitmap(file.getAbsolutePath());
        if (galleryList.get(i).getIsSync()>0) {
            bitmap = photoHelper.overlayBitmapToCenter(bitmap,  BitmapFactory.decodeResource(context.getResources(), android.R.drawable.ic_menu_upload));
        }
        viewHolder.img.setTag(i);
        viewHolder.img.setImageBitmap(bitmap);


        viewHolder.img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File file = new File(path,galleryList.get( (int) v.getTag()).getFilename());


                // далее идё блок, который показывает изображение, если идёт переворот файла
                /*
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());

                if (bitmap==null)  // если большого изображения нет, то подсунем Preview (такой моежет быть, если изображение ещё сохраняется)
                {
                    file = new File(path,galleryList.get( (int) v.getTag()).getFilename_thumdnail());
                    bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                }

                bigimageview.setImageBitmap(bitmap);
                */

                photoHelper.LoadImageFromFile(file,bigimageview);
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


}
