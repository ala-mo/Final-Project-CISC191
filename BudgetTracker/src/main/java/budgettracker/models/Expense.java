package budgettracker.models;

import java.time.LocalDate;

public class Expense extends Transaction {

    // references these fields from Transaction.java
    public Expense(double amount, String description, LocalDate date, Category category) {
        super(amount, description, date, category);
    }

    @Override
    public double getSignedAmount() {
        // expenses subtracted from balance
        return -getAmount();
    }

    @Override
    public String toString() {
        return "EXPENSE: " + super.toString();
    }
}