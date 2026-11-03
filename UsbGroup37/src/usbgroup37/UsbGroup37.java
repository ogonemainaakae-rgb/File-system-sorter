package usbgroup37;

import java.util.Scanner;

public class UsbGroup37 {
    public static void main(String[] args) {
        EventRepository repository = new EventRepository();
        TrustedDeviceRegistry registry = new TrustedDeviceRegistry();
        EventLogger logger = new EventLogger();

        // Load both files!
        repository.load();
        registry.load();

        DriverScanner scanner = new DriverScanner();
        ReportGenerator reportGen = new ReportGenerator(repository);

        System.out.println("=== USB MONITOR SYSTEM ===");
        System.out.println("1. Command Line (CLI)");
        System.out.println("2. GUI - Real Hardware");
        System.out.println("3. GUI - Simulation Mode");
        System.out.print("Choice: ");
        
        Scanner input = new Scanner(System.in);
        String choice = input.nextLine();

        if (choice.equals("2")) {
            UsbDetector realDetector = new PollingUsbDetector(); 
            UsbMonitorService realService = new UsbMonitorService(realDetector, repository, registry, logger);
            
            MonitorGui gui = new MonitorGui(realService, repository, registry); // Pass registry!
            gui.setVisible(true);
            realService.start(); 
            
        } else if (choice.equals("3")) {
            MockUsbDetector mockDetector = new MockUsbDetector(); 
            UsbMonitorService mockService = new UsbMonitorService(mockDetector, repository, registry, logger);
            
            mockDetector.loadSixDirectories(); 
            
            MonitorGui gui = new MonitorGui(mockService, repository, registry); // Pass registry!
            gui.setVisible(true);
            mockService.start(); 
            
        } else {
            MockUsbDetector mockDetector = new MockUsbDetector(); 
            UsbMonitorService mockService = new UsbMonitorService(mockDetector, repository, registry, logger);
            CliMenu cli = new CliMenu(mockService, mockDetector, repository, scanner, reportGen);
            cli.start();
        }
    }
}