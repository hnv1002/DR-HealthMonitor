package edu.rit.drsystemhealthmonitor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

/**
 * Created by Chris on 11/3/2017.
 */

public interface RestAPI {
    @GET("message")
    Call<Result> getMessage(@Query("input") String input);

    @GET("file")
    Call<ResponseBody> getFile(@Query("input") String filename);
}
