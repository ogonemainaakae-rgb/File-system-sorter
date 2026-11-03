package usbgroup37;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import javax.swing.filechooser.FileSystemView;

public class PollingUsbDetector implements UsbDetector {

    @Override
    public Set<UsbDrive> scanDevices() {
        Set<UsbDrive> connectedDrives = new HashSet<>();
        File[] roots = File.listRoots();
        FileSystemView fsv = FileSystemView.getFileSystemView();

        if (roots != null) {
            for (File root : roots) {
                String path = root.getAbsolutePath();
                
                // Get the type of drive (e.g., "Local Disk" or "Removable Disk")
                String typeDesc = fsv.getSystemTypeDescription(root);
                
                // If it's your internal hard drive (C:\), skip it!
                if (typeDesc != null) {
                    String lowerType = typeDesc.toLowerCase();
                    if (lowerType.contains("local disk") || lowerType.contains("fixed") || lowerType.contains("network drive")) {
                        continue; 
                    }
                }

                String label = fsv.getSystemDisplayName(root);
                if (label == null || label.trim().isEmpty()) {
                    label = "Removable Disk";
                }
                
                long totalSpace = root.getTotalSpace();
                
                // Create the UsbDrive object
                UsbDrive drive = new UsbDrive(path, label, path, "Unknown", totalSpace);
                connectedDrives.add(drive);
            }
        }
        return connectedDrives;
    }
}