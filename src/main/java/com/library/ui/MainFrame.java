package com.library.ui;

import com.library.model.SmsSettings;
import com.library.model.SmsReminderSummary;
import com.library.service.LibraryService;
import com.library.service.SmsSettingsService;
import com.library.util.Constants;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;

/**
 * Main application frame — the top-level window of the Library Management System GUI.
 *
 * OOP Concept - INHERITANCE:
 * MainFrame extends JFrame (IS-A JFrame), inheriting all top-level window behavior
 * (title bar, minimize/maximize/close, menu bar support, etc.) while adding
 * our library-specific UI layout.
 *
 * OOP Concept - COMPOSITION:
 * MainFrame HAS many child components: JMenuBar, JTabbedPane, DashboardPanel, BookPanel, etc.
 * Each panel is a self-contained component responsible for its own UI and behavior.
 * This demonstrates the principle of "favor composition over inheritance."
 *
 * OOP Concept - ENCAPSULATION:
 * Each tab panel encapsulates its own data and behavior. MainFrame only knows the
 * public interface of each panel (refreshData()), not the internal implementation details.
 */
public class MainFrame extends JFrame {

    private final LibraryService libraryService;
    private final SmsSettingsService smsSettingsService;

    // Tab panels
    private DashboardPanel dashboardPanel;
    private BookPanel bookPanel;
    private IssuePanel issuePanel;
    private SearchPanel searchPanel;
    private ReportPanel reportPanel;

    // Tabbed pane
    private JTabbedPane tabbedPane;

    // Status bar labels
    private JLabel statusTotalBooks;
    private JLabel statusAvailable;
    private JLabel statusBorrowed;

    public MainFrame() {
        this.libraryService = LibraryService.getInstance();
        this.smsSettingsService = SmsSettingsService.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        // Basic frame setup
        setTitle(Constants.APP_TITLE + " v" + Constants.APP_VERSION);
        setSize(Constants.WINDOW_WIDTH, Constants.WINDOW_HEIGHT);
        setMinimumSize(new Dimension(900, 600));
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null); // Center on screen

