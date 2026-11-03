package usbgroup37;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class UsbMonitorService implements Runnable {

    private final UsbDetector detector;
    private final EventRepository repository;
    private final TrustedDeviceRegistry registry;
    private final EventLogger logger;
    private final DriverScanner scanner;

    private ScheduledExecutorService executor;
    private Set<UsbDrive> previousSnapshot;
    private final List<UsbEventListener> listeners;

    public UsbMonitorService(UsbDetector detector, EventRepository repository, TrustedDeviceRegistry registry, EventLogger logger) {
        this.detector = detector;
        this.repository = repository;
        this.registry = registry;
        this.logger = logger;
        this.scanner = new DriverScanner();
        this.previousSnapshot = new HashSet<>();
        this.listeners = new ArrayList<>(); 
    }

    public void addListener(UsbEventListener listener) {
        listeners.add(listener);
    }
    
    public UsbDetector getDetector() {
        return detector;
    }

    public void start() {
        if (executor == null || executor.isShutdown()) {
            executor = Executors.newSingleThreadScheduledExecutor();
            executor.scheduleAtFixedRate(this, 0, 2, TimeUnit.SECONDS);
            System.out.println("USB Monitoring Service Started...");
        }
    }

    public void stop() {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            System.out.println("USB Monitoring Service Stopped.");
        }
    }

    @Override
    public void run() {
        poll();
    }

    private void poll() {
        Set<UsbDrive> currentSnapshot = detector.scanDevices();

        // Detect CONNECTED
        for (UsbDrive drive : currentSnapshot) {
            if (!previousSnapshot.contains(drive)) {
                boolean isTrusted = registry.isTrusted(drive.getDeviceID());
                UsbEvent event = new UsbEvent(EventType.CONNECTED, drive, isTrusted);
                processEvent(event);
                
                // AUTO-SCAN IN BACKGROUND THREAD
                new Thread(() -> {
                    System.out.println("\n[Auto-Scan] Started scanning " + drive.getMountPoint());
                    String summary = scanner.getSummary(drive.getMountPoint());
                    System.out.println(summary);
                }).start();
            }
        }

        // Detect REMOVED
        for (UsbDrive drive : previousSnapshot) {
            if (!currentSnapshot.contains(drive)) {
                boolean isTrusted = registry.isTrusted(drive.getDeviceID());
                UsbEvent event = new UsbEvent(EventType.REMOVED, drive, isTrusted);
                processEvent(event);
            }
        }

        previousSnapshot = new HashSet<>(currentSnapshot);
    }

    private void processEvent(UsbEvent event) {
        logger.log(event);           
        repository.add(event);       
        notifyListeners(event); 
    }

    private void notifyListeners(UsbEvent event) {
        for (UsbEventListener listener : listeners) {
            listener.onUsbEvent(event);
        }
    }
}