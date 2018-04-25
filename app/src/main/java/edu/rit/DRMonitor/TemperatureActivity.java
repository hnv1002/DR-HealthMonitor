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

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static edu.rit.DRMonitor.Utils.STORE_DIR;

public class TemperatureActivity extends AppCompatActivity {

    private MultiSpinner spinner;
    private BarData data;
    private BarData deletedData = new BarData();
    private BarChart chart;
    private TextView domain;
    private TextView range;
    private String currentScale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_temperature);
        domain = findViewById(R.id.temperature_domain);
        range = findViewById(R.id.temperature_range);

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

        new DoInBackGroundWithProgressDialog("Loading temperature graph...", this) {
            @Override
            protected Void doInBackground(Void... voids) {
                List<HistoricalDataFile> filesToPlot = (List<HistoricalDataFile>) getIntent().getSerializableExtra("filesToPlot");
                Map<String, float[]> dataFile;
                data = null;
                if (filesToPlot != null && !filesToPlot.isEmpty()) {
                    if (filesToPlot.size() == 1) {
                        dataFile = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());
                        data = plotTemperature(dataFile, Color.BLACK, Color.BLUE, Color.RED, false, null);
                    } else if (filesToPlot.size() == 2) {
                        Map<String, float[]> dataFile1 = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());
                        Map<String, float[]> dataFile2 = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(1).getFileName());
                        data = plotTemperature(dataFile1, Color.BLACK, Color.BLUE, Color.RED, true, dataFile2);
                    }
                } else {
                    dataFile = MainActivity.latestData;
                    data = plotTemperature(dataFile, Color.BLACK, Color.BLUE, Color.RED, false, null);
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
                    spinner = (MultiSpinner) findViewById(R.id.temperatureDatasetSpinner);
                    spinner.setItems(items, "All Data Sets", new MultiSpinner.MultiSpinnerListener() {
                        @Override
                        public void onItemsSelected(boolean[] selected) {
                            for (int i = 0; i < selected.length; i++) {
                                if (!selected[i]) {
                                    IBarDataSet toRemove = data.getDataSetByLabel(itemsCopy.get(i), false);
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
                            chart = findViewById(R.id.temperatureBarChart);
                            chart.setData(data);
                            chart.getDescription().setText("Temperatures");
                            chart.getDescription().setTextSize(30);
                            chart.getXAxis().setDrawGridLines(false);
                            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                            chart.getXAxis().setTextSize(24);
                            chart.getAxisLeft().setTextSize(24);
                            chart.getAxisRight().setEnabled(false);
                            chart.invalidate();
                        }
                    });
                    currentScale = "c";
                    chart = findViewById(R.id.temperatureBarChart);
                    chart.setData(data);
                    chart.getDescription().setText("Temperatures");
                    chart.getDescription().setTextSize(30);
                    chart.getXAxis().setDrawGridLines(false);
                    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    chart.getXAxis().setTextSize(24);
                    chart.getAxisLeft().setTextSize(24);
                    chart.getAxisRight().setEnabled(false);
                    chart.invalidate();
                    range.setText("Temp (°C)");
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.temperature_menu, menu);
        return true;
    }

    public void goToHomeView(MenuItem item) {
        finish();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

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

    private BarData plotTemperature(Map<String, float[]> dataFile, int temp1Color, int temp2Color, int temp3Color, boolean secondData, Map<String, float[]> dataFile2) {
        if (dataFile != null) {
            float[] temp1 = dataFile.get(Utils.TEMP_1_KEY);
            float[] temp2 = dataFile.get(Utils.TEMP_2_KEY);
            float[] temp3 = dataFile.get(Utils.TEMP_3_KEY);
            if (secondData && dataFile2 != null) {
                float[] temp1_2 = dataFile2.get(Utils.TEMP_1_KEY);
                float[] temp2_2 = dataFile2.get(Utils.TEMP_2_KEY);
                float[] temp3_2 = dataFile2.get(Utils.TEMP_3_KEY);
                if (temp1 != null && temp2 != null && temp3 != null && temp1_2 != null && temp2_2 != null && temp3_2 != null) {
                    ArrayList<BarEntry> temp1Entries = new ArrayList<>();
                    ArrayList<BarEntry> temp2Entries = new ArrayList<>();
                    ArrayList<BarEntry> temp3Entries = new ArrayList<>();

                    ArrayList<BarEntry> temp1_2Entries = new ArrayList<>();
                    ArrayList<BarEntry> temp2_2Entries = new ArrayList<>();
                    ArrayList<BarEntry> temp3_2Entries = new ArrayList<>();

                    temp1Entries.add(new BarEntry(0f, temp1[0]));
                    temp1_2Entries.add(new BarEntry(1f, temp1_2[0]));
                    temp1Entries.add(new BarEntry(7f, temp1[1]));
                    temp1_2Entries.add(new BarEntry(8f, temp1_2[1]));

                    temp2Entries.add(new BarEntry(2f, temp2[0]));
                    temp2_2Entries.add(new BarEntry(3f, temp2_2[0]));
                    temp2Entries.add(new BarEntry(9f, temp2[1]));
                    temp2_2Entries.add(new BarEntry(10f, temp2_2[1]));

                    temp3Entries.add(new BarEntry(4f, temp3[0]));
                    temp3_2Entries.add(new BarEntry(5f, temp3_2[0]));
                    temp3Entries.add(new BarEntry(11f, temp3[1]));
                    temp3_2Entries.add(new BarEntry(12f, temp3_2[1]));

                    BarData data = new BarData();
                    String temp1Label = Utils.TEMP_1_KEY;
                    BarDataSet temp1Dataset = new BarDataSet(temp1Entries, temp1Label);
                    temp1Dataset.setColor(temp1Color);
                    data.addDataSet(temp1Dataset);
                    BarDataSet temp1_2Dataset = new BarDataSet(temp1_2Entries, temp1Label+"_2");
                    temp1_2Dataset.setColor(Color.GREEN);
                    data.addDataSet(temp1_2Dataset);

                    String temp2Label = Utils.TEMP_2_KEY;
                    BarDataSet temp2Dataset = new BarDataSet(temp2Entries, temp2Label);
                    temp2Dataset.setColor(temp2Color);
                    data.addDataSet(temp2Dataset);
                    BarDataSet temp2_2Dataset = new BarDataSet(temp2_2Entries, temp2Label+"_2");
                    temp2_2Dataset.setColor(Color.CYAN);
                    data.addDataSet(temp2_2Dataset);

                    String temp3Label = Utils.TEMP_3_KEY;
                    BarDataSet temp3Dataset = new BarDataSet(temp3Entries, temp3Label);
                    temp3Dataset.setColor(temp3Color);
                    data.addDataSet(temp3Dataset);
                    BarDataSet temp3_2Dataset = new BarDataSet(temp3_2Entries, temp3Label+"_2");
                    temp3_2Dataset.setColor(Color.GRAY);
                    data.addDataSet(temp3_2Dataset);

                    return data;
                }
            } else {
                if (temp1 != null && temp2 != null && temp3 != null) {
                    ArrayList<BarEntry> temp1Entries = new ArrayList<>();
                    ArrayList<BarEntry> temp2Entries = new ArrayList<>();
                    ArrayList<BarEntry> temp3Entries = new ArrayList<>();
                    temp1Entries.add(new BarEntry(0f, temp1[0]));
                    temp1Entries.add(new BarEntry(4f, temp1[1]));

                    temp2Entries.add(new BarEntry(1f, temp2[0]));
                    temp2Entries.add(new BarEntry(5f, temp2[1]));

                    temp3Entries.add(new BarEntry(2f, temp3[0]));
                    temp3Entries.add(new BarEntry(6f, temp3[1]));

                    BarData data = new BarData();
                    String temp1Label = Utils.TEMP_1_KEY;
                    if (secondData) {
                        temp1Label += "2";
                    }
                    BarDataSet temp1Dataset = new BarDataSet(temp1Entries, temp1Label);
                    temp1Dataset.setColor(temp1Color);
                    data.addDataSet(temp1Dataset);

                    String temp2Label = Utils.TEMP_2_KEY;
                    if (secondData) {
                        temp2Label += "2";
                    }
                    BarDataSet temp2Dataset = new BarDataSet(temp2Entries, temp2Label);
                    temp2Dataset.setColor(temp2Color);
                    data.addDataSet(temp2Dataset);

                    String temp3Label = Utils.TEMP_3_KEY;
                    if (secondData) {
                        temp3Label += "2";
                    }
                    BarDataSet temp3Dataset = new BarDataSet(temp3Entries, temp3Label);
                    temp3Dataset.setColor(temp3Color);
                    data.addDataSet(temp3Dataset);
                    return data;
                }
            }
        }
        return null;
    }

    public void cScale(MenuItem item) {
        if (chart != null && currentScale != null && !currentScale.equals("c")) {
            BarData newData = new BarData();
            for (IBarDataSet oldDataSet : data.getDataSets()) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int i = 0; i < oldDataSet.getEntryCount(); i++) {
                    entries.add(new BarEntry(oldDataSet.getEntryForIndex(i).getX(), (float) ((oldDataSet.getEntryForIndex(i).getY() - 32 ) / 1.8)));
                }
                BarDataSet newDataSet = new BarDataSet(entries, oldDataSet.getLabel());
                newDataSet.setColor(oldDataSet.getColor());
                newData.addDataSet(newDataSet);
            }
            currentScale = "c";
            chart.setData(newData);
            data = newData;
            chart.invalidate();
            range.setText("Temp (°C)");
        }
    }

    public void fScale(MenuItem item) {
        if (chart != null && currentScale != null && !currentScale.equals("f")) {
            BarData newData = new BarData();
            for (IBarDataSet oldDataSet : data.getDataSets()) {
                ArrayList<BarEntry> entries = new ArrayList<>();
                for (int i = 0; i < oldDataSet.getEntryCount(); i++) {
                    entries.add(new BarEntry(oldDataSet.getEntryForIndex(i).getX(), (float) ((oldDataSet.getEntryForIndex(i).getY() * 1.8) + 32)));
                }
                BarDataSet newDataSet = new BarDataSet(entries, oldDataSet.getLabel());
                newDataSet.setColor(oldDataSet.getColor());
                newData.addDataSet(newDataSet);
            }
            currentScale = "f";
            chart.setData(newData);
            data = newData;
            chart.invalidate();
            range.setText("Temp (°F)");
        }
    }
}
