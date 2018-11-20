package ru.ovod.foto2.NetworkRelatedClass;

import ru.ovod.foto2.ModelClass.ResponseModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface ApiInterface {

    @Multipart
    @POST("https://smit.ovod.ru/upload/upl.php")
    Call<ResponseModel> fileUpload(
            @Part("sender_information") RequestBody description,
            @Part MultipartBody.Part file);

}

