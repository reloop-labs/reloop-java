package sh.reloop;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class RequestParameters {
    private static final Map<String, String> REQUEST_KEY_MAP = Map.ofEntries(
        Map.entry("first_name", "firstName"),
        Map.entry("last_name", "lastName"),
        Map.entry("group_ids", "groupIds"),
        Map.entry("group_id", "groupId"),
        Map.entry("fallback_value", "fallbackValue"),
        Map.entry("default_subscription", "defaultSubscription"),
        Map.entry("channel_id", "channelId"),
        Map.entry("property_name", "propertyName"),
        Map.entry("property_type", "propertyType"),
        Map.entry("contact_id", "contactId"),
        Map.entry("rate_limit_enabled", "rateLimitEnabled"),
        Map.entry("user_id", "userId")
    );

    private RequestParameters() {}

    public static Map<String, Object> forRequest(Map<String, Object> parameters) {
        Map<String, Object> normalized = new HashMap<>();

        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            if ("unsubscribed".equals(key)) {
                if (!parameters.containsKey("status") && value instanceof Boolean unsubscribed) {
                    normalized.put("status", unsubscribed ? "unsubscribed" : "subscribed");
                }
                continue;
            }

            String apiKey = REQUEST_KEY_MAP.getOrDefault(key, toCamelCase(key));
            normalized.put(apiKey, normalizeValue(value, true));
        }

        return normalized;
    }

    public static Map<String, Object> forQuery(Map<String, Object> options) {
        return forRequest(options);
    }

    private static Object normalizeValue(Object value, boolean isRequest) {
        if (value instanceof Map<?, ?> mapValue) {
            Map<String, Object> converted = new HashMap<>();
            for (Map.Entry<?, ?> entry : mapValue.entrySet()) {
                converted.put(String.valueOf(entry.getKey()), normalizeValue(entry.getValue(), isRequest));
            }
            return isRequest ? forRequest(converted) : converted;
        }

        if (value instanceof List<?> listValue) {
            List<Object> converted = new ArrayList<>();
            for (Object item : listValue) {
                converted.add(normalizeValue(item, isRequest));
            }
            return converted;
        }

        return value;
    }

    private static String toCamelCase(String key) {
        if (REQUEST_KEY_MAP.containsKey(key) || !key.contains("_")) {
            return REQUEST_KEY_MAP.getOrDefault(key, key);
        }

        String[] parts = key.split("_");
        StringBuilder builder = new StringBuilder(parts[0]);
        for (int index = 1; index < parts.length; index++) {
            if (parts[index].isEmpty()) {
                continue;
            }
            builder.append(Character.toUpperCase(parts[index].charAt(0)));
            if (parts[index].length() > 1) {
                builder.append(parts[index].substring(1));
            }
        }
        return builder.toString();
    }
}
