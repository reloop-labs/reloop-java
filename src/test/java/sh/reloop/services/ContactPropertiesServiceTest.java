package sh.reloop.services;

import org.junit.jupiter.api.Test;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.ContactPropertyResponse;
import sh.reloop.models.ContactModels.CreatePropertyParams;
import sh.reloop.models.ContactModels.PropertyType;
import sh.reloop.models.ContactModels.UpdatePropertyParams;
import sh.reloop.test.TestHttpServer;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContactPropertiesServiceTest {
    @Test
    void createHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact_property\",\"id\":\"prop_1\","
                    + "\"propertyName\":\"company\",\"propertyType\":\"string\",\"event\":\"property.created\"}");
            CreatePropertyParams params = new CreatePropertyParams();
            params.name = "company";
            params.type = PropertyType.STRING;

            ContactPropertyResponse res = server.client().contacts.properties.create(params);
            assertEquals("POST", server.last().method);
            assertEquals("/api/contacts/v1/properties/create", server.last().path);
            assertEquals("rl_test", server.last().apiKey);
            assertTrue(server.last().body.contains("\"name\":\"company\""));
            assertTrue(server.last().body.contains("\"type\":\"string\""));
            assertEquals("prop_1", res.id);
        }
    }

    @Test
    void createApiError() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(400, "{\"message\":\"invalid\"}");
            CreatePropertyParams params = new CreatePropertyParams();
            params.name = "company";
            params.type = PropertyType.STRING;
            assertThrows(
                    ReloopApiException.class, () -> server.client().contacts.properties.create(params));
        }
    }

    @Test
    void createValidationNoHttp() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            CreatePropertyParams params = new CreatePropertyParams();
            params.name = "";
            params.type = PropertyType.STRING;
            assertThrows(
                    ReloopValidationException.class, () -> server.client().contacts.properties.create(params));
            assertEquals(0, server.hits());
        }
    }

    @Test
    void listHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"object\":\"contact_property\",\"event\":\"properties.listed\"}");
            server.client().contacts.properties.list(null);
            assertEquals("GET", server.last().method);
            assertEquals("/api/contacts/v1/properties/list", server.last().path);
        }
    }

    @Test
    void updateHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"id\":\"prop_1\"}");
            UpdatePropertyParams params = new UpdatePropertyParams();
            params.fallbackValue = "default";
            server.client().contacts.properties.update("prop_1", params);
            assertEquals("PATCH", server.last().method);
            assertEquals("/api/contacts/v1/properties/prop_1", server.last().path);
            assertTrue(server.last().body.contains("\"fallbackValue\":\"default\""));
        }
    }

    @Test
    void deleteHappyPath() throws Exception {
        try (TestHttpServer server = new TestHttpServer()) {
            server.respond(200, "{\"success\":true,\"id\":\"prop_1\"}");
            server.client().contacts.properties.delete("prop_1");
            assertEquals("DELETE", server.last().method);
            assertEquals("/api/contacts/v1/properties/prop_1", server.last().path);
        }
    }

    @Test
    void surfaceLock() {
        Set<String> methods = Arrays.stream(ContactPropertiesService.class.getDeclaredMethods())
                .filter(m -> Modifier.isPublic(m.getModifiers()))
                .map(Method::getName)
                .collect(Collectors.toSet());
        assertEquals(Set.of("create", "delete", "list", "update"), methods);
    }
}
