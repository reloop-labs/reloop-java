package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.RequestParameters;

import java.util.Map;

public class ContactChannelsService {
    private final ReloopClient client;

    public ContactChannelsService(ReloopClient client) {
        this.client = client;
    }

    public Map<String, Object> create(Map<String, Object> parameters) {
        return client.fetchMap(
            "POST",
            "/api/contacts/v1/channels/create",
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> list(Map<String, Object> options) {
        return client.fetchMap(
            "GET",
            "/api/contacts/v1/channels/list" + ContactsService.buildQuery(RequestParameters.forQuery(options)),
            null
        );
    }

    public Map<String, Object> get(String channelId) {
        return client.fetchMap("GET", "/api/contacts/v1/channels/" + channelId, null);
    }

    public Map<String, Object> update(String channelId, Map<String, Object> parameters) {
        return client.fetchMap(
            "PATCH",
            "/api/contacts/v1/channels/" + channelId,
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> delete(String channelId) {
        return client.fetchMap("DELETE", "/api/contacts/v1/channels/" + channelId, null);
    }

    public Map<String, Object> addContact(String channelId, Map<String, Object> parameters) {
        return client.fetchMap(
            "POST",
            "/api/contacts/channel/" + channelId,
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> updateSubscription(String channelId, Map<String, Object> parameters) {
        return client.fetchMap(
            "PATCH",
            "/api/contacts/channel/" + channelId,
            RequestParameters.forRequest(parameters)
        );
    }
}
