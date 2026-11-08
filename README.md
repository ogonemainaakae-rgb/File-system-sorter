# USB Device Activity Monitor

**SDN260S Software Design 2 · Group 37 · Cape Peninsula University of Technology (CPUT)**

A Java tool that detects when USB drives are connected or removed, it records the device details and time, flags any device that is not on the trusted list. History can be reviewed, searched, sorted and exported. It runs from a command-line menu or a Swing dashboard and includes a Simulation Mode so it can be tested without physical hardware.

---

## Why this exists

USB drives are a common route for data theft, malware infection and accidental data loss. Most users have no record of which devices were plugged into their computer, or when. This tool keeps that record and highlights devices you have not marked as trusted.

## Features

- Detects USB drive **connection and removal** (background polling every 2 seconds)
- Records device ID, label, mount point, file system, size and event time
- **Flags untrusted devices**; mark a device as trusted from the GUI
- **Persistent history** across runs (Java serialization)
- **Search** history by label or drive, and **sort** by time (GUI)
- **Recursive drive scan** with folder, file and space summary
- **CSV export** of the full history
- **CLI** and **Swing GUI**
- **Simulation Mode** with six mock drives for testing without hardware

## Requirements

- Java JDK 8 or later
- Apache NetBeans or VS Code (or any Java IDE / command line)
- A computer that can mount USB drives (not needed for Simulation Mode)
- Windows recommended: real-drive detection relies on the Windows drive-type description

## Getting started

### Option A: IDE (recommended)

1. Open the `UsbGroup37` project in NetBeans or VS Code.
2. Run the `UsbGroup37` class (`src/usbgroup37/UsbGroup37.java`).

### Option B: Command line

From the project root (`UsbGroup37/`):

```bash
mkdir -p out
javac -d out src/usbgroup37/*.java
java -cp out usbgroup37.UsbGroup37
```

Run it from the project root so the data files (`events.ser`, `trusted.ser`, `usb_log.txt`, `usb_report.csv`) are created and read in the same place each time.

## Usage

On launch, choose a mode:

| Choice | Mode |
|---|---|
| `1` | Command line (CLI) |
| `2` | GUI with real hardware (detects real USB drives) |
| `3` | GUI in Simulation Mode (six fake directories) |

Then, in the dashboard:

1. Click **Start Background Monitoring**, then plug in or remove a USB drive. In Simulation Mode, use **Simulate USB Plug-in**.
2. Click **View Current & Past History** to open the history table.
   - Use the search box and sort button to filter and order events.
   - Select a row and click **Mark as Trusted** to trust that device.
   - Click a row to see the drive's files and space summary.
3. Click **Generate CSV Report** to export history to `usb_report.csv`.
4. Click **Exit** (or **Stop Monitoring and Exit**) when finished.

**Troubleshooting:** if no events appear, check that monitoring has been started and that the drive has a mount point. Use Simulation Mode to test without hardware.

## Architecture

All classes live in the `usbgroup37` package, organised into four logical layers:

| Layer | Responsibility | Classes |
|---|---|---|
| Data model | Data classes | `UsbDrive`, `UsbEvent`, `EventType` |
| Service | Detection and business logic | `UsbMonitorService`, `UsbDetector`, `PollingUsbDetector`, `MockUsbDetector`, `UsbEventListener`, `EventLogger`, `TrustedDeviceRegistry`, `DriverScanner` |
| Storage | Persistence and reports | `EventRepository`, `ReportGenerator` |
| User interface | Interfaces and launcher | `UsbGroup37`, `CliMenu`, `MonitorGui` |

### Design principles

- **Abstraction and decoupling:** `UsbMonitorService` depends on the `UsbDetector` interface. `PollingUsbDetector` reads real drives; `MockUsbDetector` supplies simulated ones. Either can be swapped in without changing the service.
- **Observer pattern:** the service notifies registered `UsbEventListener` objects on each event. `MonitorGui` registers as a listener and refreshes its history table.
- **Encapsulation:** private fields with getters; `UsbDrive` overrides `equals` and `hashCode` (drives are compared by device ID).
- **Separation of concerns:** UI classes call the service, scanner and storage classes instead of detecting drives or writing logs themselves.

### Data flow

1. A `ScheduledExecutorService` in `UsbMonitorService` calls `poll()` every 2 seconds.
2. `poll()` asks the `UsbDetector` for the current drives (`scanDevices()`).
3. The result is compared with the previous snapshot (`Set<UsbDrive>`) to find new and removed drives.
4. For each change a `UsbEvent` is created, the trusted flag is set via `TrustedDeviceRegistry`, and `processEvent(event)` is called.
5. `processEvent` logs the event (`EventLogger`), stores it (`EventRepository`, saved to disk) and notifies listeners such as `MonitorGui`.
6. On a `CONNECTED` event, a background thread runs `DriverScanner.getSummary(drive)`.

