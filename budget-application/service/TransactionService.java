package budget.service;

import budget.model.Category;
import budget.model.Transaction;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * All business logic lives here — no UI code, no file code.
 * This is the "Model" in MVC.
 *
 * Module 2: Functional interfaces — filter() accepts a Predicate<Transaction>.
 * Module 5: Recursion — sumRecursive() adds up amounts without a loop.
 * Module 6: Collections + Streams — transactions stored in a List;
 *           streams used for totals, grouping, and filtering.
 */
public class TransactionService {

    // Module 6: List collection holds all transactions
    private final List<Transaction> transactions = new ArrayList<>();

    private int nextId = 1; // auto-incrementing ID

    // ----------------------------------------------------------------
    // Module 1: 2D array — monthly spending grid [month][category]
    // Rows 0-11 = January-December, columns = Category ordinals.
    // Updated every time a transaction is added or removed.
    // ----------------------------------------------------------------
    private final double[][] monthlyGrid = new double[12][Category.values().length];

    // --- Add / Remove ---

    public void addTransaction(Transaction t) {
        transactions.add(t);
        updateGrid(t, +1);
    }

    public void removeTransaction(int id) {
        Transaction found = findById(id);
        if (found != null) {
            transactions.remove(found);
            updateGrid(found, -1);
        }
    }

    /** Updates the 2D array when a transaction is added (+1) or removed (-1). */
    private void updateGrid(Transaction t, int sign) {
        int month    = t.getDate().getMonthValue() - 1; // 0-based
        int catIndex = t.getCategory().ordinal();
        monthlyGrid[month][catIndex] += sign * t.getAmount();
    }

    /**
     * Returns the full 12x(numCategories) monthly spending grid.
     * Module 1: 2D array processing.
     */
    public double[][] getMonthlyGrid() {
        return monthlyGrid;
    }

    /**
     * Returns spending for a specific month and category from the 2D array.
     * Module 1: searching inside a 2D array.
     */
    public double getMonthlySpending(int month, Category cat) {
        return monthlyGrid[month - 1][cat.ordinal()];
    }

    // --- Module 2: Predicate-based filter ---

    /**
     * Returns only the transactions that pass the given test.
     * Example: filter(t -> t.getCategory() == Category.FOOD)
     */
    public List<Transaction> filter(Predicate<Transaction> test) {
        return transactions.stream()
                .filter(test)
                .collect(Collectors.toList());
    }

    // --- Module 5: Recursion ---

    /**
     * Recursively sums the amounts in a list.
     * Base case: empty list → 0.
     * Recursive case: first element + sum of the rest.
     */
    public double sumRecursive(List<Transaction> list) {
        if (list.isEmpty()) return 0.0;                              // base case
        return list.get(0).getAmount() + sumRecursive(list.subList(1, list.size())); // recursive case
    }

    // --- Module 6: Streams ---

    /** Total income across all transactions. */
    public double getTotalIncome() {
        return transactions.stream()
                .filter(t -> t.getLabel().equals("Income"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Total expenses across all transactions. */
    public double getTotalExpenses() {
        return transactions.stream()
                .filter(t -> t.getLabel().equals("Expense"))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    /** Net balance = income - expenses. */
    public double getBalance() {
        return getTotalIncome() - getTotalExpenses();
    }

    /**
     * Groups transactions by category and returns a map of
     * category → list of transactions in that category.
     * Module 6: Collectors.groupingBy
     */
    public Map<Category, List<Transaction>> groupByCategory() {
        return transactions.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory));
    }

    /**
     * Returns all transactions sorted newest-first.
     * Module 3: uses compareTo() defined on Transaction.
     */
    public List<Transaction> getSorted() {
        List<Transaction> sorted = new ArrayList<>(transactions);
        Collections.sort(sorted); // uses Transaction.compareTo()
        return sorted;
    }

    /** Returns all transactions (unsorted). */
    public List<Transaction> getAll() {
        return Collections.unmodifiableList(transactions);
    }

    /** Clears everything — used when loading from file. */
    public void clearAll() {
        transactions.clear();
        for (double[] row : monthlyGrid) Arrays.fill(row, 0.0);
        nextId = 1;
    }

    public int getNextId() { return nextId++; }

    private Transaction findById(int id) {
        for (Transaction t : transactions) {
            if (t.getId() == id) return t;
        }
        return null;
    }
}
