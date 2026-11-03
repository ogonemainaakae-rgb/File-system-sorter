package usbgroup37;

import java.io.File;

public class DriverScanner {

    public static class ScanStats {
        public long totalFiles = 0;
        public long totalDirs = 0;
        public long totalBytes = 0;
    }

    public void scan(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) return;
        ScanStats stats = new ScanStats();
        System.out.println("\n--- SCANNING DIRECTORY: " + folderPath + " ---");
        scanRecursive(folder, 0, stats);
        System.out.println("Total Files: " + stats.totalFiles + " | Total Size: " + (stats.totalBytes / (1024 * 1024)) + " MB");
    }

    public String getSummary(String folderPath) {
        File folder = new File(folderPath);
        if (!folder.exists() || !folder.isDirectory()) {
            return "Error: The path '" + folderPath + "' does not exist.";
        }
        ScanStats stats = new ScanStats();
        countRecursive(folder, stats);
        return "Total Folders: " + stats.totalDirs + "\n" +
               "Total Files:   " + stats.totalFiles + "\n" +
               "Total Size:    " + (stats.totalBytes / (1024 * 1024)) + " MB";
    }

    private void scanRecursive(File folder, int depth, ScanStats stats) {
        File[] files = folder.listFiles();
        if (files == null) return; 
        StringBuilder indentBuilder = new StringBuilder();
        for (int i = 0; i < depth * 2; i++) indentBuilder.append(" ");
        String indent = indentBuilder.toString();

        for (File file : files) {
            if (file.isDirectory()) {
                stats.totalDirs++;
                System.out.println(indent + "[DIR]  " + file.getName());
                scanRecursive(file, depth + 1, stats);
            } else {
                stats.totalFiles++;
                stats.totalBytes += file.length();
            }
        }
    }

    private void countRecursive(File folder, ScanStats stats) {
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (file.isDirectory()) {
                stats.totalDirs++;
                countRecursive(file, stats);
            } else {
                stats.totalFiles++;
                stats.totalBytes += file.length();
            }
        }
    }
}