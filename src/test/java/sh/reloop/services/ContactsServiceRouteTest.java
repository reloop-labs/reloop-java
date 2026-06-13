package sh.reloop.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.reloop.test.RecordingReloopClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ContactsServiceRouteTest {
    private RecordingReloopClient client;
    private ContactsService service;

    @BeforeEach
    void setUp() {
        client = new RecordingReloopClient();
        service = new ContactsService(client);
    }

    @Test
    void createUsesContactsCreateRoute() {
        service.create(Map.of("email", "user@example.com", "first_name", "Ada"));

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/contacts/create", client.lastPath);
        assertInstanceOf(Map.class, client.lastBody);
    }

    @Test
    void getUsesRetrieveRoute() {
        service.get("con_1");

        assertEquals("GET", client.lastMethod);
        assertEquals("/api/contacts/retrieve/con_1", client.lastPath);
    }

    @Test
    void listWithGroupIdUsesGroupContactsRoute() {
        service.list(Map.of("groupId", "grp_1", "page", 1));

        assertEquals("GET", client.lastMethod);
        assertEquals("/api/contacts/v1/groups/grp_1/contacts?page=1", client.lastPath);
    }

    @Test
    void createPropertyUsesPropertiesCreateRoute() {
        service.createProperty(Map.of("property_name", "company", "property_type", "string"));

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/contacts/v1/properties/create", client.lastPath);
    }

    @Test
    void channelsAddContactUsesChannelRoute() {
        service.channels.addContact("ch_1", Map.of("contact_id", "con_1"));

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/contacts/channel/ch_1", client.lastPath);
    }
}
