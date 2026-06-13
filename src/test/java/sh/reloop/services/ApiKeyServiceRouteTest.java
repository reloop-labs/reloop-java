package sh.reloop.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.reloop.models.Models.ApiKeyListParams;
import sh.reloop.models.Models.CreateApiKeyParams;
import sh.reloop.test.RecordingReloopClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApiKeyServiceRouteTest {
    private RecordingReloopClient client;
    private ApiKeyService service;

    @BeforeEach
    void setUp() {
        client = new RecordingReloopClient();
        service = new ApiKeyService(client);
    }

    @Test
    void createUsesApiKeyCreateRoute() {
        service.create(new CreateApiKeyParams("Production Key", true, true));

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/api-key/v1/", client.lastPath);
    }

    @Test
    void listBuildsQueryPath() {
        service.list(new ApiKeyListParams(2, 5, true, null, "prod"));

        assertEquals("GET", client.lastMethod);
        assertEquals("/api/api-key/v1/?page=2&limit=5&enabled=true&q=prod&", client.lastPath);
    }

    @Test
    void rotateUsesRotateRoute() {
        service.rotate("key_1");

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/api-key/v1/rotate/key_1", client.lastPath);
    }

    @Test
    void pauseUsesDisableRoute() {
        service.pause("key_1");

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/api-key/v1/disable/key_1", client.lastPath);
    }
}
