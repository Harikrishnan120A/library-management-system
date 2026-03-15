package com.library.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Student model class representing a library member who can borrow books.
 *
 * OOP Concept - ENCAPSULATION:
 * The borrowedBooks list is private and can only be modified through controlled methods
 * like addBorrowedBook() and removeBorrowedBook(). This prevents external code from
 * directly manipulating the list in invalid ways.
 *
 * OOP Concept - DATA HIDING:
 * maxBorrowLimit is set with a default value and the canBorrow() method encapsulates
 * the business rule that a student cannot borrow more books than their limit.
 */
public class Student implements Serializable {

    private static final long serialVersionUID = 1L;

    private String studentId;
    private String name;
    private String email;
    private String phoneNumber;
    private List<String> borrowedBooks; // List of bookIds currently borrowed
    private int maxBorrowLimit;

    // Default constructor
    public Student() {
        this.borrowedBooks = new ArrayList<>();
        this.maxBorrowLimit = 3; // Default borrow limit
    }

    // Parameterized constructor
    public Student(String studentId, String name, String email) {
        this.studentId = studentId;
        this.name = name;
        this.email = email;
        this.phoneNumber = "";
        this.borrowedBooks = new ArrayList<>();
        this.maxBorrowLimit = 3;
    }

    // --- Getters and Setters ---

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public List<String> getBorrowedBooks() {
        // Return a copy to prevent external modification — defensive copying (encapsulation)
        return new ArrayList<>(borrowedBooks);
    }

    public void setBorrowedBooks(List<String> borrowedBooks) {
        this.borrowedBooks = new ArrayList<>(borrowedBooks);
    }

    public int getMaxBorrowLimit() {
        return maxBorrowLimit;
    }

    public void setMaxBorrowLimit(int maxBorrowLimit) {
        this.maxBorrowLimit = maxBorrowLimit;
    }

    // --- Business logic methods (Encapsulation — behavior is inside the object) ---

    /**
     * Checks if the student can borrow more books.
     * Encapsulates the borrow-limit business rule within the Student object itself.
     */
    public boolean canBorrow() {
        return borrowedBooks.size() < maxBorrowLimit;
    }

    /**
     * Adds a book ID to the student's borrowed list.
     */
    public void addBorrowedBook(String bookId) {
        if (!borrowedBooks.contains(bookId)) {
            borrowedBooks.add(bookId);
        }
    }

    /**
     * Removes a book ID from the student's borrowed list when returned.
     */
    public void removeBorrowedBook(String bookId) {
        borrowedBooks.remove(bookId);
    }

    /**
     * Returns the number of books currently borrowed.
     */
    public int getBorrowedCount() {
        return borrowedBooks.size();
    }

    @Override
    public String toString() {
        return String.format("Student[id=%s, name='%s', email='%s', phone='%s', borrowed=%d/%d]",
            studentId, name, email, phoneNumber, borrowedBooks.size(), maxBorrowLimit);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return java.util.Objects.equals(studentId, student.studentId);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(studentId);
    }
}
