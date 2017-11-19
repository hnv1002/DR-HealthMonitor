package edu.rit.drsystemhealthmonitor;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static java.security.AccessController.getContext;

/**
 * Created by Chris on 11/3/2017.
 */

public class APIClient implements Callback<Result> {

    static final String BASE_URL = "http://192.168.0.16:8080/api/";
    private String input;

    public APIClient(String input) {
        this.input = input;
    }

    public void start() {
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        RestAPI restAPI = retrofit.create(RestAPI.class);

        Call<Result> call = restAPI.getMessage(input);
        call.enqueue( this);
    }

    @Override
    public void onResponse(Call<Result> call, Response<Result> response) {
        if(response.isSuccessful()) {
            Result result = response.body();
            System.out.println(result);
        } else {
            System.out.println(response.errorBody());
        }
    }

    @Override
    public void onFailure(Call<Result> call, Throwable t) {
        t.printStackTrace();
    }
}
