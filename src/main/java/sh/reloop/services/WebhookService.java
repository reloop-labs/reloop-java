package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.WebhookModels.CreateWebhookParams;
import sh.reloop.models.WebhookModels.DeleteWebhookResponse;
import sh.reloop.models.WebhookModels.ListWebhookDeliveriesParams;
import sh.reloop.models.WebhookModels.ListWebhooksParams;
import sh.reloop.models.WebhookModels.RetryWebhookDeliveryResponse;
import sh.reloop.models.WebhookModels.TriggerWebhookParams;
import sh.reloop.models.WebhookModels.TriggerWebhookResponse;
import sh.reloop.models.WebhookModels.UpdateWebhookParams;
import sh.reloop.models.WebhookModels.VerifyWebhookParams;
import sh.reloop.models.WebhookModels.Webhook;
import sh.reloop.models.WebhookModels.WebhookDeliveryListResponse;
import sh.reloop.models.WebhookModels.WebhookDeliveryStatus;
import sh.reloop.models.WebhookModels.WebhookEvent;
import sh.reloop.models.WebhookModels.WebhookListResponse;
import sh.reloop.models.WebhookModels.WebhookStatus;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.Map;

/** Manages webhooks and verifies signed payloads. */
public class WebhookService {
    private static final String WEBHOOK_V1 = "/api/webhook/v1";

    private final ReloopClient client;

    public WebhookService(ReloopClient client) {
        this.client = client;
    }

    public static WebhookEvent constructEvent(byte[] payload, String signature, String secret, int tolerance) {
        return WebhookVerify.constructEvent(payload, signature, secret, tolerance);
    }

    public WebhookEvent verify(VerifyWebhookParams params) {
        return WebhookVerify.verify(params);
    }

    public Webhook create(CreateWebhookParams params) {
        if (params == null) {
            throw new ReloopValidationException("create params are required and must be an object.", "params");
        }
        return client.request("POST", WEBHOOK_V1 + "/", params, Webhook.class);
    }

    public WebhookListResponse list(ListWebhooksParams params) {
        Map<String, String> query = buildListWebhooksQuery(params);
        return client.request("GET", WEBHOOK_V1, null, query, WebhookListResponse.class);
    }

    public Webhook get(String webhookId) {
        String id = requireWebhookId(webhookId);
        return client.request("GET", WEBHOOK_V1 + "/" + id, null, Webhook.class);
    }

    public Webhook update(String webhookId, UpdateWebhookParams params) {
        String id = requireWebhookId(webhookId);
        UpdateWebhookParams body = validateUpdateParams(params);
        return client.request("PATCH", WEBHOOK_V1 + "/" + id, body, Webhook.class);
    }

    public DeleteWebhookResponse delete(String webhookId) {
        String id = requireWebhookId(webhookId);
        return client.request("DELETE", WEBHOOK_V1 + "/" + id, null, DeleteWebhookResponse.class);
    }

    public Webhook pause(String webhookId) {
        UpdateWebhookParams params = new UpdateWebhookParams();
        params.status = WebhookStatus.PAUSED;
        return update(webhookId, params);
    }

    public Webhook enable(String webhookId) {
        UpdateWebhookParams params = new UpdateWebhookParams();
        params.status = WebhookStatus.ACTIVE;
        return update(webhookId, params);
    }

    public Webhook disable(String webhookId) {
        UpdateWebhookParams params = new UpdateWebhookParams();
        params.status = WebhookStatus.DISABLED;
        return update(webhookId, params);
    }

    public TriggerWebhookResponse trigger(TriggerWebhookParams params) {
        TriggerWebhookParams body = validateTriggerParams(params);
        return client.request("POST", WEBHOOK_V1 + "/trigger", body, TriggerWebhookResponse.class);
    }

    public WebhookDeliveryListResponse listDeliveries(String webhookId, ListWebhookDeliveriesParams params) {
        String id = requireWebhookId(webhookId);
        Map<String, String> query = buildListDeliveriesQuery(params);
        return client.request("GET", WEBHOOK_V1 + "/" + id + "/deliveries", null, query,
                WebhookDeliveryListResponse.class);
    }

    public RetryWebhookDeliveryResponse retryDelivery(String deliveryId) {
        String id = requireDeliveryId(deliveryId);
        return client.request("POST", "/api/webhook/deliveries/" + id + "/retry", null,
                RetryWebhookDeliveryResponse.class);
    }

    private static String requireWebhookId(String id) {
        try {
            return Validators.requireNonEmptyString(id, "webhookId");
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Webhook webhookId is required and must be a non-empty string.", "webhookId");
        }
    }

