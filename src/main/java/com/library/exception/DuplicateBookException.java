package com.library.exception;

/**
 * Custom exception thrown when attempting to add a book with a duplicate ID.
 *
 * OOP Concept - INHERITANCE:
 * This class inherits from Exception, specializing it for our library domain.
 * The "extends" keyword creates a parent-child (IS-A) relationship.
 */
public class DuplicateBookException extends Exception {

    public DuplicateBookException(String message) {
        super(message);
    }

    public DuplicateBookException(String bookId, boolean isId) {
        super("A book with ID '" + bookId + "' already exists in the library.");
    }
}
