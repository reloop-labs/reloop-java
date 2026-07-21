package sh.reloop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class ApiKeyModels {
    private ApiKeyModels() {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class User {
        public String id;
        public String name;
        public String image;
        public String email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiKey {
        public String id;
        public String name;
        public String start;
        public String prefix;
        public String organizationId;
        public String userId;
        public Integer refillInterval;
        public Integer refillAmount;
        public String lastRefillAt;
        public boolean enabled;
        public boolean rateLimitEnabled;
        public int rateLimitTimeWindow;
        public int rateLimitMax;
        public int requestCount;
        public Integer remaining;
        public String lastRequest;
        public String expiresAt;
        public String createdAt;
        public String updatedAt;
        public String permissions;
        public String metadata;
        public User createdBy;
        public String object;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiKeyWithKey {
        public String id;
        public String name;
        public String key;
        public boolean enabled;
        public String createdAt;
        public String updatedAt;
        public String permissions;
        public String object;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ApiKeyListResponse {
        public String object;
        @JsonProperty("apiKeys")
        public List<ApiKey> apiKeys;
        public int total;
        public int page;
        public int limit;
        public String event;
    }

    public static class ApiKeyListParams {
        public Integer page;
        public Integer limit;
        public Boolean enabled;
        public String userId;
        public String q;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteApiKeyResponse {
        public String id;
        public String message;
        public String object;
        public String event;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateApiKeyParams {
        public String name;

        public CreateApiKeyParams() {}

        public CreateApiKeyParams(String name) {
            this.name = name;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateApiKeyParams {
        public String name;

        public UpdateApiKeyParams() {}

        public UpdateApiKeyParams(String name) {
            this.name = name;
        }
    }
}
