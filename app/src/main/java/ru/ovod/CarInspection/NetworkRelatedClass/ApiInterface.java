package ru.ovod.CarInspection.NetworkRelatedClass;

import ru.ovod.CarInspection.ModelClass.ResponseModel;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;


public interface ApiInterface {

    @Multipart
    @POST(RetrofitApiClient.BASE_URL) // возьмем адрес для отправки фото из статичной переменной класса RetrofitApiClient
    Call<ResponseModel> fileUpload(
            @Part("sender_information") RequestBody description,
            @Part MultipartBody.Part file);

}

