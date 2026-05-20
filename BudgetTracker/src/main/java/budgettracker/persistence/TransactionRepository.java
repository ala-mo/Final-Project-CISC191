package budgettracker.persistence;

import budgettracker.exceptions.TransactionNotFoundException;
import budgettracker.models.*;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class TransactionRepository implements Repository<Transaction, Integer> {

    private final List<Transaction> transactions = new ArrayList<>();
    private final String filePath;

    public TransactionRepository(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public void add(Transaction t) {
        // adding transactions to the ArrayList
        transactions.add(t);
    }

    @Override
    public void remove(Integer id) {
        // using getId() from Transaction.java to remove transactions
        boolean removed = transactions.removeIf(t -> t.getId() == id);
        if (!removed) { throw new TransactionNotFoundException(id); }
    }

    @Override
    public Transaction findById(Integer id) {
        // using getId() from Transaction.java to identify a specific transaction
        return transactions.stream()
                .filter(t -> t.getId() == id)
                .findFirst()
                .orElseThrow(() -> new TransactionNotFoundException(id));
    }

    @Override
    public List<Transaction> findAll() {
        // returning a copy
        return new ArrayList<>(transactions);

    // writes each transaction as a CSV line
    public void saveToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Format: type,amount,description,date,category
            for (Transaction t : transactions) {
                String type = (t instanceof Income) ? "INCOME" : "EXPENSE";
                writer.write(type + "," + t.getAmount() + "," + t.getDescription()
                        + "," + t.getDate() + "," + t.getCategory());
                writer.newLine();
            }
        }
    }

    // reaches each line and recreates the correct Transaction subclass
    public void loadFromFile() throws IOException {
        File file = new File(filePath);
        if (!file.exists()) return; 

        transactions.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 5);
                if (parts.length < 5) { continue; } // skips malformed lines

                String type = parts[0];
                double amount = Double.parseDouble(parts[1]);
                String description = parts[2];
                LocalDate date = LocalDate.parse(parts[3]);
                Category category = Category.valueOf(parts[4]);

                if (type.equals("INCOME")) {
                    transactions.add(new Income(amount, description, date, category));
                } else {
                    transactions.add(new Expense(amount, description, date, category));
                }
            }
        }
    }
}