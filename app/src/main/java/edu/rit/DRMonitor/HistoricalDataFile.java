package edu.rit.DRMonitor;

import java.io.File;
import java.io.Serializable;

/**
 * Object representation of a data file which contains the file itself,
 * the filename and whether the file is currently selected via checkbox
 */

public class HistoricalDataFile implements Serializable{
    private static final long serialVersionUID = 1L;
    private String fileName;
    private boolean isSelected;
    private File file;

    // Constructor
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
