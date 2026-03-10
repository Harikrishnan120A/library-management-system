package com.library.ui;

import com.library.model.BorrowRecord;
import com.library.model.LibraryReport;
import com.library.service.LibraryService;
import com.library.util.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Reports tab panel — generates and displays library statistics.
 *
 * OOP Concept - COMPOSITION:
 * ReportPanel HAS-A LibraryService and uses it to generate reports.
 * The report data is encapsulated in a LibraryReport object (DTO pattern),
 * cleanly separating data generation from data display.
 */
public class ReportPanel extends JPanel {

    private final LibraryService libraryService;

    // Summary labels
    private JLabel totalBooksVal, availableVal, issuedVal, overdueVal;
    private JLabel studentsVal, recordsVal, finesVal;

    // Most borrowed table
    private JTable mostBorrowedTable;
    private DefaultListModel<String> mostBorrowedModel;

    // Overdue table
    private JTable overdueTable;
    private OverdueTableModel overdueModel;

    // Genre distribution
    private JTextArea genreArea;

    public ReportPanel() {
        this.libraryService = LibraryService.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top: Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton generateBtn = createButton("Generate Report", new Color(52, 152, 219));
        JButton exportBtn = createButton("Export to CSV", new Color(46, 204, 113));

        generateBtn.addActionListener(e -> generateReport());
        exportBtn.addActionListener(e -> exportToCSV());

        buttonPanel.add(generateBtn);
        buttonPanel.add(exportBtn);
        add(buttonPanel, BorderLayout.NORTH);

        // Center: Report content
        JPanel reportContent = new JPanel(new BorderLayout(10, 10));

        // Summary panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        summaryPanel.setBorder(BorderFactory.createTitledBorder("Summary Statistics"));

        totalBooksVal = createStatLabel("Total Books", "—");
        availableVal = createStatLabel("Available", "—");
        issuedVal = createStatLabel("Issued", "—");
        overdueVal = createStatLabel("Overdue", "—");
        studentsVal = createStatLabel("Students", "—");
        recordsVal = createStatLabel("Total Records", "—");
        finesVal = createStatLabel("Fines Collected", "—");

        summaryPanel.add(wrapStat("Total Books", totalBooksVal));
        summaryPanel.add(wrapStat("Available", availableVal));
        summaryPanel.add(wrapStat("Issued", issuedVal));
        summaryPanel.add(wrapStat("Overdue", overdueVal));
        summaryPanel.add(wrapStat("Students", studentsVal));
        summaryPanel.add(wrapStat("Total Records", recordsVal));
        summaryPanel.add(wrapStat("Fines Collected", finesVal));

        reportContent.add(summaryPanel, BorderLayout.NORTH);

        // Bottom split: Most Borrowed + Overdue + Genre
        JPanel tablesPanel = new JPanel(new GridLayout(1, 3, 10, 0));

        // Most Borrowed list
        JPanel borrowedPanel = new JPanel(new BorderLayout());
        borrowedPanel.setBorder(BorderFactory.createTitledBorder("Most Borrowed Books"));
        mostBorrowedModel = new DefaultListModel<>();
        JList<String> borrowedList = new JList<>(mostBorrowedModel);
        borrowedList.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        borrowedPanel.add(new JScrollPane(borrowedList), BorderLayout.CENTER);
        tablesPanel.add(borrowedPanel);

        // Overdue Records
        JPanel overduePanel = new JPanel(new BorderLayout());
        overduePanel.setBorder(BorderFactory.createTitledBorder("Overdue Records"));
        overdueModel = new OverdueTableModel();
        overdueTable = new JTable(overdueModel);
        overdueTable.setRowHeight(25);
        overdueTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        overdueTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        overduePanel.add(new JScrollPane(overdueTable), BorderLayout.CENTER);
        tablesPanel.add(overduePanel);

        // Genre Distribution
        JPanel genrePanel = new JPanel(new BorderLayout());
        genrePanel.setBorder(BorderFactory.createTitledBorder("Genre Distribution"));
        genreArea = new JTextArea();
        genreArea.setEditable(false);
        genreArea.setFont(new Font("Consolas", Font.PLAIN, 13));
        genreArea.setMargin(new Insets(5, 10, 5, 10));
        genrePanel.add(new JScrollPane(genreArea), BorderLayout.CENTER);
        tablesPanel.add(genrePanel);

        reportContent.add(tablesPanel, BorderLayout.CENTER);
        add(reportContent, BorderLayout.CENTER);
    }

