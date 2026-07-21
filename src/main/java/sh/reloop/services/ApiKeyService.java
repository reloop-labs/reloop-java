package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.models.ApiKeyModels.ApiKey;
import sh.reloop.models.ApiKeyModels.ApiKeyListParams;
import sh.reloop.models.ApiKeyModels.ApiKeyListResponse;
import sh.reloop.models.ApiKeyModels.ApiKeyWithKey;
import sh.reloop.models.ApiKeyModels.CreateApiKeyParams;
import sh.reloop.models.ApiKeyModels.DeleteApiKeyResponse;
import sh.reloop.models.ApiKeyModels.UpdateApiKeyParams;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.Map;

/** Manages organization API keys. */
public class ApiKeyService {
    private static final String API_KEY_V1 = "/api/api-key/v1";

    private final ReloopClient client;

    public ApiKeyService(ReloopClient client) {
        this.client = client;
    }

    public ApiKeyWithKey create(CreateApiKeyParams params) {
        String name = Validators.requireApiKeyName(params == null ? null : params.name, "name");
        return client.request("POST", API_KEY_V1 + "/", Map.of("name", name), ApiKeyWithKey.class);
    }

    public ApiKeyListResponse list(ApiKeyListParams params) {
        Map<String, String> query = new LinkedHashMap<>();
        if (params != null) {
            if (params.page != null) {
                Validators.requirePage(params.page, "page");
                query.put("page", Integer.toString(params.page));
            }
            if (params.limit != null) {
                Validators.requireLimit(params.limit, 1, 100, "limit");
                query.put("limit", Integer.toString(params.limit));
            }
            if (params.enabled != null) {
                query.put("enabled", Boolean.toString(params.enabled));
            }
            if (params.userId != null) {
                query.put("userId", params.userId);
            }
            if (params.q != null) {
                query.put("q", params.q);
            }
        }
        return client.request("GET", API_KEY_V1 + "/", null, query, ApiKeyListResponse.class);
    }

    public ApiKey get(String apiKeyId) {
        String id = Validators.requireApiKeyId(apiKeyId, "apiKeyId");
        return client.request("GET", API_KEY_V1 + "/" + id, null, ApiKey.class);
    }

    public ApiKey update(String apiKeyId, UpdateApiKeyParams params) {
        String id = Validators.requireApiKeyId(apiKeyId, "apiKeyId");
        String name = Validators.requireApiKeyName(params == null ? null : params.name, "name");
        return client.request("PATCH", API_KEY_V1 + "/" + id, Map.of("name", name), ApiKey.class);
    }

    public DeleteApiKeyResponse delete(String apiKeyId) {
        String id = Validators.requireApiKeyId(apiKeyId, "apiKeyId");
        return client.request("DELETE", API_KEY_V1 + "/" + id, null, DeleteApiKeyResponse.class);
    }

    public ApiKeyWithKey rotate(String apiKeyId) {
        String id = Validators.requireApiKeyId(apiKeyId, "apiKeyId");
        return client.request("POST", API_KEY_V1 + "/rotate/" + id, Map.of(), ApiKeyWithKey.class);
    }

    public ApiKey enable(String apiKeyId) {
        String id = Validators.requireApiKeyId(apiKeyId, "apiKeyId");
        return client.request("POST", API_KEY_V1 + "/enable/" + id, Map.of(), ApiKey.class);
    }

    public ApiKey disable(String apiKeyId) {
        String id = Validators.requireApiKeyId(apiKeyId, "apiKeyId");
        return client.request("POST", API_KEY_V1 + "/disable/" + id, Map.of(), ApiKey.class);
    }
}
