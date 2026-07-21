package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.DomainModels.CreateDomainParams;
import sh.reloop.models.DomainModels.Domain;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainServiceTest {
    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"id\":\"dom_1\",\"domain\":\"example.com\",\"object\":\"domain\"}");
            Domain res = server.client().domain.create(new CreateDomainParams("example.com"));
            assertEquals("POST", server.last().method);
            assertEquals("/api/domain/v1/create", server.last().path);
            assertTrue(server.last().body.contains("\"domain\":\"example.com\""));
            assertEquals("dom_1", res.id);
        }
    }

    @Test
    void validationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            assertThrows(ReloopValidationException.class, () -> server.client().domain.get("  "));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void apiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(404, "{\"message\":\"missing\"}");
            assertThrows(ReloopApiException.class, () -> server.client().domain.get("dom_missing"));
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(DomainService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("create", "list", "get", "update", "delete", "verify"), methods);
    }
}
