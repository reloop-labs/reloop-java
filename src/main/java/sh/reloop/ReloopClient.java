package sh.reloop;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import sh.reloop.exceptions.ApiErrorBody;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.services.ApiKeyService;
import sh.reloop.services.ContactsService;
import sh.reloop.services.DomainService;
import sh.reloop.services.InboxService;
import sh.reloop.services.MailService;
import sh.reloop.services.WebhookService;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/** Reloop SDK entry point. */
public class ReloopClient {
    private static final String DEFAULT_BASE_URL = "https://reloop.sh";

    private final String apiKeyCredential;
    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public final ApiKeyService apiKey;
    public final ContactsService contacts;
    public final DomainService domain;
    public final MailService mail;
    public final WebhookService webhook;
    public final InboxService inbox;

    public ReloopClient(String apiKey) {
        this(apiKey, DEFAULT_BASE_URL);
    }

    public ReloopClient(String apiKey, String baseUrl) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IllegalArgumentException("Reloop SDK requires an APIKey");
        }
        this.apiKeyCredential = apiKey.trim();
        String trimmedBase = baseUrl == null ? DEFAULT_BASE_URL : baseUrl.trim();
        while (trimmedBase.endsWith("/")) {
            trimmedBase = trimmedBase.substring(0, trimmedBase.length() - 1);
        }
        this.baseUrl = trimmedBase.isEmpty() ? DEFAULT_BASE_URL : trimmedBase;

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();

        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        this.apiKey = new ApiKeyService(this);
        this.contacts = new ContactsService(this);
        this.domain = new DomainService(this);
        this.mail = new MailService(this);
        this.webhook = new WebhookService(this);
        this.inbox = new InboxService(this);
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public <T> T request(String method, String path, Object body, Map<String, String> query, Class<T> responseType) {
        try {
            String fullUrl = baseUrl + path;
            if (query != null && !query.isEmpty()) {
                String qs = query.entrySet().stream()
                        .filter(e -> e.getValue() != null)
                        .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                        .collect(Collectors.joining("&"));
                if (!qs.isEmpty()) {
                    fullUrl = fullUrl + (fullUrl.contains("?") ? "&" : "?") + qs;
                }
            }

            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofSeconds(30))
                    .header("x-api-key", this.apiKeyCredential)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json");

            if (body != null) {
                String jsonBody = objectMapper.writeValueAsString(body);
                requestBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else {
                requestBuilder.method(method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 400) {
                ApiErrorBody errBody = new ApiErrorBody();
                try {
                    if (response.body() != null && !response.body().isBlank()) {
                        errBody = objectMapper.readValue(response.body(), ApiErrorBody.class);
                    }
                } catch (Exception ignored) {
                    errBody.message = response.body();
                }
                throw new ReloopApiException(response.statusCode(), Integer.toString(response.statusCode()), errBody);
            }

            if (response.statusCode() == 204 || responseType == Void.class || responseType == void.class) {
                return null;
            }
            if (response.body() == null || response.body().isBlank()) {
                return null;
            }
            return objectMapper.readValue(response.body(), responseType);
        } catch (ReloopApiException e) {
            throw e;
        } catch (Exception e) {
            throw new ReloopApiException("Reloop network error: " + e.getMessage(), e);
        }
    }

    public <T> T request(String method, String path, Object body, Class<T> responseType) {
        return request(method, path, body, null, responseType);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
