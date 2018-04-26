package edu.rit.DRMonitor;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

/**
 * Class to perform an asynchronuous task in the background
 */

public class DoInBackGroundWithProgressDialog extends AsyncTask<Void, Void, Void> {
    private ProgressDialog pb;
    private Context context;
    private String loadingMessage;

    // Public constructor for a new task
    public DoInBackGroundWithProgressDialog(String loadingMessage, Context context) {
        this.context = context;
        this.loadingMessage = loadingMessage;
    }

    /**
     * Main function that performs the task in background
     * @param voids
     * @return
     */
    @Override
    protected Void doInBackground(Void... voids) {
        // Do asynchronous task in background
        return null;
    }

    /**
     * Things to do after task is performed. The progress dialog gets dismissed first
     * @param params
     */
    @Override
    protected void onPostExecute(Void params) {
        pb.dismiss();
    }

    /**
     * Things to do before performing the desired task.
     * Progress dialog is showned to user
     */
    @Override
    protected void onPreExecute() {
        pb = new ProgressDialog(context);
        pb.setMessage(loadingMessage);
        pb.setCancelable(false);
        pb.setIndeterminate(true);
        pb.show();
    }
}
