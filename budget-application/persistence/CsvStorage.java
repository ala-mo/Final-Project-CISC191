package budget.persistence;

import budget.model.*;
import budget.service.TransactionService;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and loads transactions as a plain CSV file (budget.csv).
 *
 * Module 4: File I/O + custom exceptions.
 * CSV format per line: id,amount,description,category,date,label
 */
public class CsvStorage {

    private static final String FILE_NAME = "budget.csv";

    /** Writes every transaction to budget.csv, one per line. */
    public static void save(TransactionService service) throws IOException {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : service.getAll()) {
                writer.println(t.toCsvLine());
            }
        }
    }

    /**
     * Reads budget.csv and re-adds every transaction to the service.
     * Bad lines are skipped with a printed warning instead of crashing.
     * Module 4: robust input handling — catches InvalidTransactionException per line.
     */
    public static void load(TransactionService service) throws IOException {
        File file = new File(FILE_NAME);
        if (!file.exists()) return; // nothing saved yet, that's fine

        service.clearAll();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                try {
                    Transaction t = parseLine(line);
                    service.addTransaction(t);
                } catch (InvalidTransactionException e) {
                    System.err.println("Skipping bad line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Parses one CSV line into a Transaction.
     * Throws InvalidTransactionException if the line is malformed.
     */
    private static Transaction parseLine(String line) throws InvalidTransactionException {
        String[] parts = line.split(",", 6);
        if (parts.length != 6) {
            throw new InvalidTransactionException("Expected 6 fields, got " + parts.length + ": " + line);
        }

        try {
            int id            = Integer.parseInt(parts[0].trim());
            double amount     = Double.parseDouble(parts[1].trim());
            String desc       = parts[2].trim();
            Category category = Category.valueOf(parts[3].trim());
            LocalDate date    = LocalDate.parse(parts[4].trim());
            String label      = parts[5].trim();

            if (label.equals("Income")) {
                return new Income(id, amount, desc, category, date);
            } else {
                return new Expense(id, amount, desc, category, date);
            }
        } catch (Exception e) {
            throw new InvalidTransactionException("Could not parse: " + line + " — " + e.getMessage());
        }
    }
}
