package usbgroup37;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

public class ReportGenerator {
    private final EventRepository repository;

    public ReportGenerator(EventRepository repository) {
        this.repository = repository;
    }

    public void generateReport(String filePath) {
        List<UsbEvent> events = repository.getAllEvents();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            writer.println("Timestamp,Event Type,Mount Point,Device Label,Device ID,Trusted Status");
            for (UsbEvent e : events) {
                writer.printf("%s,%s,%s,%s,%s,%s\n",
                        e.getTimestamp(), e.getType(), e.getDevice().getMountPoint(),
                        e.getDevice().getLabel(), e.getDevice().getDeviceID(), e.isTrusted());
            }
            System.out.println("Report successfully generated at: " + filePath);
        } catch (IOException e) {
            System.out.println("Error generating report: " + e.getMessage());
        }
    }
}