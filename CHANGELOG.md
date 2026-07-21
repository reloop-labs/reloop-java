# Changelog

## 2.0.0

Breaking rewrite for Node SDK parity (version aligned with Node/Python/Go `2.0.0`):

- Typed params/responses (Jackson POJOs) instead of untyped maps for mail/contacts
- `ReloopValidationException` for invalid client input (no HTTP); `ReloopApiException` for HTTP/network
- Facade: `client.apiKey` (was `apiKeys`), `mail`, `domain`, `contacts`, `webhook`, `inbox`
- Typed Contacts with nested `properties`, `groups`, `channels`
- Full Webhook CRUD + pause/enable/disable/trigger/deliveries + local HMAC `WebhookVerify`
- Full Inbox: `mailboxes`, `messages`, `threads`
- Removed: API key `pause`, domain `getNameservers` / `forwardDns`
- Paths aligned with Node (`/api/mail/v1/send`, `/api/api-key/v1/`, `/api/domain/v1/`, …)
- JUnit 5 + JDK `HttpServer` tests with surface locks

## 0.1.0

Initial thin client for mail, domain, API keys, and map-based contacts.
