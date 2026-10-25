package sh.reloop.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import sh.reloop.exceptions.WebhookSignatureException;
import sh.reloop.models.WebhookModels.VerifyWebhookParams;
import sh.reloop.models.WebhookModels.WebhookEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;

/** HMAC-SHA256 webhook signature verification. */
public final class WebhookVerify {
    /** Header Reloop signs every delivery with: {@code t=<unix>,v1=<hex>}. */
    public static final String WEBHOOK_SIGNATURE_HEADER = "Reloop-Signature";
    public static final String WEBHOOK_TIMESTAMP_HEADER = "Reloop-Timestamp";
    public static final String WEBHOOK_ID_HEADER = "Reloop-Id";

    /** Header name used before 2.0.1. Still accepted as a fallback. */
    private static final String LEGACY_WEBHOOK_SIGNATURE_HEADER = "x-webhook-signature";
    public static final int DEFAULT_TOLERANCE_SECONDS = 300;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private WebhookVerify() {}

    public static WebhookEvent verify(VerifyWebhookParams params) {
        if (params == null || params.secret == null || params.secret.isEmpty()) {
            throw new WebhookSignatureException("Webhook secret is required");
        }
        if (params.payload == null) {
            throw new WebhookSignatureException("Webhook payload is required");
        }

        int tolerance = params.tolerance == null || params.tolerance == 0
                ? DEFAULT_TOLERANCE_SECONDS
                : params.tolerance;

        String signatureHeader = getHeader(params.headers, WEBHOOK_SIGNATURE_HEADER);
        if (signatureHeader == null || signatureHeader.isEmpty()) {
            signatureHeader = getHeader(params.headers, LEGACY_WEBHOOK_SIGNATURE_HEADER);
        }
        if (signatureHeader == null || signatureHeader.isEmpty()) {
            throw new WebhookSignatureException("Missing " + WEBHOOK_SIGNATURE_HEADER + " header");
        }

        ParsedSignature parsed = parseSignatureHeader(signatureHeader);
        verifyTimestamp(parsed.timestamp, tolerance);

        String expected = computeExpectedSignature(params.secret, parsed.timestamp, params.payload);
        boolean valid = false;
        for (String sig : parsed.signatures) {
            if (verifySignature(expected, sig)) {
                valid = true;
                break;
            }
        }
        if (!valid) {
            throw new WebhookSignatureException("Webhook signature verification failed");
        }

        return parseWebhookEvent(params.payload);
    }

    public static WebhookEvent constructEvent(byte[] payload, String signature, String secret, int tolerance) {
        VerifyWebhookParams params = new VerifyWebhookParams();
        params.payload = payload;
        params.secret = secret;
        params.tolerance = tolerance;
        if (signature != null && !signature.isEmpty()) {
            params.headers = Map.of(WEBHOOK_SIGNATURE_HEADER, signature);
        } else {
            params.headers = Map.of();
        }
        return verify(params);
    }

    private static String getHeader(Map<String, String> headers, String name) {
        if (headers == null) {
            return null;
        }
        String lower = name.toLowerCase();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            if (entry.getKey() != null && entry.getKey().toLowerCase().equals(lower)) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static final class ParsedSignature {
        final String timestamp;
        final String[] signatures;

        ParsedSignature(String timestamp, String[] signatures) {
            this.timestamp = timestamp;
            this.signatures = signatures;
        }
    }

    private static ParsedSignature parseSignatureHeader(String header) {
        String timestamp = null;
        java.util.List<String> signatures = new java.util.ArrayList<>();

        for (String element : header.split(",")) {
            String trimmed = element.trim();
            int eqIndex = trimmed.indexOf('=');
            if (eqIndex == -1) {
                continue;
            }
            String prefix = trimmed.substring(0, eqIndex);
            String value = trimmed.substring(eqIndex + 1);
            if ("t".equals(prefix)) {
                timestamp = value;
            } else if ("v1".equals(prefix)) {
                signatures.add(value);
            }
        }

        if (timestamp == null || timestamp.isEmpty() || signatures.isEmpty()) {
            throw new WebhookSignatureException(
                    "Invalid " + WEBHOOK_SIGNATURE_HEADER + " header: expected t= and v1= values");
        }
        return new ParsedSignature(timestamp, signatures.toArray(new String[0]));
    }

    static String computeExpectedSignature(String secret, String timestamp, byte[] payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            mac.update(payload);
            return HexFormat.of().formatHex(mac.doFinal());
        } catch (Exception e) {
            throw new WebhookSignatureException("Failed to compute webhook signature");
        }
    }

