package sh.reloop.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import sh.reloop.models.Models.CreateDomainParams;
import sh.reloop.models.Models.ListDomainsParams;
import sh.reloop.models.Models.UpdateDomainParams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createParamsSerializeWithSnakeCase() throws Exception {
        String json = objectMapper.writeValueAsString(
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

        assertTrue(json.contains("\"click_tracking\":true"));
        assertTrue(json.contains("\"custom_return_path\":\"inbound\""));
        assertFalse(json.contains("clickTracking"));
    }

    @Test
    void updateParamsSerializeWithSnakeCase() throws Exception {
        String json = objectMapper.writeValueAsString(
            new UpdateDomainParams(false, true, true, null, "enforced")
        );

        assertTrue(json.contains("\"click_tracking\":false"));
        assertTrue(json.contains("\"open_tracking\":true"));
        assertFalse(json.contains("clickTracking"));
    }

    @Test
    void buildListQueryEncodesFilters() {
        String query = DomainService.buildListQuery(
            new ListDomainsParams(2, 5, "example", "active")
        );

        assertEquals("page=2&limit=5&q=example&status=active", query);
    }

    @Test
    void buildListQueryReturnsEmptyForNullParams() {
        assertEquals("", DomainService.buildListQuery(null));
    }
}
