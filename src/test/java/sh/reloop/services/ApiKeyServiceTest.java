package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ApiKeyModels.ApiKeyWithKey;
import sh.reloop.models.ApiKeyModels.CreateApiKeyParams;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyServiceTest {
    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"id\":\"key_1\",\"key\":\"rl_secret\",\"object\":\"api_key\"}");
            ApiKeyWithKey res = server.client().apiKey.create(new CreateApiKeyParams("prod"));
            assertEquals("POST", server.last().method);
            assertEquals("/api/api-key/v1/", server.last().path);
            assertTrue(server.last().body.contains("\"name\":\"prod\""));
            assertEquals("rl_secret", res.key);
        }
    }

    @Test
    void validationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            assertThrows(ReloopValidationException.class,
                    () -> server.client().apiKey.create(new CreateApiKeyParams("")));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void apiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(500, "{\"message\":\"boom\"}");
            assertThrows(ReloopApiException.class, () -> server.client().apiKey.get("key_1"));
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(ApiKeyService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("create", "list", "get", "update", "delete", "rotate", "enable", "disable"), methods);
    }
}
