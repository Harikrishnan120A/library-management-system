package com.library;

import com.library.exception.*;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.LibraryReport;
import com.library.service.LibraryService;
import com.library.ui.MainFrame;
import com.library.util.Constants;
import com.library.util.DateUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Main entry point for the Library Management System.
 *
 * OOP Concept - SEPARATION OF CONCERNS:
 * This class acts as the entry point and controller. It decides whether to launch
 * the GUI or the console menu based on the environment. The actual business logic
 * is delegated to LibraryService (Single Responsibility Principle).
 *
 * OOP Concept - POLYMORPHISM (Multiple Interfaces):
 * The same LibraryService is used by both the GUI and console modes.
 * This demonstrates how a well-designed service layer can support multiple frontends
 * without any changes — the service doesn't know or care about the UI layer.
 */
public class Main {

    private static final LibraryService libraryService = LibraryService.getInstance();
    private static Scanner scanner;

    public static void main(String[] args) {
        // Check if console mode is explicitly requested
        boolean consoleMode = false;
        for (String arg : args) {
            if ("--console".equalsIgnoreCase(arg) || "-c".equalsIgnoreCase(arg)) {
                consoleMode = true;
                break;
            }
        }

        if (consoleMode || GraphicsEnvironment.isHeadless()) {
            // Console fallback mode
            System.out.println("Starting Library Management System in Console Mode...");
            runConsoleMenu();
        } else {
            // GUI mode
            launchGUI();
        }
    }

    // ==================== GUI MODE ====================

