package edu.rit.DRMonitor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Chris on 11/3/2017.
 */

public interface RestAPI {
    @GET("message")
    Call<Result> getMessage(@Query("input") String input);

    @GET("file")
    Call<ResponseBody> getFile(@Query("directory") String directory, @Query("filename") String filename);

    @GET("get_files")
    Call<Result> getFiles(@Query("directory") String dir, @Query("start") long startDate, @Query("end") long endDate);

    @GET("ping_server")
    Call<ResponseBody> pingServer();

    @GET("update_calibration")
    Call<ResponseBody> updateCalibrationSettings(@Query("input") String settings);

    @GET("get_usb_storage")
    Call<String> getUsbStorage();
}
