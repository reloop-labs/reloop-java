package sh.reloop.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import sh.reloop.models.Models.CreateDomainParams;
import sh.reloop.models.Models.ForwardDNSParams;
import sh.reloop.models.Models.ListDomainsParams;
import sh.reloop.models.Models.UpdateDomainParams;
import sh.reloop.test.RecordingReloopClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class DomainServiceRouteTest {
    private RecordingReloopClient client;
    private DomainService service;

    @BeforeEach
    void setUp() {
        client = new RecordingReloopClient();
        service = new DomainService(client);
    }

    @Test
    void createUsesDomainCreateRoute() {
        service.create(
            new CreateDomainParams(
                "send.example.com",
                "inbound",
                null,
                true,
                null,
                "opportunistic",
                true,
                true
            )
        );

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/domain/v1/create", client.lastPath);
        assertInstanceOf(CreateDomainParams.class, client.lastBody);
    }

    @Test
    void listBuildsQueryPath() {
        service.list(new ListDomainsParams(2, 5, "example", "active"));

        assertEquals("GET", client.lastMethod);
        assertEquals("/api/domain/v1/list?page=2&limit=5&q=example&status=active", client.lastPath);
    }

    @Test
    void getNameserversUsesNameserversRoute() {
        service.getNameservers("dom_1");

        assertEquals("GET", client.lastMethod);
        assertEquals("/api/domain/v1/nameservers/dom_1", client.lastPath);
    }

    @Test
    void forwardDnsUsesForwardDnsRoute() {
        service.forwardDns("dom_1", new ForwardDNSParams("admin@example.com"));

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/domain/v1/verify/dom_1/forward-dns", client.lastPath);
        assertInstanceOf(ForwardDNSParams.class, client.lastBody);
    }

    @Test
    void verifyUsesVerifyRoute() {
        service.verify("dom_1");

        assertEquals("POST", client.lastMethod);
        assertEquals("/api/domain/v1/verify/dom_1", client.lastPath);
    }

    @Test
    void updateUsesPatchRoute() {
        service.update("dom_1", new UpdateDomainParams(false, true, true, null, "enforced"));

        assertEquals("PATCH", client.lastMethod);
        assertEquals("/api/domain/v1/dom_1", client.lastPath);
    }

    @Test
    void deleteUsesDeleteRoute() {
        service.delete("dom_1");

        assertEquals("DELETE", client.lastMethod);
        assertEquals("/api/domain/v1/dom_1", client.lastPath);
    }
}