    private static String requireDeliveryId(String id) {
        try {
            return Validators.requireNonEmptyString(id, "deliveryId");
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Webhook delivery deliveryId is required and must be a non-empty string.", "deliveryId");
        }
    }

    private static void requireWebhookStatus(String status, String field) {
        if (status == null
                || (!WebhookStatus.ACTIVE.equals(status)
                && !WebhookStatus.PAUSED.equals(status)
                && !WebhookStatus.DISABLED.equals(status)
                && !WebhookStatus.FAILED.equals(status))) {
            throw new ReloopValidationException("update status must be a valid webhook status.", field);
        }
    }

    private static void requireDeliveryStatus(String status, String field) {
        if (status == null
                || (!WebhookDeliveryStatus.PENDING.equals(status)
                && !WebhookDeliveryStatus.SUCCESS.equals(status)
                && !WebhookDeliveryStatus.FAILED.equals(status)
                && !WebhookDeliveryStatus.RETRYING.equals(status))) {
            throw new ReloopValidationException("listDeliveries status must be a valid delivery status.", field);
        }
    }

    private static UpdateWebhookParams validateUpdateParams(UpdateWebhookParams params) {
        if (params == null) {
            throw new ReloopValidationException("update requires at least one field to change.", "params");
        }
        if (params.description == null && params.name == null && params.url == null && params.secret == null
                && params.status == null && params.customHeaders == null && params.rateLimitEnabled == null
                && params.maxRequestsPerMinute == null && params.maxRetries == null
                && params.retryBackoffMultiplier == null && params.filteringOptions == null) {
            throw new ReloopValidationException("update requires at least one field to change.", "params");
        }
        if (params.status != null) {
            requireWebhookStatus(params.status, "status");
        }
        if (params.maxRequestsPerMinute != null && !Double.isFinite(params.maxRequestsPerMinute)) {
            throw new ReloopValidationException(
                    "update maxRequestsPerMinute must be a number when provided.", "maxRequestsPerMinute");
        }
        if (params.maxRetries != null && !Double.isFinite(params.maxRetries)) {
            throw new ReloopValidationException("update maxRetries must be a number when provided.", "maxRetries");
        }
        if (params.retryBackoffMultiplier != null && !Double.isFinite(params.retryBackoffMultiplier)) {
            throw new ReloopValidationException(
                    "update retryBackoffMultiplier must be a number when provided.", "retryBackoffMultiplier");
        }
        return params;
    }

    private static TriggerWebhookParams validateTriggerParams(TriggerWebhookParams params) {
        if (params == null) {
            throw new ReloopValidationException("trigger params are required and must be an object.", "params");
        }
        String event = Validators.requireNonEmptyString(params.event, "event");
        if (params.payload == null) {
            throw new ReloopValidationException("trigger payload is required and must be an object.", "payload");
        }
        TriggerWebhookParams body = new TriggerWebhookParams();
        body.event = event;
        body.payload = params.payload;
        if (params.organizationId != null) {
            body.organizationId = Validators.requireNonEmptyString(params.organizationId, "organizationId");
        }
        if (params.userId != null) {
            body.userId = Validators.requireNonEmptyString(params.userId, "userId");
        }
        return body;
    }

    private static Map<String, String> buildListWebhooksQuery(ListWebhooksParams params) {
        Map<String, String> query = new LinkedHashMap<>();
        if (params == null) {
            return query;
        }
        if (params.page != null) {
            Validators.requirePage(params.page, "page");
            query.put("page", Integer.toString(params.page));
        }
        if (params.limit != null) {
            Validators.requireLimit(params.limit, 1, 100, "limit");
            query.put("limit", Integer.toString(params.limit));
        }
        if (params.status != null) {
            requireWebhookStatus(params.status, "status");
            query.put("status", params.status);
        }
        if (params.organizationId != null) {
            query.put("organizationId", Validators.requireNonEmptyString(params.organizationId, "organizationId"));
        }
        if (params.userId != null) {
            query.put("userId", Validators.requireNonEmptyString(params.userId, "userId"));
        }
        return query;
    }

    private static Map<String, String> buildListDeliveriesQuery(ListWebhookDeliveriesParams params) {
        Map<String, String> query = new LinkedHashMap<>();
        if (params == null) {
            return query;
        }
        if (params.page != null) {
            Validators.requirePage(params.page, "page");
            query.put("page", Integer.toString(params.page));
        }
        if (params.limit != null) {
            Validators.requireLimit(params.limit, 1, 100, "limit");
            query.put("limit", Integer.toString(params.limit));
        }
        if (params.status != null && !params.status.isEmpty()) {
            requireDeliveryStatus(params.status, "status");
            query.put("status", params.status);
        }
        return query;
    }
}
