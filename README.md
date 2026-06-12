# Reloop Java SDK

Official Java client for the Reloop API.

## Install

```xml
<dependency>
  <groupId>sh.reloop</groupId>
  <artifactId>reloop-java</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Usage

```java
import sh.reloop.ReloopClient;
import java.util.*;

ReloopClient reloop = new ReloopClient("re_123456789");

Map<String, Object> params = new HashMap<>();
params.put("email", "john.doe@example.com");
params.put("first_name", "John");
params.put("last_name", "Doe");
params.put("unsubscribed", false);

Map<String, Object> contact = reloop.contacts.create(params);
```

## API Keys

```java
import sh.reloop.ReloopClient;
import sh.reloop.models.Models.*;

ReloopClient reloop = new ReloopClient("rl_123456789");

ApiKeyWithKey apiKey = reloop.apiKeys.create(
    new CreateApiKeyParams("Production key", true, true)
);

ApiKeyListResponse keys = reloop.apiKeys.list(
    new ApiKeyListParams(1, 10, null, null, null)
);
```

## Contacts

```java
reloop.contacts.get("cont_123456789");

reloop.contacts.list(Map.of("page", 1, "limit", 10));

reloop.contacts.groups.addContact(
    "grp_123456789",
    Map.of("contact_id", "cont_123456789")
);
```
