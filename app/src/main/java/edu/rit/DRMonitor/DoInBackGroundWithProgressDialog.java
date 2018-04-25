package edu.rit.DRMonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Created by H on 4/21/2018.
 */

public class DoInBackGroundWithProgressDialog extends AsyncTask<Void, Void, Void> {
    private ProgressDialog pb;
    private Context context;
    private String loadingMessage;

    public DoInBackGroundWithProgressDialog(String loadingMessage, Context context) {
        this.context = context;
        this.loadingMessage = loadingMessage;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // Do asynchronous task in background
        return null;
    }

    @Override
    protected void onPostExecute(Void params) {
        pb.dismiss();
    }

    @Override
    protected void onPreExecute() {
        pb = new ProgressDialog(context);
        pb.setMessage(loadingMessage);
        pb.setCancelable(false);
        pb.setIndeterminate(true);
        pb.show();
    }
}
