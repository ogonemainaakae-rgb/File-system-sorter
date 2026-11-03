package usbgroup37;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class TrustedDeviceRegistry {
    private final Set<String> trustedDeviceIDs;
    private static final String FILE_PATH = "trusted.ser";

    public TrustedDeviceRegistry() {
        this.trustedDeviceIDs = new HashSet<>();
    }

    public boolean isTrusted(String deviceID) {
        return trustedDeviceIDs.contains(deviceID);
    }

    public void addTrustedDevice(String deviceID) {
        trustedDeviceIDs.add(deviceID);
        save();
    }
    
    @SuppressWarnings("unchecked")
    public void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            Set<String> loaded = (Set<String>) ois.readObject();
            trustedDeviceIDs.clear();
            trustedDeviceIDs.addAll(loaded);
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading trusted devices: " + e.getMessage());
        }
    }

    private void save() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
            oos.writeObject(trustedDeviceIDs);
        } catch (IOException e) {
            System.err.println("Error saving trusted devices: " + e.getMessage());
        }
    }
}