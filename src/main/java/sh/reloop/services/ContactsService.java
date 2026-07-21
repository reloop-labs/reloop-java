package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.Contact;
import sh.reloop.models.ContactModels.ChannelSubscription;
import sh.reloop.models.ContactModels.ContactChannelInput;
import sh.reloop.models.ContactModels.ContactListResponse;
import sh.reloop.models.ContactModels.ContactResponse;
import sh.reloop.models.ContactModels.ContactStatus;
import sh.reloop.models.ContactModels.CreateContactParams;
import sh.reloop.models.ContactModels.DeleteContactResponse;
import sh.reloop.models.ContactModels.ListContactsParams;
import sh.reloop.models.ContactModels.UpdateContactParams;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/** Manages contacts and nested property/group/channel services. */
public class ContactsService {
    private static final String CONTACTS_BASE = "/api/contacts";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern CREATE_PROPERTY_KEY_PATTERN = Pattern.compile("^[a-z0-9_]+$");
    private static final Pattern UPDATE_PROPERTY_KEY_PATTERN = Pattern.compile("^[a-z_]+$");

    private final ReloopClient client;

    public final ContactPropertiesService properties;
    public final ContactGroupsService groups;
    public final ContactChannelsService channels;

    public ContactsService(ReloopClient client) {
        this.client = client;
        this.properties = new ContactPropertiesService(client);
        this.groups = new ContactGroupsService(client);
        this.channels = new ContactChannelsService(client);
    }

    public ContactResponse create(CreateContactParams params) {
        Map<String, Object> body = validateCreateParams(params);
        return client.request("POST", CONTACTS_BASE + "/create", body, ContactResponse.class);
    }

    public Contact get(String id) {
        String contactId = requireContactId(id, "id");
        return client.request("GET", CONTACTS_BASE + "/retrieve/" + contactId, null, Contact.class);
    }

    public ContactListResponse list(ListContactsParams params) {
        Map<String, String> query = validateListParams(params);
        return client.request("GET", CONTACTS_BASE + "/list", null, query, ContactListResponse.class);
    }

    public ContactResponse update(String id, UpdateContactParams params) {
        String contactId = requireContactId(id, "id");
        Map<String, Object> body = validateUpdateParams(params);
        return client.request("PATCH", CONTACTS_BASE + "/" + contactId, body, ContactResponse.class);
    }

    public DeleteContactResponse delete(String id) {
        String contactId = requireContactId(id, "id");
        return client.request("DELETE", CONTACTS_BASE + "/" + contactId, null, DeleteContactResponse.class);
    }

    private static String requireContactId(String id, String field) {
        try {
            return Validators.requireNonEmptyString(id, field);
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Contact " + field + " is required and must be a non-empty string.", field);
        }
    }

