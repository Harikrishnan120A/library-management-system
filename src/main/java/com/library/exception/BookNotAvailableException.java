package com.library.exception;

/**
 * Custom exception thrown when a book has no available copies for borrowing.
 *
 * OOP Concept - INHERITANCE:
 * Extends Exception to create a domain-specific error type.
 * This makes error handling more precise — catch blocks can handle this specific
 * case differently from other exceptions.
 */
public class BookNotAvailableException extends Exception {

    public BookNotAvailableException(String message) {
        super(message);
    }

    public BookNotAvailableException(String bookId, String title) {
        super("Book '" + title + "' (ID: " + bookId + ") has no available copies.");
    }
}
