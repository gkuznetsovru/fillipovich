
package ru.ovod.foto2.ModelClass;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ImageSenderInfo implements Parcelable {

    @SerializedName("orderid")
    private String orderid;

    @SerializedName("number")
    private String number;

    public ImageSenderInfo() {
    }

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


    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(orderid);
        dest.writeValue(number);
    }

    public int describeContents() {
        return  0;
    }

}
