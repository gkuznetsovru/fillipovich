
package ru.ovod.CarInspection.ModelClass;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

// объявим класс для передачи информации вместе с фото на базе Интерфейса Parcelable (посылаем)
public class ImageSenderInfo implements Parcelable {

    @SerializedName("orderid")
    private String orderid;

    @SerializedName("number")
    private String number;

    public ImageSenderInfo() {
    }

    // Constructor c нужными переменными
    public ImageSenderInfo(String orderid, String number) {
        this.orderid = orderid;
        this.number = number;
    }

    public final static Parcelable.Creator<ImageSenderInfo> CREATOR = new Creator<ImageSenderInfo>() {

        @SuppressWarnings({
            "unchecked"
        })
        public ImageSenderInfo createFromParcel(Parcel in) {
            ImageSenderInfo instance = new ImageSenderInfo();
            instance.orderid = ((String) in.readValue((String.class.getClassLoader())));
            instance.number = ((String) in.readValue((String.class.getClassLoader())));
            return instance;
        }

        public ImageSenderInfo[] newArray(int size) {
            return (new ImageSenderInfo[size]);
        }

    };


    // загрузим наши данные в посылку
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(orderid);
        dest.writeValue(number);
    }

    public int describeContents() {
        return  0;
    }

}
