package sh.reloop.exceptions;

/** Thrown when client arguments are invalid (no HTTP call is made). */
public class ReloopValidationException extends RuntimeException {
    private final String field;

    public ReloopValidationException(String message) {
        this(message, null);
    }

    public ReloopValidationException(String message, String field) {
        super(message);
        this.field = field;
    }

    public String getField() {
        return field;
    }
}
