package budgettracker.models;

public class Budget {
    private final double[] limits;

    public Budget() {
        limits = new double[Category.values().length]; 
        // one slot per category, default 0.0
    }

    // sets a monthly spending limit for a category
    public void setLimit(Category category, double limit) {
        if (limit < 0) { throw new IllegalArgumentException("Budget limit cannot be negative."); }
        limits[category.ordinal()] = limit;
    }

    // gets the limit for a category
    public double getLimit(Category category) {
        return limits[category.ordinal()];
    }

    // checks if spending in a category has exceeded its limit 
    // (0.0 limit = no limit set)
    public boolean isExceeded(Category category, double amountSpent) {
        double limit = limits[category.ordinal()];
        return limit > 0 && amountSpent > limit;
    }

    // loops through all categories and returns any categories with an exceeded limit
    public String[] getExceededCategories(double[] spentPerCategory) {
        
        // counts how many are exceeded
        int count = 0;
        for (int i = 0; i < limits.length; i++) {
            if (limits[i] > 0 && spentPerCategory[i] > limits[i]) count++;
        }

        // fills the result array
        String[] exceeded = new String[count];
        int index = 0;
        for (int i = 0; i < limits.length; i++) {
            if (limits[i] > 0 && spentPerCategory[i] > limits[i]) {
                exceeded[index++] = Category.values()[i].name();
            }
        }
        return exceeded;
    }
}