package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.MailModels.SendMailParams;
import sh.reloop.models.MailModels.SendMailResponse;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MailServiceTest {
    @Test
    void sendHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"messageId\":\"msg_1\",\"status\":\"queued\",\"timestamp\":\"t\",\"id\":\"em_1\"}");
            SendMailParams params = new SendMailParams();
            params.from = "a@example.com";
            params.to = "b@example.com";
            params.subject = "Hi";
            SendMailResponse res = server.client().mail.send(params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/mail/v1/send", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertTrue(server.last().body.contains("\"from\":\"a@example.com\""));
            assertTrue(res.success);
            assertEquals("em_1", res.id);
        }
    }

    @Test
    void sendApiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(400, "{\"message\":\"invalid\"}");
            SendMailParams params = new SendMailParams();
            params.from = "a@x.com";
            params.to = "b@x.com";
            params.subject = "Hi";
            assertThrows(ReloopApiException.class, () -> server.client().mail.send(params));
        }
    }

    @Test
    void sendValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            SendMailParams params = new SendMailParams();
            params.from = "";
            params.to = "b@x.com";
            params.subject = "Hi";
            ReloopValidationException err = assertThrows(
                    ReloopValidationException.class, () -> server.client().mail.send(params));
            assertEquals("from", err.getField());
            assertEquals(0, server.hits());
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(MailService.class.getDeclaredMethods())
                .filter(m -> java.lang.reflect.Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("send"), methods);
    }
}
