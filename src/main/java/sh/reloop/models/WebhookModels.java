package sh.reloop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;
import java.util.Map;

public final class WebhookModels {
    private WebhookModels() {}

    public static final class WebhookStatus {
        public static final String ACTIVE = "active";
        public static final String PAUSED = "paused";
        public static final String DISABLED = "disabled";
        public static final String FAILED = "failed";

        private WebhookStatus() {}
    }

    public static final class WebhookDeliveryStatus {
        public static final String PENDING = "pending";
        public static final String SUCCESS = "success";
        public static final String FAILED = "failed";
        public static final String RETRYING = "retrying";

        private WebhookDeliveryStatus() {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Webhook {
        public String id;
        public String name;
        public String url;
        public String secret;
        public String status;
        public Map<String, String> customHeaders;
        public boolean rateLimitEnabled;
        public int maxRequestsPerMinute;
        public int maxRetries;
        public double retryBackoffMultiplier;
        public Map<String, Object> filteringOptions;
        public String lastTriggeredAt;
        public int successCount;
        public int failureCount;
        public int consecutiveFailures;
        public List<String> events;
        public String createdAt;
        public String updatedAt;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateWebhookParams {
        public String description;
        public String url;
        public List<String> events;

        public CreateWebhookParams() {}

        public CreateWebhookParams(String description, String url, List<String> events) {
            this.description = description;
            this.url = url;
            this.events = events;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateWebhookParams {
        public String description;
        public String name;
        public String url;
        public String secret;
        public String status;
        public Map<String, String> customHeaders;
        public Boolean rateLimitEnabled;
        public Double maxRequestsPerMinute;
        public Double maxRetries;
        public Double retryBackoffMultiplier;
        public Map<String, Object> filteringOptions;
    }

    public static class ListWebhooksParams {
        public Integer page;
        public Integer limit;
        public String status;
        public String organizationId;
        public String userId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookListResponse {
        public List<Webhook> webhooks;
        public int total;
        public int page;
        public int limit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteWebhookResponse {
        public String id;
        public String message;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TriggerWebhookParams {
        public String event;
        public Map<String, Object> payload;
        public String organizationId;
        public String userId;

        public TriggerWebhookParams() {}

        public TriggerWebhookParams(String event, Map<String, Object> payload) {
            this.event = event;
            this.payload = payload;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TriggerWebhookResponse {
        public boolean success;
        public String message;
        public String jobId;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookDelivery {
        public String id;
        public String webhookId;
        public String webhookEventId;
        public String eventType;
        public Map<String, Object> eventData;
        public String status;
        public String requestUrl;
        public Map<String, String> requestHeaders;
        public Map<String, Object> requestBody;
        public Integer responseStatus;
        public String responseBody;
        public Map<String, String> responseHeaders;
        public int attemptNumber;
        public int maxAttempts;
        public String nextRetryAt;
        public String lastAttemptAt;
        public String errorMessage;
        public Map<String, Object> errorDetails;
        public String completedAt;
        public Integer durationMs;
        public String createdAt;
    }

    public static class ListWebhookDeliveriesParams {
        public Integer page;
        public Integer limit;
        public String status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookDeliveryListResponse {
        public List<WebhookDelivery> deliveries;
        public int total;
        public int page;
        public int limit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RetryWebhookDeliveryResponse {
        public boolean success;
        public String message;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WebhookEvent {
        public String id;
        public String event;
        public Map<String, Object> payload;
        public double timestamp;
    }

    public static class VerifyWebhookParams {
        public byte[] payload;
        public Map<String, String> headers;
        public String secret;
        public Integer tolerance;

        public VerifyWebhookParams() {}

        public VerifyWebhookParams(byte[] payload, Map<String, String> headers, String secret) {
            this.payload = payload;
            this.headers = headers;
            this.secret = secret;
        }
    }
}
