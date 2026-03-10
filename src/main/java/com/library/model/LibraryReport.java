package com.library.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * LibraryReport model class — a data holder for library statistics and reports.
 *
 * OOP Concept - DATA TRANSFER OBJECT (DTO):
 * This class acts as a container to transfer report data from the service layer to the UI.
 * It bundles multiple related data items into a single object, making it easy to pass
 * around without needing multiple method parameters.
 */
public class LibraryReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private int totalBooks;
    private int availableBooks;
    private int issuedBooks;
    private int overdueBooks;
    private int totalStudents;
    private int totalBorrowRecords;
    private double totalFinesCollected;
    private List<String> mostBorrowedBooks;     // List of "Title (count)" strings
    private Map<String, Integer> genreDistribution; // Genre → count
    private List<String> overdueDetails;          // Formatted overdue record details

    public LibraryReport() {
    }

    // --- Getters and Setters ---

    public int getTotalBooks() {
        return totalBooks;
    }

    public void setTotalBooks(int totalBooks) {
        this.totalBooks = totalBooks;
    }

    public int getAvailableBooks() {
        return availableBooks;
    }

    public void setAvailableBooks(int availableBooks) {
        this.availableBooks = availableBooks;
    }

    public int getIssuedBooks() {
        return issuedBooks;
    }

    public void setIssuedBooks(int issuedBooks) {
        this.issuedBooks = issuedBooks;
    }

    public int getOverdueBooks() {
        return overdueBooks;
    }

    public void setOverdueBooks(int overdueBooks) {
        this.overdueBooks = overdueBooks;
    }

    public int getTotalStudents() {
        return totalStudents;
    }

    public void setTotalStudents(int totalStudents) {
        this.totalStudents = totalStudents;
    }

    public int getTotalBorrowRecords() {
        return totalBorrowRecords;
    }

    public void setTotalBorrowRecords(int totalBorrowRecords) {
        this.totalBorrowRecords = totalBorrowRecords;
    }

    public double getTotalFinesCollected() {
        return totalFinesCollected;
    }

    public void setTotalFinesCollected(double totalFinesCollected) {
        this.totalFinesCollected = totalFinesCollected;
    }

    public List<String> getMostBorrowedBooks() {
        return mostBorrowedBooks;
    }

    public void setMostBorrowedBooks(List<String> mostBorrowedBooks) {
        this.mostBorrowedBooks = mostBorrowedBooks;
    }

    public Map<String, Integer> getGenreDistribution() {
        return genreDistribution;
    }

    public void setGenreDistribution(Map<String, Integer> genreDistribution) {
        this.genreDistribution = genreDistribution;
    }

    public List<String> getOverdueDetails() {
        return overdueDetails;
    }

    public void setOverdueDetails(List<String> overdueDetails) {
        this.overdueDetails = overdueDetails;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("╔══════════════════════════════════════════════╗\n");
        sb.append("║         LIBRARY MANAGEMENT REPORT           ║\n");
        sb.append("╠══════════════════════════════════════════════╣\n");
        sb.append(String.format("║  Total Books:          %-20d ║%n", totalBooks));
        sb.append(String.format("║  Available Books:      %-20d ║%n", availableBooks));
        sb.append(String.format("║  Issued Books:         %-20d ║%n", issuedBooks));
        sb.append(String.format("║  Overdue Books:        %-20d ║%n", overdueBooks));
        sb.append(String.format("║  Total Students:       %-20d ║%n", totalStudents));
        sb.append(String.format("║  Total Records:        %-20d ║%n", totalBorrowRecords));
        sb.append(String.format("║  Fines Collected:  Rs. %-20.2f ║%n", totalFinesCollected));
        sb.append("╠══════════════════════════════════════════════╣\n");

        if (mostBorrowedBooks != null && !mostBorrowedBooks.isEmpty()) {
            sb.append("║  Most Borrowed Books:                        ║\n");
            for (String book : mostBorrowedBooks) {
                sb.append(String.format("║    - %-40s ║%n", book));
            }
        }

        if (genreDistribution != null && !genreDistribution.isEmpty()) {
            sb.append("╠══════════════════════════════════════════════╣\n");
            sb.append("║  Genre Distribution:                         ║\n");
            for (Map.Entry<String, Integer> entry : genreDistribution.entrySet()) {
                sb.append(String.format("║    %-25s : %-13d ║%n", entry.getKey(), entry.getValue()));
            }
        }

        sb.append("╚══════════════════════════════════════════════╝\n");
        return sb.toString();
    }
}
