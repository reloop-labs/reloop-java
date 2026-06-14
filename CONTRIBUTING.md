# Contributing to the Reloop Java SDK

Maven artifact: **`sh.reloop:reloop-java`**.

**License:** [Apache License 2.0](./LICENSE) with additional use restrictions from Reloop Labs.

**API reference:** [reloop.sh/docs](https://reloop.sh/docs)

Port new endpoints from the [Node.js SDK](https://github.com/reloop-labs/reloop-node) reference.

---

## Development setup

```bash
git clone git@github.com:reloop-labs/reloop-java.git
cd reloop-java
mvn verify
```

Requires **JDK 17+**.

---

## Project layout

```
src/main/java/sh/reloop/
  ReloopClient.java
  services/             # MailService, DomainService, …
  models/Models.java    # Records with @JsonProperty
src/test/java/sh/reloop/
  services/             # Route tests (RecordingReloopClient)
pom.xml                 # version
```

---

## Conventions

| Topic | Rule |
|-------|------|
| Domain params | Records with `@JsonProperty("snake_case")` |
| Mail send | `Map<String, Object>` with snake_case keys |
| Contacts | `RequestParameters.forRequest()` for camelCase |
| Tests | `RecordingReloopClient`; assert method, path, body type |

---

## Pull request checklist

- [ ] `mvn verify` passes
- [ ] Version in `pom.xml` bumped only for releases

---

## Releasing

Version: **`pom.xml`** → `<version>`.

```bash
git commit -am "chore: release v1.9.0"
git push origin main
git tag v1.9.0
git push origin v1.9.0
```

[`.github/workflows/release.yml`](./.github/workflows/release.yml) attaches source zip + JARs to GitHub Releases.

Publish: [`.github/workflows/publish.yml`](./.github/workflows/publish.yml) (GitHub Packages).
