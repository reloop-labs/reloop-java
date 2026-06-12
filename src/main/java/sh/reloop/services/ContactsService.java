package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.RequestParameters;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ContactsService {
    public final ContactGroupsService groups;
    public final ContactChannelsService channels;

    private final ReloopClient client;

    public ContactsService(ReloopClient client) {
        this.client = client;
        this.groups = new ContactGroupsService(client);
        this.channels = new ContactChannelsService(client);
    }

    public Map<String, Object> create(Map<String, Object> parameters) {
        return client.fetchMap("POST", "/api/contacts/create", RequestParameters.forRequest(parameters));
    }

    public Map<String, Object> get(String contactId) {
        return client.fetchMap("GET", "/api/contacts/retrieve/" + contactId, null);
    }

    public Map<String, Object> list(Map<String, Object> options) {
        Object groupId = options.get("group_id");
        if (groupId == null) {
            groupId = options.get("groupId");
        }

        if (groupId != null) {
            Map<String, Object> filtered = new HashMap<>(options);
            filtered.remove("group_id");
            filtered.remove("groupId");
            return groups.listContacts(String.valueOf(groupId), filtered);
        }

        return client.fetchMap("GET", "/api/contacts/list" + buildQuery(RequestParameters.forQuery(options)), null);
    }

    public Map<String, Object> update(String contactId, Map<String, Object> parameters) {
        return client.fetchMap(
            "PATCH",
            "/api/contacts/" + contactId,
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> delete(String contactId) {
        return client.fetchMap("DELETE", "/api/contacts/" + contactId, null);
    }

    public Map<String, Object> createProperty(Map<String, Object> parameters) {
        return client.fetchMap(
            "POST",
            "/api/contacts/v1/properties/create",
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> listProperties(Map<String, Object> options) {
        return client.fetchMap(
            "GET",
            "/api/contacts/v1/properties/list" + buildQuery(RequestParameters.forQuery(options)),
            null
        );
    }

    public Map<String, Object> updateProperty(String propertyId, Map<String, Object> parameters) {
        return client.fetchMap(
            "PATCH",
            "/api/contacts/v1/properties/" + propertyId,
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> deleteProperty(String propertyId) {
        return client.fetchMap("DELETE", "/api/contacts/v1/properties/" + propertyId, null);
    }

    public Map<String, Object> createGroup(Map<String, Object> parameters) {
        return client.fetchMap(
            "POST",
            "/api/contacts/v1/groups/create",
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> listGroups(Map<String, Object> options) {
        return client.fetchMap(
            "GET",
            "/api/contacts/v1/groups/list" + buildQuery(RequestParameters.forQuery(options)),
            null
        );
    }

    public Map<String, Object> getGroup(String groupId) {
        return client.fetchMap("GET", "/api/contacts/v1/groups/" + groupId, null);
    }

    public Map<String, Object> updateGroup(String groupId, Map<String, Object> parameters) {
        return client.fetchMap(
            "PATCH",
            "/api/contacts/v1/groups/" + groupId,
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> deleteGroup(String groupId) {
        return client.fetchMap("DELETE", "/api/contacts/v1/groups/" + groupId, null);
    }

    static String buildQuery(Map<String, Object> values) {
        if (values == null || values.isEmpty()) {
            return "";
        }

        StringBuilder query = new StringBuilder("?");
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            query.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                .append("=")
                .append(URLEncoder.encode(String.valueOf(entry.getValue()), StandardCharsets.UTF_8))
                .append("&");
        }

        return query.substring(0, query.length() - 1);
    }
}
