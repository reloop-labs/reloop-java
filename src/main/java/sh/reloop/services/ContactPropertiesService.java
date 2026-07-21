package sh.reloop.services;

import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.ContactModels.ContactPropertyResponse;
import sh.reloop.models.ContactModels.CreatePropertyParams;
import sh.reloop.models.ContactModels.DeletePropertyResponse;
import sh.reloop.models.ContactModels.ListPropertiesParams;
import sh.reloop.models.ContactModels.PropertyListResponse;
import sh.reloop.models.ContactModels.PropertyType;
import sh.reloop.models.ContactModels.UpdatePropertyParams;
import sh.reloop.validation.Validators;

import java.util.LinkedHashMap;
import java.util.Map;

/** Manages contact property definitions. */
public class ContactPropertiesService {
    private static final String PROPERTIES_BASE = "/api/contacts/v1/properties";

    private final ReloopClient client;

    public ContactPropertiesService(ReloopClient client) {
        this.client = client;
    }

    public ContactPropertyResponse create(CreatePropertyParams params) {
        Map<String, Object> body = validateCreateParams(params);
        return client.request("POST", PROPERTIES_BASE + "/create", body, ContactPropertyResponse.class);
    }

    public PropertyListResponse list(ListPropertiesParams params) {
        Map<String, String> query = validateListParams(params);
        return client.request("GET", PROPERTIES_BASE + "/list", null, query, PropertyListResponse.class);
    }

    public ContactPropertyResponse update(String id, UpdatePropertyParams params) {
        String propertyId = requirePropertyId(id, "id");
        Map<String, Object> body = validateUpdateParams(params);
        return client.request("PATCH", PROPERTIES_BASE + "/" + propertyId, body, ContactPropertyResponse.class);
    }

    public DeletePropertyResponse delete(String id) {
        String propertyId = requirePropertyId(id, "id");
        return client.request("DELETE", PROPERTIES_BASE + "/" + propertyId, null, DeletePropertyResponse.class);
    }

    private static String requirePropertyId(String id, String field) {
        try {
            return Validators.requireNonEmptyString(id, field);
        } catch (ReloopValidationException e) {
            throw new ReloopValidationException(
                    "Property " + field + " is required and must be a non-empty string.", field);
        }
    }

    private static String requirePropertyName(String name, String field) {
        if (name == null) {
            throw new ReloopValidationException(
                    "Property " + field + " is required and must be a string.", field);
        }
        String trimmed = name.trim();
        if (trimmed.codePointCount(0, trimmed.length()) < 1) {
            throw new ReloopValidationException(
                    "Property " + field + " must be at least 1 character.", field);
        }
        if (trimmed.codePointCount(0, trimmed.length()) > 255) {
            throw new ReloopValidationException(
                    "Property " + field + " must be at most 255 characters.", field);
        }
        return trimmed;
    }

    private static String requirePropertyType(String type, String field) {
        if (type == null || (!PropertyType.STRING.equals(type) && !PropertyType.NUMBER.equals(type))) {
            throw new ReloopValidationException(
                    "Property " + field + " must be \"string\" or \"number\".", field);
        }
        return type;
    }

    private static Map<String, Object> validateCreateParams(CreatePropertyParams params) {
        if (params == null) {
            throw new ReloopValidationException("create params are required and must be an object.", "params");
        }
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("name", requirePropertyName(params.name, "name"));
        body.put("type", requirePropertyType(params.type, "type"));
        if (params.fallbackValue != null) {
            body.put("fallbackValue", params.fallbackValue);
        }
        return body;
    }

    private static Map<String, Object> validateUpdateParams(UpdatePropertyParams params) {
        Map<String, Object> body = new LinkedHashMap<>();
        if (params != null) {
            body.put("fallbackValue", params.fallbackValue);
        } else {
            body.put("fallbackValue", null);
        }
        return body;
    }

    private static Map<String, String> validateListParams(ListPropertiesParams params) {
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
        if (params.type != null) {
            requirePropertyType(params.type, "type");
            query.put("type", params.type);
        }
        return query;
    }
}
