package edu.rit.DRMonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static edu.rit.DRMonitor.Utils.gson;

/**
 * History screen which shows list of data files and a menu with
 * options to delete the files or plot them
 */
public class HistoryActivity extends AppCompatActivity {

    private ListView listView;
    private FileRowAdapter fileRowAdapter;
    private ArrayList<HistoricalDataFile> dataFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Behavior of the help page sliding panel at the bottom
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

        new DoInBackGroundWithProgressDialog("Loading historical data files...", this) {
            long sdFreeSpace;
            long sdTotalSpace;
            double usedSpaceSD;
            int usedSpaceUsb;
            NumberProgressBar usbBar = findViewById(R.id.pi_usb_storage);

            @Override
            protected Void doInBackground(Void... voids) {
                dataFiles = Utils.getListOfFiles();
                sdFreeSpace = Environment.getExternalStorageDirectory().getUsableSpace();
                sdTotalSpace = Environment.getExternalStorageDirectory().getTotalSpace();
                usedSpaceSD =  ((double)sdFreeSpace/sdTotalSpace)*100;
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(MainActivity.BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();
                // Send a request to RestAPI
                RestAPI restAPI = retrofit.create(RestAPI.class);
                Call<String> call = restAPI.getUsbStorage();
                call.enqueue(new Callback<String>() {
                    @Override
                    public void onResponse(Call<String> call, Response<String> response) {
                        if (response.isSuccessful()) {
                            usedSpaceUsb = Integer.valueOf(response.body().toString());
                            usbBar.setProgress(usedSpaceUsb);
                            usbBar.setProgressTextSize(20);
                            usbBar.setReachedBarHeight(10);
                            usbBar.setUnreachedBarHeight(10);
                        }
                    }

                    @Override
                    public void onFailure(Call<String> call, Throwable t) {
                        TextView usbBarText = findViewById(R.id.usb_storage_text);
                        usbBarText.setVisibility(View.INVISIBLE);
                        usbBar.setVisibility(View.INVISIBLE);
                    }
                });
                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                super.onPostExecute(params);
                // Find the ListView resource
                listView = (ListView) findViewById(R.id.list);
                fileRowAdapter = new FileRowAdapter(HistoryActivity.this, dataFiles);

                // Set the ArrayAdapter as the ListView's adapter.
                listView.setAdapter( fileRowAdapter );
                listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

                NumberProgressBar sdBar = findViewById(R.id.sd_card_storage);
                sdBar.setProgress(100 - (int) Math.round(usedSpaceSD));
                sdBar.setProgressTextSize(20);
                sdBar.setReachedBarHeight(10);
                sdBar.setUnreachedBarHeight(10);
            }
        }.execute();
    }

    /**
     * Menu on action bar which has Delete, Raw Data, PV Graph, and Vibration button
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.historical_data_menu, menu);
        return true;
    }

    /**
     * Delete selected files
     * @param item
     */
    public void deleteFiles(MenuItem item) {
        boolean deleted = false;
        boolean noFileToDelete = true;
        if (dataFiles != null) {
            for (HistoricalDataFile file : dataFiles) {
                if (file.isSelected()) {
                    try {
                        if (file.getFile().delete()) {
                            System.out.println("Successfully delete file: " + file.getFileName());
                            deleted = true;
                            noFileToDelete = false;
                        } else {
                            System.out.println("Couldn't delete file: " + file.getFileName());
                            deleted = false;
                        }
                    } catch (RuntimeException e) {
                        System.out.println("Error deleting file: " + file.getFileName());
                    }
                }
            }

        }
        finish();
        MainActivity.dataFiles = Utils.getListOfFiles();
        startActivity(getIntent());
        if (deleted) {
            Toast toast = Toast.makeText(getApplicationContext(), "Successfully deleted file(s)", Toast.LENGTH_SHORT);
            toast.show();
        } else if(noFileToDelete) {
            Toast toast = Toast.makeText(getApplicationContext(), "No file to delete", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            Toast toast = Toast.makeText(getApplicationContext(), "Failed to delete file(s)", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Plot Pressure Volume graph of selected data
     * @param item
     */
    public void plotPVGraph(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            Intent intent = new Intent(getBaseContext(), PVActivity.class);
            intent.putExtra("filesToPlot", (Serializable) files);
            startActivity(intent);
        }
    }

    /**
     * Plot FFT graph with Hanning window
     * @param item
     */
    public void fftHanningGraph(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            Intent intent = new Intent(getBaseContext(), FFTActivity.class);
            intent.putExtra("filesToPlot", (Serializable) files);
            intent.putExtra("window", "hanning");
            startActivity(intent);
        }
    }

    /**
     * Plot FFT graph with Hamming window
     * @param item
     */
    public void fftHammingGraph(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            Intent intent = new Intent(getBaseContext(), FFTActivity.class);
            intent.putExtra("filesToPlot", (Serializable) files);
            intent.putExtra("window", "hamming");
            startActivity(intent);
        }
    }

    /**
     * Plot FFT graph with no window
     * @param item
     */
    public void fftGraph(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            Intent intent = new Intent(getBaseContext(), FFTActivity.class);
            intent.putExtra("filesToPlot", (Serializable) files);
            startActivity(intent);
        }
    }

    /**
     * Plot Pressure graph
     * @param item
     */
    public void pressureGraph(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            Intent intent = new Intent(getBaseContext(), PressureActivity.class);
            intent.putExtra("filesToPlot", (Serializable) files);
            startActivity(intent);
        }
    }

    /**
     * Plot Temperature graph
     * @param item
     */
    public void temperatureGraph(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            Intent intent = new Intent(getBaseContext(), TemperatureActivity.class);
            intent.putExtra("filesToPlot", (Serializable) files);
            startActivity(intent);
        }
    }

    /**
     * Plot vibration graph
     * @param item
     */
    public void vibrationGraph(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            Intent intent = new Intent(getBaseContext(), VibrationActivity.class);
            intent.putExtra("filesToPlot", (Serializable) files);
            startActivity(intent);
        }
    }

    /**
     * Show statistics page of selected data file
     * @param item
     */
    public void statisticsPage(MenuItem item) {
        List<HistoricalDataFile> files = getSelectedFiles();
        if (files != null) {
            if (files.size() > 1) {
                Toast toast = Toast.makeText(getApplicationContext(), "Statistics page only supports 1 file at this version", Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Intent intent = new Intent(getBaseContext(), StatisticsActivity.class);
                intent.putExtra("filesToPlot", (Serializable) files);
                startActivity(intent);
            }
        }
    }

    /**
     * Loop through and return currently selected files (via checkboxes)
     * @return
     */
    private List<HistoricalDataFile> getSelectedFiles() {
        List<HistoricalDataFile> files = new ArrayList<>();
        for (HistoricalDataFile file : dataFiles) {
            if (file.isSelected()) {
                files.add(file);
            }
        }
        if (files.size() <= 2) {
            return files;
        } else {
            // Current version only support comparison of at most 2 files
            Toast toast = Toast.makeText(getApplicationContext(), "Data comparison is only supported for 2 files at this time", Toast.LENGTH_SHORT);
            toast.show();
            return null;
        }
    }
}
