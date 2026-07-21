# Contributing to the Reloop Java SDK

Maven artifact: **`sh.reloop:reloop-java`** (version aligned with Node/Python/Go at **2.0.0**).

**License:** [Apache License 2.0](./LICENSE) with additional use restrictions from Reloop Labs.

**API reference:** [reloop.sh/docs](https://reloop.sh/docs)

Port new endpoints from the [Node.js SDK](https://github.com/reloop-labs/reloop-node) — Node wins on paths, bodies, and validation messages.

---

## Development setup

```bash
git clone git@github.com:reloop-labs/reloop-java.git
cd reloop-java
mvn test
```

Requires **JDK 17+**. Runtime deps: Jackson only (plus JDK `HttpClient`).

---

## Project layout

```
src/main/java/sh/reloop/
  ReloopClient.java
  Version.java
  exceptions/          # ReloopValidationException, ReloopApiException, WebhookSignatureException
  validation/          # Validators
  models/              # Mail, ApiKey, Domain, Contact, Webhook, Inbox POJOs
  services/            # Mail, ApiKey, Domain, Contacts*, Webhook*, Inbox*
src/test/java/sh/reloop/
  test/TestHttpServer.java
  *Test.java           # HttpServer route + surface-lock tests
pom.xml                # version 2.0.0
```

---

## Conventions

| Topic | Rule |
|-------|------|
| Errors | `ReloopValidationException` (no HTTP) / `ReloopApiException` (HTTP/network) |
| Wire JSON | Match Node (`@JsonProperty` for snake_case vs camelCase) |
| Validation | Fail before HTTP; tests assert **0** requests |
| Tests | JDK `HttpServer`: method, path, `x-api-key`, body; surface-lock method sets |
| Endpoints | Do not invent — copy from Node `paths.ts` |
| Version | Same semver as Node/Python/Go |

---

## Pull request checklist

- [ ] `mvn test` passes
- [ ] New ops have happy path + API error + validation (0 HTTP) + surface test
- [ ] `pom.xml` / `Version.VERSION` updated only when releasing

---

## Releasing

1. Set `pom.xml` `<version>` and `Version.VERSION` to the same value (match other SDKs)
2. Tag and publish via existing GitHub Actions / Central publishing plugins
