package com.library.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Utility class for generating unique IDs for books, students, and borrow records.
 *
 * OOP Concept - STATIC STATE with THREAD SAFETY:
 * AtomicInteger is used for counters to ensure thread-safe ID generation.
 * Even though this is a single-threaded app, using AtomicInteger is a best practice
 * that demonstrates awareness of concurrent programming concepts.
 *
 * The counters are static — they belong to the class and are shared across all code.
 * This ensures IDs are unique throughout the application's lifetime.
 */
public final class IDGenerator {

    // AtomicInteger counters for each entity type
    private static final AtomicInteger bookCounter = new AtomicInteger(0);
    private static final AtomicInteger recordCounter = new AtomicInteger(0);
    private static final AtomicInteger studentCounter = new AtomicInteger(0);

    // Private constructor prevents instantiation
    private IDGenerator() {
        throw new UnsupportedOperationException("IDGenerator cannot be instantiated");
    }

    /**
     * Generates a unique book ID in the format "BK-0001".
     * Uses AtomicInteger.incrementAndGet() for thread-safe increment.
     */
    public static String generateBookId() {
        return String.format("%s-%04d", Constants.BOOK_ID_PREFIX, bookCounter.incrementAndGet());
    }

    /**
     * Generates a unique borrow record ID in the format "BR-0001".
     */
    public static String generateRecordId() {
        return String.format("%s-%04d", Constants.RECORD_ID_PREFIX, recordCounter.incrementAndGet());
    }

    /**
     * Generates a unique student ID in the format "ST-0001".
     */
    public static String generateStudentId() {
        return String.format("%s-%04d", Constants.STUDENT_ID_PREFIX, studentCounter.incrementAndGet());
    }

    /**
     * Initializes the book counter based on existing data.
     * Called during application startup to avoid duplicate IDs.
     *
     * @param maxExistingId the highest existing book counter number
     */
    public static void initializeBookCounter(int maxExistingId) {
        bookCounter.set(maxExistingId);
    }

    /**
     * Initializes the record counter based on existing data.
     */
    public static void initializeRecordCounter(int maxExistingId) {
        recordCounter.set(maxExistingId);
    }

    /**
     * Initializes the student counter based on existing data.
     */
    public static void initializeStudentCounter(int maxExistingId) {
        studentCounter.set(maxExistingId);
    }

    /**
     * Extracts the numeric part from an ID string (e.g., "BK-0042" → 42).
     */
    public static int extractNumber(String id) {
        if (id == null || !id.contains("-")) return 0;
        try {
            return Integer.parseInt(id.substring(id.indexOf('-') + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
