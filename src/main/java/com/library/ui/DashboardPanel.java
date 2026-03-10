package com.library.ui;

import com.library.model.BorrowRecord;
import com.library.service.LibraryService;
import com.library.util.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.List;

/**
 * Dashboard tab panel showing library statistics and recent activity.
 *
 * OOP Concept - INHERITANCE:
 * DashboardPanel extends JPanel (IS-A JPanel), inheriting all its GUI capabilities
 * while adding our custom dashboard layout and behavior.
 *
 * OOP Concept - COMPOSITION:
 * DashboardPanel HAS-A LibraryService (uses it to get data).
 * It also HAS many Swing components (JLabels, JTable, etc.).
 */
public class DashboardPanel extends JPanel {

    private final LibraryService libraryService;

    // Statistics labels — updated on refresh
    private JLabel totalBooksLabel;
    private JLabel availableBooksLabel;
    private JLabel issuedBooksLabel;
    private JLabel overdueBooksLabel;

    // Recent activity table
    private JTable activityTable;
    private ActivityTableModel activityModel;

    // Reference to parent frame for tab switching
    private MainFrame parentFrame;

    public DashboardPanel(MainFrame parentFrame) {
        this.parentFrame = parentFrame;
        this.libraryService = LibraryService.getInstance();
        initializeUI();
        refreshData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(new Color(245, 245, 250));

        // Title
        JLabel titleLabel = new JLabel("  Library Dashboard", JLabel.LEFT);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(44, 62, 80));
        titleLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);

        // Center: Stats cards + Recent Activity
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setOpaque(false);

        // Stats cards panel (top row)
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        statsPanel.setOpaque(false);
        statsPanel.setBorder(new EmptyBorder(0, 0, 15, 0));

        totalBooksLabel = new JLabel("0", JLabel.CENTER);
        availableBooksLabel = new JLabel("0", JLabel.CENTER);
        issuedBooksLabel = new JLabel("0", JLabel.CENTER);
        overdueBooksLabel = new JLabel("0", JLabel.CENTER);

        statsPanel.add(createStatCard("Total Books", totalBooksLabel, new Color(52, 152, 219)));
        statsPanel.add(createStatCard("Available", availableBooksLabel, new Color(46, 204, 113)));
        statsPanel.add(createStatCard("Issued", issuedBooksLabel, new Color(241, 196, 15)));
        statsPanel.add(createStatCard("Overdue", overdueBooksLabel, new Color(231, 76, 60)));

        centerPanel.add(statsPanel, BorderLayout.NORTH);

        // Recent activity table
        JPanel activityPanel = new JPanel(new BorderLayout(0, 5));
        activityPanel.setOpaque(false);

        JLabel activityTitle = new JLabel("  Recent Activity (Last 10 Transactions)");
        activityTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        activityTitle.setForeground(new Color(44, 62, 80));
        activityPanel.add(activityTitle, BorderLayout.NORTH);

        activityModel = new ActivityTableModel();
        activityTable = new JTable(activityModel);
        activityTable.setRowHeight(28);
        activityTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        activityTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        activityTable.getTableHeader().setBackground(new Color(52, 73, 94));
        activityTable.getTableHeader().setForeground(Color.WHITE);
        activityTable.setSelectionBackground(new Color(52, 152, 219, 50));
        activityTable.setGridColor(new Color(220, 220, 220));

        JScrollPane scrollPane = new JScrollPane(activityTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        activityPanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(activityPanel, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // Quick action buttons (bottom)
        JPanel quickActions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        quickActions.setOpaque(false);
        quickActions.setBorder(BorderFactory.createTitledBorder("Quick Actions"));

        JButton addBookBtn = createActionButton("Add Book", new Color(52, 152, 219));
        addBookBtn.addActionListener(e -> {
            if (parentFrame != null) parentFrame.switchToTab(1); // Books tab
        });

        JButton issueBookBtn = createActionButton("Issue Book", new Color(46, 204, 113));
        issueBookBtn.addActionListener(e -> {
            if (parentFrame != null) parentFrame.switchToTab(2); // Issue tab
        });

        JButton returnBookBtn = createActionButton("Return Book", new Color(230, 126, 34));
        returnBookBtn.addActionListener(e -> {
            if (parentFrame != null) parentFrame.switchToTab(2); // Issue tab
        });

        JButton searchBtn = createActionButton("Search Books", new Color(155, 89, 182));
        searchBtn.addActionListener(e -> {
            if (parentFrame != null) parentFrame.switchToTab(3); // Search tab
        });

        quickActions.add(addBookBtn);
        quickActions.add(issueBookBtn);
        quickActions.add(returnBookBtn);
        quickActions.add(searchBtn);

        add(quickActions, BorderLayout.SOUTH);
    }

    /**
     * Creates a styled statistics card panel.
     */
    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(color, 2, true),
                new EmptyBorder(15, 15, 15, 15)));

        // Color bar at top
        JPanel colorBar = new JPanel();
        colorBar.setBackground(color);
        colorBar.setPreferredSize(new Dimension(0, 4));
        card.add(colorBar, BorderLayout.NORTH);

        JLabel titleLbl = new JLabel(title, JLabel.CENTER);
        titleLbl.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLbl.setForeground(new Color(127, 140, 141));

        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        valueLabel.setForeground(color);

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(valueLabel, BorderLayout.CENTER);
        textPanel.add(titleLbl, BorderLayout.SOUTH);
        card.add(textPanel, BorderLayout.CENTER);

        return card;
    }

    /**
     * Creates a styled action button.
     */
    private JButton createActionButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Refreshes the dashboard with current data.
     */
    public void refreshData() {
        totalBooksLabel.setText(String.valueOf(libraryService.getTotalBookCount()));
        availableBooksLabel.setText(String.valueOf(libraryService.getAvailableBookCount()));
        issuedBooksLabel.setText(String.valueOf(libraryService.getActiveBorrowCount()));
        overdueBooksLabel.setText(String.valueOf(libraryService.getOverdueCount()));
        activityModel.refreshData();
    }

    // ==================== INNER CLASS: Activity Table Model ====================

    /**
     * OOP Concept - INNER CLASS & INHERITANCE:
     * ActivityTableModel is an inner class that extends AbstractTableModel.
     * It inherits the table model infrastructure while providing our custom
     * data source (recent borrow records).
     *
     * OOP Concept - POLYMORPHISM:
     * By overriding getRowCount(), getColumnCount(), getValueAt(), etc.,
     * the JTable uses our custom implementation through the AbstractTableModel interface.
     * This is runtime polymorphism via method overriding.
     */
    private class ActivityTableModel extends AbstractTableModel {
        private final String[] columns = {"Record ID", "Book", "Student", "Date", "Due Date", "Status"};
        private List<BorrowRecord> records;

        public ActivityTableModel() {
            records = libraryService.getRecentActivity(10);
        }

        public void refreshData() {
            records = libraryService.getRecentActivity(10);
            fireTableDataChanged(); // Notifies JTable to repaint
        }

        @Override
        public int getRowCount() {
            return records.size();
        }

        @Override
        public int getColumnCount() {
            return columns.length;
        }

        @Override
        public String getColumnName(int column) {
            return columns[column];
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            BorrowRecord record = records.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> record.getRecordId();
                case 1 -> libraryService.getBookTitle(record.getBookId());
                case 2 -> record.getStudentId();
                case 3 -> DateUtils.formatDate(record.getIssueDate());
                case 4 -> DateUtils.formatDate(record.getDueDate());
                case 5 -> record.getStatus().name();
                default -> "";
            };
        }
    }
}
