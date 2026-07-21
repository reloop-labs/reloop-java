package sh.reloop.validation;

import sh.reloop.exceptions.ReloopValidationException;

import java.util.Collection;
import java.util.List;

/** Shared client-side field validators (no HTTP). */
public final class Validators {
    private Validators() {}

    public static String requireNonEmptyString(String value, String field) {
        if (value == null || value.trim().isEmpty()) {
            throw new ReloopValidationException(
                    field + " is required and must be a non-empty string.", field);
        }
        return value.trim();
    }

    public static String requireMailString(String value, String field) {
        return requireNonEmptyString(value, field);
    }

    public static Object requireRecipient(Object value, String field) {
        if (value instanceof String s) {
            return requireMailString(s, field);
        }
        if (value instanceof Collection<?> collection) {
            if (collection.isEmpty()) {
                throw new ReloopValidationException(
                        field + " must contain at least one address.", field);
            }
            return collection.stream()
                    .map(entry -> {
                        if (!(entry instanceof String s)) {
                            throw new ReloopValidationException(
                                    field + " is required and must be a string or string array.", field);
                        }
                        return requireMailString(s, field);
                    })
                    .toList();
        }
        if (value instanceof String[] arr) {
            return requireRecipient(List.of(arr), field);
        }
        throw new ReloopValidationException(
                field + " is required and must be a string or string array.", field);
    }

    public static String requireApiKeyName(String name, String field) {
        String trimmed = requireNonEmptyString(name, field);
        if (trimmed.codePointCount(0, trimmed.length()) > 255) {
            throw new ReloopValidationException(
                    "API key " + field + " must be at most 255 characters.", field);
        }
        return trimmed;
    }

    public static String requireApiKeyId(String id, String field) {
        return requireNonEmptyString(id, field);
    }

    public static void requirePage(int page, String field) {
        if (page < 1) {
            throw new ReloopValidationException(
                    "list " + field + " must be an integer >= 1.", field);
        }
    }

    public static void requireLimit(int limit, int min, int max, String field) {
        if (limit < min || limit > max) {
            throw new ReloopValidationException(
                    "list " + field + " must be an integer between " + min + " and " + max + ".",
                    field);
        }
    }

    public static String requireMailboxId(String id, String field) {
        try {
            return requireNonEmptyString(id, field);
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Mailbox " + field + " is required and must be a non-empty string.", field);
        }
    }

    public static String requireMessageId(String id, String field) {
        try {
            return requireNonEmptyString(id, field);
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Message " + field + " is required and must be a non-empty string.", field);
        }
    }

    public static String requireThreadId(String id, String field) {
        try {
            return requireNonEmptyString(id, field);
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Thread " + field + " is required and must be a non-empty string.", field);
        }
    }

    public static String requireInboxAttachmentId(String id, String field) {
        return requireNonEmptyString(id, field);
    }

    public static void requireInboxLimit(int limit, String field) {
        requireLimit(limit, 1, 200, field);
    }

    public static void requireInboxOffset(int offset, String field) {
        if (offset < 0) {
            throw new ReloopValidationException(
                    "list " + field + " must be an integer >= 0.", field);
        }
    }

    public static List<String> requireInboxIdArray(List<String> ids, String field, int max) {
        if (ids == null || ids.isEmpty()) {
            throw new ReloopValidationException(
                    field + " is required and must be a non-empty array.", field);
        }
        if (ids.size() > max) {
            throw new ReloopValidationException(
                    field + " must contain at most " + max + " items.", field);
        }
        List<String> out = new java.util.ArrayList<>(ids.size());
        for (int i = 0; i < ids.size(); i++) {
            try {
                out.add(requireNonEmptyString(ids.get(i), field));
            } catch (ReloopValidationException e) {
                throw new ReloopValidationException(
                        field + "[" + i + "] must be a non-empty string.", field);
            }
        }
        return out;
    }

    public static void requireMailboxStatus(String status, String field) {
        if (!"active".equals(status) && !"disabled".equals(status)) {
            throw new ReloopValidationException(
                    field + " must be \"active\" or \"disabled\".", field);
        }
    }

    public static void requireThreadStatus(String status, String field) {
        if (!"active".equals(status) && !"archived".equals(status)
                && !"closed".equals(status) && !"trash".equals(status)) {
            throw new ReloopValidationException(
                    field + " must be \"active\", \"archived\", \"closed\", or \"trash\".", field);
        }
    }

    public static void requireThreadFilter(String filter, String field) {
        if (!"primary".equals(filter) && !"alerts".equals(filter)
                && !"person".equals(filter) && !"tag".equals(filter)) {
            throw new ReloopValidationException(
                    field + " must be \"primary\", \"alerts\", \"person\", or \"tag\".", field);
        }
    }

    public static void requireThreadBatchAction(String action) {
        if (action == null) {
            throw new ReloopValidationException(
                    "batch action must be a valid thread batch action.", "action");
        }
        switch (action) {
            case "archive", "trash", "restore", "star", "unstar", "read", "unread",
                    "important", "unimportant", "spam", "unspam", "pin", "unpin" -> {
                return;
            }
            default -> throw new ReloopValidationException(
                    "batch action must be a valid thread batch action.", "action");
        }
    }

    public static void requireComposeBody(String text, String html) {
        if (text == null && html == null) {
            throw new ReloopValidationException(
                    "at least one of text or html is required.", "params");
        }
    }

    public static void requireFiniteNumber(double value, String field) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new ReloopValidationException(
                    field + " must be a number when provided.", field);
        }
    }
}
