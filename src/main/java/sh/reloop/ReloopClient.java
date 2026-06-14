package sh.reloop;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import sh.reloop.services.ApiKeyService;
import sh.reloop.services.ContactsService;
import sh.reloop.services.DomainService;
import sh.reloop.services.MailService;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class ReloopClient {
    private final String apiKey;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    
    public final ApiKeyService apiKeys;
    public final ContactsService contacts;
    public final DomainService domain;
    public final MailService mail;

    public ReloopClient(String apiKey) {
        this(apiKey, "https://reloop.sh");
    }

    public ReloopClient(String apiKey, String baseUrl) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Reloop SDK requires an api_key.");
        }
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
                
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        
        this.apiKeys = new ApiKeyService(this);
        this.contacts = new ContactsService(this);
        this.domain = new DomainService(this);
        this.mail = new MailService(this);
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchMap(String method, String path, Object body) {
        return fetch(method, path, body, Map.class);
    }

    public <T> T fetch(String method, String path, Object body, Class<T> responseType) {
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(this.baseUrl + path))
                    .header("x-api-key", this.apiKey)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            if (body != null) {
                String jsonBody = objectMapper.writeValueAsString(body);
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpRequest request = requestBuilder.build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                throw new RuntimeException("Reloop API Error: " + response.statusCode() + " " + response.body());
            }

            if (response.statusCode() == 204 || responseType == Void.class) {
                return null;
            }

            return objectMapper.readValue(response.body(), responseType);
        } catch (Exception e) {
            throw new RuntimeException("Reloop Network Error", e);
        }
    }
}
