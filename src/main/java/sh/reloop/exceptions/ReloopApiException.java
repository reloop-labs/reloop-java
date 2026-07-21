package sh.reloop.exceptions;

/** Thrown for non-2xx HTTP responses and network failures. */
public class ReloopApiException extends RuntimeException {
    private final int status;
    private final String statusText;
    private final ApiErrorBody body;

    public ReloopApiException(int status, String statusText, ApiErrorBody body) {
        super(buildMessage(status, statusText, body));
        this.status = status;
        this.statusText = statusText;
        this.body = body != null ? body : new ApiErrorBody();
    }

    public ReloopApiException(String message, Throwable cause) {
        super(message, cause);
        this.status = 0;
        this.statusText = "Network Error";
        this.body = new ApiErrorBody();
        this.body.message = message;
    }

    private static String buildMessage(int status, String statusText, ApiErrorBody body) {
        if (body != null && body.message != null && !body.message.isBlank()) {
            return body.message;
        }
        if (status == 0) {
            return "Reloop network error: " + statusText;
        }
        return "Reloop API Error: " + status + " " + statusText;
    }

    public int getStatus() {
        return status;
    }

    public String getStatusText() {
        return statusText;
    }

    public ApiErrorBody getBody() {
        return body;
    }
}
