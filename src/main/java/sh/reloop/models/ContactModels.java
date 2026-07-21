package sh.reloop.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public final class ContactModels {
    private ContactModels() {}

    public static final class ContactStatus {
        public static final String SUBSCRIBED = "subscribed";
        public static final String UNSUBSCRIBED = "unsubscribed";
        public static final String BLOCKED = "blocked";

        private ContactStatus() {}
    }

    public static final class ChannelSubscription {
        public static final String OPT_IN = "opt_in";
        public static final String OPT_OUT = "opt_out";

        private ChannelSubscription() {}
    }

    public static final class ChannelVisibility {
        public static final String PRIVATE = "private";
        public static final String PUBLIC = "public";

        private ChannelVisibility() {}
    }

    public static final class PropertyType {
        public static final String STRING = "string";
        public static final String NUMBER = "number";

        private PropertyType() {}
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ContactChannelInput {
        public String channelId;
        public String subscription;

        public ContactChannelInput() {}

        public ContactChannelInput(String channelId, String subscription) {
            this.channelId = channelId;
            this.subscription = subscription;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactGroupRef {
        public String id;
        public String name;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactChannelRef {
        public String id;
        public String name;
        public String subscription;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Contact {
        public String object;
        public String id;
        public String email;
        public String firstName;
        public String lastName;
        public String status;
        public Map<String, Object> properties;
        public List<ContactGroupRef> groups;
        public List<ContactChannelRef> channels;
        public String suppressionReason;
        public String suppressedAt;
        public String createdAt;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactResponse extends Contact {
        public String event;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateContactParams {
        public String email;
        public String firstName;
        public String lastName;
        public String status;
        public Map<String, Object> properties;
        public List<String> groupIds;
        public List<ContactChannelInput> channels;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateContactParams {
        public String email;
        public String firstName;
        public String lastName;
        public String status;
        public Map<String, Object> properties;
    }

    public static class ListContactsParams {
        public Integer page;
        public Integer limit;
        public String search;
        public String status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactListResponse {
        public String object;
        public List<Contact> contacts;
        public int total;
        public int page;
        public int limit;
        public int totalContacts;
        public int subscribedContacts;
        public int unsubscribedContacts;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteContactResponse {
        public boolean success;
        public String object;
        public String id;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactProperty {
        public String object;
        public String id;
        public String propertyName;
        public String propertyType;
        public String defaultValue;
        public String createdAt;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactPropertyResponse extends ContactProperty {
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactPropertyListItem {
        public String id;
        public String propertyName;
        public String propertyType;
        public String defaultValue;
        public String createdAt;
        public String updatedAt;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreatePropertyParams {
        public String name;
        public String type;
        public String fallbackValue;
    }

    public static class UpdatePropertyParams {
        public String fallbackValue;
    }

    public static class ListPropertiesParams {
        public Integer page;
        public Integer limit;
        public String search;
        public String type;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class PropertyListResponse {
        public String object;
        public List<ContactPropertyListItem> properties;
        public int total;
        public int page;
        public int limit;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeletePropertyResponse {
        public String object;
        public boolean success;
        public String id;
        public String name;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactGroup {
        public String object;
        public String id;
        public String name;
        public String createdAt;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactGroupResponse extends ContactGroup {
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactGroupListItem {
        public String id;
        public String name;
        public String createdAt;
        public String updatedAt;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateGroupParams {
        public String name;

        public CreateGroupParams() {}

        public CreateGroupParams(String name) {
            this.name = name;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateGroupParams {
        public String name;

        public UpdateGroupParams() {}

        public UpdateGroupParams(String name) {
            this.name = name;
        }
    }

    public static class ListGroupsParams {
        public Integer page;
        public Integer limit;
        public String search;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupListResponse {
        public String object;
        public List<ContactGroupListItem> groups;
        public int total;
        public int page;
        public int limit;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteGroupResponse {
        public String object;
        public boolean success;
        public String id;
        public String name;
        public String event;
    }

    public static class ListGroupContactsParams {
        public Integer page;
        public Integer limit;
        public String search;
        public String status;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupContactItem {
        public String id;
        public String email;
        public String firstName;
        public String lastName;
        public String status;
        public Map<String, Object> properties;
        public String createdAt;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GroupContactListResponse {
        public String object;
        public ContactGroupRef group;
        public List<GroupContactItem> contacts;
        public int total;
        public int page;
        public int limit;
        public String event;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class GroupMembershipParams {
        @JsonProperty("contact_id")
        public String contactId;
        public String email;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddContactToGroupResponse {
        public boolean success;
        public String object;
        public String id;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RemoveContactFromGroupResponse {
        public boolean success;
        public String object;
        public String id;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactChannel {
        public String object;
        public String id;
        public String name;
        public String description;
        public String defaultSubscription;
        public String visibility;
        public String createdAt;
        public String updatedAt;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactChannelResponse extends ContactChannel {
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ContactChannelListItem {
        public String id;
        public String name;
        public String description;
        public String defaultSubscription;
        public String visibility;
        public String createdAt;
        public String updatedAt;
        public Integer subscriberCount;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class CreateChannelParams {
        public String name;
        public String description;
        public String defaultSubscription;
        public String visibility;
    }

    public static class UpdateChannelParams {
        public String name;
        public String description;
        public String visibility;
        public boolean descriptionPresent;
        public boolean descriptionClear;
    }

    public static class ListChannelsParams {
        public Integer page;
        public Integer limit;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ChannelListResponse {
        public String object;
        public List<ContactChannelListItem> channels;
        public int total;
        public int page;
        public int limit;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DeleteChannelResponse {
        public String object;
        public boolean success;
        public String id;
        public String name;
        public String event;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AddContactToChannelParams {
        @JsonProperty("contact_id")
        public String contactId;
        public String email;
        public String subscription;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UpdateContactChannelParams {
        @JsonProperty("contact_id")
        public String contactId;
        public String email;
        public String subscription;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AddContactToChannelResponse {
        public ContactResponse contact;
        public String subscriptionId;
        public String event;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class UpdateContactChannelResponse {
        public boolean success;
        public String status;
        public String event;
    }
}
