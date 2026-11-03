package usbgroup37;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UsbEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;

    private EventType type;
    private UsbDrive device;
    private LocalDateTime timestamp;
    private boolean trusted;

    public UsbEvent(EventType type, UsbDrive device, boolean trusted) {
        this.type = type;
        this.device = device;
        this.trusted = trusted;
        this.timestamp = LocalDateTime.now(); 
    }

    // --- THESE ARE THE MISSING GETTERS ---
    public EventType getType() { return type; }
    public UsbDrive getDevice() { return device; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public boolean isTrusted() { return trusted; }
    // ------------------------------------

    public String toLogLine() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String timeStr = timestamp.format(formatter);
        String status = trusted ? "TRUSTED" : "UNTRUSTED";
        
        return String.format("[%s] %s | %s | Device: %s (%s) | Status: %s",
                timeStr,
                type,
                device.getMountPoint(),
                device.getLabel(),
                device.getDeviceID(),
                status);
    }

    @Override
    public String toString() {
        return toLogLine();
    }
}