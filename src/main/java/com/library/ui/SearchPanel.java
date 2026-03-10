package com.library.ui;

import com.library.model.Book;
import com.library.service.LibraryService;
import com.library.util.DateUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Search tab panel — real-time search with results table and detail side panel.
 *
 * OOP Concept - INTERFACE IMPLEMENTATION (DocumentListener):
 * We implement the DocumentListener interface to react to text changes in real time.
 * This is an example of the Observer Pattern — the search field (observable) notifies
 * our listener (observer) whenever the text changes.
 *
 * OOP Concept - POLYMORPHISM:
 * The search behavior changes based on the selected search type (By ID, By Title, By Author).
 * Same search action, different behavior — this is polymorphism achieved through conditional logic.
 */
public class SearchPanel extends JPanel {

    private final LibraryService libraryService;

    private JComboBox<String> searchTypeCombo;
    private JTextField searchField;
    private JTable resultsTable;
    private SearchTableModel searchModel;

    // Detail panel labels
    private JLabel detailId, detailTitle, detailAuthor, detailGenre;
    private JLabel detailTotal, detailAvailable, detailDate;
    private JPanel detailPanel;

    public SearchPanel() {
        this.libraryService = LibraryService.getInstance();
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Top: Search bar ---
        JPanel searchBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        searchBar.setBorder(new EmptyBorder(5, 5, 5, 5));

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        searchBar.add(searchLabel);

        searchTypeCombo = new JComboBox<>(new String[]{"By Title", "By Author", "By ID"});
        searchTypeCombo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchTypeCombo.addActionListener(e -> performSearch());
        searchBar.add(searchTypeCombo);

        searchField = new JTextField(30);
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        searchField.setToolTipText("Type to search in real-time...");

        /**
         * OOP Concept - OBSERVER PATTERN (DocumentListener):
         * The DocumentListener interface defines three methods for text change events.
         * By implementing this interface, our panel "observes" the search field and
         * reacts to every keystroke — this creates a real-time search experience.
         */
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { performSearch(); }
            @Override
            public void removeUpdate(DocumentEvent e) { performSearch(); }
            @Override
            public void changedUpdate(DocumentEvent e) { performSearch(); }
        });
        searchBar.add(searchField);

        JButton clearBtn = new JButton("Clear");
        clearBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        clearBtn.addActionListener(e -> {
            searchField.setText("");
            clearDetailPanel();
        });
        searchBar.add(clearBtn);

        add(searchBar, BorderLayout.NORTH);

        // --- Center: Split pane with results table and detail panel ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.7);
        splitPane.setDividerLocation(750);

        // Results table (left side)
        searchModel = new SearchTableModel();
        resultsTable = new JTable(searchModel);
        resultsTable.setRowHeight(28);
        resultsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        resultsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultsTable.getTableHeader().setBackground(new Color(52, 73, 94));
        resultsTable.getTableHeader().setForeground(Color.WHITE);
        resultsTable.setSelectionBackground(new Color(52, 152, 219, 50));
        resultsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsTable.setGridColor(new Color(220, 220, 220));

        // Row click listener — shows book details in side panel
        resultsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showBookDetails();
            }
        });

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        tableScroll.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        splitPane.setLeftComponent(tableScroll);

        // Detail panel (right side)
        detailPanel = createDetailPanel();
        JScrollPane detailScroll = new JScrollPane(detailPanel);
        detailScroll.setBorder(BorderFactory.createTitledBorder("Book Details"));
        splitPane.setRightComponent(detailScroll);

        add(splitPane, BorderLayout.CENTER);

        // Status label
        JLabel hintLabel = new JLabel("  Start typing to search. Click a row to view details.");
        hintLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        hintLabel.setForeground(Color.GRAY);
        add(hintLabel, BorderLayout.SOUTH);
    }

    /**
     * Creates the book detail side panel.
     */
    private JPanel createDetailPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 5, 8, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTHWEST;

        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font valueFont = new Font("Segoe UI", Font.PLAIN, 13);

        detailId = new JLabel("-"); detailId.setFont(valueFont);
        detailTitle = new JLabel("-"); detailTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        detailAuthor = new JLabel("-"); detailAuthor.setFont(valueFont);
        detailGenre = new JLabel("-"); detailGenre.setFont(valueFont);
        detailTotal = new JLabel("-"); detailTotal.setFont(valueFont);
        detailAvailable = new JLabel("-"); detailAvailable.setFont(valueFont);
        detailDate = new JLabel("-"); detailDate.setFont(valueFont);

        int row = 0;

        // Title (full width)
        gbc.gridx = 0; gbc.gridy = row; gbc.gridwidth = 2; gbc.weightx = 1;
        panel.add(detailTitle, gbc);
        row++;

        gbc.gridwidth = 1;

        // Fields
        String[] labels = {"Book ID:", "Author:", "Genre:", "Total Copies:", "Available:", "Added Date:"};
        JLabel[] values = {detailId, detailAuthor, detailGenre, detailTotal, detailAvailable, detailDate};

        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0;
            JLabel lbl = new JLabel(labels[i]);
            lbl.setFont(labelFont);
            lbl.setForeground(new Color(100, 100, 100));
            panel.add(lbl, gbc);

            gbc.gridx = 1; gbc.weightx = 1;
            panel.add(values[i], gbc);
            row++;
        }

        // Fill remaining space
        gbc.gridx = 0; gbc.gridy = row; gbc.weighty = 1;
        panel.add(Box.createVerticalGlue(), gbc);

        return panel;
    }

    /**
     * Performs the search based on selected type and search text.
     * Called on every keystroke via the DocumentListener.
     */
    private void performSearch() {
        String query = searchField.getText().trim();
        String searchType = (String) searchTypeCombo.getSelectedItem();

        List<Book> results;
        if (query.isEmpty()) {
            results = libraryService.getAllBooks();
        } else {
            results = switch (searchType) {
                case "By ID" -> libraryService.searchByBookId(query)
                        .map(List::of).orElse(new ArrayList<>());
                case "By Author" -> libraryService.searchByAuthor(query);
                default -> libraryService.searchByTitle(query);
            };
        }

        searchModel.updateResults(results);
    }

    /**
     * Shows book details in the side panel when a row is clicked.
     */
    private void showBookDetails() {
        int selectedRow = resultsTable.getSelectedRow();
        if (selectedRow == -1) return;

        int modelRow = resultsTable.convertRowIndexToModel(selectedRow);
        String bookId = (String) searchModel.getValueAt(modelRow, 0);

        libraryService.searchByBookId(bookId).ifPresent(book -> {
            detailId.setText(book.getBookId());
            detailTitle.setText(book.getTitle());
            detailAuthor.setText(book.getAuthor());
            detailGenre.setText(book.getGenre());
            detailTotal.setText(String.valueOf(book.getTotalCopies()));

            int avail = book.getAvailableCopies();
            detailAvailable.setText(String.valueOf(avail));
            detailAvailable.setForeground(avail > 0 ? new Color(39, 174, 96) : new Color(231, 76, 60));

            detailDate.setText(DateUtils.formatDate(book.getAddedDate()));
        });
    }

    /**
     * Clears the detail panel.
     */
    private void clearDetailPanel() {
        detailId.setText("-");
        detailTitle.setText("-");
        detailAuthor.setText("-");
        detailGenre.setText("-");
        detailTotal.setText("-");
        detailAvailable.setText("-");
        detailDate.setText("-");
    }

    /**
     * Refreshes search to show all books (called when tab is selected).
     */
    public void refreshData() {
        performSearch();
    }

    // ==================== INNER CLASS: Search Table Model ====================

    private class SearchTableModel extends AbstractTableModel {
        private final String[] columns = {"Book ID", "Title", "Author", "Genre", "Available"};
        private List<Book> books = new ArrayList<>();

        public void updateResults(List<Book> results) {
            this.books = results;
            fireTableDataChanged();
        }

        @Override
        public int getRowCount() { return books.size(); }

        @Override
        public int getColumnCount() { return columns.length; }

        @Override
        public String getColumnName(int column) { return columns[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Book book = books.get(rowIndex);
            return switch (columnIndex) {
                case 0 -> book.getBookId();
                case 1 -> book.getTitle();
                case 2 -> book.getAuthor();
                case 3 -> book.getGenre();
                case 4 -> book.getAvailableCopies() + "/" + book.getTotalCopies();
                default -> "";
            };
        }
    }
}
