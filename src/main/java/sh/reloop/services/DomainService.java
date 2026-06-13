package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.models.Models.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class DomainService {
    private final ReloopClient client;

    public DomainService(ReloopClient client) {
        this.client = client;
    }

    public Domain create(CreateDomainParams params) {
        return client.fetch("POST", "/api/domain/v1/create", params, Domain.class);
    }

    public DomainListResponse list(ListDomainsParams params) {
        String path = "/api/domain/v1/list";
        String query = buildListQuery(params);
        if (!query.isEmpty()) {
            path += "?" + query;
        }
        return client.fetch("GET", path, null, DomainListResponse.class);
    }

    public Domain get(String domainId) {
        return client.fetch("GET", "/api/domain/v1/" + domainId, null, Domain.class);
    }

    public DomainNameserversResponse getNameservers(String domainId) {
        return client.fetch(
            "GET",
            "/api/domain/v1/nameservers/" + domainId,
            null,
            DomainNameserversResponse.class
        );
    }

    public Domain update(String domainId, UpdateDomainParams params) {
        return client.fetch("PATCH", "/api/domain/v1/" + domainId, params, Domain.class);
    }

    public Domain delete(String domainId) {
        return client.fetch("DELETE", "/api/domain/v1/" + domainId, null, Domain.class);
    }

    public DomainStatusResponse verify(String domainId) {
        return client.fetch("POST", "/api/domain/v1/verify/" + domainId, null, DomainStatusResponse.class);
    }

    public ForwardDNSResponse forwardDns(String domainId, ForwardDNSParams params) {
        return client.fetch(
            "POST",
            "/api/domain/v1/verify/" + domainId + "/forward-dns",
            params,
            ForwardDNSResponse.class
        );
    }

    static String buildListQuery(ListDomainsParams params) {
        if (params == null) {
            return "";
        }

        StringBuilder query = new StringBuilder();
        if (params.page() != null) {
            query.append("page=").append(params.page()).append("&");
        }
        if (params.limit() != null) {
            query.append("limit=").append(params.limit()).append("&");
        }
        if (params.q() != null) {
            query.append("q=").append(URLEncoder.encode(params.q(), StandardCharsets.UTF_8)).append("&");
        }
        if (params.status() != null) {
            query.append("status=").append(URLEncoder.encode(params.status(), StandardCharsets.UTF_8)).append("&");
        }

        if (query.length() == 0) {
            return "";
        }

        query.setLength(query.length() - 1);
        return query.toString();
    }
}
