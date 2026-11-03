package usbgroup37;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EventRepository {
    
    // THREAD SAFETY: We wrap the ArrayList in a synchronized list.
    private final List<UsbEvent> events;
    private static final String FILE_PATH = "events.ser";

    public EventRepository() {
        this.events = Collections.synchronizedList(new ArrayList<>());
    }

    public void add(UsbEvent event) {
        events.add(event);
        save();
    }

    // BIG O COMPLEXITY: O(n) because we must copy every element
    public List<UsbEvent> getAllEvents() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    public void save() {
        synchronized (events) {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_PATH))) {
                oos.writeObject(new ArrayList<>(events));
            } catch (IOException e) {
                System.err.println("Error saving repository: " + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unchecked")
    public void load() {
        File file = new File(FILE_PATH);
        if (!file.exists()) return;

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_PATH))) {
            List<UsbEvent> loaded = (List<UsbEvent>) ois.readObject();
            synchronized (events) {
                events.clear();
                events.addAll(loaded);
            }
            System.out.println("Loaded " + events.size() + " past events from history.");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading repository: " + e.getMessage());
        }
    }
}