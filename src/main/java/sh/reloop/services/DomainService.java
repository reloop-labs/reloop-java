package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.models.DomainModels.CreateDomainParams;
import sh.reloop.models.DomainModels.Domain;
import sh.reloop.models.DomainModels.DomainListResponse;
import sh.reloop.models.DomainModels.DomainStatusResponse;
import sh.reloop.models.DomainModels.ListDomainsParams;
import sh.reloop.models.DomainModels.UpdateDomainParams;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.Map;

/** Manages sending/receiving domains. */
public class DomainService {
    private static final String DOMAIN_V1 = "/api/domain/v1";

    private final ReloopClient client;

    public DomainService(ReloopClient client) {
        this.client = client;
    }

    public Domain create(CreateDomainParams params) {
        String domain = Validators.requireNonEmptyString(params == null ? null : params.domain, "domain");
        CreateDomainParams body = new CreateDomainParams(domain);
        if (params != null) {
            body.clickTracking = params.clickTracking;
            body.openTracking = params.openTracking;
            body.tls = params.tls;
            body.sendingEmail = params.sendingEmail;
            body.receivingEmail = params.receivingEmail;
        }
        return client.request("POST", DOMAIN_V1 + "/create", body, Domain.class);
    }

    public DomainListResponse list(ListDomainsParams params) {
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
            if (params.q != null) {
                query.put("q", params.q);
            }
            if (params.status != null) {
                query.put("status", params.status);
            }
        }
        return client.request("GET", DOMAIN_V1 + "/list", null, query, DomainListResponse.class);
    }

    public Domain get(String domainId) {
        String id = Validators.requireNonEmptyString(domainId, "domainId");
        return client.request("GET", DOMAIN_V1 + "/" + id, null, Domain.class);
    }

    public Domain update(String domainId, UpdateDomainParams params) {
        String id = Validators.requireNonEmptyString(domainId, "domainId");
        return client.request("PATCH", DOMAIN_V1 + "/" + id, params == null ? Map.of() : params, Domain.class);
    }

    public void delete(String domainId) {
        String id = Validators.requireNonEmptyString(domainId, "domainId");
        client.request("DELETE", DOMAIN_V1 + "/" + id, null, Void.class);
    }

    public DomainStatusResponse verify(String domainId) {
        String id = Validators.requireNonEmptyString(domainId, "domainId");
        return client.request("POST", DOMAIN_V1 + "/verify/" + id, Map.of(), DomainStatusResponse.class);
    }
}
