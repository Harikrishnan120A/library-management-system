package com.library.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * BorrowRecord model class representing a book borrowing transaction.
 *
 * OOP Concept - ENCAPSULATION with ENUM:
 * The Status enum is a nested type that restricts the status field to only valid values
 * (ACTIVE, RETURNED, OVERDUE). This is stronger than using plain Strings because
 * the compiler enforces type safety — you cannot set an invalid status.
 *
 * OOP Concept - COHESION:
 * This class groups all borrow-related data together (bookId, studentId, dates, status).
 * It also contains behavior related to its data (isOverdue(), calculateFine()).
 * High cohesion means the class has a single, well-defined responsibility.
 */
public class BorrowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Enum representing the status of a borrow record.
     * OOP Concept - ENUM TYPE SAFETY:
     * Enums restrict values to a predefined set, preventing invalid states.
     */
    public enum Status {
        ACTIVE,     // Book is currently borrowed
        RETURNED,   // Book has been returned
        OVERDUE     // Book is past due date and not yet returned
    }

    private String recordId;
    private String bookId;
    private String studentId;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;  // null if not yet returned
    private Status status;

    // Default constructor
    public BorrowRecord() {
    }

    // Parameterized constructor for creating a new borrow record
    public BorrowRecord(String recordId, String bookId, String studentId,
                        LocalDate issueDate, LocalDate dueDate) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.studentId = studentId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = null;
        this.status = Status.ACTIVE;
    }

    // Full constructor (used when loading from storage)
    public BorrowRecord(String recordId, String bookId, String studentId,
                        LocalDate issueDate, LocalDate dueDate,
                        LocalDate returnDate, Status status) {
        this.recordId = recordId;
        this.bookId = bookId;
        this.studentId = studentId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
    }

    // --- Getters and Setters ---

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
        this.bookId = bookId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    // --- Business logic methods ---

    /**
     * Checks if the borrow record is overdue based on the current date.
     * Encapsulation: the overdue logic lives inside the record object itself.
     */
    public boolean isOverdue() {
        return status == Status.ACTIVE && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Calculates the fine for an overdue book.
     * Fine = Rs. 5 per day after the due date.
     * Returns 0.0 if the book is not overdue.
     */
    public double calculateFine() {
        LocalDate endDate = (returnDate != null) ? returnDate : LocalDate.now();
        if (endDate.isAfter(dueDate)) {
            long overdueDays = ChronoUnit.DAYS.between(dueDate, endDate);
            return overdueDays * 5.0; // Rs. 5 per day
        }
        return 0.0;
    }

    /**
     * Returns the number of days the book has been borrowed.
     */
    public long getDaysBorrowed() {
        LocalDate endDate = (returnDate != null) ? returnDate : LocalDate.now();
        return ChronoUnit.DAYS.between(issueDate, endDate);
    }

    @Override
    public String toString() {
        return String.format("BorrowRecord[id=%s, book=%s, student=%s, status=%s, issued=%s, due=%s]",
                recordId, bookId, studentId, status, issueDate, dueDate);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BorrowRecord that = (BorrowRecord) o;
        return Objects.equals(recordId, that.recordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(recordId);
    }
}
