package com.library.service;

import com.library.exception.*;
import com.library.model.*;
import com.library.model.BorrowRecord.Status;
import com.library.util.Constants;
import com.library.util.DateUtils;
import com.library.util.IDGenerator;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.*;
import java.util.stream.Collectors;

/**
 * Core business logic service for the Library Management System.
 *
 * OOP Concept - SINGLETON PATTERN:
 * Only one LibraryService instance manages all library operations.
 * This ensures consistent data state across the entire application
 * (both GUI panels and console menu access the same data).
 *
 * OOP Concept - ABSTRACTION:
 * This class provides a high-level API (addBook, issueBook, etc.) that hides
 * the complexity of data management, validation, file persistence, and logging.
 * The UI layer doesn't need to know HOW data is stored or validated — it just
 * calls these methods and handles the results.
 *
 * OOP Concept - COMPOSITION (HAS-A relationship):
 * LibraryService HAS-A FileStorageService (uses it for persistence).
 * This is composition — LibraryService delegates storage responsibility
 * to FileStorageService rather than inheriting from it.
 */
public class LibraryService {

    private static final Logger LOGGER = Logger.getLogger(LibraryService.class.getName());

    // Singleton instance
    private static volatile LibraryService instance;

    // Data collections held in memory — loaded from files at startup
    private List<Book> books;
    private List<BorrowRecord> borrowRecords;
    private Map<String, Student> students; // studentId → Student

    // Composition: LibraryService HAS-A FileStorageService
    private final FileStorageService storageService;

    /**
     * Private constructor — Singleton pattern.
     * Initializes the service by loading data from files.
     */
    private LibraryService() {
        this.storageService = FileStorageService.getInstance();
        initializeLogger();
        loadAllData();

        // If this is a first run, load sample books
        if (books.isEmpty() && storageService.isFirstRun()) {
            loadSampleBooks();
            LOGGER.info("First run detected. Loaded 10 sample books.");
        }
    }

    /**
     * Returns the single instance of LibraryService.
     */
    public static LibraryService getInstance() {
        if (instance == null) {
            synchronized (LibraryService.class) {
                if (instance == null) {
                    instance = new LibraryService();
                }
            }
        }
        return instance;
    }

    // ==================== BOOK OPERATIONS ====================

