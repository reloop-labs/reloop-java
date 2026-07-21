package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.InboxModels.BatchThreadsParams;
import sh.reloop.models.InboxModels.ListThreadsParams;
import sh.reloop.models.InboxModels.Thread;
import sh.reloop.models.InboxModels.ThreadBatchAction;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InboxThreadsServiceTest {
    @Test
    void listHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200,
                    "[{\"id\":\"thr_1\",\"organizationId\":\"org_1\",\"lastMessageAt\":\"t\",\"status\":\"active\"}]");
            ListThreadsParams params = new ListThreadsParams();
            params.limit = 50;
            Thread[] res = server.client().inbox.threads.list(params);
            assertEquals("GET", server.last().method);
            assertEquals("/api/inbox/v1/threads", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertEquals("limit=50", server.last().query);
            assertEquals(1, res.length);
            assertEquals("thr_1", res[0].id);
        }
    }

    @Test
    void apiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(400, "{\"message\":\"invalid\"}");
            assertThrows(ReloopApiException.class, () -> server.client().inbox.threads.get("thr_missing"));
        }
    }

    @Test
    void validationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            BatchThreadsParams params = new BatchThreadsParams();
            params.ids = List.of();
            params.action = ThreadBatchAction.ARCHIVE;
            ReloopValidationException err = assertThrows(
                    ReloopValidationException.class, () -> server.client().inbox.threads.batch(params));
            assertEquals("ids", err.getField());
            assertEquals(0, server.hits());
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(InboxThreadsService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of(
                "archive", "batch", "delete", "get", "getAttachment", "list",
                "restore", "setRead", "setStar", "trash", "update"), methods);
    }
}