### Key classes

| Class | Role |
|---|---|
| `UsbDrive` | Drive details: deviceID, label, mountPoint, fileSystem, totalBytes |
| `UsbEvent` | One event: type (`CONNECTED` / `REMOVED`), drive, timestamp, trusted flag |
| `PollingUsbDetector` | Lists file-system roots, skipping local, fixed and network drives |
| `MockUsbDetector` | Editable set of simulated drives for testing and Simulation Mode |
| `UsbMonitorService` | Start/stop polling, snapshot diffing, event processing, listener notification |
| `EventLogger` | Appends one line per event to `usb_log.txt` |
| `TrustedDeviceRegistry` | `Set<String>` of trusted IDs, saved to `trusted.ser` |
| `EventRepository` | Synchronized event list, saved to `events.ser` |
| `DriverScanner` | Recursive folder scan; simulated results for `MOCK` drives |
| `ReportGenerator` | Exports history to CSV |
| `CliMenu` / `MonitorGui` | Console menu / Swing dashboard and history screen |
| `UsbGroup37` | Launcher (`main`) |

## Algorithms and Java features

| Item | Where | Complexity |
|---|---|---|
| Set diff of drive snapshots | `UsbMonitorService.poll` | O(d) per poll (d = drives) |
| HashSet of trusted IDs | `TrustedDeviceRegistry` | O(1) average lookup |
| Stream filter (search) | `MonitorGui.refreshTable` | O(n) |
| `Comparator` sort by time | `MonitorGui.refreshTable` | O(n log n) |
| Recursive folder traversal | `DriverScanner` | O(f), f = files and folders visited |

Advanced features used: multithreading (`ScheduledExecutorService`, synchronized event list), object serialization, generic collections, lambdas and streams, interfaces and polymorphism, and file handling (log, serialized data, CSV).

## Output files

Created in the working directory at runtime:

| File | Purpose |
|---|---|
| `events.ser` | Serialized event history |
| `trusted.ser` | Serialized trusted device IDs |
| `usb_log.txt` | Plain-text event log |
| `usb_report.csv` | CSV export (`Timestamp, Event Type, Mount Point, Device Label, Device ID, Trusted Status`) |

## Testing

Features were tested with normal and edge inputs, using real USB drives where possible and `MockUsbDetector` for repeatable cases.

| ID | Feature | Action | Result |
|---|---|---|---|
| T1 | Detection | Plug in a real flash drive | Pass |
| T2 | Detection | Remove the same drive | Pass |
| T3 | Trust flagging | Plug in an untrusted drive | Pass |
| T4 | Persistence | Close and reopen after earlier sessions | Pass |
| T5 | Simulation | Start Simulation Mode | Pass |
| T6 | Scanner | Scan a path that does not exist (edge) | Pass |
| T7 | Report | Generate CSV from the GUI | Pass |
| T8 | Summary | Click a history row | Pass |

## Known limitations

- **Fixed drives can appear as USB events:** the filter uses the Windows drive-type description, which is not reliable on every machine (the system drive `C:\` appeared in testing).
- **Weak device identification:** Java has no native USB API, so devices are identified by label, mount point, size and file system. Two identical drives could be confused.
- **Polling delay:** an event is detected only at the next polling cycle (up to 2 seconds).
- **No revoke, limited CLI:** a trusted device cannot be un-trusted, and the CLI has no search, sort or trust options.
- **Trust status in reports:** the CSV records trust at the time of the event, while the GUI shows current status.

## Future work

- Exclude fixed drives reliably
- Add a revoke option for trusted devices
- Add search, sort and trust options to the CLI
- Use operating-system events instead of polling
- Identify devices by hardware vendor and serial number

## Team

| Member | Student No. | Role | Responsibilities |
|---|---|---|---|
| Thato Ernest | 230383920 | Team Lead, Tester / QA | `UsbGroup37`, testing, report, integration |
| Tumisang Mainaakae | 230999891 | Core Service and UI | `UsbMonitorService`, `PollingUsbDetector`, `CliMenu`, `MonitorGui` |
| Ashwill Visagie | 230225934 | Data & Storage, Documentation | `UsbDrive`, `UsbEvent`, `EventType`, `EventRepository`, `TrustedDeviceRegistry`, UML, README, report |
| Kwalunga Ligwa | 222385561 | Logging & Reports | `EventLogger`, `DriverScanner`, `ReportGenerator` |
| Wande Mchwayi | 231003463 | Documentation | Report sections 1 and 4 |

## Course information

- **Module:** SDN260S Software Design 2
- **Institution:** Cape Peninsula University of Technology (CPUT)
- **Due date:** 8 November 2026
