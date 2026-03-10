package com.library.exception;

/**
 * Custom exception thrown when a book is not found in the library.
 *
 * OOP Concept - INHERITANCE:
 * This class extends Exception, inheriting all its behavior (message, stack trace, etc.)
 * while adding a specific meaning for our library domain. This is "IS-A" relationship:
 * BookNotFoundException IS-A Exception.
 *
 * OOP Concept - ABSTRACTION:
 * Custom exceptions abstract away error details — the caller only needs to know that
 * a book was not found, not the technical details of how the search was performed.
 */
public class BookNotFoundException extends Exception {

    public BookNotFoundException(String message) {
        super(message);
    }

    public BookNotFoundException(String bookId, boolean isId) {
        super("Book not found with " + (isId ? "ID: " : "title: ") + bookId);
    }
}
