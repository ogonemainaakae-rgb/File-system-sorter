package usbgroup37;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class EventLogger {
    
    private static final String LOG_FILE = "usb_log.txt";

    public void log(UsbEvent event) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(LOG_FILE, true))) {
            writer.write(event.toLogLine());
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Error writing to log file: " + e.getMessage());
        }
    }
}