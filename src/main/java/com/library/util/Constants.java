package com.library.util;

import java.time.format.DateTimeFormatter;

/**
 * Constants class containing application-wide constant values.
 *
 * OOP Concept - ENCAPSULATION & DESIGN PATTERN:
 * All constants are declared as public static final, meaning they:
 * - public: accessible from anywhere
 * - static: belong to the class, not instances (no need to create an object)
 * - final: cannot be changed after initialization (immutable)
 *
 * The private constructor prevents instantiation — this is a utility class pattern.
 */
public final class Constants {

    // Private constructor prevents instantiation of this utility class
    private Constants() {
        throw new UnsupportedOperationException("Constants class cannot be instantiated");
    }

    // --- Application Settings ---
    public static final String APP_TITLE = "Library Management System";
    public static final String APP_VERSION = "1.0";
    public static final int WINDOW_WIDTH = 1200;
    public static final int WINDOW_HEIGHT = 800;

    // --- Borrowing Rules ---
    public static final int BORROW_PERIOD_DAYS = 14;    // Default borrow period: 14 days
    public static final double FINE_PER_DAY = 5.0;      // Rs. 5 fine per overdue day
    public static final int MAX_BORROW_LIMIT = 3;       // Maximum books a student can borrow

    // --- File Paths for Data Persistence ---
    public static final String DATA_DIR = "data";
    public static final String BOOKS_FILE = DATA_DIR + "/books.dat";
    public static final String RECORDS_FILE = DATA_DIR + "/records.dat";
    public static final String STUDENTS_FILE = DATA_DIR + "/students.dat";
    public static final String SMS_SETTINGS_FILE = DATA_DIR + "/sms.properties";
    public static final String LOG_FILE = "library_log.txt";

    // --- Backup File Suffix ---
    public static final String BACKUP_SUFFIX = ".bak";

    // --- Date Format ---
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    public static final DateTimeFormatter DATE_FORMATTER_DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy");

    // --- ID Prefixes ---
    public static final String BOOK_ID_PREFIX = "BK";
    public static final String RECORD_ID_PREFIX = "BR";
    public static final String STUDENT_ID_PREFIX = "ST";

    // --- Table Column Names ---
    public static final String[] BOOK_TABLE_COLUMNS = {
            "Book ID", "Title", "Author", "Genre", "Total Copies", "Available", "Added Date"
    };

    public static final String[] BORROW_TABLE_COLUMNS = {
            "Record ID", "Book ID", "Student ID", "Issue Date", "Due Date", "Status"
    };

    public static final String[] SEARCH_RESULT_COLUMNS = {
            "Book ID", "Title", "Author", "Genre", "Available Copies"
    };

    // --- SMS Reminder Configuration ---
    public static final String SMS_PROVIDER_ENV = "SMS_PROVIDER";
    public static final String SMS_PROVIDER_GENERIC = "GENERIC";
    public static final String SMS_PROVIDER_TWILIO = "TWILIO";
    public static final String SMS_API_URL_ENV = "SMS_API_URL";
    public static final String SMS_API_TOKEN_ENV = "SMS_API_TOKEN";
    public static final String SMS_SENDER_ID_ENV = "SMS_SENDER_ID";
    public static final String TWILIO_ACCOUNT_SID_ENV = "TWILIO_ACCOUNT_SID";
    public static final String TWILIO_AUTH_TOKEN_ENV = "TWILIO_AUTH_TOKEN";
    public static final String TWILIO_FROM_NUMBER_ENV = "TWILIO_FROM_NUMBER";
}
