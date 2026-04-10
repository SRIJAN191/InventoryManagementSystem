# Inventory Management System

JavaFX-based inventory management system for product tracking, sales recording, reporting, and staff management.

## Tech Stack

- Java 21
- JavaFX 21
- Maven
- MySQL

## How to Run

1. Open the project root in VS Code or a terminal.
2. Create a `db.properties` file in the project root by copying `db.properties.example`.
3. Update the MySQL username and password.
4. Run the application:

```powershell
mvn javafx:run
```

## Running in VS Code

1. Open the project root folder in VS Code.
2. Open `src/main/java/com/ims/Main.java`.
3. Click `Run` above the `main` method.
4. If the database settings are missing or incorrect, the app will prompt you for your MySQL host, port, database, username, and password, then save them to `db.properties`.

## Default Login Accounts

- Admin: `admin` / `admin123`
- Admin: `owner` / `owner123`
- Staff: `staff` / `staff123`

## Submission Notes

- Include: `src/`, `lib/`, `pom.xml`, `README.md`, `.gitignore`, and `db.properties.example`
- Exclude: `target/`, `bin/`, `.vscode/`, `.sixth/`, and the real `db.properties`
- The evaluator should create their own `db.properties` from `db.properties.example`
