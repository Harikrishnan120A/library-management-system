package com.library.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Utility class for date-related operations.
 *
 * OOP Concept - UTILITY CLASS (Static Methods):
 * All methods are static — they can be called without creating an instance.
 * This is appropriate because DateUtils doesn't need to maintain any state.
 * Example: DateUtils.formatDate(someDate) — called directly on the class.
 */
public final class DateUtils {

    // Private constructor prevents instantiation
    private DateUtils() {
        throw new UnsupportedOperationException("DateUtils cannot be instantiated");
    }

    /**
     * Formats a LocalDate using the application's standard date format.
     */
    public static String formatDate(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(Constants.DATE_FORMATTER_DISPLAY);
    }

    /**
     * Formats a LocalDate using the short format (dd-MM-yyyy).
     */
    public static String formatDateShort(LocalDate date) {
        if (date == null) return "N/A";
        return date.format(Constants.DATE_FORMATTER);
    }

    /**
     * Calculates the number of days between two dates.
     */
    public static long daysBetween(LocalDate start, LocalDate end) {
        if (start == null || end == null) return 0;
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates the fine based on due date.
     * Fine = Rs. 5 per day after the due date.
     *
     * @param dueDate the due date of the borrow record
     * @return the fine amount (0.0 if not overdue)
     */
    public static double calculateFine(LocalDate dueDate) {
        return calculateFine(dueDate, LocalDate.now());
    }

    /**
     * Calculates the fine based on due date and return date.
     */
    public static double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        if (dueDate == null || returnDate == null) return 0.0;
        if (returnDate.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
            return overdueDays * Constants.FINE_PER_DAY;
        }
        return 0.0;
    }

    /**
     * Checks if a due date has passed (i.e., the book is overdue).
     */
    public static boolean isOverdue(LocalDate dueDate) {
        if (dueDate == null) return false;
        return LocalDate.now().isAfter(dueDate);
    }

    /**
     * Calculates the due date from the issue date (issue date + borrow period).
     */
    public static LocalDate calculateDueDate(LocalDate issueDate) {
        return issueDate.plusDays(Constants.BORROW_PERIOD_DAYS);
    }

    /**
     * Returns today's date.
     */
    public static LocalDate today() {
        return LocalDate.now();
    }
}
