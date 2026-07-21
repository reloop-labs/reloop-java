package sh.reloop.exceptions;

/** Thrown when webhook signature verification fails. */
public class WebhookSignatureException extends RuntimeException {
    public WebhookSignatureException(String message) {
        super(message);
    }
}
