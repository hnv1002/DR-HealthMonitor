package edu.rit.DRMonitor;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static edu.rit.DRMonitor.Utils.STORE_DIR;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    static String SERVER_IP = "172.24.1.1";
    static String SERVER_PORT = "8080";
    static String BASE_URL = "http://" + SERVER_IP + ":" + SERVER_PORT + "/api/";
    static String SYSTEM_SETTINGS_FILE = Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + "systemSettings.txt";
    static String CALIBRATION_SETTINGS_FILE = Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + "calibrationSettings.txt";
    static List<HistoricalDataFile> dataFiles = new ArrayList<>();
    static Map<String, float[]> latestData;
    static String latestFile;
    static SystemSettings serverSystemSettings;
    static CalibrationSettings serverCalibrationSettings;
    static boolean serverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SlidingUpPanelLayout panel = findViewById(R.id.sliding_layout);
        panel.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {}

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                ImageView arrow = findViewById(R.id.help_page_arrow);
                if (newState.equals(SlidingUpPanelLayout.PanelState.EXPANDED)) {
                    arrow.setImageResource(android.R.drawable.arrow_down_float);
                } else if (newState.equals(SlidingUpPanelLayout.PanelState.COLLAPSED)) {
                    arrow.setImageResource(android.R.drawable.arrow_up_float);
                }
            }
        });

        Toast toast = Toast.makeText(getApplicationContext(), "Connecting to server...", Toast.LENGTH_SHORT);
        toast.show();

        File dataFolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR);
        if (!dataFolder.exists()) {
            dataFolder.mkdir();
        }

        updateWifiSignal();
        serverSystemSettings = Utils.getSystemSettings(SYSTEM_SETTINGS_FILE);
        BASE_URL = "http://" + serverSystemSettings.getIpAddress() + ":" + serverSystemSettings.getServerPort() + "/api/";
        updateLastConnectedTime();
        serverSystemSettings.setLastConnectedTime(System.currentTimeMillis());
        Utils.updateSettingsFile(serverSystemSettings, SYSTEM_SETTINGS_FILE);
        serverCalibrationSettings = Utils.getCalibrationSettings(CALIBRATION_SETTINGS_FILE);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build();
        // Send a request to RestAPI
        RestAPI restAPI = retrofit.create(RestAPI.class);
        Call<ResponseBody> call = restAPI.pingServer();
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                TextView server_status = (TextView) findViewById(R.id.server_status_text);
                if (response.isSuccessful()) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Server is online", Toast.LENGTH_SHORT);
                    toast.show();
                    server_status.setText("ON");
                    serverAvailable = true;
                    getFileRequest(STORE_DIR, "latest");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                TextView server_status = (TextView) findViewById(R.id.server_status_text);
                Toast toast = Toast.makeText(getApplicationContext(), "Server is offline", Toast.LENGTH_SHORT);
                toast.show();
                server_status.setText("OFF");
                serverAvailable = false;
            }
        });

        // Create and populate a List of historical data files
        File folder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR);
        if (!folder.exists()) {
            new File(dataFolder.getPath()).mkdir();
        }

        dataFiles = Utils.getListOfFiles();
        latestFile = Utils.getLatestDataFile(dataFiles);
        if (latestFile != null) {
            latestData = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + latestFile);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_pv) {
            Intent intent = new Intent(getBaseContext(), PVActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_vibration) {
            Intent intent = new Intent(getBaseContext(), VibrationActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_pressure) {
            Intent intent = new Intent(getBaseContext(), PressureActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_temperature) {
            Intent intent = new Intent(getBaseContext(), TemperatureActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_fft) {
            Intent intent = new Intent(getBaseContext(), FFTActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_history) {
            Intent intent = new Intent(this, HistoryActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_calibration_settings) {
            Intent intent = new Intent(getBaseContext(), CalibrationSettingsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_system_settings) {
                Intent intent = new Intent(getBaseContext(), SystemSettingsActivity.class);
                startActivity(intent);
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(getBaseContext(), AboutActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void getListOfFiles(View view) {
        Toast toast;
        if (serverAvailable) {
            long start;
            long end;
            EditText startDateText = findViewById(R.id.startDate);
            String startDate = startDateText.getText().toString();
            EditText endDateText = findViewById(R.id.endDate);
            String endDate = endDateText.getText().toString();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            try {
                if (!Strings.isNullOrEmpty(startDate) && !Strings.isNullOrEmpty(endDate)) {
                    start = dateFormat.parse(startDate).getTime();
                    end = dateFormat.parse(endDate).getTime() + (24*3600*1000);
                } else if (!Strings.isNullOrEmpty(startDate) && Strings.isNullOrEmpty(endDate)) {
                    start = dateFormat.parse(startDate).getTime();
                    end = System.currentTimeMillis();
                } else if (Strings.isNullOrEmpty(startDate) && !Strings.isNullOrEmpty(endDate)) {
                    start = 0;
                    end = dateFormat.parse(endDate).getTime() + (24*3600*1000);
                } else {
                    start = 0;
                    end = System.currentTimeMillis();
                }
                if (start > end) {
                    toast = Toast.makeText(getApplicationContext(), "Invalid date range, start date must be smaller than end date", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    toast = Toast.makeText(getApplicationContext(), "Syncing with server", Toast.LENGTH_SHORT);
                    toast.show();
                    final Gson gson = new GsonBuilder()
                            .setLenient()
                            .create();
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                    // Send a request to RestAPI
                    RestAPI restAPI = retrofit.create(RestAPI.class);
                    Call<Result> call = restAPI.getFiles(STORE_DIR, start, end);
                    call.enqueue(new Callback<Result>() {
                        @Override
                        public void onResponse(Call<Result> call, Response<Result> response) {
                            if (response.isSuccessful()) {
                                String filesStr = response.body().getOutput();
                                List<String> files = new ArrayList<>(Arrays.asList(filesStr.split(",")));
                                for(String file : Utils.getNewFiles(files)){
                                    if (file.contains("csv")) {
                                        getFileRequest(STORE_DIR, file);
                                    }
                                }
                            }
                        }

                        @Override
                        public void onFailure(Call<Result> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });
                }
            } catch (ParseException e) {
                toast = Toast.makeText(getApplicationContext(), "Error date format", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            toast = Toast.makeText(getApplicationContext(), "Server is offline", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void getFileRequest(final String directory, final String filename) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .build();
        // Send a request to RestAPI
        RestAPI restAPI = retrofit.create(RestAPI.class);
        Call<ResponseBody> call = restAPI.getFile(directory, filename);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            boolean writtenToDisk = writeResponseBodyToDisk(response.body(), directory, response.headers().get("filename"));
                            if (writtenToDisk) {
                                System.out.println("Successfully download " + filename);
                                dataFiles = Utils.getListOfFiles();
                            } else {
                                System.out.println("Failed to download " + filename);
                            }
                            return null;
                        }
                    }.execute();
                } else {
                    System.out.println("Failed to download the file");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private boolean writeResponseBodyToDisk(ResponseBody body, String directory, String filename) {
        try {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + directory + File.separator + filename);
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

    private void updateWifiSignal() {
        WifiManager wifiManager = (WifiManager) getBaseContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int numberOfLevels = 5;
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int level = WifiManager.calculateSignalLevel(wifiInfo.getRssi(), numberOfLevels);
        TextView wifiSignal = findViewById(R.id.signalStrength);
        wifiSignal.setText(Utils.SignalStrength.getSignalStrength(level).toString());
    }

    private void updateLastConnectedTime() {
        Date date = new Date(serverSystemSettings.getLastConnectedTime());
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy' 'HH:mm:ss");
        TextView lastConnectedTime = findViewById(R.id.lastConnectedTime);
        lastConnectedTime.setText(simpleDateFormat.format(date));
    }

    public void setStartDate(View view) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                new android.app.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        EditText dateEditText = findViewById(R.id.startDate);
                        dateEditText.setText(year + "/" + ++monthOfYear + "/" + dayOfMonth);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    public void setEndDate(View view) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(MainActivity.this,
                new android.app.DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        EditText dateEditText = findViewById(R.id.endDate);
                        dateEditText.setText(year + "/" + ++monthOfYear + "/" + dayOfMonth);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }
}
