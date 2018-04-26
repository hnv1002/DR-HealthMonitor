package edu.rit.DRMonitor;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Custom adapter to display list of historical data files on History screen
 */

public class FileRowAdapter extends ArrayAdapter<HistoricalDataFile> {
    private Context context;
    public static ArrayList<HistoricalDataFile> files;

    // Constructor
    public FileRowAdapter(Context context, ArrayList<HistoricalDataFile> files) {
        super(context, R.layout.file_row, files);
        this.context = context;
        this.files = files;
    }

    /**
     * Each file is returned as a row with a checkbox and filename
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        view = inflater.inflate(R.layout.file_row, parent, false);

        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkBox);
        TextView fileRow = (TextView) view.findViewById(R.id.rowTextView);

        fileRow.setText(files.get(position).getFileName());
        checkBox.setChecked(files.get(position).isSelected());

        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkBox.isChecked()) {
                    files.get(position).setSelected(true);
                } else {
                    files.get(position).setSelected(false);
                }
            }
        });

        return view;
    }
}
