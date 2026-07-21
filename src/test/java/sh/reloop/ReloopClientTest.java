package sh.reloop;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.test.TestHttpServer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReloopClientTest {
    @Test
    void requiresApiKey() {
        assertThrows(IllegalArgumentException.class, () -> new ReloopClient(null));
        assertThrows(IllegalArgumentException.class, () -> new ReloopClient("   "));
    }

    @Test
    void wiresServicesAndDefaultBaseUrl() {
        ReloopClient client = new ReloopClient("rl_test");
        assertEquals("https://reloop.sh", client.getBaseUrl());
        assertNotNull(client.apiKey);
        assertNotNull(client.mail);
        assertNotNull(client.domain);
        assertNotNull(client.contacts);
        assertNotNull(client.webhook);
        assertNotNull(client.inbox);
        assertEquals("2.0.0", Version.VERSION);
    }

    @Test
    void sendsAuthHeaderAndMapsApiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(401, "{\"message\":\"bad key\"}");
            ReloopClient client = server.client();
            ReloopApiException err = assertThrows(
                    ReloopApiException.class,
                    () -> client.request("GET", "/v1/test", null, Void.class));
            assertEquals(401, err.getStatus());
            assertEquals("bad key", err.getBody().message);
            assertEquals("rl_test", server.last().apiKey);
        }
    }

    @Test
    void networkErrorBecomesApiException() {
        ReloopClient client = new ReloopClient("rl_test", "http://127.0.0.1:1");
        ReloopApiException err = assertThrows(
                ReloopApiException.class,
                () -> client.request("GET", "/v1/test", null, Void.class));
        assertEquals(0, err.getStatus());
        assertTrue(err.getMessage().contains("network") || err.getCause() != null);
    }
}
