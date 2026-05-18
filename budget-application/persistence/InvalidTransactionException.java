package budget.persistence;

/**
 * Thrown when a CSV line cannot be parsed into a Transaction.
 * Module 4: Custom exception for robust error handling during file I/O.
 */
public class InvalidTransactionException extends Exception {
    public InvalidTransactionException(String message) {
        super(message);
    }
}
