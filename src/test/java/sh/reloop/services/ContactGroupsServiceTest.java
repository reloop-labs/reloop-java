package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.ContactGroupResponse;
import sh.reloop.models.ContactModels.CreateGroupParams;
import sh.reloop.models.ContactModels.GroupMembershipParams;
import sh.reloop.models.ContactModels.UpdateGroupParams;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactGroupsServiceTest {
    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact_group\",\"id\":\"grp_1\",\"name\":\"VIP\","
                    + "\"event\":\"group.created\"}");
            ContactGroupResponse res = server.client().contacts.groups.create(new CreateGroupParams("VIP"));
            assertEquals("POST", server.last().method);
            assertEquals("/api/contacts/v1/groups/create", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertTrue(server.last().body.contains("\"name\":\"VIP\""));
            assertEquals("grp_1", res.id);
        }
    }

    @Test
    void createApiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(400, "{\"message\":\"invalid\"}");
            assertThrows(
                    ReloopApiException.class,
                    () -> server.client().contacts.groups.create(new CreateGroupParams("VIP")));
        }
    }

    @Test
    void createValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            assertThrows(
                    ReloopValidationException.class,
                    () -> server.client().contacts.groups.create(new CreateGroupParams("")));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void listHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact_group\"}");
            server.client().contacts.groups.list(null);
            assertEquals("GET", server.last().method);
            assertEquals("/api/contacts/v1/groups/list", server.last().path);
        }
    }

    @Test
    void getHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact_group\",\"id\":\"grp_1\"}");
            server.client().contacts.groups.get("grp_1");
            assertEquals("/api/contacts/v1/groups/grp_1", server.last().path);
        }
    }

    @Test
    void updateHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"id\":\"grp_1\"}");
            server.client().contacts.groups.update("grp_1", new UpdateGroupParams("New Name"));
            assertEquals("PATCH", server.last().method);
            assertEquals("/api/contacts/v1/groups/grp_1", server.last().path);
        }
    }

    @Test
    void deleteHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"id\":\"grp_1\"}");
            server.client().contacts.groups.delete("grp_1");
            assertEquals("DELETE", server.last().method);
            assertEquals("/api/contacts/v1/groups/grp_1", server.last().path);
        }
    }

    @Test
    void listContactsHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact_group\"}");
            server.client().contacts.groups.listContacts("grp_1", null);
            assertEquals("/api/contacts/v1/groups/grp_1/contacts", server.last().path);
        }
    }

    @Test
    void addContactHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"id\":\"con_1\"}");
            GroupMembershipParams params = new GroupMembershipParams();
            params.email = "a@x.com";
            server.client().contacts.groups.addContact("grp_1", params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/contacts/group/grp_1", server.last().path);
            assertTrue(server.last().body.contains("\"email\":\"a@x.com\""));
        }
    }

    @Test
    void removeContactHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"id\":\"con_1\"}");
            GroupMembershipParams params = new GroupMembershipParams();
            params.contactId = "con_1";
            server.client().contacts.groups.removeContact("grp_1", params);
            assertEquals("DELETE", server.last().method);
            assertEquals("/api/contacts/group/grp_1", server.last().path);
        }
    }

    @Test
    void addContactValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            assertThrows(
                    ReloopValidationException.class,
                    () -> server.client().contacts.groups.addContact("grp_1", new GroupMembershipParams()));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(ContactGroupsService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(
                Set.of("addContact", "create", "delete", "get", "list", "listContacts", "removeContact", "update"),
                methods);
    }
}
