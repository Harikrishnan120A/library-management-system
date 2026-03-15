# 📚 Library Management System

A complete **Java desktop application** for managing library operations — built with **Swing GUI** and a **console fallback** mode. This project demonstrates core Object-Oriented Programming concepts and is structured as a Maven project targeting **Java 17**.

---

## ✨ Features

- **Add, Edit, Delete Books** with full validation
- **Issue & Return Books** to/from students with automatic availability tracking
- **Search Books** by ID, Title, or Author (case-insensitive, real-time filtering)
- **Fine Calculation** — ₹5 per day after a 14-day borrow period
- **Overdue Detection** — highlights overdue records in red
- **SMS Due-Date Alerts** — send reminder SMS for books due soon or already overdue
- **Dashboard** with statistics cards (Total Books, Available, Issued, Overdue)
- **Reports** with summary statistics, most borrowed books, and CSV export
- **CSV Import/Export** for books data
- **Data Persistence** using Java Object Serialization (`.dat` files with auto-backup)
- **Logging** — all operations logged to `library_log.txt`
- **10 Sample Books** pre-loaded on first run
- **Console Mode** — full text-based menu for headless environments

---

## 🛠️ Tech Stack

| Component        | Technology                          |
|------------------|-------------------------------------|
| Language         | Java 17                             |
| Build Tool       | Apache Maven                        |
| GUI Framework    | Java Swing (Nimbus Look & Feel)     |
| Persistence      | Java Object Serialization (`.dat`)  |
| Logging          | `java.util.logging`                 |
| External Deps    | None — pure JDK                     |

---

## 📁 Project Structure

```
src/main/java/com/library/
├── Main.java                          # Entry point (GUI + Console fallback)
├── model/
│   ├── Book.java                      # Book entity (Serializable)
│   ├── Student.java                   # Student entity (Serializable)
│   ├── BorrowRecord.java             # Borrow record with Status enum
│   └── LibraryReport.java            # Report data holder
├── exception/
│   ├── BookNotFoundException.java
│   ├── BookNotAvailableException.java
│   ├── DuplicateBookException.java
│   └── StudentLimitExceededException.java
├── service/
│   ├── LibraryService.java           # Core business logic (Singleton)
│   └── FileStorageService.java       # Serialization-based persistence (Singleton)
├── ui/
│   ├── MainFrame.java                # Main window with tabs, menu bar, status bar
│   ├── DashboardPanel.java           # Dashboard tab — stats & recent activity
│   ├── BookPanel.java                # Books tab — CRUD with JTable
│   ├── IssuePanel.java               # Issue/Return tab
│   ├── SearchPanel.java              # Search tab — real-time filtering
│   └── ReportPanel.java              # Reports tab — stats & CSV export
└── util/
    ├── Constants.java                 # Application-wide constants
    ├── DateUtils.java                 # Date helper methods
    └── IDGenerator.java              # Auto-incrementing ID generation
```

---

## 🚀 Getting Started

### Prerequisites

- **Java 17** or higher — [Download OpenJDK](https://learn.microsoft.com/en-us/java/openjdk/download)
- **Apache Maven 3.6+** — [Download Maven](https://maven.apache.org/download.cgi)

### Build & Run

```bash
# Clone the repository
git clone git@github.com:Harikrishnan120A/library-management-system.git
cd library-management-system

# Compile the project
mvn compile

# Run with GUI (default)
mvn exec:java -Dexec.mainClass="com.library.Main"

# Run in Console mode
mvn exec:java -Dexec.mainClass="com.library.Main" -Dexec.args="--console"
```

### Build JAR

```bash
mvn package
java -jar target/library-management-system-1.0-SNAPSHOT.jar
```

---

## 🖥️ GUI Overview

The Swing GUI features a **5-tab interface** with a professional layout:

| Tab           | Description                                              |
|---------------|----------------------------------------------------------|
| **Dashboard** | Statistics cards, recent activity table, quick actions    |
| **Books**     | Full book table with sorting, Add/Edit/Delete dialogs    |
| **Issue/Return** | Issue books to students, return with fine calculation |
| **Search**    | Real-time search by ID, Title, or Author with detail panel |
| **Reports**   | Library statistics, most borrowed books, CSV export      |

**Menu Bar:** File (Import/Export CSV, Exit) · Reports (Generate, View) · Help (About)
**Status Bar:** Live counts of total books, available books, and active borrows

---

## 💻 Console Menu

When running with `--console` or in a headless environment:

```
╔══════════════════════════════════════════╗
║     LIBRARY MANAGEMENT SYSTEM            ║
╠══════════════════════════════════════════╣
║  1. Add Book                             ║
║  2. View All Books                       ║
║  3. Issue Book                           ║
║  4. Return Book                          ║
║  5. Search Book (by ID or Title)         ║
║  6. View Overdue Books                   ║
║  7. Generate Report                      ║
║  8. Exit                                 ║
╚══════════════════════════════════════════╝
```

---

## ⚙️ Configuration

Key constants defined in `Constants.java`:

| Constant            | Value  | Description                        |
|---------------------|--------|------------------------------------|
| `BORROW_PERIOD_DAYS`| 14     | Default loan period in days        |
| `FINE_PER_DAY`      | ₹5.00  | Fine charged per overdue day       |
| `MAX_BORROW_LIMIT`  | 3      | Maximum books a student can borrow |

Data files are stored in the `data/` directory:
- `books.dat` — Serialized book records
- `records.dat` — Serialized borrow records
- `students.dat` — Serialized student records
- `*.dat.bak` — Backup files created before each save

SMS gateway environment variables (optional):
- `SMS_PROVIDER` — set to `TWILIO` to use Twilio, or leave unset for generic HTTP mode
- `SMS_API_URL` — HTTP endpoint that accepts JSON `{ "to", "message", "sender" }`
- `SMS_API_TOKEN` — Bearer token (if your provider needs authorization)
- `SMS_SENDER_ID` — Sender label (defaults to `LIBRARY`)

Twilio variables (required when `SMS_PROVIDER=TWILIO`):
- `TWILIO_ACCOUNT_SID`
- `TWILIO_AUTH_TOKEN`
- `TWILIO_FROM_NUMBER` (E.164 format, e.g. `+14155551234`)

You can trigger reminders from:
- GUI: `Reports -> Send Due-Date SMS Alerts...`
- Console: menu option `8. Send Due-Date SMS Reminders`

You can configure SMS providers from GUI:
- `Settings -> SMS Settings...`
- Saved locally to `data/sms.properties`
- If environment variables are set, they take precedence over saved settings

---

## 🎓 OOP Concepts Demonstrated

This project was built as a **Java course project** and includes inline comments explaining these OOP principles:

| Concept            | Where Used                                                    |
|--------------------|---------------------------------------------------------------|
| **Encapsulation**  | Private fields with public getters/setters in all model classes |
| **Inheritance**    | Custom exceptions extending `Exception`                       |
| **Polymorphism**   | `AbstractTableModel` subclasses, custom `TableCellRenderer`   |
| **Abstraction**    | Service layer hides business logic from UI; `FileStorageService` hides persistence details |
| **Singleton Pattern** | `LibraryService` and `FileStorageService`                  |
| **Enum**           | `BorrowRecord.Status` (ACTIVE, RETURNED, OVERDUE)            |
| **Serialization**  | All model classes implement `Serializable` for file persistence |

---

## 📄 License

This project is for educational purposes.

---

## 👤 Author

**Harikrishnan** — [GitHub](https://github.com/Harikrishnan120A)
