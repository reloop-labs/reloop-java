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
import sh.reloop.services.ApiKeyService;

ReloopClient reloop = new ReloopClient("rl_123456789");

ApiKeyWithKey created = reloop.apiKeys.create(
    new CreateApiKeyParams("Production Key", true, true)
);

ApiKeyListResponse keys = reloop.apiKeys.list(
    new ApiKeyListParams(1, 10, true, null, null)
);

reloop.apiKeys.rotate("key_123456789");
reloop.apiKeys.pause("key_123456789");
reloop.apiKeys.enable("key_123456789");
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

## Domains

```java
import sh.reloop.models.Models.*;

Domain domain = reloop.domain.create(
    new CreateDomainParams(
        "send.example.com",
        "inbound",
        null,
        true,
        true,
        "opportunistic",
        true,
        true
    )
);

DomainListResponse domains = reloop.domain.list(
    new ListDomainsParams(1, 10, null, "active")
);

Domain one = reloop.domain.get("domain_123456789");

reloop.domain.update(
    "domain_123456789",
    new UpdateDomainParams(false, true, true, null, null)
);

DomainStatusResponse status = reloop.domain.verify("domain_123456789");

reloop.domain.forwardDns(
    "domain_123456789",
    new ForwardDNSParams("admin@example.com")
);

DomainNameserversResponse nameservers = reloop.domain.getNameservers("domain_123456789");

reloop.domain.delete("domain_123456789");
```
