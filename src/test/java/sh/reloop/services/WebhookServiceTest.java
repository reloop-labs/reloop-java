package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.exceptions.WebhookSignatureException;
import sh.reloop.models.WebhookModels.CreateWebhookParams;
import sh.reloop.models.WebhookModels.TriggerWebhookParams;
import sh.reloop.models.WebhookModels.UpdateWebhookParams;
import sh.reloop.models.WebhookModels.VerifyWebhookParams;
import sh.reloop.models.WebhookModels.Webhook;
import sh.reloop.models.WebhookModels.WebhookEvent;
import sh.reloop.test.TestHttpServer;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebhookServiceTest {
    private static final String WEBHOOK_FIXTURE = """
            {"id":"wh_123456789","name":"Production webhook","url":"https://example.com/webhooks/reloop",\
            "secret":"whsec_test","status":"active","customHeaders":null,"rateLimitEnabled":false,\
            "maxRequestsPerMinute":60,"maxRetries":3,"retryBackoffMultiplier":2,"filteringOptions":null,\
            "lastTriggeredAt":null,"successCount":0,"failureCount":0,"consecutiveFailures":0,\
            "events":["domain.created"],"createdAt":"2026-01-01T00:00:00.000Z","updatedAt":"2026-01-01T00:00:00.000Z"}\
            """;

    private static final String SECRET = "whsec_test_secret";
    private static final String PAYLOAD = """
            {"id":"evt_123456789","event":"domain.created","payload":{"domainId":"dom_1"},"timestamp":1735689600}\
            """;

    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, WEBHOOK_FIXTURE);
            Webhook res = server.client().webhook.create(new CreateWebhookParams(
                    "Production webhook",
                    "https://example.com/webhooks/reloop",
                    List.of("domain.created")));
            assertEquals("POST", server.last().method);
            assertEquals("/api/webhook/v1/", server.last().path);
            assertTrue(server.last().body.contains("\"description\":\"Production webhook\""));
            assertEquals("wh_123456789", res.id);
        }
    }

    @Test
    void validationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            assertThrows(ReloopValidationException.class,
                    () -> server.client().webhook.get(""));
            assertThrows(ReloopValidationException.class,
                    () -> server.client().webhook.update("wh_1", new UpdateWebhookParams()));
            assertThrows(ReloopValidationException.class,
                    () -> server.client().webhook.trigger(new TriggerWebhookParams()));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void apiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(500, "{\"message\":\"boom\"}");
            assertThrows(ReloopApiException.class, () -> server.client().webhook.get("wh_1"));
        }
    }

    @Test
    void surfaceLock() {
        Set<String> instanceMethods = Arrays.stream(WebhookService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> !Modifier.isStatic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "create",
                "delete",
                "disable",
                "enable",
                "get",
                "list",
                "listDeliveries",
                "pause",
                "retryDelivery",
                "trigger",
                "update",
                "verify"), instanceMethods);

        Set<String> staticMethods = Arrays.stream(WebhookService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .filter(m -> Modifier.isStatic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("constructEvent"), staticMethods);
    }

    @Test
    void pauseEnableDisablePatchStatus() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, WEBHOOK_FIXTURE);
            var client = server.client().webhook;

            client.pause("wh_1");
            assertTrue(server.last().body.contains("\"status\":\"paused\""));

            client.enable("wh_1");
            assertTrue(server.last().body.contains("\"status\":\"active\""));

            client.disable("wh_1");
            assertTrue(server.last().body.contains("\"status\":\"disabled\""));
        }
    }

    @Test
    void verifyAcceptsValidSignature() throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String header = signPayload(SECRET, timestamp, PAYLOAD);

        VerifyWebhookParams params = new VerifyWebhookParams();
        params.payload = PAYLOAD.getBytes(StandardCharsets.UTF_8);
        params.headers = Map.of(WebhookVerify.WEBHOOK_SIGNATURE_HEADER, header);
        params.secret = SECRET;

        WebhookEvent event = WebhookVerify.verify(params);
        assertEquals("evt_123456789", event.id);
        assertEquals("domain.created", event.event);
        assertEquals("dom_1", event.payload.get("domainId"));
    }

    @Test
    void constructEventVerifiesSignature() throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String header = signPayload(SECRET, timestamp, PAYLOAD);

        WebhookEvent event = WebhookService.constructEvent(
                PAYLOAD.getBytes(StandardCharsets.UTF_8), header, SECRET, 300);
        assertEquals("domain.created", event.event);
    }

    @Test
    void verifyRejectsWrongSecret() throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String header = signPayload(SECRET, timestamp, PAYLOAD);

        VerifyWebhookParams params = new VerifyWebhookParams();
        params.payload = PAYLOAD.getBytes(StandardCharsets.UTF_8);
        params.headers = Map.of(WebhookVerify.WEBHOOK_SIGNATURE_HEADER, header);
        params.secret = "wrong_secret";

        assertThrows(WebhookSignatureException.class, () -> WebhookVerify.verify(params));
    }

    @Test
    void verifyRejectsExpiredTimestamp() throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond() - 600);
        String header = signPayload(SECRET, timestamp, PAYLOAD);

        VerifyWebhookParams params = new VerifyWebhookParams();
        params.payload = PAYLOAD.getBytes(StandardCharsets.UTF_8);
        params.headers = Map.of(WebhookVerify.WEBHOOK_SIGNATURE_HEADER, header);
        params.secret = SECRET;
        params.tolerance = 300;

        WebhookSignatureException err = assertThrows(
                WebhookSignatureException.class, () -> WebhookVerify.verify(params));
        assertTrue(err.getMessage().toLowerCase().contains("tolerance"));
    }

    @Test
    void instanceVerifyDelegates() throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String header = signPayload(SECRET, timestamp, PAYLOAD);

        VerifyWebhookParams params = new VerifyWebhookParams();
        params.payload = PAYLOAD.getBytes(StandardCharsets.UTF_8);
        params.headers = Map.of(WebhookVerify.WEBHOOK_SIGNATURE_HEADER, header);
        params.secret = SECRET;

        WebhookEvent event = new WebhookService(null).verify(params);
        assertNotNull(event.id);
    }

    private static final String PRODUCTION_PAYLOAD =
            "{\"id\":\"whev_01abc\",\"type\":\"email.received\",\"created_at\":\"2026-10-26T12:00:00.000Z\","
                    + "\"data\":{\"email_id\":\"in_1\",\"subject\":\"Hello\"}}";

    @Test
    @SuppressWarnings("deprecation")
    void verifyAcceptsProductionDelivery() throws Exception {
        assertEquals("Reloop-Signature", WebhookVerify.WEBHOOK_SIGNATURE_HEADER);
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String header = signPayload(SECRET, timestamp, PRODUCTION_PAYLOAD);

        VerifyWebhookParams params = new VerifyWebhookParams();
        params.payload = PRODUCTION_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        params.headers = Map.of(
                "reloop-signature", header,
                "reloop-timestamp", timestamp,
                "reloop-id", "whev_01abc");
        params.secret = SECRET;

        WebhookEvent event = WebhookVerify.verify(params);
        assertEquals("whev_01abc", event.id);
        assertEquals("email.received", event.type);
        assertEquals("2026-10-26T12:00:00.000Z", event.createdAt);
        assertEquals("Hello", event.data.get("subject"));
        assertEquals(event.type, event.event);
    }

    @Test
    void verifyAcceptsLegacyHeader() throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String header = signPayload(SECRET, timestamp, PRODUCTION_PAYLOAD);

        VerifyWebhookParams params = new VerifyWebhookParams(
                PRODUCTION_PAYLOAD.getBytes(StandardCharsets.UTF_8),
                Map.of("x-webhook-signature", header),
                SECRET);

        assertEquals("email.received", WebhookVerify.verify(params).type);
    }

    @Test
    void verifyRejectsTamperedProductionBody() throws Exception {
        String timestamp = Long.toString(Instant.now().getEpochSecond());
        String header = signPayload(SECRET, timestamp, PRODUCTION_PAYLOAD);
        byte[] tampered = PRODUCTION_PAYLOAD.replace("Hello", "Hacked").getBytes(StandardCharsets.UTF_8);

        assertThrows(WebhookSignatureException.class,
                () -> WebhookService.constructEvent(tampered, header, SECRET, 300));
    }

    @Test
    void verifyMissingHeaderNamesReloopSignature() {
        VerifyWebhookParams params = new VerifyWebhookParams(
                PRODUCTION_PAYLOAD.getBytes(StandardCharsets.UTF_8), Map.of(), SECRET);

        WebhookSignatureException err = assertThrows(
                WebhookSignatureException.class, () -> WebhookVerify.verify(params));
        assertTrue(err.getMessage().contains("Reloop-Signature"));
    }

    @Test
    @SuppressWarnings("deprecation")
    void apiErrorBodyReadsFixAndLegacyTip() throws Exception {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        sh.reloop.exceptions.ApiErrorBody withFix = mapper.readValue(
                "{\"message\":\"Domain not verified\",\"why\":\"No DKIM\",\"fix\":\"Verify the domain\"}",
                sh.reloop.exceptions.ApiErrorBody.class);
        assertEquals("Verify the domain", withFix.fix);
        assertEquals("Verify the domain", withFix.tip);

        sh.reloop.exceptions.ApiErrorBody withTip = mapper.readValue(
                "{\"message\":\"x\",\"tip\":\"Try again\"}", sh.reloop.exceptions.ApiErrorBody.class);
        assertEquals("Try again", withTip.fix);
    }

    private static String signPayload(String secret, String timestamp, String payload) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        mac.update((timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));
        String signature = HexFormat.of().formatHex(mac.doFinal());
        return "t=" + timestamp + ",v1=" + signature;
    }
}
