package edu.rit.DRMonitor;

/**
 * Created by Huy on 3/5/18.
 */

public class SystemSettings {
    private String ipAddress;
    private int serverPort;
    private long lastConnectedTime;

    public SystemSettings(){}

    public SystemSettings(String ipAddress, int serverPort, long lastConnectedTime) {
        this.ipAddress = ipAddress;
        this.serverPort = serverPort;
        this.lastConnectedTime = lastConnectedTime;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public long getLastConnectedTime() {
        return lastConnectedTime;
    }

    public void setLastConnectedTime(long lastConnectedTime) {
        this.lastConnectedTime = lastConnectedTime;
    }
}
