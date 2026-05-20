package budgettracker.models;

public class User {

    private final String username;

    public User(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username cannot be empty.");
        }

        this.username = username.trim();
    }

    // getter method
    public String getUsername() { return username; }

    @Override
    public String toString() { return "User(" + username + ")"; }
}