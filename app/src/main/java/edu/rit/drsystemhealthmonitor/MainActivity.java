package edu.rit.drsystemhealthmonitor;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    static final String BASE_URL = "http://192.168.0.16:8080/api/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
    }

    /** Called when the user taps the Get button */
    public void sendMessageRequest(View view) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                Gson gson = new GsonBuilder()
                        .setLenient()
                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
                // Send a request to RestAPI
                RestAPI restAPI = retrofit.create(RestAPI.class);
                EditText editText = (EditText) findViewById(R.id.editText);
                Call<Result> call = restAPI.getMessage(editText.getText().toString());
                call.enqueue(new Callback<Result>() {
                    @Override
                    public void onResponse(Call<Result> call, Response<Result> response) {
                        System.out.println(response.code());
                        if (response.isSuccessful()) {
                            Intent intent = new Intent(getBaseContext(), DisplayMessageActivity.class);
                            String message = response.body().toString();
                            intent.putExtra("result", message);
                            startActivity(intent);
                        }
                    }

                    @Override
                    public void onFailure(Call<Result> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
                /*try {
                    Result result = call.execute().body();
                    Intent intent = new Intent(getBaseContext(), DisplayMessageActivity.class);
                    String message = result.toString();
                    intent.putExtra("result", message);
                    startActivity(intent);
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
            }
        });
        thread.start();
    }

    public void getFileRequest(View view) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build();
        // Send a request to RestAPI
        RestAPI restAPI = retrofit.create(RestAPI.class);
        EditText editText = (EditText) findViewById(R.id.editText2);
        final String filename = editText.getText().toString();
        Call<ResponseBody> call = restAPI.getFile(filename);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body(), filename);
                            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "file.zip");
                            if (writtenToDisk) {
                                Intent intent = new Intent(getBaseContext(), DisplayMessageActivity.class);
                                String message = "Sucessfully download the file";
                                intent.putExtra("result", message);
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getBaseContext(), DisplayMessageActivity.class);
                                String message = "Failed to download the file";
                                intent.putExtra("result", message);
                                startActivity(intent);
                            }
                            return null;
                        }
                    }.execute();
                } else {
                    Intent intent = new Intent(getBaseContext(), DisplayMessageActivity.class);
                    String message = "Failed to download the file";
                    intent.putExtra("result", message);
                    startActivity(intent);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean writeResponseBodyToDisk(ResponseBody body, String filename) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + filename + ".zip");
            System.out.println(file.getAbsolutePath());
            InputStream inputStream = null;
            OutputStream outputStream = null;

            int check = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (check == PackageManager.PERMISSION_GRANTED) {
                try {
                    byte[] fileReader = new byte[4096];
                    inputStream = body.byteStream();
                    outputStream = new FileOutputStream(file);

                    while (true) {
                        int read = inputStream.read(fileReader);

                        if (read == -1) {
                            break;
                        }

                        outputStream.write(fileReader, 0, read);
                    }

                    outputStream.flush();

                    return true;
                } catch (IOException e) {
                    System.out.println("error when writing file" + e);
                    return false;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } else {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1024);
                try {
                    byte[] fileReader = new byte[4096];
                    inputStream = body.byteStream();
                    outputStream = new FileOutputStream(file);

                    while (true) {
                        int read = inputStream.read(fileReader);

                        if (read == -1) {
                            break;
                        }

                        outputStream.write(fileReader, 0, read);
                    }

                    outputStream.flush();

                    return true;
                } catch (IOException e) {
                    System.out.println("error when writing file" + e);
                    return false;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("couldn't get write directory" + e);
            return false;
        }
    }
}
