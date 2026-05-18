package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.models.Models.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class ApiKeyService {
    private final ReloopClient client;

    public ApiKeyService(ReloopClient client) {
        this.client = client;
    }

    public ApiKeyWithKey create(CreateApiKeyParams params) {
        return client.fetch("POST", "/api-key/v1/", params, ApiKeyWithKey.class);
    }

    public ApiKeyListResponse list(ApiKeyListParams params) {
        StringBuilder query = new StringBuilder();
        if (params != null) {
            if (params.page() != null) query.append("page=").append(params.page()).append("&");
            if (params.limit() != null) query.append("limit=").append(params.limit()).append("&");
            if (params.enabled() != null) query.append("enabled=").append(params.enabled()).append("&");
            if (params.userId() != null) query.append("userId=").append(URLEncoder.encode(params.userId(), StandardCharsets.UTF_8)).append("&");
            if (params.q() != null) query.append("q=").append(URLEncoder.encode(params.q(), StandardCharsets.UTF_8)).append("&");
        }
        
        String path = "/api-key/v1/";
        if (query.length() > 0) {
            path += "?" + query.toString();
        }
        
        return client.fetch("GET", path, null, ApiKeyListResponse.class);
    }

    public ApiKey get(String id) {
        return client.fetch("GET", "/api-key/v1/" + id, null, ApiKey.class);
    }

    public ApiKey update(String id, UpdateApiKeyParams params) {
        return client.fetch("PATCH", "/api-key/v1/" + id, params, ApiKey.class);
    }

    public DeleteApiKeyResponse delete(String id) {
        return client.fetch("DELETE", "/api-key/v1/" + id, null, DeleteApiKeyResponse.class);
    }

    public ApiKeyWithKey rotate(String id) {
        return client.fetch("POST", "/api-key/v1/rotate/" + id, null, ApiKeyWithKey.class);
    }

    public ApiKey enable(String id) {
        return client.fetch("POST", "/api-key/v1/enable/" + id, null, ApiKey.class);
    }

    public ApiKey disable(String id) {
        return client.fetch("POST", "/api-key/v1/disable/" + id, null, ApiKey.class);
    }
}
