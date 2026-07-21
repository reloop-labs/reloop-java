package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.AddContactToGroupResponse;
import sh.reloop.models.ContactModels.ContactGroup;
import sh.reloop.models.ContactModels.ContactGroupResponse;
import sh.reloop.models.ContactModels.ContactStatus;
import sh.reloop.models.ContactModels.CreateGroupParams;
import sh.reloop.models.ContactModels.DeleteGroupResponse;
import sh.reloop.models.ContactModels.GroupContactListResponse;
import sh.reloop.models.ContactModels.GroupListResponse;
import sh.reloop.models.ContactModels.GroupMembershipParams;
import sh.reloop.models.ContactModels.ListGroupContactsParams;
import sh.reloop.models.ContactModels.ListGroupsParams;
import sh.reloop.models.ContactModels.RemoveContactFromGroupResponse;
import sh.reloop.models.ContactModels.UpdateGroupParams;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.Map;

/** Manages contact groups and group membership. */
public class ContactGroupsService {
    private static final String GROUPS_BASE = "/api/contacts/v1/groups";
    private static final String GROUP_MEMBERSHIP = "/api/contacts/group";

    private final ReloopClient client;

    public ContactGroupsService(ReloopClient client) {
        this.client = client;
    }

    public ContactGroupResponse create(CreateGroupParams params) {
        String name = requireCreateGroupName(params == null ? null : params.name, "name");
        return client.request("POST", GROUPS_BASE + "/create", Map.of("name", name), ContactGroupResponse.class);
    }

    public GroupListResponse list(ListGroupsParams params) {
        Map<String, String> query = validateListParams(params);
        return client.request("GET", GROUPS_BASE + "/list", null, query, GroupListResponse.class);
    }

    public ContactGroup get(String id) {
        String groupId = requireGroupId(id, "id");
        return client.request("GET", GROUPS_BASE + "/" + groupId, null, ContactGroup.class);
    }

    public ContactGroupResponse update(String id, UpdateGroupParams params) {
        String groupId = requireGroupId(id, "id");
        String name = requireUpdateGroupName(params == null ? null : params.name, "name");
        return client.request("PATCH", GROUPS_BASE + "/" + groupId, Map.of("name", name), ContactGroupResponse.class);
    }

    public DeleteGroupResponse delete(String id) {
        String groupId = requireGroupId(id, "id");
        return client.request("DELETE", GROUPS_BASE + "/" + groupId, null, DeleteGroupResponse.class);
    }

    public GroupContactListResponse listContacts(String id, ListGroupContactsParams params) {
        String groupId = requireGroupId(id, "id");
        Map<String, String> query = validateListContactsParams(params);
        return client.request(
                "GET", GROUPS_BASE + "/" + groupId + "/contacts", null, query, GroupContactListResponse.class);
    }

    public AddContactToGroupResponse addContact(String id, GroupMembershipParams params) {
        String groupId = requireGroupId(id, "id");
        Map<String, Object> body = validateMembershipParams(params);
        return client.request("POST", GROUP_MEMBERSHIP + "/" + groupId, body, AddContactToGroupResponse.class);
    }

    public RemoveContactFromGroupResponse removeContact(String id, GroupMembershipParams params) {
        String groupId = requireGroupId(id, "id");
        Map<String, Object> body = validateMembershipParams(params);
        return client.request("DELETE", GROUP_MEMBERSHIP + "/" + groupId, body, RemoveContactFromGroupResponse.class);
    }

    private static String requireGroupId(String id, String field) {
        try {
            return Validators.requireNonEmptyString(id, field);
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Group " + field + " is required and must be a non-empty string.", field);
        }
    }

    private static String requireCreateGroupName(String name, String field) {
        if (name == null) {
            throw new ReloopValidationException(
                    "Group " + field + " is required and must be a string.", field);
        }
        String trimmed = name.trim();
        if (trimmed.codePointCount(0, trimmed.length()) < 1) {
            throw new ReloopValidationException(
                    "Group " + field + " must be at least 1 character.", field);
        }
        if (trimmed.codePointCount(0, trimmed.length()) > 50) {
            throw new ReloopValidationException(
                    "Group " + field + " must be at most 50 characters.", field);
        }
        return trimmed;
    }

    private static String requireUpdateGroupName(String name, String field) {
        if (name == null) {
            throw new ReloopValidationException(
                    "Group " + field + " is required and must be a string.", field);
        }
        String trimmed = name.trim();
        if (trimmed.codePointCount(0, trimmed.length()) < 1) {
            throw new ReloopValidationException(
                    "Group " + field + " must be at least 1 character.", field);
        }
        if (trimmed.codePointCount(0, trimmed.length()) > 255) {
            throw new ReloopValidationException(
                    "Group " + field + " must be at most 255 characters.", field);
        }
        return trimmed;
    }

    private static void requireListContactStatus(String status, String field) {
        if (status == null
                || (!ContactStatus.SUBSCRIBED.equals(status)
                && !ContactStatus.UNSUBSCRIBED.equals(status)
                && !ContactStatus.BLOCKED.equals(status))) {
            throw new ReloopValidationException(
                    "list " + field + " must be \"subscribed\", \"unsubscribed\", or \"blocked\".", field);
        }
    }

    private static Map<String, Object> validateMembershipParams(GroupMembershipParams params) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (params != null && params.contactId != null) {
            try {
                body.put("contact_id", Validators.requireNonEmptyString(params.contactId, "contact_id"));
            } catch (ReloopValidationException e) {
                throw new ReloopValidationException(
                        "contact_id must be a non-empty string when provided.", "contact_id");
            }
        }
        if (params != null && params.email != null) {
            try {
                body.put("email", Validators.requireNonEmptyString(params.email, "email"));
            } catch (ReloopValidationException e) {
                throw new ReloopValidationException(
                        "email must be a non-empty string when provided.", "email");
            }
        }
        if (body.isEmpty()) {
            throw new ReloopValidationException("Either contact_id or email is required.", "params");
        }
        return body;
    }

    private static Map<String, String> validateListParams(ListGroupsParams params) {
        Map<String, String> query = new LinkedHashMap<>();
        if (params == null) {
            return query;
        }
        if (params.page != null) {
            Validators.requirePage(params.page, "page");
            query.put("page", Integer.toString(params.page));
        }
        if (params.limit != null) {
            Validators.requireLimit(params.limit, 1, 100, "limit");
            query.put("limit", Integer.toString(params.limit));
        }
        if (params.search != null) {
            query.put("search", params.search);
        }
        return query;
    }

    private static Map<String, String> validateListContactsParams(ListGroupContactsParams params) {
        Map<String, String> query = new LinkedHashMap<>();
        if (params == null) {
            return query;
        }
        if (params.page != null) {
            Validators.requirePage(params.page, "page");
            query.put("page", Integer.toString(params.page));
        }
        if (params.limit != null) {
            Validators.requireLimit(params.limit, 1, 100, "limit");
            query.put("limit", Integer.toString(params.limit));
        }
        if (params.search != null) {
            query.put("search", params.search);
        }
        if (params.status != null) {
            requireListContactStatus(params.status, "status");
            query.put("status", params.status);
        }
        return query;
    }
}
