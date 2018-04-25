package edu.rit.DRMonitor;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Environment;

import com.google.gson.Gson;

import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by H on 2/21/2018.
 */

public class Utils {
    public static final String STORE_DIR = "DR_Data";
    public static final String TIME_KEY = "Time";
    public static final String TEMP_1_KEY = "T1";
    public static final String TEMP_2_KEY = "T2";
    public static final String TEMP_3_KEY = "T3";
    public static final String ACCEL_X_KEY = "FrameAccelX";
    public static final String ACCEL_Y_KEY = "FrameAccelY";
    public static final String ACCEL_Z_KEY = "FrameAccelZ";
    public static final String HEAD_CYL_PRESS_KEY = "HeadCylPress";
    public static final String CRANK_CYL_PRESS_KEY = "CrankCylPress";
    public static final String ENCODER_ANG_POS_KEY = "EncoderAngPos";

    public static Gson gson = new Gson();

    enum SignalStrength {
        EXCELLENT(4),
        GOOD(3),
        FAIR(2),
        WEAK(1),
        NONE(0);

        private int value;
        SignalStrength(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public static SignalStrength getSignalStrength(int value) {
            for (SignalStrength signal : values()) {
                if (signal.getValue() == value) {
                    return signal;
                }
            }
            return null;
        }
    }

    public static class FileNameSort implements Comparator<HistoricalDataFile> {
        @Override
        public int compare(HistoricalDataFile historicalDataFile, HistoricalDataFile other) {
            return other.getFileName().compareTo(historicalDataFile.getFileName());
        }
    }

    public Result extractZipFile(String fileToExtract, String destDir) {
        Result result = new Result("Extract File");
        result.setInput(fileToExtract);
        FileInputStream fis;

        //buffer for read and write data to file
        byte[] buffer = new byte[1024];
        try {
            fis = new FileInputStream(fileToExtract);
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(destDir + File.separator + fileName);
                System.out.println("Unzipping to "+newFile.getAbsolutePath());
                //create directories for sub directories in zip
                new File(newFile.getParent()).mkdirs();
                FileOutputStream fos = new FileOutputStream(newFile);
                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }
                fos.close();
                //close this ZipEntry
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            //close last ZipEntry
            zis.closeEntry();
            zis.close();
            fis.close();
            result.setOutput("successful");
        } catch (IOException e) {
            e.printStackTrace();
            result.setOutput("Unsuccessful");
        }
        return result;
    }

