package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.RequestParameters;

import java.util.Map;

public class ContactGroupsService {
    private final ReloopClient client;

    public ContactGroupsService(ReloopClient client) {
        this.client = client;
    }

    public Map<String, Object> addContact(String groupId, Map<String, Object> parameters) {
        return client.fetchMap(
            "POST",
            "/api/contacts/group/" + groupId,
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> removeContact(String groupId, Map<String, Object> parameters) {
        return client.fetchMap(
            "DELETE",
            "/api/contacts/group/" + groupId,
            RequestParameters.forRequest(parameters)
        );
    }

    public Map<String, Object> listContacts(String groupId, Map<String, Object> options) {
        return client.fetchMap(
            "GET",
            "/api/contacts/v1/groups/" + groupId + "/contacts"
                + ContactsService.buildQuery(RequestParameters.forQuery(options)),
            null
        );
    }
}
