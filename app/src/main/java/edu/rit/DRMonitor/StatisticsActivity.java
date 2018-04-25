package edu.rit.DRMonitor;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import org.jtransforms.fft.FloatFFT_1D;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static edu.rit.DRMonitor.Utils.STORE_DIR;

public class StatisticsActivity extends AppCompatActivity {
    private float crankPressPeak;
    private float headPressPeak;
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

        new DoInBackGroundWithProgressDialog("Loading FFT graph...", this) {
            @Override
            protected Void doInBackground(Void... voids) {
                List<HistoricalDataFile> filesToPlot = (List<HistoricalDataFile>) getIntent().getSerializableExtra("filesToPlot");
                Map<String, float[]> dataFile;
                if (filesToPlot.size() == 1) {
                    dataFile = Utils.readData(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR + File.separator + filesToPlot.get(0).getFileName());
                    processPressure(dataFile);
                    processTemperature(dataFile);
                    processAccelAndFFT(dataFile);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                super.onPostExecute(params);

            }
        }.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.statistics_menu, menu);
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

    private void processPressure(Map<String, float[]> dataFile) {
        if (dataFile != null) {
            float[] headPress = dataFile.get(Utils.HEAD_CYL_PRESS_KEY);
            float[] crankPress = dataFile.get(Utils.CRANK_CYL_PRESS_KEY);
            if (headPress != null && crankPress != null) {
                for (int i = 0; i < headPress.length; i++) {
                    if (headPress[i] > headPressPeak) {
                        headPressPeak = headPress[i];
                    }
                    if (crankPress[i] > crankPressPeak) {
                        crankPressPeak = crankPress[i];
                    }
                }
            }
        }
    }

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

    private void processAccelAndFFT(Map<String, float[]> dataFile) {
        if (dataFile != null) {
            float[] accelX = dataFile.get(Utils.ACCEL_X_KEY);
            float[] accelY = dataFile.get(Utils.ACCEL_Y_KEY);
            float[] accelZ = dataFile.get(Utils.ACCEL_Z_KEY);
            if (accelX != null && accelY != null && accelZ != null) {
                removeDCOffset(accelX, accelY, accelZ);
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

                    float reY = accelY[i * 2];
                    float imY = accelY[i * 2 + 1];
                    resultY[i] = (float) Math.sqrt(reY * reY + imY * imY) / resultY.length;
                    if(resultY[i] > fft_accelYPeak) {
                        fft_accelYPeak_freq = i;
                    }
                    fft_accelYPeak = Math.max(fft_accelYPeak, resultY[i]);

                    float reZ = accelZ[i * 2];
                    float imZ = accelZ[i * 2 + 1];
                    resultZ[i] = (float) Math.sqrt(reZ * reZ + imZ * imZ) / resultZ.length;
                    if(resultZ[i] > fft_accelZPeak) {
                        fft_accelZPeak_freq = i;
                    }
                    fft_accelZPeak = Math.max(fft_accelZPeak, resultZ[i]);
                }
            }
        }
    }

    private void removeDCOffset(float[] accelX, float[] accelY, float[] accelZ) {
        float accelXMean = 0;
        float accelYMean = 0;
        float accelZMean = 0;
        for (int i = 0; i < accelX.length; i++) {
            accelXMean += accelX[i];
            accelYMean += accelY[i];
            accelZMean += accelZ[i];
        }
        accelXMean /= accelX.length;
        accelYMean /= accelY.length;
        accelZMean /= accelZ.length;

        for (int i = 0; i < accelX.length; i++) {
            accelX[i] = accelX[i] - accelXMean;
            accelY[i] = accelY[i] - accelYMean;
            accelZ[i] = accelZ[i] - accelZMean;
        }
    }
}
