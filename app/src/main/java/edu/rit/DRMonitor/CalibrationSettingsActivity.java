package edu.rit.DRMonitor;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.io.Serializable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static edu.rit.DRMonitor.MainActivity.BASE_URL;

/**
 * Functionality in the calibration screen
 */
public class CalibrationSettingsActivity extends AppCompatActivity {
    static CalibrationSettings calibrationSettings;
    private EditText g1_scale_edit;
    private EditText g2_scale_edit;
    private EditText g3_scale_edit;
    private EditText p1_scale_edit;
    private EditText p2_scale_edit;
    private EditText t1_scale_edit;
    private EditText t2_scale_edit;
    private EditText t3_scale_edit;
    private EditText g1_shift_edit;
    private EditText g2_shift_edit;
    private EditText g3_shift_edit;
    private EditText t1_shift_edit;
    private EditText t2_shift_edit;
    private EditText t3_shift_edit;
    private EditText p1_shift_edit;
    private EditText p2_shift_edit;
    private EditText num_samp_edit;
    private EditText interval_edit;

    /**
     * When the screen first loaded, created and populated calibration fields
     * using the calibration file
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration_settings);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        calibrationSettings = MainActivity.serverCalibrationSettings;

        // Behavior of help page sliding panel at the bottom
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

        // Populating all calibration fields in background
        new DoInBackGroundWithProgressDialog("Loading calibration settings file...", this) {
            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                super.onPostExecute(params);
                g1_scale_edit = findViewById(R.id.g1_scale);
                g1_scale_edit.setText(String.valueOf(calibrationSettings.getG1_scale()));
                g2_scale_edit = findViewById(R.id.g2_scale);
                g2_scale_edit.setText(String.valueOf(calibrationSettings.getG2_scale()));
                g3_scale_edit = findViewById(R.id.g3_scale);
                g3_scale_edit.setText(String.valueOf(calibrationSettings.getG3_scale()));

                g1_shift_edit = findViewById(R.id.g1_shift);
                g1_shift_edit.setText(String.valueOf(calibrationSettings.getG1_shift()));
                g2_shift_edit = findViewById(R.id.g2_shift);
                g2_shift_edit.setText(String.valueOf(calibrationSettings.getG2_shift()));
                g3_shift_edit = findViewById(R.id.g3_shift);
                g3_shift_edit.setText(String.valueOf(calibrationSettings.getG3_shift()));

                p1_scale_edit = findViewById(R.id.p1_scale);
                p1_scale_edit.setText(String.valueOf(calibrationSettings.getP1_scale()));
                p2_scale_edit = findViewById(R.id.p2_scale);
                p2_scale_edit.setText(String.valueOf(calibrationSettings.getP2_scale()));

                p1_shift_edit = findViewById(R.id.p1_shift);
                p1_shift_edit.setText(String.valueOf(calibrationSettings.getP1_shift()));
                p2_shift_edit = findViewById(R.id.p2_shift);
                p2_shift_edit.setText(String.valueOf(calibrationSettings.getP2_shift()));

                t1_scale_edit = findViewById(R.id.t1_scale);
                t1_scale_edit.setText(String.valueOf(calibrationSettings.getT1_scale()));
                t2_scale_edit = findViewById(R.id.t2_scale);
                t2_scale_edit.setText(String.valueOf(calibrationSettings.getT2_scale()));
                t3_scale_edit = findViewById(R.id.t3_scale);
                t3_scale_edit.setText(String.valueOf(calibrationSettings.getT3_scale()));

                t1_shift_edit = findViewById(R.id.t1_shift);
                t1_shift_edit.setText(String.valueOf(calibrationSettings.getT1_shift()));
                t2_shift_edit = findViewById(R.id.t2_shift);
                t2_shift_edit.setText(String.valueOf(calibrationSettings.getT2_shift()));
                t3_shift_edit = findViewById(R.id.t3_shift);
                t3_shift_edit.setText(String.valueOf(calibrationSettings.getT3_shift()));

                num_samp_edit = findViewById(R.id.numSamp_val);
                num_samp_edit.setText(String.valueOf(calibrationSettings.getNumSamp()));

                interval_edit = findViewById(R.id.interval_val);
                interval_edit.setText(String.valueOf(calibrationSettings.getInterval()));
            }
        }.execute();
    }

    /**
     * Populate the menu on the top action bar
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.calibration_settings_menu, menu);
        return true;
    }

    /**
     * When Home button on action is clicked, navigate to Home screen
     * @param item
     */
    public void goToHomeView(MenuItem item) {
        finish();
        Intent intent = new Intent(getBaseContext(), MainActivity.class);
        startActivity(intent);
    }

    /**
     * When the back button is clicked, navigate to History screen
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
     * When save button is clicked, retrieve the entered values and update calibration
     * file with new values
     * @param view
     */
    public void updateCalibrationSettings(View view) {
        try {
            calibrationSettings.setG1_scale(Integer.valueOf(g1_scale_edit.getText().toString()));
            calibrationSettings.setG2_scale(Integer.valueOf(g2_scale_edit.getText().toString()));
            calibrationSettings.setG3_scale(Integer.valueOf(g3_scale_edit.getText().toString()));

            calibrationSettings.setG1_shift(Integer.valueOf(g1_shift_edit.getText().toString()));
            calibrationSettings.setG2_shift(Integer.valueOf(g2_shift_edit.getText().toString()));
            calibrationSettings.setG3_shift(Integer.valueOf(g3_shift_edit.getText().toString()));

            calibrationSettings.setT1_scale(Integer.valueOf(t1_scale_edit.getText().toString()));
            calibrationSettings.setT2_scale(Integer.valueOf(t2_scale_edit.getText().toString()));
            calibrationSettings.setT3_scale(Integer.valueOf(t3_scale_edit.getText().toString()));

            calibrationSettings.setT1_shift(Integer.valueOf(t1_shift_edit.getText().toString()));
            calibrationSettings.setT2_shift(Integer.valueOf(t2_shift_edit.getText().toString()));
            calibrationSettings.setT3_shift(Integer.valueOf(t3_shift_edit.getText().toString()));

            calibrationSettings.setP1_scale(Integer.valueOf(p1_scale_edit.getText().toString()));
            calibrationSettings.setP2_scale(Integer.valueOf(p2_scale_edit.getText().toString()));

            calibrationSettings.setP1_shift(Integer.valueOf(p1_shift_edit.getText().toString()));
            calibrationSettings.setP2_shift(Integer.valueOf(p2_shift_edit.getText().toString()));

            calibrationSettings.setNumSamp(Integer.valueOf(num_samp_edit.getText().toString()));
            calibrationSettings.setInterval(Integer.valueOf(interval_edit.getText().toString()));

            Utils.updateSettingsFile(calibrationSettings, MainActivity.CALIBRATION_SETTINGS_FILE);
            if (MainActivity.serverAvailable) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .build();
                // Send a request to RestAPI to update it on the Pi
                RestAPI restAPI = retrofit.create(RestAPI.class);
                Call<ResponseBody> call = restAPI.updateCalibrationSettings(Utils.toJson(calibrationSettings));
                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Successfully updated system calibrationSettings file locally and on server", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        System.out.println("Failed to update calibration file on server");
                    }
                });
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), "Successfully updated system calibrationSettings file locally", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid calibration settings");
        }
    }
}
