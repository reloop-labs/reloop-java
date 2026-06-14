# Reloop Java SDK

## Before you send

You need two things:

1. **API key** — create one in your Reloop account
2. **Verified domain** — add and verify a sending domain; use it in the `from` address

For setup details and the full API reference, see [reloop.sh/docs](https://reloop.sh/docs).

## Send email

```xml
<dependency>
  <groupId>sh.reloop</groupId>
  <artifactId>reloop-java</artifactId>
  <version>1.8.0</version>
</dependency>
```

```java
import sh.reloop.ReloopClient;
import sh.reloop.models.Models.SendMailResponse;

import java.util.HashMap;
import java.util.Map;

ReloopClient reloop = new ReloopClient("rl_your_api_key_here");

Map<String, Object> params = new HashMap<>();
params.put("from", "Reloop <hello@your-verified-domain.com>");
params.put("to", "user@example.com");
params.put("subject", "Welcome to Reloop");
params.put("html", "<p>Thanks for signing up.</p>");
params.put("text", "Thanks for signing up.");

SendMailResponse result = reloop.mail.send(params);

System.out.println(result.messageId() + " " + result.id());
```

More examples and optional fields: [reloop.sh/docs](https://reloop.sh/docs)

## License

Licensed under the [Apache License 2.0](./LICENSE) with additional use restrictions from Reloop Labs (same as the [Reloop project](https://github.com/reloop-labs/reloop/blob/main/LICENSE)).
