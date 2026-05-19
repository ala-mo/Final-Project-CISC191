# Budget Tracker — CISC191 Final Project

A JavaFX desktop application for tracking personal income and expenses. Users can add and delete transactions, filter by type, sort by date, view spending by category, and generate a monthly summary report. Data persists to a CSV file between sessions.

---

## Running the Project

**Requirements:** Java 17+, Maven, JavaFX 21

```bash
# From the project root
mvn javafx:run
```

---

## Running the Tests

```bash
mvn test
```

All tests are in `src/test/java/budgettracker/BudgetTrackerTest.java`.

---

## Module Integration

| Module | Topic | Where It Appears |
|--------|-------|-----------------|
| Module 1 | Arrays + OO | `Budget.java` — parallel array stores per-category spending limits; `User.java`, `Transaction.java` demonstrate encapsulation and constructors |
| Module 2 | OO Design + Functional Interfaces | `FinanceService.filterTransactions(Predicate<Transaction>)` — accepts any lambda as a filter; callers pass `t -> t instanceof Expense`, etc. |
| Module 3 | Inheritance + Polymorphism | `Transaction` (abstract) → `Expense` and `Income` subclasses; `getSignedAmount()` is overridden differently in each; `Transaction implements Comparable` |
| Module 4 | Exceptions + File I/O | `TransactionNotFoundException` (custom exception); `TransactionRepository.saveToFile()` and `loadFromFile()` use `BufferedWriter`/`BufferedReader` for CSV persistence |
| Module 5 | Recursion + Algorithms | `FinanceService.recursiveSum()` — recursive list summation; `sortByDate()` — manual insertion sort using `Comparable` |
| Module 6 | Collections + Generics + Streams | `Repository<T, ID>` generic interface; `ArrayList` in `TransactionRepository`; `getSpendingByCategory()` uses `stream().collect(groupingBy(..., summingDouble(...)))` |
| Module 7 | JavaFX + Events + Lambdas | `BudgetTrackerApp` + `BudgetController` + `main-view.fxml` — full JavaFX MVC; lambda event handlers; `TableRow` color coding via lambda; background save thread using `Task<Void>` |

---

## Reflection

**What I'm most proud of:**
The `filterTransactions(Predicate<Transaction>)` method in `FinanceService` is a clean demonstration of Module 2 — it accepts any lambda so the UI can filter by type, category, date, or anything else without changing the service code. The polymorphic `getSignedAmount()` design also makes `calculateBalance()` dead simple.

**What I would improve with more time:**
I'd add a proper budget limit UI where users can set per-category limits and see visual warnings when they're exceeded. I'd also add a chart (JavaFX `PieChart` or `BarChart`) to visualize spending by category, and improve the CSV format to use a proper database (SQLite via JDBC) for more robustness.
