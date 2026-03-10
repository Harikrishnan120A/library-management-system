package com.library.service;

import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Student;
import com.library.util.Constants;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class for persisting data using Java Object Serialization.
 *
 * OOP Concept - SINGLETON PATTERN:
 * Only one instance of FileStorageService exists throughout the application.
 * This ensures all parts of the app read/write to the same data files consistently.
 * The Singleton pattern uses:
 *   1. A private constructor (prevents external instantiation)
 *   2. A private static instance variable
 *   3. A public static getInstance() method (global access point)
 *
 * OOP Concept - ABSTRACTION:
 * This class hides the complexity of file I/O operations behind simple method names
 * like saveBooks() and loadBooks(). The caller doesn't need to know about
 * ObjectOutputStream, file paths, or backup logic.
 */
public class FileStorageService {

    private static final Logger LOGGER = Logger.getLogger(FileStorageService.class.getName());

    // Singleton instance — volatile ensures visibility across threads
    private static volatile FileStorageService instance;

    // Private constructor — Singleton pattern: prevents external instantiation
    private FileStorageService() {
        ensureDataDirectoryExists();
    }

    /**
     * Returns the single instance of FileStorageService.
     * Uses double-checked locking for thread safety.
     *
     * OOP Concept - SINGLETON ACCESS:
     * This is the only way to get an instance. The "synchronized" block
     * ensures thread safety during lazy initialization.
     */
    public static FileStorageService getInstance() {
        if (instance == null) {
            synchronized (FileStorageService.class) {
                if (instance == null) {
                    instance = new FileStorageService();
                }
            }
        }
        return instance;
    }

    /**
     * Creates the data directory if it doesn't exist.
     */
    private void ensureDataDirectoryExists() {
        File dataDir = new File(Constants.DATA_DIR);
        if (!dataDir.exists()) {
            boolean created = dataDir.mkdirs();
            if (created) {
                LOGGER.info("Data directory created: " + dataDir.getAbsolutePath());
            }
        }
    }

    // ==================== SAVE OPERATIONS ====================

    /**
     * Saves the list of books to a .dat file using Java Object Serialization.
     *
     * OOP Concept - ABSTRACTION:
     * The caller simply says saveBooks(list) — all file handling, backup creation,
     * and error recovery is hidden inside this method.
     */
    @SuppressWarnings("unchecked")
    public void saveBooks(List<Book> books) {
        createBackup(Constants.BOOKS_FILE);
        saveObject(books, Constants.BOOKS_FILE);
        LOGGER.info("Saved " + books.size() + " books to " + Constants.BOOKS_FILE);
    }

    /**
     * Saves the list of borrow records to a .dat file.
     */
    @SuppressWarnings("unchecked")
    public void saveBorrowRecords(List<BorrowRecord> records) {
        createBackup(Constants.RECORDS_FILE);
        saveObject(records, Constants.RECORDS_FILE);
        LOGGER.info("Saved " + records.size() + " borrow records to " + Constants.RECORDS_FILE);
    }

    /**
     * Saves the map of students to a .dat file.
     */
    @SuppressWarnings("unchecked")
    public void saveStudents(Map<String, Student> students) {
        createBackup(Constants.STUDENTS_FILE);
        saveObject(students, Constants.STUDENTS_FILE);
        LOGGER.info("Saved " + students.size() + " students to " + Constants.STUDENTS_FILE);
    }

    // ==================== LOAD OPERATIONS ====================

    /**
     * Loads books from the .dat file.
     * Returns an empty list if the file doesn't exist or is corrupted.
     */
    @SuppressWarnings("unchecked")
    public List<Book> loadBooks() {
        Object obj = loadObject(Constants.BOOKS_FILE);
        if (obj instanceof List<?>) {
            LOGGER.info("Loaded books from " + Constants.BOOKS_FILE);
            return (List<Book>) obj;
        }
        LOGGER.info("No existing book data found. Starting fresh.");
        return new ArrayList<>();
    }

    /**
     * Loads borrow records from the .dat file.
     */
    @SuppressWarnings("unchecked")
    public List<BorrowRecord> loadBorrowRecords() {
        Object obj = loadObject(Constants.RECORDS_FILE);
        if (obj instanceof List<?>) {
            LOGGER.info("Loaded borrow records from " + Constants.RECORDS_FILE);
            return (List<BorrowRecord>) obj;
        }
        LOGGER.info("No existing borrow records found. Starting fresh.");
        return new ArrayList<>();
    }

    /**
     * Loads students from the .dat file.
     */
    @SuppressWarnings("unchecked")
    public Map<String, Student> loadStudents() {
        Object obj = loadObject(Constants.STUDENTS_FILE);
        if (obj instanceof Map<?, ?>) {
            LOGGER.info("Loaded students from " + Constants.STUDENTS_FILE);
            return (Map<String, Student>) obj;
        }
        LOGGER.info("No existing student data found. Starting fresh.");
        return new HashMap<>();
    }

    // ==================== PRIVATE HELPER METHODS ====================

    /**
     * Generic method to save any Serializable object to a file.
     * Uses try-with-resources to ensure the stream is properly closed.
     */
    private void saveObject(Object obj, String filePath) {
        ensureDataDirectoryExists();
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(filePath)))) {
            oos.writeObject(obj);
            oos.flush();
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Error saving data to " + filePath, e);
        }
    }

    /**
     * Generic method to load a Serializable object from a file.
     * Returns null if the file doesn't exist or an error occurs.
     */
    private Object loadObject(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(filePath)))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "Error loading data from " + filePath + ". Attempting backup.", e);
            // Try loading from backup
            return loadFromBackup(filePath);
        }
    }

    /**
     * Creates a backup (.bak) of the file before overwriting.
     * This ensures data recovery is possible if save fails.
     */
    private void createBackup(String filePath) {
        File original = new File(filePath);
        if (original.exists()) {
            try {
                Path source = original.toPath();
                Path backup = Path.of(filePath + Constants.BACKUP_SUFFIX);
                Files.copy(source, backup, StandardCopyOption.REPLACE_EXISTING);
                LOGGER.fine("Backup created: " + backup);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Failed to create backup for " + filePath, e);
            }
        }
    }

    /**
     * Attempts to load data from a backup file if the main file is corrupted.
     */
    private Object loadFromBackup(String filePath) {
        String backupPath = filePath + Constants.BACKUP_SUFFIX;
        File backupFile = new File(backupPath);
        if (!backupFile.exists()) {
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(backupPath)))) {
            LOGGER.info("Successfully loaded data from backup: " + backupPath);
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.log(Level.SEVERE, "Failed to load from backup: " + backupPath, e);
            return null;
        }
    }

    /**
     * Checks if data files exist (used to determine if this is a first run).
     */
    public boolean isFirstRun() {
        return !new File(Constants.BOOKS_FILE).exists();
    }
}
