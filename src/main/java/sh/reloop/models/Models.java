package sh.reloop.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public class Models {
    
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record User(
        String id,
        String name,
        String image,
        String email
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiKey(
        String id,
        String name,
        String start,
        String prefix,
        String organizationId,
        String userId,
        Integer refillInterval,
        Integer refillAmount,
        String lastRefillAt,
        Boolean enabled,
        Boolean rateLimitEnabled,
        Integer rateLimitTimeWindow,
        Integer rateLimitMax,
        Integer requestCount,
        Integer remaining,
        String lastRequest,
        String expiresAt,
        String createdAt,
        String updatedAt,
        String permissions,
        String metadata,
        User createdBy,
        String object,
        String event
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiKeyWithKey(
        String id,
        String name,
        String key,
        Boolean enabled,
        String createdAt,
        String updatedAt,
        String permissions,
        String object,
        String event
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ApiKeyListResponse(
        String object,
        List<ApiKey> apiKeys,
        Integer total,
        Integer page,
        Integer limit,
        String event
    ) {}

    public record ApiKeyListParams(
        Integer page,
        Integer limit,
        Boolean enabled,
        String userId,
        String q
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DeleteApiKeyResponse(
        String id,
        String message,
        String object,
        String event
    ) {}

    public record CreateApiKeyParams(
        String name
    ) {}

    public record UpdateApiKeyParams(
        String name
    ) {}
}
