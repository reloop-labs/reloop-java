package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.AddContactToChannelParams;
import sh.reloop.models.ContactModels.AddContactToChannelResponse;
import sh.reloop.models.ContactModels.ChannelListResponse;
import sh.reloop.models.ContactModels.ChannelSubscription;
import sh.reloop.models.ContactModels.ChannelVisibility;
import sh.reloop.models.ContactModels.ContactChannel;
import sh.reloop.models.ContactModels.ContactChannelResponse;
import sh.reloop.models.ContactModels.CreateChannelParams;
import sh.reloop.models.ContactModels.DeleteChannelResponse;
import sh.reloop.models.ContactModels.ListChannelsParams;
import sh.reloop.models.ContactModels.UpdateChannelParams;
import sh.reloop.models.ContactModels.UpdateContactChannelParams;
import sh.reloop.models.ContactModels.UpdateContactChannelResponse;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.Map;

/** Manages contact channels and channel subscriptions. */
public class ContactChannelsService {
    private static final String CHANNELS_BASE = "/api/contacts/v1/channels";
    private static final String CHANNEL_MEMBERSHIP = "/api/contacts/channel";

    private final ReloopClient client;

    public ContactChannelsService(ReloopClient client) {
        this.client = client;
    }

    public ContactChannelResponse create(CreateChannelParams params) {
        Map<String, Object> body = validateCreateParams(params);
        return client.request("POST", CHANNELS_BASE + "/create", body, ContactChannelResponse.class);
    }

    public ChannelListResponse list(ListChannelsParams params) {
        Map<String, String> query = validateListParams(params);
        return client.request("GET", CHANNELS_BASE + "/list", null, query, ChannelListResponse.class);
    }

    public ContactChannel get(String id) {
        String channelId = requireChannelId(id, "id");
        return client.request("GET", CHANNELS_BASE + "/" + channelId, null, ContactChannel.class);
    }

    public ContactChannelResponse update(String id, UpdateChannelParams params) {
        String channelId = requireChannelId(id, "id");
        Map<String, Object> body = validateUpdateParams(params);
        return client.request("PATCH", CHANNELS_BASE + "/" + channelId, body, ContactChannelResponse.class);
    }

    public DeleteChannelResponse delete(String id) {
        String channelId = requireChannelId(id, "id");
        return client.request("DELETE", CHANNELS_BASE + "/" + channelId, null, DeleteChannelResponse.class);
    }

    public AddContactToChannelResponse addContact(String id, AddContactToChannelParams params) {
        String channelId = requireChannelId(id, "id");
        Map<String, Object> body = validateAddContactParams(params);
        return client.request("POST", CHANNEL_MEMBERSHIP + "/" + channelId, body, AddContactToChannelResponse.class);
    }

    public UpdateContactChannelResponse updateSubscription(String id, UpdateContactChannelParams params) {
        String channelId = requireChannelId(id, "id");
        Map<String, Object> body = validateUpdateSubscriptionParams(params);
        return client.request("PATCH", CHANNEL_MEMBERSHIP + "/" + channelId, body, UpdateContactChannelResponse.class);
    }

    private static String requireChannelId(String id, String field) {
        try {
            return Validators.requireNonEmptyString(id, field);
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Channel " + field + " is required and must be a non-empty string.", field);
        }
    }

    private static String requireChannelName(String name, String field) {
        if (name == null) {
            throw new ReloopValidationException(
                    "Channel " + field + " is required and must be a string.", field);
        }
        String trimmed = name.trim();
        if (trimmed.codePointCount(0, trimmed.length()) < 1) {
            throw new ReloopValidationException(
                    "Channel " + field + " must be at least 1 character.", field);
        }
        if (trimmed.codePointCount(0, trimmed.length()) > 255) {
            throw new ReloopValidationException(
                    "Channel " + field + " must be at most 255 characters.", field);
        }
        return trimmed;
    }

