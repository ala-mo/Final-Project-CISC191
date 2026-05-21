package budgettracker.services;

import budgettracker.models.*;
import budgettracker.persistence.TransactionRepository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// sets up the logic for the user-interface
public class FinanceService {

    private final TransactionRepository repository;

    public FinanceService(TransactionRepository repository) {
        this.repository = repository;
    }
    
    public void addTransaction(Transaction t) {
        repository.add(t);
    }

    public void removeTransaction(int id) {
        repository.remove(id); 
        // throws TransactionNotFoundException if not found
    }

    public List<Transaction> getAllTransactions() {
        return repository.findAll();
    }

    public List<Transaction> filterTransactions(Predicate<Transaction> condition) {
        return repository.findAll().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    // sums up the transaction amounts in a category
    public Map<Category, Double> getSpendingByCategory() {
        return repository.findAll().stream()
                .filter(t -> t instanceof Expense)
                .collect(Collectors.groupingBy(
                        Transaction::getCategory,
                        Collectors.summingDouble(Transaction::getAmount)
                ));
    }

    // calculates current balance
    public double calculateBalance() {
        return repository.findAll().stream()
                .mapToDouble(Transaction::getSignedAmount)
                .sum();
    }

    // sums up a list of transactions using recursion
    public double recursiveSum(List<Transaction> list, int index) {
        if (index >= list.size()) { return 0.0; } // base case
        return list.get(index).getSignedAmount() + recursiveSum(list, index + 1); // recursive case
    }

    // sorting transactions by date using the compareTo() method in Transaction.java
    public List<Transaction> sortByDate(List<Transaction> transactions) {
        List<Transaction> sorted = new ArrayList<>(transactions);
        for (int i = 1; i < sorted.size(); i++) {
            Transaction current = sorted.get(i);
            int j = i - 1;
            while (j >= 0 && sorted.get(j).compareTo(current) > 0) {
                sorted.set(j + 1, sorted.get(j));
                j--;
            }
            sorted.set(j + 1, current);
        }
        return sorted;
    }

    // generates a monthly report
    public String generateReport() {
        List<Transaction> all = repository.findAll();
        double income  = all.stream().filter(t -> t instanceof Income).mapToDouble(Transaction::getAmount).sum();
        double expense = all.stream().filter(t -> t instanceof Expense).mapToDouble(Transaction::getAmount).sum();
        double balance = income - expense;

        return String.format(
                "=== Monthly Report ===\nTotal Income:   $%.2f\nTotal Expenses: $%.2f\nNet Balance:    $%.2f",
                income, expense, balance
        );
    }
}