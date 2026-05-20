package budgettracker;

import budgettracker.exceptions.TransactionNotFoundException;
import budgettracker.models.*;
import budgettracker.persistence.TransactionRepository;
import budgettracker.services.FinanceService;
import org.junit.jupiter.api.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class BudgetTrackerTest {

    private TransactionRepository repo;
    private FinanceService service;
    private static final LocalDate TODAY = LocalDate.now();

    @BeforeEach
    void setUp() {
        Transaction.resetIdCounter(); 
        // keep IDs predictable across tests
        repo = new TransactionRepository("test-data.csv");
        service = new FinanceService(repo);
    }

    // MODULE 1: Arrays + OO
    // testing parallel array structure in Budget.java for per-category limits
    @Test
    @DisplayName("Module 1 – Budget correctly detects exceeded category limit")
    void testBudgetArrayLimits() {
        Budget budget = new Budget();
        budget.setLimit(Category.FOOD, 200.0);

        // under the limit — not exceeded
        assertFalse(budget.isExceeded(Category.FOOD, 150.0));

        // over the limit — exceeded
        assertTrue(budget.isExceeded(Category.FOOD, 250.0));

        // no limit set — never exceeded
        assertFalse(budget.isExceeded(Category.RENT, 9999.0));
    }

    // MODULE 2: OO Design + Functional Interfaces
    // testing filterTransactions() in FinanceService.java with a Predicate lambda
    @Test
    @DisplayName("Module 2 – filterTransactions() works with a Predicate lambda")
    void testFunctionalInterfaceFilter() {
        service.addTransaction(new Expense(50.0, "Groceries", TODAY, Category.FOOD));
        service.addTransaction(new Income(1000.0, "Paycheck", TODAY, Category.INCOME));
        service.addTransaction(new Expense(20.0, "Bus pass", TODAY, Category.TRANSPORTATION));

        // passing a lambda as the Predicate — only keeping Expense instances
        List<Transaction> expenses = service.filterTransactions(t -> t instanceof Expense);
        assertEquals(2, expenses.size());

        // filtering by category
        List<Transaction> food = service.filterTransactions(t -> t.getCategory() == Category.FOOD);
        assertEquals(1, food.size());
        assertEquals("Groceries", food.get(0).getDescription());
    }

    // MODULE 3: Inheritance + Polymorphism
    // testing getSignedAmount() to check that Expense.java and Income.java behave differently
    @Test
    @DisplayName("Module 3 – Expense and Income return correct signed amounts (polymorphism)")
    void testPolymorphism() {
        Transaction expense = new Expense(100.0, "Rent", TODAY, Category.RENT);
        Transaction income  = new Income(500.0, "Salary", TODAY, Category.INCOME);

        // testing the different behavior in both Expense.java and Income.java
        assertEquals(-100.0, expense.getSignedAmount(), 0.001);
        assertEquals(+500.0, income.getSignedAmount(), 0.001);

        // using compareTo() from Transaction.java; same date should return 0
        assertEquals(0, expense.compareTo(income));
    }

    // MODULE 4: Exceptions + File I/O
    // testing custom exception TransactionNotFoundException
    // testing the CSV save/load round-trip from saveToFile() and loadFromFile() in TransactionRepository.java
    @Test
    @DisplayName("Module 4 – TransactionNotFoundException thrown for missing ID")
    void testCustomException() {
        // testing custom exception TransactionNotFoundException
        assertThrows(TransactionNotFoundException.class, () -> service.removeTransaction(999));
    }

    @Test
    @DisplayName("Module 4 – Transactions survive a save/load round-trip")
    void testFileIO() throws IOException {
        service.addTransaction(new Expense(75.0, "Electricity", TODAY, Category.UTILITIES));
        service.addTransaction(new Income(2000.0, "Freelance", TODAY, Category.INCOME));

        repo.saveToFile();

        // loading into a fresh repository
        TransactionRepository freshRepo = new TransactionRepository("test-data.csv");
        freshRepo.loadFromFile();

        List<Transaction> loaded = freshRepo.findAll();
        assertEquals(2, loaded.size());
        assertEquals("Electricity", loaded.get(0).getDescription());
        assertEquals(75.0, loaded.get(0).getAmount(), 0.001);

        // cleaning up
        Files.deleteIfExists(Path.of("test-data.csv"));
    }

    // MODULE 5: Recursion + Algorithms
    // testing recursiveSum() and sortByDate() in FinanceService.java
    @Test
    @DisplayName("Module 5 – recursiveSum() correctly totals signed amounts")
    void testRecursiveSum() {
        // testing recursiveSum()
        List<Transaction> list = List.of(
                new Income(500.0, "Salary", TODAY, Category.INCOME),
                new Expense(100.0, "Rent", TODAY, Category.RENT),
                new Expense(50.0, "Food", TODAY, Category.FOOD)
        );

        double result = service.recursiveSum(list, 0);
        assertEquals(350.0, result, 0.001); 
        // 500 - 100 - 50 = 350
    }

    @Test
    @DisplayName("Module 5 – sortByDate() returns transactions in chronological order")
    void testSortByDate() {
        // testing sortByDate()
        LocalDate jan = LocalDate.of(2025, 1, 1);
        LocalDate mar = LocalDate.of(2025, 3, 15);
        LocalDate feb = LocalDate.of(2025, 2, 10);

        service.addTransaction(new Expense(10.0, "March", mar, Category.OTHER));
        service.addTransaction(new Income(20.0, "January", jan, Category.INCOME));
        service.addTransaction(new Expense(30.0, "February", feb, Category.OTHER));

        List<Transaction> sorted = service.sortByDate(service.getAllTransactions());
        assertEquals("January",  sorted.get(0).getDescription());
        assertEquals("February", sorted.get(1).getDescription());
        assertEquals("March",    sorted.get(2).getDescription());
    }

    // MODULE 6: Collections + Generics + Streams
    // testing getSpendingByCategory() and calculateBalance() in FinanceService.java
    @Test
    @DisplayName("Module 6 – getSpendingByCategory() groups and sums correctly")
    void testSpendingByCategory() {
        // testing getSpendingByCategory()
        service.addTransaction(new Expense(50.0, "Pizza", TODAY, Category.FOOD));
        service.addTransaction(new Expense(30.0, "Burger", TODAY, Category.FOOD));
        service.addTransaction(new Expense(100.0, "Rent", TODAY, Category.RENT));

        Map<Category, Double> spending = service.getSpendingByCategory();

        assertEquals(80.0, spending.get(Category.FOOD), 0.001);
        assertEquals(100.0, spending.get(Category.RENT), 0.001);
        assertFalse(spending.containsKey(Category.INCOME)); // income excluded
    }

    @Test
    @DisplayName("Module 6 – calculateBalance() uses streams to sum signed amounts")
    void testCalculateBalance() {
        // testing calculateBalance()
        service.addTransaction(new Income(1000.0, "Job", TODAY, Category.INCOME));
        service.addTransaction(new Expense(400.0, "Rent", TODAY, Category.RENT));
        service.addTransaction(new Expense(100.0, "Food", TODAY, Category.FOOD));

        assertEquals(500.0, service.calculateBalance(), 0.001);
    }
    
    // MODULE 7: JavaFX + Events + Lambdas
    // testing generateReport() in FinanceService.java
    @Test
    @DisplayName("Module 7 – generateReport() returns a correctly formatted summary")
    void testGenerateReport() {
        // testing generateReport()
        service.addTransaction(new Income(1000.0, "Salary", TODAY, Category.INCOME));
        service.addTransaction(new Expense(300.0, "Rent", TODAY, Category.RENT));

        String report = service.generateReport();
        assertTrue(report.contains("Total Income:   $1000.00"));
        assertTrue(report.contains("Total Expenses: $300.00"));
        assertTrue(report.contains("Net Balance:    $700.00"));
    }
}