package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.AddContactToChannelParams;
import sh.reloop.models.ContactModels.ChannelSubscription;
import sh.reloop.models.ContactModels.ContactChannelResponse;
import sh.reloop.models.ContactModels.CreateChannelParams;
import sh.reloop.models.ContactModels.UpdateChannelParams;
import sh.reloop.models.ContactModels.UpdateContactChannelParams;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactChannelsServiceTest {
    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"channel\",\"id\":\"chn_1\",\"name\":\"Newsletter\","
                    + "\"event\":\"channel.created\"}");
            CreateChannelParams params = new CreateChannelParams();
            params.name = "Newsletter";

            ContactChannelResponse res = server.client().contacts.channels.create(params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/contacts/v1/channels/create", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertTrue(server.last().body.contains("\"name\":\"Newsletter\""));
            assertEquals("chn_1", res.id);
        }
    }

    @Test
    void createApiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(400, "{\"message\":\"invalid\"}");
            CreateChannelParams params = new CreateChannelParams();
            params.name = "Newsletter";
            assertThrows(
                    ReloopApiException.class, () -> server.client().contacts.channels.create(params));
        }
    }

    @Test
    void createValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            CreateChannelParams params = new CreateChannelParams();
            params.name = "";
            assertThrows(
                    ReloopValidationException.class, () -> server.client().contacts.channels.create(params));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void listHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"channel\"}");
            server.client().contacts.channels.list(null);
            assertEquals("GET", server.last().method);
            assertEquals("/api/contacts/v1/channels/list", server.last().path);
        }
    }

    @Test
    void getHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"channel\",\"id\":\"chn_1\"}");
            server.client().contacts.channels.get("chn_1");
            assertEquals("/api/contacts/v1/channels/chn_1", server.last().path);
        }
    }

    @Test
    void updateHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"id\":\"chn_1\"}");
            UpdateChannelParams params = new UpdateChannelParams();
            params.name = "Updated";
            server.client().contacts.channels.update("chn_1", params);
            assertEquals("PATCH", server.last().method);
            assertEquals("/api/contacts/v1/channels/chn_1", server.last().path);
        }
    }

    @Test
    void deleteHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"id\":\"chn_1\"}");
            server.client().contacts.channels.delete("chn_1");
            assertEquals("DELETE", server.last().method);
            assertEquals("/api/contacts/v1/channels/chn_1", server.last().path);
        }
    }

    @Test
    void addContactHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"subscriptionId\":\"sub_1\"}");
            AddContactToChannelParams params = new AddContactToChannelParams();
            params.email = "a@x.com";
            params.subscription = ChannelSubscription.OPT_IN;
            server.client().contacts.channels.addContact("chn_1", params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/contacts/channel/chn_1", server.last().path);
            assertTrue(server.last().body.contains("\"email\":\"a@x.com\""));
        }
    }

    @Test
    void updateSubscriptionHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"status\":\"enrolled\"}");
            UpdateContactChannelParams params = new UpdateContactChannelParams();
            params.email = "a@x.com";
            params.subscription = ChannelSubscription.OPT_OUT;
            server.client().contacts.channels.updateSubscription("chn_1", params);
            assertEquals("PATCH", server.last().method);
            assertEquals("/api/contacts/channel/chn_1", server.last().path);
        }
    }

    @Test
    void updateSubscriptionValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            UpdateContactChannelParams params = new UpdateContactChannelParams();
            params.subscription = ChannelSubscription.OPT_IN;
            assertThrows(
                    ReloopValidationException.class,
                    () -> server.client().contacts.channels.updateSubscription("chn_1", params));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(ContactChannelsService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(
                Set.of("addContact", "create", "delete", "get", "list", "update", "updateSubscription"),
                methods);
    }
}
