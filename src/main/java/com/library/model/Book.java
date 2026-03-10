package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Book model class representing a book in the library.
 *
 * OOP Concept - ENCAPSULATION:
 * All fields are declared private, and access is provided through public getter/setter methods.
 * This protects the internal state of the object and allows controlled modification.
 * For example, availableCopies cannot be set to a negative value directly.
 *
 * OOP Concept - SERIALIZABLE INTERFACE (Abstraction):
 * By implementing Serializable, this class can be converted to a byte stream for file storage.
 * This is an example of abstraction — the serialization mechanism is hidden from the user.
 */
public class Book implements Serializable {

    // serialVersionUID ensures version compatibility during deserialization
    private static final long serialVersionUID = 1L;

    // Private fields — encapsulation: data hiding from external access
    private String bookId;
    private String title;
    private String author;
    private String genre;
    private int totalCopies;
    private int availableCopies;
    private LocalDate addedDate;

    // Default constructor
    public Book() {
        this.addedDate = LocalDate.now();
    }

    // Parameterized constructor — provides a convenient way to create fully initialized objects
    public Book(String bookId, String title, String author, String genre, int totalCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies; // Initially all copies are available
        this.addedDate = LocalDate.now();
    }

    // Full parameterized constructor (used when loading from storage)
    public Book(String bookId, String title, String author, String genre,
                int totalCopies, int availableCopies, LocalDate addedDate) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
        this.addedDate = addedDate;
    }

    // --- Getters and Setters (Encapsulation) ---
    // Public methods provide controlled access to private fields

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public void setTotalCopies(int totalCopies) {
        this.totalCopies = totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }

    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    public LocalDate getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDate addedDate) {
        this.addedDate = addedDate;
    }

    // --- Helper methods ---

    /**
     * Checks if the book has copies available for borrowing.
     * This is an example of encapsulation — the logic is inside the object itself.
     */
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    /**
     * Decrements available copies when a book is issued.
     */
    public void decrementAvailable() {
        if (availableCopies > 0) {
            availableCopies--;
        }
    }

    /**
     * Increments available copies when a book is returned.
     */
    public void incrementAvailable() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    // --- Object class method overrides ---

    /**
     * OOP Concept - POLYMORPHISM:
     * toString() is overridden from the Object class. This is runtime polymorphism —
     * when System.out.println(book) is called, Java invokes this version instead of Object's.
     */
    @Override
    public String toString() {
        return String.format("Book[id=%s, title='%s', author='%s', genre='%s', copies=%d/%d]",
                bookId, title, author, genre, availableCopies, totalCopies);
    }

    /**
     * equals() compares books by their bookId.
     * Two books are considered equal if they have the same bookId.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(bookId, book.bookId);
    }

    /**
     * hashCode() must be consistent with equals().
     * If two objects are equal, they must have the same hash code.
     */
    @Override
    public int hashCode() {
        return Objects.hash(bookId);
    }
}