    private static void requireOptionalDescription(String description, String field) {
        if (description != null && description.codePointCount(0, description.length()) > 1000) {
            throw new ReloopValidationException(
                    "Channel " + field + " must be at most 1000 characters.", field);
        }
    }

    private static void requireOptionalSubscription(String subscription, String field) {
        if (subscription != null
                && !ChannelSubscription.OPT_IN.equals(subscription)
                && !ChannelSubscription.OPT_OUT.equals(subscription)) {
            throw new ReloopValidationException(
                    "Channel " + field + " must be \"opt_in\" or \"opt_out\".", field);
        }
    }

    private static String requireSubscription(String subscription, String field) {
        requireOptionalSubscription(subscription, field);
        if (subscription == null) {
            throw new ReloopValidationException(
                    "Channel " + field + " is required and must be \"opt_in\" or \"opt_out\".", field);
        }
        return subscription;
    }

    private static void requireOptionalVisibility(String visibility, String field) {
        if (visibility != null
                && !ChannelVisibility.PRIVATE.equals(visibility)
                && !ChannelVisibility.PUBLIC.equals(visibility)) {
            throw new ReloopValidationException(
                    "Channel " + field + " must be \"private\" or \"public\".", field);
        }
    }

    private static Map<String, Object> validateMembershipParams(String contactId, String email) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (contactId != null) {
            try {
                body.put("contact_id", Validators.requireNonEmptyString(contactId, "contact_id"));
            } catch (ReloopValidationException e) {
                throw new ReloopValidationException(
                        "contact_id must be a non-empty string when provided.", "contact_id");
            }
        }
        if (email != null) {
            try {
                body.put("email", Validators.requireNonEmptyString(email, "email"));
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

    private static Map<String, Object> validateCreateParams(CreateChannelParams params) {
        if (params == null) {
            throw new ReloopValidationException("create params are required and must be an object.", "params");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", requireChannelName(params.name, "name"));

        if (params.description != null) {
            requireOptionalDescription(params.description, "description");
            body.put("description", params.description);
        }
        if (params.defaultSubscription != null) {
            requireOptionalSubscription(params.defaultSubscription, "defaultSubscription");
            body.put("defaultSubscription", params.defaultSubscription);
        }
        if (params.visibility != null) {
            requireOptionalVisibility(params.visibility, "visibility");
            body.put("visibility", params.visibility);
        }
        return body;
    }

    private static Map<String, Object> validateUpdateParams(UpdateChannelParams params) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (params == null) {
            throw new ReloopValidationException(
                    "update requires at least one of name, description, or visibility.", "params");
        }
        if (params.name != null) {
            body.put("name", requireChannelName(params.name, "name"));
        }
        if (params.descriptionPresent) {
            body.put("description", params.descriptionClear ? null : params.description);
        }
        if (params.visibility != null) {
            requireOptionalVisibility(params.visibility, "visibility");
            body.put("visibility", params.visibility);
        }
        if (body.isEmpty()) {
            throw new ReloopValidationException(
                    "update requires at least one of name, description, or visibility.", "params");
        }
        return body;
    }

    private static Map<String, Object> validateAddContactParams(AddContactToChannelParams params) {
        Map<String, Object> body = validateMembershipParams(
                params == null ? null : params.contactId,
                params == null ? null : params.email);
        if (params != null && params.subscription != null) {
            requireOptionalSubscription(params.subscription, "subscription");
            body.put("subscription", params.subscription);
        }
        return body;
    }

    private static Map<String, Object> validateUpdateSubscriptionParams(UpdateContactChannelParams params) {
        if (params == null) {
            throw new ReloopValidationException("Either contact_id or email is required.", "params");
        }
        Map<String, Object> body = validateMembershipParams(params.contactId, params.email);
        body.put("subscription", requireSubscription(params.subscription, "subscription"));
        return body;
    }

    private static Map<String, String> validateListParams(ListChannelsParams params) {
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
        return query;
    }
}