    /**
     * Generates the report and populates the UI.
     */
    private void generateReport() {
        LibraryReport report = libraryService.generateReport();

        // Update summary
        totalBooksVal.setText(String.valueOf(report.getTotalBooks()));
        availableVal.setText(String.valueOf(report.getAvailableBooks()));
        issuedVal.setText(String.valueOf(report.getIssuedBooks()));
        overdueVal.setText(String.valueOf(report.getOverdueBooks()));
        studentsVal.setText(String.valueOf(report.getTotalStudents()));
        recordsVal.setText(String.valueOf(report.getTotalBorrowRecords()));
        finesVal.setText(String.format("Rs. %.2f", report.getTotalFinesCollected()));

        // Update most borrowed
        mostBorrowedModel.clear();
        if (report.getMostBorrowedBooks() != null) {
            for (String book : report.getMostBorrowedBooks()) {
                mostBorrowedModel.addElement(book);
            }
        }
        if (mostBorrowedModel.isEmpty()) {
            mostBorrowedModel.addElement("No borrow records yet.");
        }

        // Update overdue table
        overdueModel.refreshData();

        // Update genre distribution
        genreArea.setText("");
        Map<String, Integer> genres = report.getGenreDistribution();
        if (genres != null) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : genres.entrySet()) {
                sb.append(String.format("%-20s : %d books%n", entry.getKey(), entry.getValue()));
                // Simple text bar chart
                sb.append("  ");
                for (int i = 0; i < entry.getValue(); i++) sb.append("█");
                sb.append("\n\n");
            }
            genreArea.setText(sb.toString());
        }

        JOptionPane.showMessageDialog(this, "Report generated successfully!",
                "Report", JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Exports the book data to a CSV file using JFileChooser.
     */
    private void exportToCSV() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Books to CSV");
        fileChooser.setSelectedFile(new File("library_books.csv"));

        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try (FileWriter writer = new FileWriter(file)) {
                String csvContent = libraryService.exportBooksToCSV();
                writer.write(csvContent);
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

    private JLabel createStatLabel(String name, String defaultValue) {
        JLabel label = new JLabel(defaultValue, JLabel.CENTER);
        label.setFont(new Font("Segoe UI", Font.BOLD, 22));
        label.setForeground(new Color(52, 73, 94));
        return label;
    }

    private JPanel wrapStat(String title, JLabel valueLabel) {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        JLabel titleLbl = new JLabel(title, JLabel.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLbl.setForeground(new Color(127, 140, 141));

        panel.add(valueLabel, BorderLayout.CENTER);
        panel.add(titleLbl, BorderLayout.SOUTH);
        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Refreshes the report data.
     */
    public void refreshData() {
        generateReport();
    }

    // ==================== INNER CLASS: Overdue Table Model ====================

    private class OverdueTableModel extends AbstractTableModel {
        private final String[] columns = {"Record", "Book", "Student", "Due Date", "Fine"};
        private List<BorrowRecord> records;

        public OverdueTableModel() {
            records = libraryService.getOverdueRecords();
        }

        public void refreshData() {
            records = libraryService.getOverdueRecords();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return records.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BorrowRecord r = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> r.getRecordId();
                case 1 -> libraryService.getBookTitle(r.getBookId());
                case 2 -> r.getStudentId();
                case 3 -> DateUtils.formatDate(r.getDueDate());
                case 4 -> String.format("Rs. %.2f", r.calculateFine());
                default -> "";
            };
        }
    }
}
