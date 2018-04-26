package edu.rit.DRMonitor;

/**
 * Class representation of the calibration file.
 * When the app first loads, the calibration file gets
 * parsed into this class
 */

public class CalibrationSettings {
    private int g1_scale = 1;
    private int g1_shift = 1;
    private int g2_scale = 1;
    private int g2_shift = 1;
    private int g3_scale = 1;
    private int g3_shift = 1;
    private int p1_scale = 1;
    private int p1_shift = 1;
    private int p2_scale = 1;
    private int p2_shift = 1;
    private int t1_scale = 1;
    private int t1_shift = 1;
    private int t2_scale = 1;
    private int t2_shift = 1;
    private int t3_scale = 1;
    private int t3_shift = 1;
    private int numSamp = 20000;
    private int interval = 3600;

    // Default constructor for deserialization
    public CalibrationSettings(){}

    // Public constructor for creating new object in case no calibration file exists on tablet
    public CalibrationSettings(int g1_scale, int g1_shift, int g2_scale, int g2_shift, int g3_scale, int g3_shift,
                                int p1_scale, int p1_shift, int p2_scale, int p2_shift, int t1_scale, int t1_shift,
                                int t2_scale, int t2_shift, int t3_scale, int t3_shift, int numSamp, int interval) {
        this.g1_scale = g1_scale;
        this.g1_shift = g1_shift;
        this.g2_scale = g2_scale;
        this.g2_shift = g2_shift;
        this.g3_scale = g3_scale;
        this.g3_shift = g3_shift;
        this.p1_scale = p1_scale;
        this.p1_shift = p1_shift;
        this.p2_scale = p2_scale;
        this.p2_shift = p2_shift;
        this.t1_scale = t1_scale;
        this.t1_shift = t1_shift;
        this.t2_scale = t2_scale;
        this.t2_shift = t2_shift;
        this.t3_scale = t3_scale;
        this.t3_shift = t3_shift;
        this.numSamp = numSamp;
        this.interval = interval;
    }

    // Getters and setters
    public int getG1_scale() {
        return g1_scale;
    }

    public void setG1_scale(int g1_scale) {
        this.g1_scale = g1_scale;
    }

    public int getG1_shift() {
        return g1_shift;
    }

    public void setG1_shift(int g1_shift) {
        this.g1_shift = g1_shift;
    }

    public int getG2_scale() {
        return g2_scale;
    }

    public void setG2_scale(int g2_scale) {
        this.g2_scale = g2_scale;
    }

    public int getG2_shift() {
        return g2_shift;
    }

    public void setG2_shift(int g2_shift) {
        this.g2_shift = g2_shift;
    }

    public int getG3_scale() {
        return g3_scale;
    }

    public void setG3_scale(int g3_scale) {
        this.g3_scale = g3_scale;
    }

    public int getG3_shift() {
        return g3_shift;
    }

    public void setG3_shift(int g3_shift) {
        this.g3_shift = g3_shift;
    }

    public int getP1_scale() {
        return p1_scale;
    }

    public void setP1_scale(int p1_scale) {
        this.p1_scale = p1_scale;
    }

    public int getP1_shift() {
        return p1_shift;
    }

    public void setP1_shift(int p1_shift) {
        this.p1_shift = p1_shift;
    }

    public int getP2_scale() {
        return p2_scale;
    }

    public void setP2_scale(int p2_scale) {
        this.p2_scale = p2_scale;
    }

    public int getP2_shift() {
        return p2_shift;
    }

    public void setP2_shift(int p2_shift) {
        this.p2_shift = p2_shift;
    }

    public int getT1_scale() {
        return t1_scale;
    }

    public void setT1_scale(int t1_scale) {
        this.t1_scale = t1_scale;
    }

    public int getT1_shift() {
        return t1_shift;
    }

    public void setT1_shift(int t1_shift) {
        this.t1_shift = t1_shift;
    }

    public int getT2_scale() {
        return t2_scale;
    }

    public void setT2_scale(int t2_scale) {
        this.t2_scale = t2_scale;
    }

    public int getT2_shift() {
        return t2_shift;
    }

    public void setT2_shift(int t2_shift) {
        this.t2_shift = t2_shift;
    }

    public int getT3_scale() {
        return t3_scale;
    }

    public void setT3_scale(int t3_scale) {
        this.t3_scale = t3_scale;
    }

    public int getT3_shift() {
        return t3_shift;
    }

    public void setT3_shift(int t3_shift) {
        this.t3_shift = t3_shift;
    }

    public int getNumSamp() {
        return numSamp;
    }

    public void setNumSamp(int numSamp) {
        this.numSamp = numSamp;
    }

    public int getInterval() { return interval; }

    public void setInterval(int interval) { this.interval = interval; }
}
