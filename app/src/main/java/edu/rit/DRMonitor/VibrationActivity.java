package edu.rit.DRMonitor;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.rit.DRMonitor.Utils.STORE_DIR;

/**
 * Vibration screen to show raw data of accelerometers
 */
public class VibrationActivity extends AppCompatActivity {

    private MultiSpinner spinner;
    private LineData data;
    private LineData deletedData = new LineData();
    private LineChart chart;
    private TextView domain;
    private TextView range;
    private String currentScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vibration);
        domain = findViewById(R.id.vibration_domain);
        range = findViewById(R.id.vibration_range);

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

        new DoInBackGroundWithProgressDialog("Loading vibration graph...", this) {
            @Override
            protected Void doInBackground(Void... voids) {
                List<HistoricalDataFile> filesToPlot = (List<HistoricalDataFile>) getIntent().getSerializableExtra("filesToPlot");
                Map<String, float[]> dataFile;
                data = null;
                if (filesToPlot != null && !filesToPlot.isEmpty()) {
                    if (filesToPlot.size() == 1) {
                        dataFile = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());
                        com.github.mikephil.charting.utils.Utils.init(getApplicationContext());
                        data = plotVibration(dataFile, Color.BLACK, Color.BLUE, Color.RED, false);
                    } else if (filesToPlot.size() == 2) {
                        Map<String, float[]> dataFile1 = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());
                        Map<String, float[]> dataFile2 = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(1).getFileName());
                        com.github.mikephil.charting.utils.Utils.init(getApplicationContext());
                        data = plotVibration(dataFile1, Color.BLACK, Color.BLUE, Color.RED, false);
                        LineData secondData = plotVibration(dataFile2, Color.GREEN, Color.YELLOW, Color.CYAN, true);
                        for (ILineDataSet dataset : secondData.getDataSets()) {
                            data.addDataSet(dataset);
                        }
                    }
                } else {
                    dataFile = MainActivity.latestData;
                    com.github.mikephil.charting.utils.Utils.init(getApplicationContext());
                    data = plotVibration(dataFile, Color.BLACK, Color.BLUE, Color.RED, false);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                super.onPostExecute(params);
                if (data != null) {
                    List<String> items = new ArrayList<>();
                    for (int i = 0; i < data.getDataSets().size(); i++) {
                        items.add(data.getDataSetByIndex(i).getLabel());
                    }
                    final List<String> itemsCopy = new ArrayList<>(items);
                    spinner = (MultiSpinner) findViewById(R.id.vibrationDatasetSpinner);
                    spinner.setItems(items, "All Data Sets", new MultiSpinner.MultiSpinnerListener() {
                        @Override
                        public void onItemsSelected(boolean[] selected) {
                            for (int i = 0; i < selected.length; i++) {
                                if (!selected[i]) {
                                    ILineDataSet toRemove = data.getDataSetByLabel(itemsCopy.get(i), false);
                                    if (toRemove != null) {
                                        deletedData.addDataSet(toRemove);
                                        data.removeDataSet(data.getIndexOfDataSet(toRemove));
                                    }
                                } else {
                                    if ((data.getDataSetByLabel(itemsCopy.get(i), false) == null) && (deletedData.getDataSetByLabel(itemsCopy.get(i), false) != null)) {
                                        data.addDataSet(deletedData.getDataSetByLabel(itemsCopy.get(i), false));
                                        deletedData.removeDataSet(deletedData.getDataSetByLabel(itemsCopy.get(i), false));
                                    }
                                }
                            }
                            chart = findViewById(R.id.vibrationLineChart);
                            chart.setData(data);
                            chart.getDescription().setText("FFT Graph");
                            chart.getDescription().setTextSize(30);
                            chart.getXAxis().setDrawGridLines(false);
                            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                            chart.getXAxis().setTextSize(24);
                            chart.getAxisLeft().setTextSize(24);
                            chart.getAxisRight().setEnabled(false);
                            chart.invalidate();
                        }
                    });
                    currentScale = "g";
                    chart = findViewById(R.id.vibrationLineChart);
                    chart.setData(data);
                    chart.getDescription().setText("Vibration Graph");
                    chart.getDescription().setTextSize(30);
                    chart.getXAxis().setDrawGridLines(false);
                    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    chart.getXAxis().setTextSize(24);
                    chart.getAxisLeft().setTextSize(24);
                    chart.getAxisRight().setEnabled(false);
                    chart.invalidate();
                    range.setText("Accel (G)");
                }
            }
        }.execute();
    }

    /**
     * Menu on action bar which has Scale and Home button
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.vibration_menu, menu);
        return true;
    }

    /**
     * When home button is clicked, navigate to Home screen
     * @param item
     */
    public void goToHomeView(MenuItem item) {
        finish();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

    /**
     * When back button is clicked, navigate to History screen
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                finish();
                Intent intent = new Intent(this, HistoryActivity.class);
                intent.putExtra("data_files", (Serializable) MainActivity.dataFiles);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Calculate and return vibration graph
     * @param dataFile
     * @param accelXColor
     * @param accelYColor
     * @param accelZColor
     * @param secondData
     * @return
     */
    private LineData plotVibration(Map<String, float[]> dataFile, int accelXColor, int accelYColor, int accelZColor, boolean secondData) {
        if (dataFile != null) {
            float[] accelX = dataFile.get(Utils.ACCEL_X_KEY);
            float[] accelY = dataFile.get(Utils.ACCEL_Y_KEY);
            float[] accelZ = dataFile.get(Utils.ACCEL_Z_KEY);
            if (accelX != null && accelY != null && accelZ != null) {
                ArrayList<Entry> entriesX = new ArrayList<>();
                ArrayList<Entry> entriesY = new ArrayList<>();
                ArrayList<Entry> entriesZ = new ArrayList<>();
                for (int i = 0; i < accelX.length; i++) {
                    entriesX.add(new Entry(i, accelX[i]));
                    entriesY.add(new Entry(i, accelY[i]));
                    entriesZ.add(new Entry(i, accelZ[i]));
                }

                LineData data = new LineData();

                String accelXLabel = Utils.ACCEL_X_KEY;
                if (secondData) {
                    accelXLabel += "2";
                }
                LineDataSet datasetX = new LineDataSet(entriesX, accelXLabel);
                datasetX.setColor(accelXColor);
                datasetX.setDrawCircles(false);
                data.addDataSet(datasetX);

                String accelYLabel = Utils.ACCEL_Y_KEY;
                if (secondData) {
                    accelYLabel += "2";
                }
                LineDataSet datasetY = new LineDataSet(entriesY, accelYLabel);
                datasetY.setColor(accelYColor);
                datasetY.setDrawCircles(false);
                data.addDataSet(datasetY);

                String accelZLabel = Utils.ACCEL_Z_KEY;
                if (secondData) {
                    accelZLabel += "2";
                }
                LineDataSet datasetZ = new LineDataSet(entriesZ, accelZLabel);
                datasetZ.setColor(accelZColor);
                datasetZ.setDrawCircles(false);
                data.addDataSet(datasetZ);
                return data;
            }
        }
        return null;
    }

    /**
     * Convert from G to dB scale
     * @param item
     */
    public void dBScale(MenuItem item) {
        if (chart != null && currentScale != null && !currentScale.equals("dB")) {
            LineData newData = new LineData();
            for (ILineDataSet oldDataSet : data.getDataSets()) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < oldDataSet.getEntryCount(); i++) {
                    float oldY = oldDataSet.getEntryForIndex(i).getY();
                    if (oldY != 0) {
                        entries.add(new Entry(oldDataSet.getEntryForIndex(i).getX(), (float) (20 * Math.log10(oldY))));
                    } else {
                        entries.add(new Entry(oldDataSet.getEntryForIndex(i).getX(), oldY));
                    }
                }
                LineDataSet newDataSet = new LineDataSet(entries, oldDataSet.getLabel());
                newDataSet.setColor(oldDataSet.getColor());
                newDataSet.setDrawCircles(false);
                newData.addDataSet(newDataSet);
            }
            currentScale = "dB";
            chart.setData(newData);
            data = newData;
            chart.invalidate();
            range.setText("Accel (dB)");
        }
    }

    /**
     * Convert from dB to G scale
     * @param item
     */
    public void gScale(MenuItem item) {
        if (chart != null && currentScale != null && !currentScale.equals("g")) {
            LineData newData = new LineData();
            for (ILineDataSet oldDataSet : data.getDataSets()) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < oldDataSet.getEntryCount(); i++) {
                    float oldY = oldDataSet.getEntryForIndex(i).getY();
                    entries.add(new Entry(oldDataSet.getEntryForIndex(i).getX(), (float) Math.pow(10, oldY/20)));
                }
                LineDataSet newDataSet = new LineDataSet(entries, oldDataSet.getLabel());
                newDataSet.setColor(oldDataSet.getColor());
                newDataSet.setDrawCircles(false);
                newData.addDataSet(newDataSet);
            }
            currentScale = "g";
            chart.setData(newData);
            data = newData;
            chart.invalidate();
            range.setText("Accel (G)");
        }
    }
}
