package athena.exception;

/**
 * Exception class for handled errors in Athena application.
 */
public class AthenaException extends RuntimeException {
    /**
     * Constructs an AthenaException object.
     *
     * @param message Error Message.
     */
    public AthenaException(String message) {
        super(message);
    }
}
