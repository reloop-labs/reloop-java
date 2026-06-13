package sh.reloop.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import sh.reloop.models.Models.CreateApiKeyParams;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiKeyServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createParamsSerializeName() throws Exception {
        String json = objectMapper.writeValueAsString(
            new CreateApiKeyParams("Production Key", true, true)
        );

        assertTrue(json.contains("\"name\":\"Production Key\""));
        assertTrue(json.contains("\"enabled\":true"));
        assertTrue(json.contains("\"rateLimitEnabled\":true"));
    }
}
