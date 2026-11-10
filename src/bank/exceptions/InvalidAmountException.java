package bank.exceptions;

/** Thrown when an amount is not a positive number with at most 2 decimals, or too large. */
public class InvalidAmountException extends IllegalArgumentException {
    public InvalidAmountException(String message) {
        super(message);
    }
}