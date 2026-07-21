package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.ChannelSubscription;
import sh.reloop.models.ContactModels.Contact;
import sh.reloop.models.ContactModels.ContactChannelInput;
import sh.reloop.models.ContactModels.ContactResponse;
import sh.reloop.models.ContactModels.ContactStatus;
import sh.reloop.models.ContactModels.CreateContactParams;
import sh.reloop.models.ContactModels.DeleteContactResponse;
import sh.reloop.models.ContactModels.ListContactsParams;
import sh.reloop.models.ContactModels.UpdateContactParams;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactsServiceTest {
    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact\",\"id\":\"con_1\",\"email\":\"john@example.com\","
                    + "\"status\":\"subscribed\",\"event\":\"contact.created\"}");
            CreateContactParams params = new CreateContactParams();
            params.email = "john@example.com";
            params.firstName = "John";
            params.lastName = "Doe";
            params.status = ContactStatus.SUBSCRIBED;
            params.properties = Map.of("company", "Reloop");
            params.groupIds = List.of("grp_1");
            params.channels = List.of(new ContactChannelInput("chn_1", ChannelSubscription.OPT_IN));

            ContactResponse res = server.client().contacts.create(params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/contacts/create", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertTrue(server.last().body.contains("\"email\":\"john@example.com\""));
            assertTrue(server.last().body.contains("\"firstName\":\"John\""));
            assertEquals("con_1", res.id);
        }
    }

    @Test
    void createApiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(409, "{\"message\":\"exists\"}");
            CreateContactParams params = new CreateContactParams();
            params.email = "john@example.com";
            assertThrows(ReloopApiException.class, () -> server.client().contacts.create(params));
        }
    }

    @Test
    void createValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            CreateContactParams params = new CreateContactParams();
            params.email = "not-an-email";
            assertThrows(ReloopValidationException.class, () -> server.client().contacts.create(params));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void getHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact\",\"id\":\"con_1\",\"email\":\"a@x.com\","
                    + "\"status\":\"subscribed\"}");
            Contact res = server.client().contacts.get("con_1");
            assertEquals("GET", server.last().method);
            assertEquals("/api/contacts/retrieve/con_1", server.last().path);
            assertEquals("con_1", res.id);
        }
    }

    @Test
    void listHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact\",\"event\":\"contacts.listed\"}");
            ListContactsParams params = new ListContactsParams();
            params.page = 2;
            params.limit = 10;
            params.search = "john";
            params.status = ContactStatus.SUBSCRIBED;
            server.client().contacts.list(params);
            assertEquals("GET", server.last().method);
            assertEquals("/api/contacts/list", server.last().path);
            assertTrue(server.last().query != null && !server.last().query.isEmpty());
        }
    }

    @Test
    void updateHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"id\":\"con_1\",\"email\":\"new@x.com\",\"event\":\"contact.updated\"}");
            UpdateContactParams params = new UpdateContactParams();
            params.email = "new@x.com";
            server.client().contacts.update("con_1", params);
            assertEquals("PATCH", server.last().method);
            assertEquals("/api/contacts/con_1", server.last().path);
            assertTrue(server.last().body.contains("\"email\":\"new@x.com\""));
        }
    }

    @Test
    void updateValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            assertThrows(
                    ReloopValidationException.class,
                    () -> server.client().contacts.update("con_1", new UpdateContactParams()));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void deleteHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"object\":\"contact\",\"id\":\"con_1\"}");
            DeleteContactResponse res = server.client().contacts.delete("con_1");
            assertEquals("DELETE", server.last().method);
            assertEquals("/api/contacts/con_1", server.last().path);
            assertTrue(res.success);
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(ContactsService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("create", "delete", "get", "list", "update"), methods);
    }
}
