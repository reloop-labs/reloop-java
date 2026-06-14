package sh.reloop.models;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

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
        String name,
        Boolean enabled,
        Boolean rateLimitEnabled
    ) {}

    public record UpdateApiKeyParams(
        String name,
        Boolean enabled
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DNSRecord(
        String id,
        String recordType,
        String recordTypeName,
        String domain,
        String name,
        String value,
        String ttl,
        Integer priority,
        String verificationError,
        String purpose,
        String createdAt,
        String status,
        String updatedAt
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Domain(
        String object,
        String id,
        String domain,
        String status,
        Boolean userVerifiedDomain,
        Boolean systemVerified,
        String customReturnPath,
        String trackingSubdomain,
        Boolean isClickTrackingEnabled,
        Boolean isOpenTrackingEnabled,
        String tls,
        Boolean isTrackingDomain,
        Boolean isSendingEmailEnabled,
        Boolean isReceivingEmailEnabled,
        String verificationFailedReason,
        List<DNSRecord> dnsRecords,
        String lastVerifiedAt,
        String createdAt,
        String updatedAt,
        String event
    ) {}

    public record CreateDomainParams(
        String domain,
        @JsonProperty("custom_return_path") String customReturnPath,
        String tracking,
        @JsonProperty("click_tracking") Boolean clickTracking,
        @JsonProperty("open_tracking") Boolean openTracking,
        String tls,
        @JsonProperty("sending_email") Boolean sendingEmail,
        @JsonProperty("receiving_email") Boolean receivingEmail
    ) {}

    public record UpdateDomainParams(
        @JsonProperty("click_tracking") Boolean clickTracking,
        @JsonProperty("open_tracking") Boolean openTracking,
        @JsonProperty("sending_email") Boolean sendingEmail,
        @JsonProperty("receiving_email") Boolean receivingEmail,
        String tls
    ) {}

    public record ListDomainsParams(
        Integer page,
        Integer limit,
        String q,
        String status
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DomainListResponse(
        String object,
        List<Domain> domains,
        Integer total,
        Integer page,
        Integer limit,
        String event
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DomainStatusResponse(
        String id,
        String status,
        String event
    ) {}

    public record ForwardDNSParams(
        String email
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ForwardDNSResponse(
        Boolean success
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record DomainNameserversResponse(
        String object,
        String domainId,
        String domain,
        List<String> nameservers,
        String dnsProvider,
        String event
    ) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SendMailResponse(
        Boolean success,
        String messageId,
        String status,
        String timestamp,
        String id
    ) {}
}
