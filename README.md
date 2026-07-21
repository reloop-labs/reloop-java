# Reloop Java SDK

Official Java client for the [Reloop](https://reloop.sh) API.

## Before you send

1. **API key** — create one in your Reloop account
2. **Verified domain** — add and verify a sending domain; use it in the `from` address

Full API reference: [reloop.sh/docs](https://reloop.sh/docs)

## Install

```xml
<dependency>
  <groupId>sh.reloop</groupId>
  <artifactId>reloop-java</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Quick start

```java
import sh.reloop.ReloopClient;
import sh.reloop.exceptions.ReloopApiException;
import sh.reloop.exceptions.ReloopValidationException;
import sh.reloop.models.MailModels.SendMailParams;
import sh.reloop.models.MailModels.SendMailResponse;

public class Example {
    public static void main(String[] args) {
        ReloopClient client = new ReloopClient("rl_your_api_key_here");

        SendMailParams params = new SendMailParams();
        params.from = "Reloop <hello@your-verified-domain.com>";
        params.to = "user@example.com";
        params.subject = "Welcome to Reloop";
        params.html = "<p>Thanks for signing up.</p>";
        params.text = "Thanks for signing up.";

        try {
            SendMailResponse result = client.mail.send(params);
            System.out.println(result.messageId + " " + result.id);
        } catch (ReloopValidationException e) {
            System.err.println("invalid request (" + e.getField() + "): " + e.getMessage());
        } catch (ReloopApiException e) {
            System.err.println("API error " + e.getStatus() + ": " + e.getMessage());
        }
    }
}
```

## Services

```java
client.apiKey;    // create, list, get, update, delete, rotate, enable, disable
client.mail;      // send
client.domain;    // create, list, get, update, delete, verify
client.contacts;  // CRUD + .properties, .groups, .channels
client.webhook;   // CRUD, pause/enable/disable, trigger, deliveries + verify
client.inbox;     // .mailboxes, .messages, .threads
```

## Errors

| Kind | Type | When |
|------|------|------|
| Bad client args | `ReloopValidationException` | Invalid params — **no HTTP call** |
| HTTP / network | `ReloopApiException` | Non-2xx response or transport failure |
| Webhook HMAC | `WebhookSignatureException` | Local signature verification failed |

Idiomatic Java exceptions — the Java equivalent of Node/Python Result objects and Go `(T, error)`.

## Webhook verification

```java
import sh.reloop.services.WebhookVerify;

var event = WebhookVerify.constructEvent(payload, signature, secret, 300);
// or client.webhook.verify(params)
```

## License

Licensed under the [Apache License 2.0](./LICENSE) with additional use restrictions from Reloop Labs (same as the [Reloop project](https://github.com/reloop-labs/reloop/blob/main/LICENSE)).
