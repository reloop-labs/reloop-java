package sh.reloop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public final class DomainModels {
    private DomainModels() {}

    public static final class DomainStatus {
        public static final String PENDING = "pending";
        public static final String VERIFYING = "verifying";
        public static final String ACTIVE = "active";
        public static final String SUSPENDED = "suspended";
        public static final String FAILED = "failed";

        private DomainStatus() {}
    }

    public static final class DomainTlsMode {
        public static final String OPPORTUNISTIC = "opportunistic";
        public static final String ENFORCED = "enforced";

        private DomainTlsMode() {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DnsRecord {
        public String id;
        public String recordType;
        public String recordTypeName;
        public String domain;
        public String name;
        public String value;
        public String ttl;
        public Integer priority;
        public String verificationError;
        public String purpose;
        public String createdAt;
        public String status;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Domain {
        public String object;
        public String id;
        public String domain;
        public String status;
        public boolean userVerifiedDomain;
        public boolean systemVerified;
        public String customReturnPath;
        public String trackingSubdomain;
        public boolean isClickTrackingEnabled;
        public boolean isOpenTrackingEnabled;
        public String tls;
        public boolean isTrackingDomain;
        public boolean isSendingEmailEnabled;
        public boolean isReceivingEmailEnabled;
        public String verificationFailedReason;
        public List<DnsRecord> dnsRecords;
        public String lastVerifiedAt;
        public String createdAt;
        public String updatedAt;
        public String event;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateDomainParams {
        public String domain;
        @JsonProperty("click_tracking")
        public Boolean clickTracking;
        @JsonProperty("open_tracking")
        public Boolean openTracking;
        public String tls;
        @JsonProperty("sending_email")
        public Boolean sendingEmail;
        @JsonProperty("receiving_email")
        public Boolean receivingEmail;

        public CreateDomainParams() {}

        public CreateDomainParams(String domain) {
            this.domain = domain;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateDomainParams {
        @JsonProperty("click_tracking")
        public Boolean clickTracking;
        @JsonProperty("open_tracking")
        public Boolean openTracking;
        @JsonProperty("sending_email")
        public Boolean sendingEmail;
        @JsonProperty("receiving_email")
        public Boolean receivingEmail;
        public String tls;
    }

    public static class ListDomainsParams {
        public Integer page;
        public Integer limit;
        public String q;
        public String status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DomainListResponse {
        public String object;
        public List<Domain> domains;
        public int total;
        public int page;
        public int limit;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DomainStatusResponse {
        public String id;
        public String status;
        public String event;
    }
}
