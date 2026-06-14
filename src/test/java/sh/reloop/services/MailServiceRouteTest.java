package sh.reloop.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.reloop.test.RecordingReloopClient;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class MailServiceRouteTest {
    private RecordingReloopClient client;
    private MailService service;

    @BeforeEach
    void setUp() {
        client = new RecordingReloopClient();
        service = new MailService(client);
    }

    @Test
    void sendUsesMailSendRoute() {
        Map<String, Object> params = new HashMap<>();
        params.put("from", "Reloop <hello@send.example.com>");
        params.put("to", "user@example.com");
        params.put("subject", "Welcome to Reloop");
        params.put("reply_to", "support@example.com");

        service.send(params);

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/mail/v1/send", client.lastPath);
        assertInstanceOf(Map.class, client.lastBody);
    }
}