    /**
     * Launches the Swing GUI application.
     * Uses SwingUtilities.invokeLater to ensure GUI creation happens on the
     * Event Dispatch Thread (EDT) — a requirement for Swing thread safety.
     */
    private static void launchGUI() {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set Nimbus Look and Feel for a modern appearance
                UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            } catch (Exception e) {
                System.err.println("Nimbus L&F not available, using default.");
            }

            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }

    // ==================== CONSOLE MODE ====================

    /**
     * Runs the text-based console menu.
     * This is the fallback mode for environments without a GUI.
     *
     * OOP Concept - ABSTRACTION:
     * The console menu provides a simple text interface, but underneath it uses
     * the same LibraryService methods as the GUI. The user doesn't know the
     * implementation details — they just interact through the menu.
     */
    private static void runConsoleMenu() {
        scanner = new Scanner(System.in);

        printBanner();

        boolean running = true;
        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-8): ");

            String input = scanner.nextLine().trim();
            int choice;
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("\n[!] Invalid input. Please enter a number between 1 and 8.\n");
                continue;
            }

            System.out.println();

            switch (choice) {
                case 1 -> addBookConsole();
                case 2 -> viewAllBooksConsole();
                case 3 -> issueBookConsole();
                case 4 -> returnBookConsole();
                case 5 -> searchBookConsole();
                case 6 -> viewOverdueConsole();
                case 7 -> generateReportConsole();
                case 8 -> {
                    System.out.println("Thank you for using the Library Management System!");
                    System.out.println("Goodbye!");
                    running = false;
                }
                default -> System.out.println("[!] Invalid choice. Please enter a number between 1 and 8.\n");
            }
        }
        scanner.close();
    }

    /**
     * Prints the application banner.
     */
    private static void printBanner() {
        System.out.println();
        System.out.println("╔══════════════════════════════════════════════════╗");
        System.out.println("║                                                  ║");
        System.out.println("║        LIBRARY MANAGEMENT SYSTEM v1.0            ║");
        System.out.println("║                                                  ║");
        System.out.println("║        Java Course Project                       ║");
        System.out.println("║                                                  ║");
        System.out.println("╚══════════════════════════════════════════════════╝");
        System.out.println();
        System.out.printf("  Total Books: %d | Available: %d | Active Borrows: %d%n",
                libraryService.getTotalBookCount(),
                libraryService.getAvailableBookCount(),
                libraryService.getActiveBorrowCount());
        System.out.println();
    }

    /**
     * Prints the main menu options.
     */
    private static void printMenu() {
        System.out.println("┌──────────────────────────────────────┐");
        System.out.println("│           MAIN MENU                  │");
        System.out.println("├──────────────────────────────────────┤");
        System.out.println("│  1. Add Book                         │");
        System.out.println("│  2. View All Books                   │");
        System.out.println("│  3. Issue Book                       │");
        System.out.println("│  4. Return Book                      │");
        System.out.println("│  5. Search Book (by ID or Title)     │");
        System.out.println("│  6. View Overdue Books               │");
        System.out.println("│  7. Generate Report                  │");
        System.out.println("│  8. Exit                             │");
        System.out.println("└──────────────────────────────────────┘");
    }

    // ==================== CONSOLE MENU HANDLERS ====================

    /**
     * Console handler: Add a new book.
     */
    private static void addBookConsole() {
        System.out.println("═══ ADD NEW BOOK ═══");

        System.out.print("Enter book title: ");
        String title = scanner.nextLine().trim();
        if (title.isEmpty()) {
            System.out.println("[!] Title cannot be empty.\n");
            return;
        }

        System.out.print("Enter author name: ");
        String author = scanner.nextLine().trim();
        if (author.isEmpty()) {
            System.out.println("[!] Author cannot be empty.\n");
            return;
        }

        System.out.print("Enter genre: ");
        String genre = scanner.nextLine().trim();
        if (genre.isEmpty()) genre = "General";

        System.out.print("Enter number of copies: ");
        int copies;
        try {
            copies = Integer.parseInt(scanner.nextLine().trim());
            if (copies <= 0) {
                System.out.println("[!] Number of copies must be positive.\n");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Invalid number.\n");
            return;
        }

        try {
            Book book = new Book();
            book.setTitle(title);
            book.setAuthor(author);
            book.setGenre(genre);
            book.setTotalCopies(copies);
            book.setAvailableCopies(copies);

            libraryService.addBook(book);
            System.out.printf("[✓] Book added successfully! (ID: %s)%n%n", book.getBookId());
        } catch (DuplicateBookException e) {
            System.out.println("[!] " + e.getMessage() + "\n");
        }
    }

    /**
     * Console handler: View all books in a formatted table.
     */
    private static void viewAllBooksConsole() {
        List<Book> books = libraryService.getAllBooks();

        if (books.isEmpty()) {
            System.out.println("No books in the library.\n");
            return;
        }

        System.out.println("═══ ALL BOOKS ═══");
        printBookTableHeader();
        for (Book book : books) {
            printBookRow(book);
        }
        printTableFooter();
        System.out.printf("Total: %d books%n%n", books.size());
    }

    /**
     * Console handler: Issue a book to a student.
     */
    private static void issueBookConsole() {
        System.out.println("═══ ISSUE BOOK ═══");

        System.out.print("Enter Book ID: ");
        String bookId = scanner.nextLine().trim();
        if (bookId.isEmpty()) {
            System.out.println("[!] Book ID cannot be empty.\n");
            return;
        }

        System.out.print("Enter Student ID: ");
        String studentId = scanner.nextLine().trim();
        if (studentId.isEmpty()) {
            System.out.println("[!] Student ID cannot be empty.\n");
            return;
        }

        try {
            BorrowRecord record = libraryService.issueBook(bookId, studentId);
            System.out.println("[✓] Book issued successfully!");
            System.out.printf("    Record ID  : %s%n", record.getRecordId());
            System.out.printf("    Book       : %s%n", libraryService.getBookTitle(bookId));
            System.out.printf("    Student    : %s%n", studentId);
            System.out.printf("    Issue Date : %s%n", DateUtils.formatDate(record.getIssueDate()));
            System.out.printf("    Due Date   : %s%n%n", DateUtils.formatDate(record.getDueDate()));
        } catch (BookNotFoundException e) {
            System.out.println("[!] " + e.getMessage() + "\n");
        } catch (BookNotAvailableException e) {
            System.out.println("[!] " + e.getMessage() + "\n");
        } catch (StudentLimitExceededException e) {
            System.out.println("[!] " + e.getMessage() + "\n");
        }
    }

    /**
     * Console handler: Return a book.
     */
    private static void returnBookConsole() {
        System.out.println("═══ RETURN BOOK ═══");

        System.out.print("Enter Record ID: ");
        String recordId = scanner.nextLine().trim();
        if (recordId.isEmpty()) {
            System.out.println("[!] Record ID cannot be empty.\n");
            return;
        }

        try {
            double fine = libraryService.returnBook(recordId);
            System.out.println("[✓] Book returned successfully!");
            if (fine > 0) {
                System.out.printf("    Overdue Fine: Rs. %.2f%n", fine);
                System.out.println("    (Rs. 5 per day after due date)");
            } else {
                System.out.println("    No fine — returned on time.");
            }
            System.out.println();
        } catch (BookNotFoundException e) {
            System.out.println("[!] " + e.getMessage() + "\n");
        }
    }

    /**
     * Console handler: Search for books by ID or title.
     */
    private static void searchBookConsole() {
        System.out.println("═══ SEARCH BOOK ═══");
        System.out.println("Search by: 1) Book ID  2) Title  3) Author");
        System.out.print("Enter choice (1-3): ");

        String choice = scanner.nextLine().trim();

        System.out.print("Enter search term: ");
        String term = scanner.nextLine().trim();
        if (term.isEmpty()) {
            System.out.println("[!] Search term cannot be empty.\n");
            return;
        }

        List<Book> results;
        switch (choice) {
            case "1" -> {
                Optional<Book> found = libraryService.searchByBookId(term);
                if (found.isPresent()) {
                    results = List.of(found.get());
                } else {
                    results = List.of();
                }
            }
            case "3" -> results = libraryService.searchByAuthor(term);
            default -> results = libraryService.searchByTitle(term);
        }

        if (results.isEmpty()) {
            System.out.println("No books found matching '" + term + "'.\n");
            return;
        }

        System.out.printf("Found %d result(s):%n", results.size());
        printBookTableHeader();
        for (Book book : results) {
            printBookRow(book);
        }
        printTableFooter();
        System.out.println();
    }

    /**
     * Console handler: View overdue books.
     */
    private static void viewOverdueConsole() {
        System.out.println("═══ OVERDUE BOOKS ═══");
        List<BorrowRecord> overdueRecords = libraryService.getOverdueRecords();

        if (overdueRecords.isEmpty()) {
            System.out.println("No overdue records found. All books are returned on time!\n");
            return;
        }

        System.out.printf("%-12s %-25s %-12s %-14s %-14s %-10s%n",
                "Record ID", "Book Title", "Student", "Issue Date", "Due Date", "Fine");
        System.out.println("─".repeat(87));

        for (BorrowRecord record : overdueRecords) {
            String bookTitle = libraryService.getBookTitle(record.getBookId());
            if (bookTitle.length() > 23) bookTitle = bookTitle.substring(0, 20) + "...";
            System.out.printf("%-12s %-25s %-12s %-14s %-14s Rs. %.2f%n",
                    record.getRecordId(),
                    bookTitle,
                    record.getStudentId(),
                    DateUtils.formatDate(record.getIssueDate()),
                    DateUtils.formatDate(record.getDueDate()),
                    record.calculateFine());
        }
        System.out.println("─".repeat(87));
        System.out.printf("Total overdue records: %d%n%n", overdueRecords.size());
    }

    /**
     * Console handler: Generate and display report.
     */
    private static void generateReportConsole() {
        LibraryReport report = libraryService.generateReport();
        System.out.println(report.toString());
    }

    // ==================== TABLE FORMATTING HELPERS ====================

    /**
     * Prints the header for the book table.
     * Uses printf for aligned column output.
     */
    private static void printBookTableHeader() {
        System.out.println("─".repeat(105));
        System.out.printf("%-10s %-30s %-22s %-16s %-7s %-7s %-12s%n",
                "Book ID", "Title", "Author", "Genre", "Total", "Avail", "Added Date");
        System.out.println("─".repeat(105));
    }

    /**
     * Prints a single book row.
     */
    private static void printBookRow(Book book) {
        String title = book.getTitle();
        if (title.length() > 28) title = title.substring(0, 25) + "...";
        String author = book.getAuthor();
        if (author.length() > 20) author = author.substring(0, 17) + "...";
        String genre = book.getGenre() != null ? book.getGenre() : "N/A";
        if (genre.length() > 14) genre = genre.substring(0, 11) + "...";

        System.out.printf("%-10s %-30s %-22s %-16s %-7d %-7d %-12s%n",
                book.getBookId(),
                title,
                author,
                genre,
                book.getTotalCopies(),
                book.getAvailableCopies(),
                DateUtils.formatDate(book.getAddedDate()));
    }

    /**
     * Prints the table footer line.
     */
    private static void printTableFooter() {
        System.out.println("─".repeat(105));
    }
}
