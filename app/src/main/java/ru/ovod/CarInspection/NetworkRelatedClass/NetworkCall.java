package ru.ovod.CarInspection.NetworkRelatedClass;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;
import ru.ovod.CarInspection.ModelClass.EventModel;
import ru.ovod.CarInspection.ModelClass.ImageSenderInfo;
import ru.ovod.CarInspection.ModelClass.ResponseModel;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkCall {

    public static void fileUpload(String filePath, ImageSenderInfo imageSenderInfo) {

        ApiInterface apiInterface = RetrofitApiClient.getClient().create(ApiInterface.class);
        // Logger.addLogAdapter(new AndroidLogAdapter());

        File file = new File(filePath);
        //create RequestBody instance from file
        RequestBody requestFile = RequestBody.create(MediaType.parse("image"), file);

        // MultipartBody.Part is used to send also the actual file name
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        Gson gson = new Gson();
        String patientData = gson.toJson(imageSenderInfo);
        Log.e("JSON toSend:", patientData);

        RequestBody description = RequestBody.create(okhttp3.MultipartBody.FORM, patientData);

        // finally, execute the request
        Call<ResponseModel> call = apiInterface.fileUpload(description, body);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(@NonNull Call<ResponseModel> call, @NonNull Response<ResponseModel> response) {
                Logger.d("Response: " + response);

                ResponseModel responseModel = response.body();

                if(responseModel != null){
                    EventBus.getDefault().post(new EventModel("response", responseModel.getMessage()));
                    Log.e("Retrofit:", "Response code " + response.code() +
                            " Response Message: " + responseModel.getMessage());
                    //Logger.d("Response code " + response.code() +
                    //        " Response Message: " + responseModel.getMessage());
                } else
                    EventBus.getDefault().post(new EventModel("response", "ResponseModel is NULL"));
            }

            @Override
            public void onFailure(@NonNull Call<ResponseModel> call, @NonNull Throwable t) {
                Logger.d("Exception: " + t);
                EventBus.getDefault().post(new EventModel("response", t.getMessage()));
            }
        });
    }



}
