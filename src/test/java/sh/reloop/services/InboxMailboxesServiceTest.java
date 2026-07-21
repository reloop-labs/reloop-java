package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.InboxModels.CreateMailboxParams;
import sh.reloop.models.InboxModels.CreateMailboxResponse;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InboxMailboxesServiceTest {
    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"id\":\"mbx_1\",\"email\":\"user@example.com\",\"status\":\"active\"}");
            CreateMailboxParams params = new CreateMailboxParams();
            params.domainId = "dom_1";
            params.email = "user@example.com";
            CreateMailboxResponse res = server.client().inbox.mailboxes.create(params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/inbox/v1/mailboxes/create", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertTrue(server.last().body.contains("\"domainId\":\"dom_1\""));
            assertTrue(server.last().body.contains("\"email\":\"user@example.com\""));
            assertEquals("mbx_1", res.id);
        }
    }

    @Test
    void apiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(400, "{\"message\":\"invalid\"}");
            assertThrows(ReloopApiException.class, () -> server.client().inbox.mailboxes.get("mbx_missing"));
        }
    }

    @Test
    void validationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            ReloopValidationException err = assertThrows(
                    ReloopValidationException.class, () -> server.client().inbox.mailboxes.get("  "));
            assertEquals("id", err.getField());
            assertEquals(0, server.hits());
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(InboxMailboxesService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("create", "delete", "get", "list", "update"), methods);
    }
}
