package usbgroup37;

import java.util.HashSet;
import java.util.Set;

public class MockUsbDetector implements UsbDetector {

    private final Set<UsbDrive> simulatedDevices;

    public MockUsbDetector() {
        this.simulatedDevices = new HashSet<>();
    }

    @Override
    public Set<UsbDrive> scanDevices() {
        return new HashSet<>(simulatedDevices);
    }

    // Method to simulate a USB being plugged in
    public void simulatePlugIn(UsbDrive drive) {
        simulatedDevices.add(drive);
        System.out.println("[Mock] USB Plugged In: " + drive.getLabel());
    }

    // Method to simulate a USB being unplugged
    public void simulateUnplug(UsbDrive drive) {
        simulatedDevices.remove(drive);
        System.out.println("[Mock] USB Unplugged: " + drive.getLabel());
    }

    // NEW METHOD: This will populate your 6 directories!
    public void loadSixDirectories() {
        simulatePlugIn(new UsbDrive("MOCK1", "Documents", "E:\\", "FAT32", 1000000L));
        simulatePlugIn(new UsbDrive("MOCK2", "Images", "F:\\", "NTFS", 2000000L));
        simulatePlugIn(new UsbDrive("MOCK3", "Videos", "G:\\", "exFAT", 3000000L));
        simulatePlugIn(new UsbDrive("MOCK4", "Music", "H:\\", "FAT32", 4000000L));
        simulatePlugIn(new UsbDrive("MOCK5", "Backups", "I:\\", "NTFS", 5000000L));
        simulatePlugIn(new UsbDrive("MOCK6", "System_Files", "J:\\", "FAT32", 6000000L));
    }
}