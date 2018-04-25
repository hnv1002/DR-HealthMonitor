package edu.rit.DRMonitor;

import java.io.File;
import java.io.Serializable;

/**
 * Created by H on 1/21/2018.
 */

public class HistoricalDataFile implements Serializable{
    private static final long serialVersionUID = 1L;
    private String fileName;
    private boolean isSelected;
    private File file;

    public HistoricalDataFile(String fileName, File file) {
        this.fileName = fileName;
        this.isSelected = false;
        this.file = file;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public String getFileName() {
        return fileName;
    }

    public File getFile() { return file; }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }
}
