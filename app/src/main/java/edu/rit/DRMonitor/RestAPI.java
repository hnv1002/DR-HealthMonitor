package edu.rit.DRMonitor;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * API calls that device can make to Pi's server to perform tasks
 */

public interface RestAPI {
    // Send a simple message to the Pi and make sure it gets it
    @GET("message")
    Call<Result> getMessage(@Query("input") String input);

    // Request to retrieve a file on the Pi
    @GET("file")
    Call<ResponseBody> getFile(@Query("directory") String directory, @Query("filename") String filename);

    // Request to retrieve list of files available on the Pi
    @GET("get_files")
    Call<Result> getFiles(@Query("directory") String dir, @Query("start") long startDate, @Query("end") long endDate);

    // Request to check if server is available or the device can connect to the Pi
    @GET("ping_server")
    Call<ResponseBody> pingServer();

    // Request to update calibration file on the Pi
    @GET("update_calibration")
    Call<ResponseBody> updateCalibrationSettings(@Query("input") String settings);

    // Request to get USB storage on the Pi
    @GET("get_usb_storage")
    Call<String> getUsbStorage();
}
