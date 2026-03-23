# CLAUDE.md — base

## Codebase Overview

`base` is a **Java 25 multi-module Maven library** (Workday, Inc., Apache 2.0). It is organized into 23 JPMS modules covering configuration, marshalling, JSON transport, reactive flow, composite traversal, retryable operations, telemetry, CLI parsing, I/O, networking, and more. All modules share the `build.base.*` package namespace.

**Stack:** Java 25, Maven, JPMS (module system), Jackson Core (JSON), Jakarta EL (expression language), AssertJ + JUnit 5 Jupiter (tests), `jtar` (archiving).
**Build:** `./mvnw clean install` | Publishes to Workday Artifactory via Jenkins (`jenkins/release/Jenkinsfile`).

**Structure:**
- `base-foundation` — root utilities (Lazy, Capture, Exceptional, Streamable, iterators, matching DSL, etc.)
- `base-configuration` / `base-option` / `base-expression` / `base-commandline` — type-safe config + CLI
- `base-marshalling` / `base-transport` / `base-transport-json` — annotation-driven serialization to JSON
- `base-flow` — custom reactive Publish/Subscribe
- `base-query` / `base-mereology` — object indexing + part-whole hierarchy traversal
- `base-retryable` — retryable supplier with configurable back-off
- `base-telemetry` / `base-telemetry-foundation` / `base-telemetry-ansi` — observability
- `base-io` / `base-network` / `base-archiving` / `base-parsing` / `base-naming` / `base-logging` / `base-table` / `base-assertion` — supporting utilities

For detailed architecture, see [docs/CODEBASE_MAP.md](docs/CODEBASE_MAP.md).

## Key Conventions

- `-parameters` compiler flag is **required** — marshalling depends on parameter names at runtime.
- Configuration: use `ConfigurationBuilder.add(option).build()` → `config.get(MyOption.class)`.
- Marshalling: annotate with `@Marshal` / `@Unmarshal`; call `Marshalling.register(Class, lookup())` in static initializer.
- Retryable: throw `EphemeralFailureException` (retry) or `PermanentFailureException` (stop).
- All JPMS `module-info.java` files are `open` modules to allow marshalling/reflection.
