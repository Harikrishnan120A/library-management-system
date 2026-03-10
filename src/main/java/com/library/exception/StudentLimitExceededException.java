package com.library.exception;

/**
 * Custom exception thrown when a student has reached their maximum borrow limit.
 *
 * OOP Concept - INHERITANCE:
 * Extends Exception to provide a meaningful error type specific to our business rules.
 * This allows callers to catch and handle borrow-limit violations separately.
 */
public class StudentLimitExceededException extends Exception {

    public StudentLimitExceededException(String message) {
        super(message);
    }

    public StudentLimitExceededException(String studentId, int limit) {
        super("Student '" + studentId + "' has reached the maximum borrow limit of " + limit + " books.");
    }
}