    public static ArrayList<HistoricalDataFile> getListOfFiles() {
        // Create and populate a List of historical data files
        File dataFolder = new File(Environment.getExternalStorageDirectory().getPath() + File.separator + STORE_DIR);
        if (!dataFolder.exists()) {
            new File(dataFolder.getPath()).mkdir();
        }

        ArrayList<HistoricalDataFile> dataFiles = new ArrayList<>();
        if(dataFolder.isDirectory()) {
            File[] listOfFiles = dataFolder.listFiles();
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile() && listOfFiles[i].getName().contains("csv")) {
                    File currentFile = listOfFiles[i];
                    dataFiles.add(new HistoricalDataFile(currentFile.getName(), currentFile));
                }
            }
        }
        Collections.sort(dataFiles, new FileNameSort());
        return dataFiles;
    }

    public static List<String> readFileLineByLine(String filename) {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            List<String> lines = new ArrayList<>();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(file));
                String line;
                while ((line = reader.readLine()) != null) {
                    lines.add(line);
                }
            } catch (FileNotFoundException e) {
                System.out.println("File not found");
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return lines;
        } else {
            return new ArrayList<>();
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public static SystemSettings getSystemSettings(String filename) {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            try {
                String settings_str = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                return (SystemSettings) fromJson(settings_str, SystemSettings.class);
            } catch (IOException e) {
                System.out.println("Error reading systemSettings file.");
            }
            return new SystemSettings();
        } else {
            SystemSettings systemSettings = new SystemSettings(MainActivity.SERVER_IP, Integer.valueOf(MainActivity.SERVER_PORT), 0);
            updateSettingsFile(systemSettings, MainActivity.SYSTEM_SETTINGS_FILE);
            return systemSettings;
        }
    }

    public static CalibrationSettings getCalibrationSettings(String filename) {
        File file = new File(filename);
        if (file.exists() && file.isFile()) {
            try {
                String settings_str = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                return (CalibrationSettings) fromJson(settings_str, CalibrationSettings.class);
            } catch (IOException e) {
                System.out.println("Error reading calibrationSettings file.");
            }
            return new CalibrationSettings();
        } else {
            CalibrationSettings calibrationSettings = new CalibrationSettings(1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,1,20000,3600);
            updateSettingsFile(calibrationSettings, MainActivity.CALIBRATION_SETTINGS_FILE);
            return calibrationSettings;
        }
    }

    /**
     * Convert a java object to a json string
     */
    public static String toJson(Object obj) {
        return gson.toJson(obj);
    }

    /**
     * Convert a json string to a java object
     * @param str	json string
     * @param clazz	class's blueprint to convert json string to object
     * @return converted object
     */
    public static Object fromJson(String str, Class clazz) {
        return gson.fromJson(str, clazz);
    }

    public static Map<String, float[]> readData(String filename) {
        Map<String, float[]> data = new HashMap<>();
        List<float[]> columns = new ArrayList<>();
        List<String> lines = Utils.readFileLineByLine(filename);
        if (lines.size() > 0) {
            String[] headers = lines.get(0).replace("\uFEFF", "").split(",");
            for (int i = 0; i < headers.length; i++) {
                columns.add(new float[lines.size()-3]);
                data.put(headers[i], columns.get(i));
            }
            for (int i = 1; i < lines.size()-2; i++) {
                String[] row = lines.get(i).split(",");
                for (int j = 0; j < row.length; j++) {
                    columns.get(j)[i-1] = Float.parseFloat(row[j]);
                }
            }
            String[] beginTemp = lines.get(lines.size()-2).split(",");
            String[] endTemp = lines.get(lines.size()-1).split(",");
            if (beginTemp.length == 3 && endTemp.length == 3) {
                float[] t1 = {Float.parseFloat(beginTemp[0]), Float.parseFloat(endTemp[0])};
                float[] t2 = {Float.parseFloat(beginTemp[1]), Float.parseFloat(endTemp[1])};
                float[] t3 = {Float.parseFloat(beginTemp[2]), Float.parseFloat(endTemp[2])};
                data.put("T1", t1);
                data.put("T2", t2);
                data.put("T3", t3);
            }
        }
        return data;
    }

    public static String getLatestDataFile(List<HistoricalDataFile> dataFiles) {
        long latest = 0;
        String latestFile = null;
        for (HistoricalDataFile file : dataFiles) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
            try {
                Date date = simpleDateFormat.parse(file.getFileName());
                if (date != null && date.getTime() > latest) {
                    latest = date.getTime();
                    latestFile = file.getFileName();
                }
            } catch (ParseException e) {
                System.out.println("Error getting latest data file");
            }
        }
        return latestFile;
    }

    public static List<String> getNewFiles(List<String> availableFiles) {
        List<String> newFiles = new ArrayList<>();
        for (String file : availableFiles) {
            if (!hasFile(file)) {
                newFiles.add(file);
            }
        }
        return newFiles;
    }

    public static float[][] computeVolume(float[] encoderPos) {
        if (encoderPos != null) {
            float[] headCylVolumes = new float[encoderPos.length];
            float[] crankCylVolumes = new float[encoderPos.length];
            float[][] volumes = {headCylVolumes, crankCylVolumes};

            for (int i = 0 ; i < encoderPos.length; i++) {
                double A = encoderPos[i]*Math.PI/180;
                double B = Math.asin(Math.sin(A)*2.5/10.25);
                double C = (Math.cos(Math.PI-A)*2.5) + (Math.cos(Math.PI-B)*10.25);
                double stroke = C + (10.25 + 2.5);
                headCylVolumes[i] = (float)((5-stroke)*28.26 + 25.434);
                crankCylVolumes[i] = (float)(stroke*28.26) + 25;
            }
            return volumes;
        }
        return null;
    }

    public static void updateSettingsFile(Object settingsFile, String filename) {
        String settings = Utils.toJson(settingsFile);
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
            writer.write(settings);
            writer.close();
        } catch (IOException e) {
            System.out.println("Error updating systemSettings file");
        }
    }

    private static boolean hasFile(String fileToCheck) {
        for (HistoricalDataFile file : getListOfFiles()) {
            if (file.getFileName().equals(fileToCheck)) {
                return true;
            }
        }
        return false;
    }
}