    private static boolean verifySignature(String expected, String received) {
        try {
            byte[] expectedBuf = HexFormat.of().parseHex(expected);
            byte[] receivedBuf = HexFormat.of().parseHex(received);
            return MessageDigest.isEqual(expectedBuf, receivedBuf);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void verifyTimestamp(String timestamp, int tolerance) {
        double ts;
        try {
            ts = Double.parseDouble(timestamp);
        } catch (NumberFormatException e) {
            throw new WebhookSignatureException("Invalid timestamp in " + WEBHOOK_SIGNATURE_HEADER + " header");
        }
        if (!Double.isFinite(ts)) {
            throw new WebhookSignatureException("Invalid timestamp in " + WEBHOOK_SIGNATURE_HEADER + " header");
        }

        long age = Math.abs(Instant.now().getEpochSecond() - (long) Math.floor(ts));
        if (age > tolerance) {
            throw new WebhookSignatureException(
                    "Timestamp outside tolerance: allowed drift is " + tolerance + " seconds");
        }
    }

    private static WebhookEvent parseWebhookEvent(byte[] raw) {
        JsonNode record;
        try {
            record = MAPPER.readTree(raw);
        } catch (Exception e) {
            throw new WebhookSignatureException("Webhook payload is not valid JSON");
        }
        if (record == null || !record.isObject()) {
            throw new WebhookSignatureException("Webhook payload must be a JSON object");
        }

        JsonNode idNode = record.get("id");
        if (idNode == null || !idNode.isTextual() || idNode.asText().isEmpty()) {
            throw new WebhookSignatureException("Webhook payload missing required field: id");
        }
        // Production envelope: {id, type, created_at, data}.
        // Legacy envelope (pre-2.0.1 fixtures): {id, event, payload, timestamp}.
        String type = textOrNull(record, "type");
        if (type == null) {
            type = textOrNull(record, "event");
        }
        if (type == null) {
            throw new WebhookSignatureException("Webhook payload missing required field: type");
        }

        JsonNode dataNode = record.get("data");
        if (dataNode == null || !dataNode.isObject()) {
            dataNode = record.get("payload");
        }
        if (dataNode == null || !dataNode.isObject()) {
            throw new WebhookSignatureException("Webhook payload missing required field: data");
        }

        String createdAt = textOrNull(record, "created_at");
        double unixSeconds;
        if (createdAt != null) {
            try {
                unixSeconds = java.time.OffsetDateTime.parse(createdAt).toInstant().toEpochMilli() / 1000.0;
            } catch (java.time.format.DateTimeParseException e) {
                throw new WebhookSignatureException("Webhook payload has an invalid created_at");
            }
        } else {
            JsonNode timestampNode = record.get("timestamp");
            if (timestampNode == null || !timestampNode.isNumber() || !Double.isFinite(timestampNode.asDouble())) {
                throw new WebhookSignatureException("Webhook payload missing required field: created_at");
            }
            unixSeconds = timestampNode.asDouble();
            createdAt = Instant.ofEpochMilli((long) (unixSeconds * 1000)).toString();
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> data = MAPPER.convertValue(dataNode, Map.class);

        WebhookEvent event = new WebhookEvent();
        event.id = idNode.asText();
        event.type = type;
        event.createdAt = createdAt;
        event.data = data;
        event.event = type;
        event.payload = data;
        event.timestamp = unixSeconds;
        return event;
    }

    private static String textOrNull(JsonNode record, String field) {
        JsonNode node = record.get(field);
        return node != null && node.isTextual() && !node.asText().isEmpty() ? node.asText() : null;
    }
}
