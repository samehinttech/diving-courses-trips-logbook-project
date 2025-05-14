package ch.fhnw.oceandive.exceptionHandler;

/**
 * Exception thrown when a business rule is violated.
 * This is a more specific exception than IllegalStateException for business logic violations.
 */
public class BusinessRuleViolationException extends RuntimeException {

    /**
     * Constructs a new BusinessRuleViolationException with the specified detail message.
     * @param message the detail message
     */
    public BusinessRuleViolationException(String message) {
        super(message);
    }

    /**
     * Constructs a new BusinessRuleViolationException with the specified detail message and cause.
     * @param message the detail message
     * @param cause the cause
     */
    public BusinessRuleViolationException(String message, Throwable cause) {
        super(message, cause);
    }
}