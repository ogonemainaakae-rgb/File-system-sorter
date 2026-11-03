package usbgroup37;

import java.io.Serializable;
import java.util.Objects;

public class UsbDrive implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private String deviceID;
    private String label;
    private String mountPoint;
    private String fileSystem;
    private long totalBytes;

    public UsbDrive(String deviceID, String label, String mountPoint, String fileSystem, long totalBytes) {
        this.deviceID = deviceID;
        this.label = label;
        this.mountPoint = mountPoint;
        this.fileSystem = fileSystem;
        this.totalBytes = totalBytes;
    }

    public String getDeviceID() { return deviceID; }
    public void setDeviceID(String deviceID) { this.deviceID = deviceID; }

    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }

    public String getMountPoint() { return mountPoint; }
    public void setMountPoint(String mountPoint) { this.mountPoint = mountPoint; }

    public String getFileSystem() { return fileSystem; }
    public void setFileSystem(String fileSystem) { this.fileSystem = fileSystem; }

    public long getTotalBytes() { return totalBytes; }
    public void setTotalBytes(long totalBytes) { this.totalBytes = totalBytes; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsbDrive usbDrive = (UsbDrive) o;
        return Objects.equals(deviceID, usbDrive.deviceID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceID);
    }

    @Override
    public String toString() {
        return "UsbDrive [ID=" + deviceID + ", Label=" + label + ", Mount=" + mountPoint + "]";
    }
}