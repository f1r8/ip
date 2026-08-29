package athena.exception;

/**
 * Represents a handled error in the Athena application.
 */
public class AthenaException extends RuntimeException {
    /**
     * Constructs an AthenaException object.
     *
     * @param message Error message to display.
     */
    public AthenaException(String message) {
        super(message);
    }
}
