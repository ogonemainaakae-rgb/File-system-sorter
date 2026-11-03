package usbgroup37;

import java.util.List;
import java.util.Scanner;

public class CliMenu {
    private final UsbMonitorService monitorService;
    private final MockUsbDetector mockDetector;
    private final EventRepository repository;
    private final DriverScanner scanner;
    private final ReportGenerator reportGen;

    public CliMenu(UsbMonitorService monitorService, MockUsbDetector mockDetector, 
                   EventRepository repository, DriverScanner scanner, ReportGenerator reportGen) {
        this.monitorService = monitorService;
        this.mockDetector = mockDetector;
        this.repository = repository;
        this.scanner = scanner;
        this.reportGen = reportGen;
    }

    public void start() {
        Scanner input = new Scanner(System.in);
        boolean running = true;

        System.out.println("\n=== USB DEVICE ACTIVITY MONITOR ===");
        
        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. Start Background Monitoring");
            System.out.println("2. Stop Monitoring");
            System.out.println("3. View Event History");
            System.out.println("4. Generate CSV Report");
            System.out.println("5. Simulate USB Plug-in (For Testing)");
            System.out.println("6. Scan a Folder (Recursive File Search)");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            String choice = input.nextLine();

            switch (choice) {
                case "1":
                    monitorService.start();
                    break;
                case "2":
                    monitorService.stop();
                    break;
                case "3":
                    viewHistory();
                    break;
                case "4":
                    reportGen.generateReport("usb_report.csv");
                    break;
                case "5":
                    System.out.print("Enter a name for the fake USB: ");
                    String name = input.nextLine();
                    UsbDrive fakeDrive = new UsbDrive("9999", name, "F:\\", "NTFS", 32000000000L);
                    mockDetector.simulatePlugIn(fakeDrive);
                    System.out.println("Simulated plug-in!");
                    break;
                case "6":
                    System.out.print("Enter the folder path to scan (e.g., C:\\Users\\ogone\\Desktop): ");
                    String path = input.nextLine();
                    scanner.scan(path);
                    break;
                case "7":
                    running = false;
                    monitorService.stop();
                    System.out.println("Exiting system. Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
        input.close();
    }

    private void viewHistory() {
        List<UsbEvent> events = repository.getAllEvents();
        if (events.isEmpty()) {
            System.out.println("No history found.");
        } else {
            System.out.println("\n--- EVENT HISTORY ---");
            for (UsbEvent e : events) {
                System.out.println(e.toLogLine());
            }
        }
    }
}