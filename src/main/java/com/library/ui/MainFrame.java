package com.library.ui;

import com.library.service.LibraryService;
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

        reportsMenu.add(generateReport);
        reportsMenu.add(viewReport);

        // --- Help Menu ---
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JMenuItem about = new JMenuItem("About");
        about.addActionListener(e -> showAboutDialog());

        helpMenu.add(about);

        menuBar.add(fileMenu);
        menuBar.add(reportsMenu);
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
}
