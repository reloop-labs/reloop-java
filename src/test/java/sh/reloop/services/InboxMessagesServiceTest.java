package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.InboxModels.SendEmailOrPendingResponse;
import sh.reloop.models.InboxModels.SendMessageParams;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InboxMessagesServiceTest {
    @Test
    void sendHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200,
                    "{\"success\":true,\"messageId\":\"msg_1\",\"status\":\"queued\",\"timestamp\":\"t\",\"id\":\"em_1\"}");
            SendMessageParams params = new SendMessageParams();
            params.mailboxId = "mbx_1";
            params.to = "user@example.com";
            params.subject = "Hello";
            params.html = "<p>Hi</p>";
            SendEmailOrPendingResponse res = server.client().inbox.messages.send(params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/inbox/v1/messages/send", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertTrue(server.last().body.contains("\"mailboxId\":\"mbx_1\""));
            assertTrue(server.last().body.contains("\"to\":\"user@example.com\""));
            assertTrue(server.last().body.contains("\"subject\":\"Hello\""));
            assertTrue(res.success);
            assertEquals("em_1", res.id);
        }
    }

    @Test
    void apiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(400, "{\"message\":\"invalid\"}");
            assertThrows(ReloopApiException.class, () -> server.client().inbox.messages.get("msg_missing"));
        }
    }

    @Test
    void validationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            SendMessageParams params = new SendMessageParams();
            params.mailboxId = "";
            params.to = "user@example.com";
            params.subject = "Hello";
            ReloopValidationException err = assertThrows(
                    ReloopValidationException.class, () -> server.client().inbox.messages.send(params));
            assertEquals("mailboxId", err.getField());
            assertEquals(0, server.hits());
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(InboxMessagesService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "batch", "cancelPending", "delete", "forward", "get", "getAttachment",
                "getRaw", "list", "listSent", "reply", "replyAll", "send", "setRead",
                "setStar", "update"), methods);
    }
}