    private static String requireContactEmail(String email, String field) {
        if (email == null) {
            throw new ReloopValidationException(
                    "Contact " + field + " is required and must be a string.", field);
        }
        String trimmed = email.trim();
        if (trimmed.isEmpty()) {
            throw new ReloopValidationException(
                    "Contact " + field + " is required and must be a string.", field);
        }
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new ReloopValidationException(
                    "Contact " + field + " must be a valid email address.", field);
        }
        return trimmed;
    }

    private static void requireContactStatus(String status, String field) {
        if (status == null
                || (!ContactStatus.SUBSCRIBED.equals(status)
                && !ContactStatus.UNSUBSCRIBED.equals(status)
                && !ContactStatus.BLOCKED.equals(status))) {
            throw new ReloopValidationException(
                    field + " must be \"subscribed\", \"unsubscribed\", or \"blocked\".", field);
        }
    }

    private static Map<String, Object> validateProperties(
            Map<String, Object> properties, Pattern keyPattern, String field) {
        if (properties == null) {
            return null;
        }
        Map<String, Object> out = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            if (!keyPattern.matcher(entry.getKey()).matches()) {
                throw new ReloopValidationException(
                        field + " keys must match " + keyPattern.pattern() + ".", field);
            }
            Object value = entry.getValue();
            if (!(value instanceof String) && !(value instanceof Number)) {
                throw new ReloopValidationException(
                        field + "." + entry.getKey() + " must be a string or number.", field);
            }
            out.put(entry.getKey(), value);
        }
        return out;
    }

    private static List<String> validateGroupIds(List<String> groupIds) {
        if (groupIds == null) {
            return null;
        }
        return groupIds.stream()
                .map(id -> {
                    try {
                        return Validators.requireNonEmptyString(id, "groupIds");
                    } catch (ReloopValidationException e) {
                        throw new ReloopValidationException(
                                "groupIds must contain non-empty strings.", "groupIds");
                    }
                })
                .toList();
    }

    private static List<ContactChannelInput> validateChannels(List<ContactChannelInput> channels) {
        if (channels == null) {
            return null;
        }
        for (int i = 0; i < channels.size(); i++) {
            ContactChannelInput entry = channels.get(i);
            if (entry == null) {
                throw new ReloopValidationException("channels[" + i + "] must be an object.", "channels");
            }
            try {
                Validators.requireNonEmptyString(entry.channelId, "channels");
            } catch (ReloopValidationException e) {
                throw new ReloopValidationException(
                        "channels[" + i + "].channelId must be a non-empty string.", "channels");
            }
            if (entry.subscription == null
                    || (!ChannelSubscription.OPT_IN.equals(entry.subscription)
                    && !ChannelSubscription.OPT_OUT.equals(entry.subscription))) {
                throw new ReloopValidationException(
                        "channels[" + i + "].subscription must be \"opt_in\" or \"opt_out\".", "channels");
            }
        }
        return channels;
    }

    private static Map<String, Object> validateCreateParams(CreateContactParams params) {
        if (params == null) {
            throw new ReloopValidationException("create params are required and must be an object.", "params");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("email", requireContactEmail(params.email, "email"));

        if (params.firstName != null) {
            body.put("firstName", params.firstName);
        }
        if (params.lastName != null) {
            body.put("lastName", params.lastName);
        }
        if (params.status != null) {
            requireContactStatus(params.status, "status");
            body.put("status", params.status);
        }

        Map<String, Object> properties = validateProperties(
                params.properties, CREATE_PROPERTY_KEY_PATTERN, "properties");
        if (properties != null) {
            body.put("properties", properties);
        }

        List<String> groupIds = validateGroupIds(params.groupIds);
        if (groupIds != null) {
            body.put("groupIds", groupIds);
        }

        List<ContactChannelInput> channels = validateChannels(params.channels);
        if (channels != null) {
            body.put("channels", channels);
        }

        return body;
    }

    private static Map<String, Object> validateUpdateParams(UpdateContactParams params) {
        if (params == null) {
            throw new ReloopValidationException(
                    "update requires at least one of email, firstName, lastName, status, or properties.",
                    "params");
        }
        Map<String, Object> body = new LinkedHashMap<>();

        if (params.email != null) {
            body.put("email", requireContactEmail(params.email, "email"));
        }
        if (params.firstName != null) {
            body.put("firstName", params.firstName);
        }
        if (params.lastName != null) {
            body.put("lastName", params.lastName);
        }
        if (params.status != null) {
            requireContactStatus(params.status, "status");
            body.put("status", params.status);
        }

        Map<String, Object> properties = validateProperties(
                params.properties, UPDATE_PROPERTY_KEY_PATTERN, "properties");
        if (properties != null) {
            body.put("properties", properties);
        }

        if (body.isEmpty()) {
            throw new ReloopValidationException(
                    "update requires at least one of email, firstName, lastName, status, or properties.",
                    "params");
        }
        return body;
    }

    private static Map<String, String> validateListParams(ListContactsParams params) {
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
            requireContactStatus(params.status, "status");
            query.put("status", params.status);
        }
        return query;
    }
}
