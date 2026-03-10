package com.library.ui;

import com.library.exception.BookNotFoundException;
import com.library.exception.DuplicateBookException;
import com.library.model.Book;
import com.library.service.LibraryService;
import com.library.util.Constants;
import com.library.util.DateUtils;
import com.library.util.IDGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

/**
 * Books management tab panel — displays all books in a JTable with CRUD operations.
 *
 * OOP Concept - INHERITANCE:
 * BookPanel extends JPanel, inheriting Swing component behavior and adding
 * our custom book management functionality.
 *
 * OOP Concept - INNER CLASSES:
 * BookTableModel is an inner class that extends AbstractTableModel.
 * It has access to the enclosing class's fields and methods, demonstrating
 * the close relationship between the panel and its data model.
 */
public class BookPanel extends JPanel {

    private final LibraryService libraryService;
    private JTable bookTable;
    private BookTableModel bookModel;
    private TableRowSorter<BookTableModel> rowSorter;

    public BookPanel() {
        this.libraryService = LibraryService.getInstance();
        initializeUI();
        refreshData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // Title
        JLabel titleLabel = new JLabel("  Book Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(44, 62, 80));

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        JButton addBtn = createButton("Add Book", new Color(46, 204, 113));
        JButton editBtn = createButton("Edit Book", new Color(52, 152, 219));
        JButton deleteBtn = createButton("Delete Book", new Color(231, 76, 60));
        JButton refreshBtn = createButton("Refresh", new Color(149, 165, 166));

        buttonPanel.add(addBtn);
        buttonPanel.add(editBtn);
        buttonPanel.add(deleteBtn);
        buttonPanel.add(refreshBtn);

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        // Book table
        bookModel = new BookTableModel();
        bookTable = new JTable(bookModel);

        /**
         * OOP Concept - POLYMORPHISM with TableRowSorter:
         * TableRowSorter uses the Comparable interface to sort columns.
         * Different column types (String, Integer, LocalDate) each have their own
         * compareTo() implementation — this is polymorphism in action.
         */
        rowSorter = new TableRowSorter<>(bookModel);
        bookTable.setRowSorter(rowSorter);

        bookTable.setRowHeight(28);
        bookTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        bookTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        bookTable.getTableHeader().setBackground(new Color(52, 73, 94));
        bookTable.getTableHeader().setForeground(Color.WHITE);
        bookTable.setSelectionBackground(new Color(52, 152, 219, 50));
        bookTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookTable.setGridColor(new Color(220, 220, 220));

        // Set column widths
        bookTable.getColumnModel().getColumn(0).setPreferredWidth(80);   // ID
        bookTable.getColumnModel().getColumn(1).setPreferredWidth(250);  // Title
        bookTable.getColumnModel().getColumn(2).setPreferredWidth(180);  // Author
        bookTable.getColumnModel().getColumn(3).setPreferredWidth(120);  // Genre
        bookTable.getColumnModel().getColumn(4).setPreferredWidth(80);   // Total
        bookTable.getColumnModel().getColumn(5).setPreferredWidth(80);   // Available
        bookTable.getColumnModel().getColumn(6).setPreferredWidth(110);  // Date

        JScrollPane scrollPane = new JScrollPane(bookTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        add(scrollPane, BorderLayout.CENTER);

        // --- Button Action Listeners ---

        addBtn.addActionListener(e -> showAddBookDialog());
        editBtn.addActionListener(e -> showEditBookDialog());
        deleteBtn.addActionListener(e -> deleteSelectedBook());
        refreshBtn.addActionListener(e -> refreshData());
    }

    /**
     * Shows a dialog for adding a new book.
     * Demonstrates form validation at the UI boundary.
     */
    private void showAddBookDialog() {
        JDialog dialog = new JDialog(
                (Frame) SwingUtilities.getWindowAncestor(this), "Add New Book", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setResizable(false);

        JPanel formPanel = createBookForm(null, dialog, false);
        dialog.setContentPane(formPanel);
        dialog.setVisible(true);
    }

    /**
     * Shows a dialog for editing the selected book.
     */
    private void showEditBookDialog() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to edit.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Convert view row to model row (important when table is sorted)
        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        String bookId = (String) bookModel.getValueAt(modelRow, 0);

        libraryService.searchByBookId(bookId).ifPresent(book -> {
            JDialog dialog = new JDialog(
                    (Frame) SwingUtilities.getWindowAncestor(this), "Edit Book", true);
            dialog.setSize(450, 400);
            dialog.setLocationRelativeTo(this);
            dialog.setResizable(false);

            JPanel formPanel = createBookForm(book, dialog, true);
            dialog.setContentPane(formPanel);
            dialog.setVisible(true);
        });
    }

    /**
     * Creates a form panel for adding/editing a book.
     *
     * @param book   existing book for edit mode (null for add mode)
     * @param dialog the parent dialog to close after save
     * @param isEdit true if editing, false if adding
     */
    private JPanel createBookForm(Book book, JDialog dialog, boolean isEdit) {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JPanel formGrid = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField titleField = new JTextField(20);
        JTextField authorField = new JTextField(20);
        JTextField genreField = new JTextField(20);
        JSpinner copiesSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 1000, 1));

        // Pre-fill fields in edit mode
        if (book != null) {
            titleField.setText(book.getTitle());
            authorField.setText(book.getAuthor());
            genreField.setText(book.getGenre());
            copiesSpinner.setValue(book.getTotalCopies());
        }

        // Row 0: Title
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        formGrid.add(new JLabel("Title: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formGrid.add(titleField, gbc);

        // Row 1: Author
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        formGrid.add(new JLabel("Author: *"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formGrid.add(authorField, gbc);

        // Row 2: Genre
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        formGrid.add(new JLabel("Genre:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formGrid.add(genreField, gbc);

        // Row 3: Total Copies
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        formGrid.add(new JLabel("Total Copies:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        formGrid.add(copiesSpinner, gbc);

        panel.add(formGrid, BorderLayout.CENTER);

        // Buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton saveBtn = new JButton(isEdit ? "Update" : "Add Book");
        saveBtn.setBackground(new Color(46, 204, 113));
        saveBtn.setForeground(Color.WHITE);
        saveBtn.setFont(new Font("Segoe UI", Font.BOLD, 13));

        JButton cancelBtn = new JButton("Cancel");
        cancelBtn.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        saveBtn.addActionListener(e -> {
            // Validate input
            String title = titleField.getText().trim();
            String author = authorField.getText().trim();
            String genre = genreField.getText().trim();
            int copies = (Integer) copiesSpinner.getValue();

            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Title is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (author.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Author is required.", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                if (isEdit && book != null) {
                    // Update existing book
                    book.setTitle(title);
                    book.setAuthor(author);
                    book.setGenre(genre.isEmpty() ? "General" : genre);
                    book.setTotalCopies(copies);
                    libraryService.updateBook(book);
                    JOptionPane.showMessageDialog(dialog, "Book updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    // Add new book
                    Book newBook = new Book();
                    newBook.setTitle(title);
                    newBook.setAuthor(author);
                    newBook.setGenre(genre.isEmpty() ? "General" : genre);
                    newBook.setTotalCopies(copies);
                    newBook.setAvailableCopies(copies);
                    libraryService.addBook(newBook);
                    JOptionPane.showMessageDialog(dialog, "Book added successfully!\nID: " + newBook.getBookId(), "Success", JOptionPane.INFORMATION_MESSAGE);
                }
                refreshData();
                dialog.dispose();
            } catch (DuplicateBookException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Duplicate Book", JOptionPane.ERROR_MESSAGE);
            } catch (BookNotFoundException ex) {
                JOptionPane.showMessageDialog(dialog, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        btnPanel.add(saveBtn);
        btnPanel.add(cancelBtn);
        panel.add(btnPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * Deletes the selected book after confirmation.
     */
    private void deleteSelectedBook() {
        int selectedRow = bookTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "Please select a book to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int modelRow = bookTable.convertRowIndexToModel(selectedRow);
        String bookId = (String) bookModel.getValueAt(modelRow, 0);
        String title = (String) bookModel.getValueAt(modelRow, 1);

        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete:\n'" + title + "' (ID: " + bookId + ")?",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                libraryService.removeBook(bookId);
                refreshData();
                JOptionPane.showMessageDialog(this, "Book deleted successfully.", "Deleted", JOptionPane.INFORMATION_MESSAGE);
            } catch (BookNotFoundException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(color);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    /**
     * Refreshes the table data from the service.
     */
    public void refreshData() {
        bookModel.refreshData();
    }

    // ==================== INNER CLASS: Book Table Model ====================

    /**
     * OOP Concept - ABSTRACT CLASS & POLYMORPHISM:
     * AbstractTableModel is an abstract class that provides default implementations
     * for most TableModel methods. We only need to override the essential ones.
     * JTable calls these methods polymorphically — it doesn't know or care about
     * our specific implementation, it just uses the TableModel interface.
     */
    private class BookTableModel extends AbstractTableModel {
        private List<Book> books;

        public BookTableModel() {
            this.books = libraryService.getAllBooks();
        }

        public void refreshData() {
            this.books = libraryService.getAllBooks();
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() {
            return books.size();
        }

        @Override
        public int getColumnCount() {
            return Constants.BOOK_TABLE_COLUMNS.length;
        }

        @Override
        public String getColumnName(int column) {
            return Constants.BOOK_TABLE_COLUMNS[column];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            // Returning proper types enables correct sorting behavior
            return switch (columnIndex) {
                case 4, 5 -> Integer.class;
                default -> String.class;
            };
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Book book = books.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> book.getBookId();
                case 1 -> book.getTitle();
                case 2 -> book.getAuthor();
                case 3 -> book.getGenre();
                case 4 -> book.getTotalCopies();
                case 5 -> book.getAvailableCopies();
                case 6 -> DateUtils.formatDate(book.getAddedDate());
                default -> "";
            };
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false; // Table is read-only; editing is done via dialog
        }
    }
}
