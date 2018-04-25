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

public class PressureActivity extends AppCompatActivity {

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
        setContentView(R.layout.activity_pressure);
        domain = findViewById(R.id.pressure_domain);
        range = findViewById(R.id.pressure_range);

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

        new DoInBackGroundWithProgressDialog("Loading pressure graph...", this) {
            @Override
            protected Void doInBackground(Void... voids) {
                List<HistoricalDataFile> filesToPlot = (List<HistoricalDataFile>) getIntent().getSerializableExtra("filesToPlot");
                Map<String, float[]> dataFile;
                data = null;
                if (filesToPlot != null && !filesToPlot.isEmpty()) {
                    if (filesToPlot.size() == 1) {
                        dataFile = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());
                        data = plotPressure(dataFile, Color.BLACK, Color.BLUE, false);
                    } else if (filesToPlot.size() == 2) {
                        Map<String, float[]> dataFile1 = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());
                        Map<String, float[]> dataFile2 = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(1).getFileName());
                        data = plotPressure(dataFile1, Color.BLACK, Color.BLUE, false);
                        LineData secondData = plotPressure(dataFile2, Color.RED, Color.GREEN, true);
                        for (ILineDataSet dataset : secondData.getDataSets()) {
                            data.addDataSet(dataset);
                        }
                    }
                } else {
                    dataFile = MainActivity.latestData;
                    data = plotPressure(dataFile, Color.BLACK, Color.BLUE, false);
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
                    spinner = (MultiSpinner) findViewById(R.id.pressureDatasetSpinner);
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
                            chart = findViewById(R.id.pressureLineChart);
                            chart.setData(data);
                            chart.getDescription().setText("Head & Crank Cylinder Pressure");
                            chart.getDescription().setTextSize(30);
                            chart.getXAxis().setDrawGridLines(false);
                            chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                            chart.getXAxis().setTextSize(24);
                            chart.getAxisLeft().setTextSize(24);
                            chart.getAxisRight().setEnabled(false);
                            chart.invalidate();
                        }
                    });
                    currentScale = "psi";
                    chart = findViewById(R.id.pressureLineChart);
                    chart.setData(data);
                    chart.getDescription().setText("Head & Crank Cylinder Pressure");
                    chart.getDescription().setTextSize(30);
                    chart.getXAxis().setDrawGridLines(false);
                    chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
                    chart.getXAxis().setTextSize(24);
                    chart.getAxisLeft().setTextSize(24);
                    chart.getAxisRight().setEnabled(false);
                    chart.invalidate();
                    range.setText("Pressure (psi)");
                }
            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.pressure_menu, menu);
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

    private LineData plotPressure(Map<String, float[]> dataFile, int headPressColor, int crankPressColor, boolean secondData) {
        if (dataFile != null) {
            float[] headPress = dataFile.get(Utils.HEAD_CYL_PRESS_KEY);
            float[] crankPress = dataFile.get(Utils.CRANK_CYL_PRESS_KEY);
            float[] time = dataFile.get(Utils.TIME_KEY);
            if (time != null && headPress != null && crankPress != null) {
                ArrayList<Entry> headPressEntries = new ArrayList<>();
                ArrayList<Entry> crankPressEntries = new ArrayList<>();
                for (int i = 0; i < crankPress.length; i++) {
                    headPressEntries.add(new Entry(time[i], headPress[i]));
                    crankPressEntries.add(new Entry(time[i], crankPress[i]));
                }

                LineData data = new LineData();

                String headPressLabel = Utils.HEAD_CYL_PRESS_KEY;
                if (secondData) {
                    headPressLabel += "2";
                }
                LineDataSet headPressDataset = new LineDataSet(headPressEntries, headPressLabel);
                headPressDataset.setDrawCircles(false);
                headPressDataset.setColor(headPressColor);
                data.addDataSet(headPressDataset);

                String crankPressLabel = Utils.CRANK_CYL_PRESS_KEY;
                if (secondData) {
                    crankPressLabel += "2";
                }
                LineDataSet crankPressDataset = new LineDataSet(crankPressEntries, crankPressLabel);
                crankPressDataset.setDrawCircles(false);
                crankPressDataset.setColor(crankPressColor);
                data.addDataSet(crankPressDataset);
                return data;
            }
        }
        return null;
    }

    public void kPaScale(MenuItem item) {
        if (chart != null && currentScale != null && !currentScale.equals("kPa")) {
            LineData newData = new LineData();
            for (ILineDataSet oldDataSet : data.getDataSets()) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < oldDataSet.getEntryCount(); i++) {
                    entries.add(new Entry(oldDataSet.getEntryForIndex(i).getX(), (float) (oldDataSet.getEntryForIndex(i).getY()*6.894745)));
                }
                LineDataSet newDataSet = new LineDataSet(entries, oldDataSet.getLabel());
                newDataSet.setColor(oldDataSet.getColor());
                newDataSet.setDrawCircles(false);
                newData.addDataSet(newDataSet);
            }
            currentScale = "kPa";
            chart.setData(newData);
            data = newData;
            chart.invalidate();
            range.setText("Pressure (kPa)");
        }
    }

    public void psiScale(MenuItem item) {
        if (chart != null && currentScale != null && !currentScale.equals("psi")) {
            LineData newData = new LineData();
            for (ILineDataSet oldDataSet : data.getDataSets()) {
                ArrayList<Entry> entries = new ArrayList<>();
                for (int i = 0; i < oldDataSet.getEntryCount(); i++) {
                    entries.add(new Entry(oldDataSet.getEntryForIndex(i).getX(), (float) (oldDataSet.getEntryForIndex(i).getY()/6.894745)));
                }
                LineDataSet newDataSet = new LineDataSet(entries, oldDataSet.getLabel());
                newDataSet.setColor(oldDataSet.getColor());
                newDataSet.setDrawCircles(false);
                newData.addDataSet(newDataSet);
            }
            currentScale = "psi";
            chart.setData(newData);
            data = newData;
            chart.invalidate();
            range.setText("Pressure (psi)");
        }
    }
}
