package usbgroup37;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class MonitorGui extends JFrame implements UsbEventListener {
    private final UsbMonitorService monitorService;
    private final EventRepository repository;
    private final TrustedDeviceRegistry registry;
    private final DriverScanner scanner;
    
    private JPanel cards;
    private CardLayout cardLayout;
    private final String MENU_CARD = "MainMenu";
    private final String HISTORY_CARD = "HistoryScreen";

    private JTable historyTable;
    private DefaultTableModel tableModel;
    private JTextArea detailsArea;
    private JTextField searchField;
    
    // State for sorting and filtering
    private boolean sortAscending = false; // Default: Newest first

    public MonitorGui(UsbMonitorService monitorService, EventRepository repository, TrustedDeviceRegistry registry) {
        this.monitorService = monitorService;
        this.repository = repository;
        this.registry = registry;
        this.scanner = new DriverScanner();
        
        this.monitorService.addListener(this);

        setTitle("USB Device Activity Monitor Dashboard");
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        createMainMenuScreen();
        createHistoryScreen();

        add(cards);
        setLocationRelativeTo(null);
        cardLayout.show(cards, MENU_CARD); 
    }

    private void createMainMenuScreen() {
        JPanel menuPanel = new JPanel(new GridLayout(7, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(50, 200, 50, 200));

        JButton startBtn = new JButton("1. Start Background Monitoring");
        JButton stopBtn = new JButton("2. Stop Monitoring");
        JButton historyBtn = new JButton("3. View Current & Past History");
        JButton reportBtn = new JButton("4. Generate CSV Report");
        JButton simulateBtn = new JButton("5. Simulate USB Plug-in (For Testing)");
        JButton exitBtn = new JButton("6. Exit");

        JLabel statusLabel = new JLabel(" Status: STOPPED ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setFont(new Font("Arial", Font.BOLD, 16));

        menuPanel.add(startBtn);
        menuPanel.add(stopBtn);
        menuPanel.add(statusLabel);
        menuPanel.add(historyBtn);
        menuPanel.add(reportBtn);
        menuPanel.add(simulateBtn);
        menuPanel.add(exitBtn);

        cards.add(menuPanel, MENU_CARD);

        startBtn.addActionListener(e -> {
            monitorService.start();
            statusLabel.setText(" Status: MONITORING... ");
            statusLabel.setForeground(new Color(0, 153, 0));
        });

        stopBtn.addActionListener(e -> {
            monitorService.stop();
            statusLabel.setText(" Status: STOPPED ");
            statusLabel.setForeground(Color.RED);
        });

        historyBtn.addActionListener(e -> {
            refreshTable(); 
            cardLayout.show(cards, HISTORY_CARD);
        });

        reportBtn.addActionListener(e -> {
            ReportGenerator reportGen = new ReportGenerator(repository);
            reportGen.generateReport("usb_report.csv");
            JOptionPane.showMessageDialog(this, "Report generated successfully: usb_report.csv");
        });

        simulateBtn.addActionListener(e -> {
            if (monitorService.getDetector() instanceof MockUsbDetector) {
                MockUsbDetector mock = (MockUsbDetector) monitorService.getDetector();
                String fakeId = "MOCK_" + System.currentTimeMillis();
                UsbDrive fakeDrive = new UsbDrive(fakeId, "Simulated_USB", "X:\\", "FAT32", 8000000000L);
                mock.simulatePlugIn(fakeDrive);
                JOptionPane.showMessageDialog(this, "Simulated USB Plug-in!");
            } else {
                JOptionPane.showMessageDialog(this, "Simulation only works in Simulation Mode.");
            }
        });

        exitBtn.addActionListener(e -> { monitorService.stop(); System.exit(0); });
    }

    private void createHistoryScreen() {
        JPanel panel = new JPanel(new BorderLayout());
        
        // --- TOP CONTROL BAR ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backBtn = new JButton("< Back");
        JButton sortBtn = new JButton("Sort by Time");
        JButton trustBtn = new JButton("Mark as Trusted");
        
        searchField = new JTextField(15);
        JButton searchBtn = new JButton("Search (Regex)");

        topPanel.add(backBtn);
        topPanel.add(new JLabel(" | Search Label: "));
        topPanel.add(searchField);
        topPanel.add(searchBtn);
        topPanel.add(sortBtn);
        topPanel.add(trustBtn);
        panel.add(topPanel, BorderLayout.NORTH);

        // --- TABLE & DETAILS ---
        String[] columns = {"Time", "Event Type", "Drive", "Label", "Size (MB)", "Status"};
        tableModel = new DefaultTableModel(columns, 0);
        historyTable = new JTable(tableModel);
        
        detailsArea = new JTextArea(6, 50);
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Monospaced", Font.BOLD, 14));
        detailsArea.setBorder(BorderFactory.createTitledBorder("Selected USB Summary"));

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(historyTable), new JScrollPane(detailsArea));
        split.setResizeWeight(0.6);
        panel.add(split, BorderLayout.CENTER);

        // --- ACTIONS ---
        backBtn.addActionListener(e -> cardLayout.show(cards, MENU_CARD));
        
        // Sorting Logic (Big O: O(n log n) using TimSort in Java)
        sortBtn.addActionListener(e -> {
            sortAscending = !sortAscending;
            sortBtn.setText(sortAscending ? "Sort: Oldest First" : "Sort: Newest First");
            refreshTable();
        });

        // Searching Logic (Using Streams and Regex)
        searchBtn.addActionListener(e -> refreshTable());

        // Trust Device Logic
        trustBtn.addActionListener(e -> {
            int row = historyTable.getSelectedRow();
            if (row == -1) {
                JOptionPane.showMessageDialog(this, "Select a device from the table first.");
                return;
            }
            // Get the Device ID (column index is 3 usually, but let's find it from our original list)
            // We will just use the Drive Path for simplicity in matching
            String drivePath = (String) tableModel.getValueAt(row, 2);
            String label = (String) tableModel.getValueAt(row, 3);
            
            // Find matching event to get the ID
            UsbEvent selectedEvent = repository.getAllEvents().stream()
                .filter(ev -> ev.getDevice().getMountPoint().equals(drivePath))
                .findFirst().orElse(null);

            if (selectedEvent != null) {
                registry.addTrustedDevice(selectedEvent.getDevice().getDeviceID());
                JOptionPane.showMessageDialog(this, label + " is now TRUSTED!");
                refreshTable(); // Refresh to show updated status
            }
        });

        // Auto-scan on click
        historyTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int row = historyTable.getSelectedRow();
                if (row != -1) {
                    String drivePath = (String) tableModel.getValueAt(row, 2);
                    String label = (String) tableModel.getValueAt(row, 3);
                    detailsArea.setText("Scanning " + drivePath + " (" + label + ")... Please wait.");
                    
                    new Thread(() -> {
                        String summary = scanner.getSummary(drivePath);
                        SwingUtilities.invokeLater(() -> detailsArea.setText("Device: " + label + " (" + drivePath + ")\n\n" + summary));
                    }).start();
                }
            }
        });

        cards.add(panel, HISTORY_CARD);
    }

    // This method uses STREAMS and REGEX to filter and sort!
    private void refreshTable() {
        tableModel.setRowCount(0);
        List<UsbEvent> events = repository.getAllEvents();
        
        String searchQuery = searchField.getText().trim();
        
        // 1. Filter with Streams and Regex (O(n) complexity)
        List<UsbEvent> filtered = events.stream()
            .filter(e -> e.getDevice().getLabel().matches("(?i).*" + searchQuery + ".*") || 
                         e.getDevice().getMountPoint().matches("(?i).*" + searchQuery + ".*"))
            .collect(Collectors.toList());

        // 2. Sort with Comparator (O(n log n) complexity)
        if (sortAscending) {
            filtered.sort(Comparator.comparing(UsbEvent::getTimestamp));
        } else {
            filtered.sort(Comparator.comparing(UsbEvent::getTimestamp).reversed());
        }

        // 3. Populate Table
        for (UsbEvent e : filtered) {
            boolean isTrusted = registry.isTrusted(e.getDevice().getDeviceID());
            String time = e.getTimestamp().toString().replace("T", " ");
            String sizeMB = (e.getDevice().getTotalBytes() / (1024 * 1024)) + " MB";
            String status = isTrusted ? "TRUSTED" : "UNTRUSTED";
            
            tableModel.addRow(new Object[]{time, e.getType().toString(), e.getDevice().getMountPoint(), e.getDevice().getLabel(), sizeMB, status});
        }
    }

    @Override
    public void onUsbEvent(UsbEvent event) {
        SwingUtilities.invokeLater(() -> refreshTable());
    }
}