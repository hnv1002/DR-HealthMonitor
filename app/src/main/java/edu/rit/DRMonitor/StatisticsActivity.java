package edu.rit.DRMonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import org.jtransforms.fft.FloatFFT_1D;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static edu.rit.DRMonitor.Utils.STORE_DIR;

/**
 * Statistics screen to display information about a data file
 */
public class StatisticsActivity extends AppCompatActivity {
    private float crankPressPeak;
    private float headPressPeak;
    private float crankPressMin;
    private float headPressMin;
    private float accelXPeak;
    private float accelYPeak;
    private float accelZPeak;
    private float accelXMin;
    private float accelYMin;
    private float accelZMin;
    private float fft_accelXPeak;
    private float fft_accelYPeak;
    private float fft_accelZPeak;
    private float fft_accelXPeak_freq;
    private float fft_accelYPeak_freq;
    private float fft_accelZPeak_freq;
    private float fft_accelXMin;
    private float fft_accelYMin;
    private float fft_accelZMin;
    private float fft_accelXMin_freq;
    private float fft_accelYMin_freq;
    private float fft_accelZMin_freq;
    private float max_temp1;
    private float min_temp1;
    private float max_temp2;
    private float min_temp2;
    private float max_temp3;
    private float min_temp3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        new DoInBackGroundWithProgressDialog("Loading statistics...", this) {
            @Override
            protected Void doInBackground(Void... voids) {
                List<HistoricalDataFile> filesToPlot = (List<HistoricalDataFile>) getIntent().getSerializableExtra("filesToPlot");
                Map<String, float[]> dataFile;
                if (filesToPlot!= null && filesToPlot.size() == 1) {
                    dataFile = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());

                } else {
                    dataFile = MainActivity.latestData;
                }
                processPressure(dataFile);
                processTemperature(dataFile);
                processAccelAndFFT(dataFile);
                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                super.onPostExecute(params);
                TextView t1_max = findViewById(R.id.t1_max);
                t1_max.setText(String.valueOf(max_temp1));
                TextView t2_max = findViewById(R.id.t2_max);
                t2_max.setText(String.valueOf(max_temp2));
                TextView t3_max = findViewById(R.id.t3_max);
                t3_max.setText(String.valueOf(max_temp3));
                TextView t1_min = findViewById(R.id.t1_min);
                t1_min.setText(String.valueOf(min_temp1));
                TextView t2_min = findViewById(R.id.t2_min);
                t2_min.setText(String.valueOf(min_temp2));
                TextView t3_min = findViewById(R.id.t3_min);
                t3_min.setText(String.valueOf(min_temp3));

                TextView head_max = findViewById(R.id.head_max);
                head_max.setText(String.valueOf(headPressPeak));
                TextView head_min = findViewById(R.id.head_min);
                head_min.setText(String.valueOf(headPressMin));
                TextView crank_max = findViewById(R.id.crank_max);
                crank_max.setText(String.valueOf(crankPressPeak));
                TextView crank_min = findViewById(R.id.crank_min);
                crank_min.setText(String.valueOf(crankPressMin));

                TextView x_max = findViewById(R.id.x_max);
                x_max.setText(String.valueOf(accelXPeak));
                TextView x_min = findViewById(R.id.x_min);
                x_min.setText(String.valueOf(accelXMin));
                TextView y_max = findViewById(R.id.y_max);
                y_max.setText(String.valueOf(accelYPeak));
                TextView y_min = findViewById(R.id.y_min);
                y_min.setText(String.valueOf(accelYMin));
                TextView z_max = findViewById(R.id.z_max);
                z_max.setText(String.valueOf(accelZPeak));
                TextView z_min = findViewById(R.id.z_min);
                z_min.setText(String.valueOf(accelZMin));

                TextView fft_x_max = findViewById(R.id.fft_x_max);
                fft_x_max.setText(String.valueOf(fft_accelXPeak)+" ("+String.valueOf(fft_accelXPeak_freq)+"Hz)");
                TextView fft_x_min = findViewById(R.id.fft_x_min);
                fft_x_min.setText(String.valueOf(fft_accelXMin)+" ("+String.valueOf(fft_accelXMin_freq)+"Hz)");
                TextView fft_y_max = findViewById(R.id.fft_y_max);
                fft_y_max.setText(String.valueOf(fft_accelYPeak)+" ("+String.valueOf(fft_accelYPeak_freq)+"Hz)");
                TextView fft_y_min = findViewById(R.id.fft_y_min);
                fft_y_min.setText(String.valueOf(fft_accelYMin)+" ("+String.valueOf(fft_accelYMin_freq)+"Hz)");
                TextView fft_z_max = findViewById(R.id.fft_z_max);
                fft_z_max.setText(String.valueOf(fft_accelZPeak)+" ("+String.valueOf(fft_accelZPeak_freq)+"Hz)");
                TextView fft_z_min = findViewById(R.id.fft_z_min);
                fft_z_min.setText(String.valueOf(fft_accelZMin)+" ("+String.valueOf(fft_accelZMin_freq)+"Hz)");
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
        getMenuInflater().inflate(R.menu.statistics_menu, menu);
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
     * Retrieve highest and lowest value of head and crank pressure
     * @param dataFile
     */
    private void processPressure(Map<String, float[]> dataFile) {
        if (dataFile != null) {
            float[] headPress = dataFile.get(Utils.HEAD_CYL_PRESS_KEY);
            float[] crankPress = dataFile.get(Utils.CRANK_CYL_PRESS_KEY);
            if (headPress != null && crankPress != null) {
                for (int i = 0; i < headPress.length; i++) {
                    if (headPress[i] > headPressPeak) {
                        headPressPeak = headPress[i];
                    }
                    if (headPress[i] < headPressMin) {
                        headPressMin = headPress[i];
                    }
                    if (crankPress[i] > crankPressPeak) {
                        crankPressPeak = crankPress[i];
                    }
                    if (crankPress[i] < crankPressMin) {
                        crankPressMin = crankPress[i];
                    }
                }
            }
        }
    }

    /**
     * Retrieve highest and lowest temperature
     * @param dataFile
     */
    private void processTemperature(Map<String, float[]> dataFile) {
        if (dataFile != null) {
            float[] temp1 = dataFile.get(Utils.TEMP_1_KEY);
            float[] temp2 = dataFile.get(Utils.TEMP_2_KEY);
            float[] temp3 = dataFile.get(Utils.TEMP_3_KEY);
            if (temp1 != null && temp2 != null && temp3 != null) {
                if (temp1[0] > temp1[1]) {
                    max_temp1 = temp1[0];
                    min_temp1 = temp1[1];
                } else {
                    max_temp1 = temp1[1];
                    min_temp1 = temp1[0];
                }
                if (temp2[0] > temp2[1]) {
                    max_temp2 = temp2[0];
                    min_temp2 = temp2[1];
                } else {
                    max_temp2 = temp2[1];
                    min_temp2 = temp2[0];
                }
                if (temp3[0] > temp3[1]) {
                    max_temp3 = temp3[0];
                    min_temp3 = temp3[1];
                } else {
                    max_temp3 = temp3[1];
                    min_temp3 = temp3[0];
                }
            }
        }
    }

    /**
     * Retrieve highest and lowest peak of accelerometers as well as their corresponding FFT
     * @param dataFile
     */
    private void processAccelAndFFT(Map<String, float[]> dataFile) {
        if (dataFile != null) {
            float[] accelX = dataFile.get(Utils.ACCEL_X_KEY);
            float[] accelY = dataFile.get(Utils.ACCEL_Y_KEY);
            float[] accelZ = dataFile.get(Utils.ACCEL_Z_KEY);
            if (accelX != null && accelY != null && accelZ != null) {
                Utils.removeDCOffset(accelX, accelY, accelZ);
                for (int i = 0; i < accelX.length; i++) {
                    if (accelX[i] > accelXPeak) {
                        accelXPeak = accelX[i];
                    }
                    if (accelX[i] < accelXMin) {
                        accelXMin = accelX[i];
                    }
                    if (accelY[i] > accelYPeak) {
                        accelYPeak = accelY[i];
                    }
                    if (accelY[i] < accelYMin) {
                        accelYMin = accelY[i];
                    }
                    if (accelZ[i] > accelZPeak) {
                        accelZPeak = accelZ[i];
                    }
                    if (accelZ[i] < accelZMin) {
                        accelZMin = accelZ[i];
                    }
                }
                FloatFFT_1D floatFFT_1D = new FloatFFT_1D(accelX.length);
                floatFFT_1D.realForward(accelX);
                floatFFT_1D.realForward(accelY);
                floatFFT_1D.realForward(accelZ);

                float[] resultX = new float[accelX.length/2];
                float[] resultY = new float[accelY.length/2];
                float[] resultZ = new float[accelZ.length/2];
                for(int i = 0; i < resultX.length; i++) {
                    float reX = accelX[i * 2];
                    float imX = accelX[i * 2 + 1];
                    resultX[i] = (float) Math.sqrt(reX * reX + imX * imX) / resultX.length;
                    if(resultX[i] > fft_accelXPeak) {
                        fft_accelXPeak_freq = i;
                    }
                    fft_accelXPeak = Math.max(fft_accelXPeak, resultX[i]);
                    if(resultX[i] < fft_accelXMin) {
                        fft_accelXMin_freq = i;
                    }
                    fft_accelXMin = Math.min(fft_accelXMin, resultX[i]);

                    float reY = accelY[i * 2];
                    float imY = accelY[i * 2 + 1];
                    resultY[i] = (float) Math.sqrt(reY * reY + imY * imY) / resultY.length;
                    if(resultY[i] > fft_accelYPeak) {
                        fft_accelYPeak_freq = i;
                    }
                    fft_accelYPeak = Math.max(fft_accelYPeak, resultY[i]);
                    if(resultX[i] < fft_accelYMin) {
                        fft_accelYMin_freq = i;
                    }
                    fft_accelYMin = Math.min(fft_accelYMin, resultY[i]);

                    float reZ = accelZ[i * 2];
                    float imZ = accelZ[i * 2 + 1];
                    resultZ[i] = (float) Math.sqrt(reZ * reZ + imZ * imZ) / resultZ.length;
                    if(resultZ[i] > fft_accelZPeak) {
                        fft_accelZPeak_freq = i;
                    }
                    fft_accelZPeak = Math.max(fft_accelZPeak, resultZ[i]);
                    if(resultZ[i] < fft_accelZMin) {
                        fft_accelZMin_freq = i;
                    }
                    fft_accelZMin = Math.min(fft_accelZMin, resultZ[i]);
                }
            }
        }
    }
}
