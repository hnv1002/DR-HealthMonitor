package edu.rit.DRMonitor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.common.net.InetAddresses;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import static edu.rit.DRMonitor.MainActivity.SYSTEM_SETTINGS_FILE;

/**
 * System Settings screen to set up device settings
 */
public class SystemSettingsActivity extends AppCompatActivity {
    static SystemSettings systemSettings;
    static EditText server_ip_edit;
    static EditText server_port_edit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_system_settings);

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

        new DoInBackGroundWithProgressDialog("Loading system settings file...", this) {
            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void params) {
                super.onPostExecute(params);
                systemSettings = MainActivity.serverSystemSettings;
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                server_ip_edit = findViewById(R.id.server_ip);
                server_port_edit = findViewById(R.id.server_port);
                server_ip_edit.setText(systemSettings.getIpAddress());
                server_port_edit.setText(String.valueOf(systemSettings.getServerPort()));
            }
        }.execute();
    }

    /**
     * Update settings to local file
     * @param view
     */
    public void updateSystemSettings(View view) {
        String server_ip = server_ip_edit.getText().toString();
        int server_port = Integer.valueOf(server_port_edit.getText().toString());

        if (InetAddresses.isInetAddress(server_ip)) {
            if (!systemSettings.getIpAddress().equals(server_ip)) {
                systemSettings.setIpAddress(server_ip);
            }
            if (systemSettings.getServerPort() != server_port) {
                systemSettings.setServerPort(server_port);
            }
            Utils.updateSettingsFile(systemSettings, MainActivity.SYSTEM_SETTINGS_FILE);
            MainActivity.serverSystemSettings = Utils.getSystemSettings(SYSTEM_SETTINGS_FILE);
            MainActivity.BASE_URL = "http://" + systemSettings.getIpAddress() + ":" + systemSettings.getServerPort() + "/api/";
            MainActivity.SERVER_IP = systemSettings.getIpAddress();
            MainActivity.SERVER_PORT = String.valueOf(systemSettings.getServerPort());
            Toast toast = Toast.makeText(getApplicationContext(), "Successfully updated system systemSettings file", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