        // Confirm exit
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                int result = JOptionPane.showConfirmDialog(MainFrame.this,
                        "Are you sure you want to exit?", "Confirm Exit",
                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (result == JOptionPane.YES_OPTION) {
                    dispose();
                    System.exit(0);
                }
            }
        });

        // Set layout
        setLayout(new BorderLayout());

        // Menu bar
        setJMenuBar(createMenuBar());

        // Tabbed pane
        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tabbedPane.setTabPlacement(JTabbedPane.TOP);

        dashboardPanel = new DashboardPanel(this);
        bookPanel = new BookPanel();
        issuePanel = new IssuePanel();
        searchPanel = new SearchPanel();
        reportPanel = new ReportPanel();

        tabbedPane.addTab("  Dashboard  ", createTabIcon(new Color(52, 152, 219)), dashboardPanel);
        tabbedPane.addTab("  Books  ", createTabIcon(new Color(46, 204, 113)), bookPanel);
        tabbedPane.addTab("  Issue/Return  ", createTabIcon(new Color(230, 126, 34)), issuePanel);
        tabbedPane.addTab("  Search  ", createTabIcon(new Color(155, 89, 182)), searchPanel);
        tabbedPane.addTab("  Reports  ", createTabIcon(new Color(231, 76, 60)), reportPanel);

        // Refresh data when switching tabs
        tabbedPane.addChangeListener(e -> refreshCurrentTab());

        add(tabbedPane, BorderLayout.CENTER);

        // Status bar
        add(createStatusBar(), BorderLayout.SOUTH);

        // Initial status update
        updateStatusBar();
    }

    /**
     * Creates the menu bar with File, Reports, and Help menus.
     */
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        menuBar.setBackground(new Color(52, 73, 94));

        // --- File Menu ---
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem importCsv = new JMenuItem("Import CSV...");
        importCsv.addActionListener(e -> importCSV());

        JMenuItem exportCsv = new JMenuItem("Export CSV...");
        exportCsv.addActionListener(e -> exportCSV());

        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> {
            dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        });

        fileMenu.add(importCsv);
        fileMenu.add(exportCsv);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // --- Reports Menu ---
        JMenu reportsMenu = new JMenu("Reports");
        reportsMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem generateReport = new JMenuItem("Generate Report");
        generateReport.addActionListener(e -> {
            tabbedPane.setSelectedIndex(4); // Switch to Reports tab
            reportPanel.refreshData();
        });

        JMenuItem viewReport = new JMenuItem("View in Console");
        viewReport.addActionListener(e -> {
            System.out.println(libraryService.generateReport());
            JOptionPane.showMessageDialog(this, "Report printed to console.",
                    "Report", JOptionPane.INFORMATION_MESSAGE);
        });

        JMenuItem smsAlerts = new JMenuItem("Send Due-Date SMS Alerts...");
        smsAlerts.addActionListener(e -> runSmsAlerts());

        reportsMenu.add(generateReport);
        reportsMenu.add(viewReport);
        reportsMenu.addSeparator();
        reportsMenu.add(smsAlerts);

        // --- Settings Menu ---
        JMenu settingsMenu = new JMenu("Settings");
        settingsMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem smsSettings = new JMenuItem("SMS Settings...");
        smsSettings.addActionListener(e -> openSmsSettingsDialog());
        settingsMenu.add(smsSettings);

        // --- Help Menu ---
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> showAboutDialog());

        helpMenu.add(about);

        menuBar.add(fileMenu);
        menuBar.add(reportsMenu);
        menuBar.add(settingsMenu);
        menuBar.add(helpMenu);

        return menuBar;
    }

    /**
     * Creates the status bar at the bottom of the frame.
     */
    private JPanel createStatusBar() {
        JPanel statusBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 5));
        statusBar.setBackground(new Color(52, 73, 94));
        statusBar.setBorder(new EmptyBorder(3, 10, 3, 10));

        statusTotalBooks = createStatusLabel("Total Books: 0");
        statusAvailable = createStatusLabel("Available: 0");
        statusBorrowed = createStatusLabel("Active Borrows: 0");

        statusBar.add(statusTotalBooks);
        statusBar.add(new JSeparator(JSeparator.VERTICAL));
        statusBar.add(statusAvailable);
        statusBar.add(new JSeparator(JSeparator.VERTICAL));
        statusBar.add(statusBorrowed);

        return statusBar;
    }

    private JLabel createStatusLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        label.setForeground(Color.WHITE);
        return label;
    }

    /**
     * Creates a small colored icon for tabs.
     */
    private Icon createTabIcon(Color color) {
        return new Icon() {
            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                g.setColor(color);
                g.fillRoundRect(x, y, 12, 12, 3, 3);
            }
            @Override
            public int getIconWidth() { return 12; }
            @Override
            public int getIconHeight() { return 12; }
        };
    }

    /**
     * Updates the status bar with current statistics.
     */
    public void updateStatusBar() {
        statusTotalBooks.setText("Total Books: " + libraryService.getTotalBookCount());
        statusAvailable.setText("Available: " + libraryService.getAvailableBookCount());
        statusBorrowed.setText("Active Borrows: " + libraryService.getActiveBorrowCount());
    }

    /**
     * Refreshes the currently visible tab and status bar.
     */
    private void refreshCurrentTab() {
        int index = tabbedPane.getSelectedIndex();
        switch (index) {
            case 0 -> dashboardPanel.refreshData();
            case 1 -> bookPanel.refreshData();
            case 2 -> issuePanel.refreshData();
            case 3 -> searchPanel.refreshData();
            // Reports tab refreshes only on explicit Generate action
        }
        updateStatusBar();
    }

    /**
     * Switches to a specific tab (used by dashboard quick-action buttons).
     */
    public void switchToTab(int index) {
        tabbedPane.setSelectedIndex(index);
    }

    /**
     * Refreshes all panels and the status bar.
     */
    public void refreshAll() {
        dashboardPanel.refreshData();
        bookPanel.refreshData();
        issuePanel.refreshData();
        searchPanel.refreshData();
        updateStatusBar();
    }

    /**
     * Shows the About dialog.
     */
    private void showAboutDialog() {
        JOptionPane.showMessageDialog(this,
                Constants.APP_TITLE + " v" + Constants.APP_VERSION + "\n\n" +
                "A comprehensive Library Management System\n" +
                "built with Java Swing.\n\n" +
                "Features:\n" +
                "• Book management (Add, Edit, Delete)\n" +
                "• Issue and Return books\n" +
                "• Real-time search\n" +
                "• Reports and statistics\n" +
                "• Data persistence with auto-save\n\n" +
                "Built for Java Course Project",
                "About", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Handles CSV import from File menu.
     */
    private void importCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Books from CSV");
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "CSV Files", "csv"));

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                int imported = libraryService.importBooksFromCSV(content.toString());
                refreshAll();
                JOptionPane.showMessageDialog(this,
                        "Imported " + imported + " books from CSV.",
                        "Import Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error reading file: " + e.getMessage(),
                        "Import Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Handles CSV export from File menu.
     */
    private void exportCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Books to CSV");
        fileChooser.setSelectedFile(new File("library_books.csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(libraryService.exportBooksToCSV());
                JOptionPane.showMessageDialog(this,
                        "Exported successfully to:\n" + file.getAbsolutePath(),
                        "Export Complete", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this,
                        "Error exporting: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Runs due-date SMS reminders from the GUI.
     */
    private void runSmsAlerts() {
        String input = JOptionPane.showInputDialog(this,
                "Send reminders for books due in how many days?",
                "2");

        if (input == null) {
            return; // user cancelled
        }

        int days;
        try {
            days = Integer.parseInt(input.trim());
            if (days < 0) {
                JOptionPane.showMessageDialog(this,
                        "Please enter zero or a positive number.",
                        "Invalid Input", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number.",
                    "Invalid Input", JOptionPane.WARNING_MESSAGE);
            return;
        }

        SmsReminderSummary summary = libraryService.sendDueDateSmsReminders(days);
        String message = String.format(
                "SMS Reminder Run Complete\n\nAttempted: %d\nSent: %d\nFailed: %d\nNo Phone: %d\nOutside Window: %d",
                summary.getAttempted(),
                summary.getSent(),
                summary.getFailed(),
                summary.getSkippedNoPhone(),
                summary.getSkippedOutsideWindow());

        JOptionPane.showMessageDialog(this, message,
                "SMS Alerts", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Opens a dialog to configure SMS provider settings.
     */
    private void openSmsSettingsDialog() {
        SmsSettings current = smsSettingsService.loadSettings();

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JComboBox<String> providerCombo = new JComboBox<>(new String[]{
                Constants.SMS_PROVIDER_GENERIC,
                Constants.SMS_PROVIDER_TWILIO
        });
        providerCombo.setSelectedItem(
                current.getProvider() == null || current.getProvider().isBlank()
                        ? Constants.SMS_PROVIDER_GENERIC
                        : current.getProvider().toUpperCase());

        JTextField apiUrlField = new JTextField(current.getSmsApiUrl(), 24);
        JTextField apiTokenField = new JTextField(current.getSmsApiToken(), 24);
        JTextField senderField = new JTextField(current.getSmsSenderId(), 24);
        JTextField sidField = new JTextField(current.getTwilioAccountSid(), 24);
        JTextField authTokenField = new JTextField(current.getTwilioAuthToken(), 24);
        JTextField fromField = new JTextField(current.getTwilioFromNumber(), 24);

        int row = 0;
        row = addSettingsRow(panel, gbc, row, "Provider:", providerCombo);
        row = addSettingsRow(panel, gbc, row, "Generic API URL:", apiUrlField);
        row = addSettingsRow(panel, gbc, row, "Generic API Token:", apiTokenField);
        row = addSettingsRow(panel, gbc, row, "Generic Sender ID:", senderField);
        row = addSettingsRow(panel, gbc, row, "Twilio Account SID:", sidField);
        row = addSettingsRow(panel, gbc, row, "Twilio Auth Token:", authTokenField);
        row = addSettingsRow(panel, gbc, row, "Twilio From Number:", fromField);

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        JLabel note = new JLabel("Environment variables override saved values when present.");
        note.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        note.setForeground(Color.DARK_GRAY);
        panel.add(note, gbc);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "SMS Settings", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result != JOptionPane.OK_OPTION) {
            return;
        }

        SmsSettings updated = new SmsSettings();
        updated.setProvider(String.valueOf(providerCombo.getSelectedItem()));
        updated.setSmsApiUrl(apiUrlField.getText());
        updated.setSmsApiToken(apiTokenField.getText());
        updated.setSmsSenderId(senderField.getText());
        updated.setTwilioAccountSid(sidField.getText());
        updated.setTwilioAuthToken(authTokenField.getText());
        updated.setTwilioFromNumber(fromField.getText());

        try {
            smsSettingsService.saveSettings(updated);
            libraryService.refreshSmsService();
            JOptionPane.showMessageDialog(this,
                    "SMS settings saved and applied.",
                    "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,
                    "Failed to save SMS settings: " + ex.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int addSettingsRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, JComponent component) {
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0;
        panel.add(new JLabel(labelText), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        panel.add(component, gbc);

        return row + 1;
    }
}
