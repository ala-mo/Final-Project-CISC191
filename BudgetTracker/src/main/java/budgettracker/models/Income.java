package budgettracker.models;

import java.time.LocalDate;

public class Income extends Transaction {

    // references these fields from Transaction.java
    public Income(double amount, String description, LocalDate date, Category category) {
        super(amount, description, date, category);
    }

    @Override
    public double getSignedAmount() {
        // income adds to balance
        return getAmount();
    }

    @Override
    public String toString() {
        return "INCOME:  " + super.toString();
    }
}