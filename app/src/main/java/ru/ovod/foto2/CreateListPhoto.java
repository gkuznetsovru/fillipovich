package ru.ovod.foto2;

import java.util.ArrayList;

public class CreateListPhoto {

    private String image_title;  // имя фото для названия (пока нигже не использую)
    private String filename_thumdnail; // имя Preview
    private String filename; // имя полной фото
    private Integer image_id; // PHOTO_ID

    public CreateListPhoto() {
    }

    public String getImage_title() {
        return image_title;
    }

    public void setImage_title(String image_title) {
        this.image_title = image_title;
    }

    public Integer getImage_id() {
        return image_id;
    }

    public void setImage_id(Integer image_id) {
        this.image_id = image_id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }


    public String getFilename_thumdnail() {
        return filename_thumdnail;
    }

    public void setFilename_thumdnail(String filename_thumdnail) {
        this.filename_thumdnail = filename_thumdnail;
    }


}