    /**
     * Adds a new book to the library.
     *
     * @throws DuplicateBookException if a book with the same ID already exists
     * @throws IllegalArgumentException if book data is invalid
     */
    public void addBook(Book book) throws DuplicateBookException {
        // Input validation — checking at the service boundary
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Book title cannot be empty");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Book author cannot be empty");
        }

        // Check for duplicate ID
        if (book.getBookId() != null && findBookById(book.getBookId()).isPresent()) {
            throw new DuplicateBookException("A book with ID '" + book.getBookId() + "' already exists.");
        }

        // Generate ID if not provided
        if (book.getBookId() == null || book.getBookId().trim().isEmpty()) {
            book.setBookId(IDGenerator.generateBookId());
        }

        // Set added date if not set
        if (book.getAddedDate() == null) {
            book.setAddedDate(LocalDate.now());
        }

        books.add(book);
        saveAllData(); // Auto-save on every write operation
        LOGGER.info("Book added: " + book.getBookId() + " - " + book.getTitle());
    }

    /**
     * Removes a book from the library by its ID.
     *
     * @throws BookNotFoundException if no book with the given ID exists
     */
    public void removeBook(String bookId) throws BookNotFoundException {
        Book book = findBookById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));

        books.remove(book);
        saveAllData();
        LOGGER.info("Book removed: " + bookId + " - " + book.getTitle());
    }

    /**
     * Updates an existing book's details.
     *
     * @throws BookNotFoundException if the book doesn't exist
     */
    public void updateBook(Book updatedBook) throws BookNotFoundException {
        Book existing = findBookById(updatedBook.getBookId())
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + updatedBook.getBookId()));

        existing.setTitle(updatedBook.getTitle());
        existing.setAuthor(updatedBook.getAuthor());
        existing.setGenre(updatedBook.getGenre());
        existing.setTotalCopies(updatedBook.getTotalCopies());
        // Adjust available copies proportionally
        int diff = updatedBook.getTotalCopies() - existing.getTotalCopies();
        existing.setAvailableCopies(Math.max(0, existing.getAvailableCopies() + diff));

        saveAllData();
        LOGGER.info("Book updated: " + existing.getBookId() + " - " + existing.getTitle());
    }

    // ==================== ISSUE / RETURN OPERATIONS ====================

    /**
     * Issues a book to a student.
     * Creates a borrow record and decrements the book's available copies.
     *
     * @throws BookNotFoundException if the book doesn't exist
     * @throws BookNotAvailableException if no copies are available
     * @throws StudentLimitExceededException if the student has reached their borrow limit
     */
    public BorrowRecord issueBook(String bookId, String studentId)
            throws BookNotFoundException, BookNotAvailableException, StudentLimitExceededException {

        // Validate book exists
        Book book = findBookById(bookId)
                .orElseThrow(() -> new BookNotFoundException("Book not found with ID: " + bookId));

        // Check availability
        if (!book.isAvailable()) {
            throw new BookNotAvailableException(bookId, book.getTitle());
        }

        // Get or create student (auto-registration)
        Student student = students.computeIfAbsent(studentId, id -> {
            Student newStudent = new Student(id, "Student " + id, id + "@library.com");
            LOGGER.info("Auto-created student: " + id);
            return newStudent;
        });

        // Check student borrow limit
        if (!student.canBorrow()) {
            throw new StudentLimitExceededException(studentId, student.getMaxBorrowLimit());
        }

        // Create borrow record
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = DateUtils.calculateDueDate(issueDate);
        BorrowRecord record = new BorrowRecord(
                IDGenerator.generateRecordId(), bookId, studentId, issueDate, dueDate);

        // Update state
        book.decrementAvailable();
        student.addBorrowedBook(bookId);
        borrowRecords.add(record);

        saveAllData();
        LOGGER.info(String.format("Book issued: %s to student %s (Record: %s, Due: %s)",
                bookId, studentId, record.getRecordId(), DateUtils.formatDate(dueDate)));

        return record;
    }

    /**
     * Returns a book using the borrow record ID.
     * Updates the record status, increments available copies, and calculates fine.
     *
     * @return the fine amount (0.0 if returned on time)
     * @throws BookNotFoundException if the record is not found
     */
    public double returnBook(String recordId) throws BookNotFoundException {
        // Find the active borrow record
        BorrowRecord record = borrowRecords.stream()
                .filter(r -> r.getRecordId().equalsIgnoreCase(recordId) && r.getStatus() != Status.RETURNED)
                .findFirst()
                .orElseThrow(() -> new BookNotFoundException(
                        "Active borrow record not found with ID: " + recordId));

        // Update record
        record.setReturnDate(LocalDate.now());
        record.setStatus(Status.RETURNED);

        // Calculate fine
        double fine = record.calculateFine();

        // Update book availability
        findBookById(record.getBookId()).ifPresent(Book::incrementAvailable);

        // Update student
        Student student = students.get(record.getStudentId());
        if (student != null) {
            student.removeBorrowedBook(record.getBookId());
        }

        saveAllData();

        String fineMsg = fine > 0 ? String.format(" Fine: Rs. %.2f", fine) : " No fine.";
        LOGGER.info(String.format("Book returned: Record %s, Book %s.%s",
                recordId, record.getBookId(), fineMsg));

        return fine;
    }

    // ==================== SEARCH OPERATIONS ====================

    /**
     * Searches for books by title (case-insensitive partial match).
     *
     * OOP Concept - JAVA STREAMS (Functional Programming support):
     * Uses stream().filter() with a lambda expression for declarative searching.
     * This is more readable than a traditional for-loop approach.
     */
    public List<Book> searchByTitle(String title) {
        if (title == null || title.trim().isEmpty()) return new ArrayList<>();
        String lowerTitle = title.toLowerCase();
        return books.stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerTitle))
                .collect(Collectors.toList());
    }

    /**
     * Searches for a single book by its ID.
     *
     * OOP Concept - OPTIONAL (Null Safety):
     * Returns Optional<Book> instead of null. This forces the caller to
     * explicitly handle the "not found" case, preventing NullPointerException.
     */
    public Optional<Book> searchByBookId(String bookId) {
        return findBookById(bookId);
    }

    /**
     * Searches for books by author (case-insensitive partial match).
     */
    public List<Book> searchByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) return new ArrayList<>();
        String lowerAuthor = author.toLowerCase();
        return books.stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(lowerAuthor))
                .collect(Collectors.toList());
    }

    // ==================== LIST / QUERY OPERATIONS ====================

    /**
     * Returns all books sorted alphabetically by title.
     */
    public List<Book> getAllBooks() {
        return books.stream()
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Returns only books that have available copies.
     */
    public List<Book> getAvailableBooks() {
        return books.stream()
                .filter(Book::isAvailable)
                .sorted(Comparator.comparing(Book::getTitle, String.CASE_INSENSITIVE_ORDER))
                .collect(Collectors.toList());
    }

    /**
     * Returns all active borrow records that are overdue.
     */
    public List<BorrowRecord> getOverdueRecords() {
        // First, update status of overdue records
        borrowRecords.stream()
                .filter(r -> r.getStatus() == Status.ACTIVE && r.isOverdue())
                .forEach(r -> r.setStatus(Status.OVERDUE));

        return borrowRecords.stream()
                .filter(r -> r.getStatus() == Status.OVERDUE)
                .collect(Collectors.toList());
    }

    /**
     * Returns all active (non-returned) borrow records.
     */
    public List<BorrowRecord> getActiveRecords() {
        return borrowRecords.stream()
                .filter(r -> r.getStatus() != Status.RETURNED)
                .sorted(Comparator.comparing(BorrowRecord::getIssueDate).reversed())
                .collect(Collectors.toList());
    }

    /**
     * Returns the most recent N borrow records (for dashboard activity feed).
     */
    public List<BorrowRecord> getRecentActivity(int count) {
        return borrowRecords.stream()
                .sorted(Comparator.comparing(BorrowRecord::getIssueDate).reversed())
                .limit(count)
                .collect(Collectors.toList());
    }

    /**
     * Returns all borrow records.
     */
    public List<BorrowRecord> getAllBorrowRecords() {
        return new ArrayList<>(borrowRecords);
    }

    // ==================== REPORT GENERATION ====================

    /**
     * Generates a comprehensive library report with statistics.
     */
    public LibraryReport generateReport() {
        LibraryReport report = new LibraryReport();

        // Basic counts
        report.setTotalBooks(books.size());
        report.setAvailableBooks((int) books.stream().filter(Book::isAvailable).count());
        report.setIssuedBooks((int) borrowRecords.stream()
                .filter(r -> r.getStatus() != Status.RETURNED).count());
        report.setOverdueBooks(getOverdueRecords().size());
        report.setTotalStudents(students.size());
        report.setTotalBorrowRecords(borrowRecords.size());

        // Total fines
        double totalFines = borrowRecords.stream()
                .filter(r -> r.getStatus() == Status.RETURNED)
                .mapToDouble(BorrowRecord::calculateFine)
                .sum();
        report.setTotalFinesCollected(totalFines);

        // Most borrowed books — count borrow records per bookId
        Map<String, Long> borrowCounts = borrowRecords.stream()
                .collect(Collectors.groupingBy(BorrowRecord::getBookId, Collectors.counting()));

        List<String> mostBorrowed = borrowCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    String title = findBookById(e.getKey())
                            .map(Book::getTitle).orElse(e.getKey());
                    return title + " (" + e.getValue() + " times)";
                })
                .collect(Collectors.toList());
        report.setMostBorrowedBooks(mostBorrowed);

        // Genre distribution
        Map<String, Integer> genreMap = new LinkedHashMap<>();
        for (Book book : books) {
            String genre = book.getGenre() != null ? book.getGenre() : "Unknown";
            genreMap.merge(genre, 1, Integer::sum);
        }
        report.setGenreDistribution(genreMap);

        // Overdue details
        List<String> overdueDetails = getOverdueRecords().stream()
                .map(r -> {
                    String bookTitle = findBookById(r.getBookId())
                            .map(Book::getTitle).orElse(r.getBookId());
                    return String.format("%s - '%s' by %s (Due: %s, Fine: Rs. %.2f)",
                            r.getRecordId(), bookTitle, r.getStudentId(),
                            DateUtils.formatDate(r.getDueDate()), r.calculateFine());
                })
                .collect(Collectors.toList());
        report.setOverdueDetails(overdueDetails);

        LOGGER.info("Library report generated.");
        return report;
    }

    // ==================== STUDENT OPERATIONS ====================

    /**
     * Gets a student by ID. Returns Optional to handle "not found" safely.
     */
    public Optional<Student> getStudent(String studentId) {
        return Optional.ofNullable(students.get(studentId));
    }

    /**
     * Returns all registered students.
     */
    public Map<String, Student> getAllStudents() {
        return Collections.unmodifiableMap(students);
    }

    // ==================== CSV IMPORT/EXPORT ====================

    /**
     * Exports all books to CSV format.
     * Returns the CSV content as a String.
     */
    public String exportBooksToCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append("Book ID,Title,Author,Genre,Total Copies,Available Copies,Added Date\n");
        for (Book book : getAllBooks()) {
            sb.append(String.format("%s,%s,%s,%s,%d,%d,%s%n",
                    escapeCsv(book.getBookId()),
                    escapeCsv(book.getTitle()),
                    escapeCsv(book.getAuthor()),
                    escapeCsv(book.getGenre()),
                    book.getTotalCopies(),
                    book.getAvailableCopies(),
                    DateUtils.formatDateShort(book.getAddedDate())));
        }
        LOGGER.info("Books exported to CSV.");
        return sb.toString();
    }

    /**
     * Imports books from CSV content.
     * Expected format: BookID,Title,Author,Genre,TotalCopies
     */
    public int importBooksFromCSV(String csvContent) {
        int imported = 0;
        String[] lines = csvContent.split("\n");

        for (int i = 1; i < lines.length; i++) { // Skip header row
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] parts = parseCsvLine(line);
            if (parts.length >= 5) {
                try {
                    Book book = new Book();
                    book.setBookId(parts[0].trim());
                    book.setTitle(parts[1].trim());
                    book.setAuthor(parts[2].trim());
                    book.setGenre(parts[3].trim());
                    int copies = Integer.parseInt(parts[4].trim());
                    book.setTotalCopies(copies);
                    book.setAvailableCopies(copies);
                    book.setAddedDate(LocalDate.now());

                    addBook(book);
                    imported++;
                } catch (DuplicateBookException e) {
                    LOGGER.warning("Skipping duplicate during import: " + parts[0]);
                } catch (NumberFormatException e) {
                    LOGGER.warning("Skipping invalid line during import: " + line);
                }
            }
        }
        LOGGER.info("Imported " + imported + " books from CSV.");
        return imported;
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Finds a book by its ID. Used internally for lookups.
     */
    private Optional<Book> findBookById(String bookId) {
        if (bookId == null) return Optional.empty();
        return books.stream()
                .filter(b -> b.getBookId().equalsIgnoreCase(bookId))
                .findFirst();
    }

    /**
     * Loads all data from files and initializes ID counters.
     */
    private void loadAllData() {
        books = storageService.loadBooks();
        borrowRecords = storageService.loadBorrowRecords();
        students = storageService.loadStudents();

        // Initialize ID counters based on existing data to avoid duplicates
        int maxBookId = books.stream()
                .mapToInt(b -> IDGenerator.extractNumber(b.getBookId()))
                .max().orElse(0);
        IDGenerator.initializeBookCounter(maxBookId);

        int maxRecordId = borrowRecords.stream()
                .mapToInt(r -> IDGenerator.extractNumber(r.getRecordId()))
                .max().orElse(0);
        IDGenerator.initializeRecordCounter(maxRecordId);

        int maxStudentId = students.keySet().stream()
                .mapToInt(IDGenerator::extractNumber)
                .max().orElse(0);
        IDGenerator.initializeStudentCounter(maxStudentId);

        LOGGER.info(String.format("Data loaded: %d books, %d records, %d students",
                books.size(), borrowRecords.size(), students.size()));
    }

    /**
     * Saves all data to files (called after every write operation).
     */
    private void saveAllData() {
        storageService.saveBooks(books);
        storageService.saveBorrowRecords(borrowRecords);
        storageService.saveStudents(students);
    }

    /**
     * Pre-loads 10 sample books on first run.
     * Covers a variety of genres to make the demo more interesting.
     */
    private void loadSampleBooks() {
        String[][] sampleData = {
                {"To Kill a Mockingbird", "Harper Lee", "Fiction"},
                {"1984", "George Orwell", "Dystopian"},
                {"The Great Gatsby", "F. Scott Fitzgerald", "Classic"},
                {"Data Structures and Algorithms", "Robert Lafore", "Computer Science"},
                {"Introduction to Java Programming", "Y. Daniel Liang", "Computer Science"},
                {"The Alchemist", "Paulo Coelho", "Fiction"},
                {"A Brief History of Time", "Stephen Hawking", "Science"},
                {"The Art of War", "Sun Tzu", "Philosophy"},
                {"Clean Code", "Robert C. Martin", "Computer Science"},
                {"Pride and Prejudice", "Jane Austen", "Classic"}
        };

        for (String[] data : sampleData) {
            try {
                Book book = new Book(IDGenerator.generateBookId(), data[0], data[1], data[2], 5);
                books.add(book);
            } catch (Exception e) {
                LOGGER.warning("Error loading sample book: " + data[0]);
            }
        }
        saveAllData();
    }

    /**
     * Escapes a CSV field (wraps in quotes if it contains commas or quotes).
     */
    private String escapeCsv(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    /**
     * Parses a CSV line handling quoted fields.
     */
    private String[] parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        fields.add(current.toString());
        return fields.toArray(new String[0]);
    }

    /**
     * Initializes the Java logging system.
     * Logs are written to library_log.txt.
     */
    private void initializeLogger() {
        try {
            Logger rootLogger = Logger.getLogger("com.library");
            // Remove default console handlers to avoid duplicate output
            for (Handler handler : rootLogger.getHandlers()) {
                rootLogger.removeHandler(handler);
            }

            // File handler — logs to library_log.txt (append mode)
            FileHandler fileHandler = new FileHandler(Constants.LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter());
            fileHandler.setLevel(Level.ALL);
            rootLogger.addHandler(fileHandler);

            // Also keep console handler for important messages
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setLevel(Level.WARNING);
            rootLogger.addHandler(consoleHandler);

            rootLogger.setLevel(Level.ALL);
        } catch (IOException e) {
            System.err.println("Warning: Could not initialize log file. Logging to console only.");
        }
    }

    /**
     * Returns the total number of books in the library.
     */
    public int getTotalBookCount() {
        return books.size();
    }

    /**
     * Returns the number of currently available books.
     */
    public int getAvailableBookCount() {
        return (int) books.stream().filter(Book::isAvailable).count();
    }

    /**
     * Returns the number of active (unreturned) borrow records.
     */
    public int getActiveBorrowCount() {
        return (int) borrowRecords.stream()
                .filter(r -> r.getStatus() != Status.RETURNED).count();
    }

    /**
     * Returns the number of overdue records.
     */
    public int getOverdueCount() {
        return (int) borrowRecords.stream()
                .filter(r -> r.getStatus() == Status.ACTIVE && r.isOverdue()).count()
                + (int) borrowRecords.stream()
                .filter(r -> r.getStatus() == Status.OVERDUE).count();
    }

    /**
     * Gets the title of a book by its ID (for display purposes).
     */
    public String getBookTitle(String bookId) {
        return findBookById(bookId).map(Book::getTitle).orElse("Unknown");
    }
}
