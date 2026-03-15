package com.library.ui;

import com.library.exception.*;
import com.library.model.BorrowRecord;
import com.library.service.LibraryService;
import com.library.util.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

/**
 * Issue/Return tab panel — handles book issuing and returning operations.
 *
 * OOP Concept - INHERITANCE:
 * IssuePanel extends JPanel, reusing all Swing component infrastructure.
 *
 * OOP Concept - POLYMORPHISM (Custom TableCellRenderer):
 * We override DefaultTableCellRenderer to customize how overdue rows are displayed.
 * The JTable calls getTableCellRendererComponent() polymorphically — our custom
 * renderer provides red highlighting for overdue records.
 */
public class IssuePanel extends JPanel {

    private final LibraryService libraryService;

    // Issue form fields
    private JTextField issueStudentIdField;
    private JTextField issueStudentPhoneField;
    private JTextField issueBookIdField;

    // Return form fields
    private JTextField returnRecordIdField;

    // Active borrows table
    private JTable borrowTable;
    private BorrowTableModel borrowModel;

    public IssuePanel() {
        this.libraryService = LibraryService.getInstance();
        initializeUI();
        refreshData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Top section: Issue and Return forms side by side
        JPanel formsPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        formsPanel.add(createIssueForm());
        formsPanel.add(createReturnForm());
        add(formsPanel, BorderLayout.NORTH);

        // Center: Active borrows table
        JPanel tablePanel = new JPanel(new BorderLayout(0, 5));
        tablePanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(52, 73, 94)),
                "Active Borrows", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(44, 62, 80)));

        borrowModel = new BorrowTableModel();
        borrowTable = new JTable(borrowModel);
        borrowTable.setRowHeight(28);
        borrowTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        borrowTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        borrowTable.getTableHeader().setBackground(new Color(52, 73, 94));
        borrowTable.getTableHeader().setForeground(Color.WHITE);
        borrowTable.setSelectionBackground(new Color(52, 152, 219, 50));
        borrowTable.setGridColor(new Color(220, 220, 220));

        /**
         * OOP Concept - POLYMORPHISM (Custom Renderer):
         * DefaultTableCellRenderer is overridden to highlight overdue rows in red.
         * The JTable calls this renderer polymorphically for each cell.
         * This is an example of the Strategy Pattern — the rendering strategy
         * can be swapped without changing the JTable code.
         */
        borrowTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);

                // Get status from the last column
                String status = (String) table.getModel().getValueAt(row, 5);
                if ("OVERDUE".equals(status)) {
                    comp.setBackground(new Color(255, 200, 200)); // Light red for overdue
                    comp.setForeground(new Color(200, 0, 0));
                } else if (!isSelected) {
                    comp.setBackground(Color.WHITE);
                    comp.setForeground(Color.BLACK);
                }
                return comp;
            }
        });

        JScrollPane scrollPane = new JScrollPane(borrowTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
    }

    /**
     * Creates the "Issue Book" form panel.
     */
    private JPanel createIssueForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 204, 113)),
                "Issue Book", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(39, 174, 96)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Student ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issueStudentIdField = new JTextField(15);
        issueStudentIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(issueStudentIdField, gbc);

        // Book ID
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(new JLabel("Book ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issueBookIdField = new JTextField(15);
        issueBookIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(issueBookIdField, gbc);

        // Phone number (optional)
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(new JLabel("Phone (SMS):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        issueStudentPhoneField = new JTextField(15);
        issueStudentPhoneField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(issueStudentPhoneField, gbc);

        // Issue button
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton issueBtn = new JButton("  Issue Book  ");
        issueBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        issueBtn.setBackground(new Color(46, 204, 113));
        issueBtn.setForeground(Color.WHITE);
        issueBtn.setFocusPainted(false);
        issueBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        issueBtn.addActionListener(e -> handleIssueBook());
        panel.add(issueBtn, gbc);

        return panel;
    }

    /**
     * Creates the "Return Book" form panel.
     */
    private JPanel createReturnForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(230, 126, 34)),
                "Return Book", TitledBorder.LEFT, TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14), new Color(211, 84, 0)));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Record ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(new JLabel("Record ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        returnRecordIdField = new JTextField(15);
        returnRecordIdField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        panel.add(returnRecordIdField, gbc);

        // Return button
        gbc.gridx = 0; gbc.gridy = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        JButton returnBtn = new JButton("  Return Book  ");
        returnBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        returnBtn.setBackground(new Color(230, 126, 34));
        returnBtn.setForeground(Color.WHITE);
        returnBtn.setFocusPainted(false);
        returnBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        returnBtn.addActionListener(e -> handleReturnBook());
        panel.add(returnBtn, gbc);

        // Hint
        gbc.gridy = 2;
        JLabel hint = new JLabel("(Enter the Record ID from the table below)");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        hint.setForeground(Color.GRAY);
        panel.add(hint, gbc);

        return panel;
    }

    /**
     * Handles the issue book action with validation and error handling.
     */
    private void handleIssueBook() {
        String studentId = issueStudentIdField.getText().trim();
        String bookId = issueBookIdField.getText().trim();
        String phoneNumber = issueStudentPhoneField.getText().trim();

        // Input validation
        if (studentId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Student ID.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            issueStudentIdField.requestFocus();
            return;
        }
        if (bookId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Book ID.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            issueBookIdField.requestFocus();
            return;
        }

        try {
            BorrowRecord record = libraryService.issueBook(bookId, studentId, phoneNumber);
            JOptionPane.showMessageDialog(this,
                    String.format("Book issued successfully!\n\nRecord ID: %s\nBook: %s\nStudent: %s\nDue Date: %s",
                            record.getRecordId(),
                            libraryService.getBookTitle(bookId),
                            studentId,
                            DateUtils.formatDate(record.getDueDate())),
                    "Issue Successful", JOptionPane.INFORMATION_MESSAGE);

            // Clear fields
            issueStudentIdField.setText("");
            issueBookIdField.setText("");
            issueStudentPhoneField.setText("");
            refreshData();

        } catch (BookNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Book Not Found", JOptionPane.ERROR_MESSAGE);
        } catch (BookNotAvailableException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Not Available", JOptionPane.ERROR_MESSAGE);
        } catch (StudentLimitExceededException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Limit Exceeded", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Handles the return book action.
     */
    private void handleReturnBook() {
        String recordId = returnRecordIdField.getText().trim();

        if (recordId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a Record ID.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            returnRecordIdField.requestFocus();
            return;
        }

        try {
            double fine = libraryService.returnBook(recordId);

            String message;
            if (fine > 0) {
                message = String.format("Book returned successfully!\n\nOverdue Fine: Rs. %.2f\n" +
                        "(Rs. 5 per day after due date)", fine);
            } else {
                message = "Book returned successfully!\nNo fine — returned on time.";
            }

            JOptionPane.showMessageDialog(this, message,
                    "Return Successful", JOptionPane.INFORMATION_MESSAGE);

            returnRecordIdField.setText("");
            refreshData();

        } catch (BookNotFoundException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(),
                    "Record Not Found", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Refreshes the active borrows table.
     */
    public void refreshData() {
        borrowModel.refreshData();
    }

    // ==================== INNER CLASS: Borrow Table Model ====================

    private class BorrowTableModel extends AbstractTableModel {
        private final String[] columns = {"Record ID", "Book", "Student ID", "Issue Date", "Due Date", "Status"};
        private List<BorrowRecord> records;

        public BorrowTableModel() {
            records = libraryService.getActiveRecords();
        }

        public void refreshData() {
            records = libraryService.getActiveRecords();
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
                case 3 -> DateUtils.formatDate(r.getIssueDate());
                case 4 -> DateUtils.formatDate(r.getDueDate());
                case 5 -> r.isOverdue() ? "OVERDUE" : r.getStatus().name();
                default -> "";
            };
        }
    }
}
