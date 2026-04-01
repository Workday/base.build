# base Library — Architecture Analysis & Enhancement Roadmap

> Analysis date: 2026-02-18

## Table of Contents

- [What This Library Is](#what-this-library-is)
- [Design Philosophy](#design-philosophy)
- [New Modules Worth Adding](#new-modules-worth-adding)
- [Major Enhancements to Existing Modules](#major-enhancements-to-existing-modules)
- [Latent Issues to Fix](#latent-issues-to-fix)
- [Unused POM Properties](#unused-pom-properties)
- [Priority Ranking](#priority-ranking)

---

## What This Library Is

A **Java 25, Virtual-Threads-first, blocking-I/O, immutable-by-default** foundation library with two orthogonal error models (`Exceptional<T>` monadic + `Ephemeral/PermanentFailureException` taxonomy), a heterogeneous-container configuration system, annotation-driven marshalling, and a custom reactive pub/sub. It is opinionated and internally consistent — with a few notable tensions.

---

## Design Philosophy

### Immutability Stance: Disciplined and Pervasive

The library is built on the belief that immutability should be the default. `Exceptional<T>` is `final` and value-based. `Configuration` is explicitly documented as immutable. `Option` is documented as "strictly immutable". `TelemetryRecorder`'s `Location` is "opaque immutable". The only significant exceptions are operational objects that model mutable state by necessity: `Connection`, `Index`, and the retry infrastructure's `BlockingRetry` loop.

### Error Handling: Error-as-Value, with Dual-Track

The codebase endorses a Railway Oriented Programming style. `Exceptional<T>` is the core carrier. The `Retryable` module introduces a two-track exception taxonomy (`EphemeralFailureException` / `PermanentFailureException`) that is orthogonal to `Exceptional` — the retry loop itself does not produce `Exceptional` values, it throws. This is a deliberate seam: synchronous retry produces values or throws; monadic composition happens around the periphery via `Exceptional`. However, these two error models are not bridged; callers must manually connect them.

### Threading Model: Virtual Threads, Blocking I/O

The library makes a clear and committed choice to use Java 21+ Virtual Threads (`Thread.ofVirtual()`) rather than reactive non-blocking I/O. `Connection` spawns Virtual Threads for message dispatch and per-request handling. `BlockingRetry` sleeps the calling thread. `RetryFrequency` provides `Duration` values consumed by blocking sleep. This is a deliberate philosophical stance: simplify concurrency by using structured blocking rather than callback/reactive chains, relying on the JVM to multiplex Virtual Threads efficiently.

### Extensibility Patterns: Annotation + Interface + Configuration

Three complementary extensibility mechanisms coexist:

1. **Typed `Option` + `@Default`** (configuration) — consumer declares a class as an `Option`, annotates a default, and the framework resolves it automatically.
2. **`@Indexable` + `@Marshal`/`@Unmarshal` annotations** (query, marshalling) — behaviour is driven by annotating static fields/methods without implementing interfaces.
3. **Functional interface + default methods** (flow) — `Subscriber<T>` is `@FunctionalInterface` with safe defaults, enabling progressively complex implementations from single-lambda to full lifecycle management.

### Architectural Tensions

**Tension 1: Two orthogonal error models with no bridge.** `Exceptional<T>` and the `EphemeralFailureException`/`PermanentFailureException` taxonomy are independent systems with no cross-references. Downstream consumers must choose one model and adapt the other manually at every seam.

**Tension 2: Custom reactive types vs JDK Flow.** The library defines its own `Publisher`/`Subscriber`/`Consumer` in `base-flow`, deliberately not extending `java.util.concurrent.Flow`. The rationale (ergonomic lambda subscribers, `@FunctionalInterface`) is sound but undocumented, and any interop with third-party reactive libraries requires hand-written adapters.

**Tension 3: Global mutable singleton in `Marshalling`.** `Marshalling.GLOBAL_SCHEMA_FACTORY` is a static mutable singleton — the only one in a library that otherwise favours immutability and local scoping. It breaks test isolation and cannot be supplied via the `Configuration` option system.

**Tension 4: `Connection` uses `synchronized` in a Virtual Thread world.** `Connection` uses `synchronized` on instance methods while explicitly spawning Virtual Threads. On JDK <24, `synchronized` pins the carrier thread, defeating the purpose of Virtual Threads. `ReentrantLock` would be appropriate.

**Tension 5: `Connection` ignores the library's own marshalling framework.** `Connection` uses raw Java serialization (`ObjectOutputStream`/`ObjectInputStream`) instead of the `Marshalling`/`Transport` infrastructure that the library provides. This is both a security risk (gadget chains) and an architectural inconsistency.

---

## New Modules Worth Adding

### 1. `base-validation`

**The gap:** There is no declarative validation anywhere. `Preconditions` validates individual arguments positionally. Options have no range checks, no mutual exclusivity constraints, no composition validation. Every constructor does its own ad hoc `requireNonNull`.

**What it would provide:** A constraint system for `Option` values and domain objects — either annotation-driven (`@NonNull`, `@Range(min=0)`, `@MutuallyExclusive`) or a fluent validator (`Validator.of(config).require(Timeout.class).requirePositive(Port.class).validate()`). Integrates naturally with `Configuration` at build time.

### 2. `base-transport-binary`

**The gap:** Only JSON transport exists. `base-network`'s `Connection` uses raw Java serialization (a security/versioning liability) instead of the `Marshalling` infrastructure. There is no compact binary format for performance-sensitive paths.

**What it would provide:** A `BinaryTransport` extending `AbstractTransport` (like `JsonTransport` does) using MessagePack, CBOR, or a custom format. This would also be the path to fixing `Connection` — replace `ObjectOutputStream` with `Marshaller` + `BinaryTransport`.

### 3. `base-scheduling`

**The gap:** `BlockingRetry` blocks the calling thread. There is no scheduler for periodic execution, delayed execution, or cron-style scheduling. Given the full commitment to Virtual Threads, a scheduler that spawns virtual threads on a schedule is a natural fit.

**What it would provide:** `Scheduler` abstraction with `schedule(Runnable, Duration)`, `scheduleAtFixedRate(...)`, `scheduleWithFixedDelay(...)`, integrated with `Configuration` for options and `TelemetryRecorder` for observability. Could also provide a non-blocking `AsyncRetry<T>` that returns `CompletableFuture<T>` rather than blocking.

### 4. `base-cache`

**The gap:** `Memoizer` exists but is bare — no TTL, no max size, no eviction policy, no statistics, no explicit invalidation. `ConcurrentWeakHashMap` provides GC-driven eviction but that's it.

**What it would provide:** A `Cache<K,V>` with `Configuration`-driven options for TTL, max entries, eviction policy (LRU/LFU), and optional `TelemetryRecorder` integration for hit/miss statistics. Could use the `Retryable` pattern for cache-miss loading.

### 5. `base-process`

**The gap:** `Terminal` and `Pipe` in `base-io` handle process I/O streams, but there is no process execution abstraction — no way to launch, monitor, or manage subprocesses. Given the `Connection` client/server model and the Virtual Threads commitment, process management is a natural extension.

**What it would provide:** `Process` builder with `Configuration` options for timeout, working directory, environment; output captured via `Pipe`/`Subscriber`; exit code as `Retryable<Integer>` for restart logic.

### 6. `base-inject` (the planned DI module)

**The gap:** The `jakarta-inject.version` property in the POM is already a placeholder for this. `Configuration` is a lightweight service locator but cannot express object graphs or manage construction order. The `Marshaller` has a hierarchical binding system that is effectively a DI container, but it's coupled to marshalling concerns.

**What it would provide:** A minimal DI container that uses the `Configuration` option system for bindings and integrates with `@Default` for auto-wiring. Would consolidate the binding logic currently in `Marshaller` and make it available to all modules.

### 7. `base-template` (the planned templating module)

**The gap:** The `mustache-java.version` property is another placeholder. Currently the library has Jakarta EL for `${variable}` resolution and `java.util.Formatter` for string formatting. No proper template engine for document generation.

**What it would provide:** Template rendering integrated with `Configuration`/`Variable` as context and `Marshalling` for complex object traversal in templates.

---

## Major Enhancements to Existing Modules

### Bridge the Two Error Models

`Exceptional<T>` (monadic, value-based) and `EphemeralFailure/PermanentFailure` (exception-based, retry-aware) are orthogonal systems with no adapters between them. This is the biggest architectural seam in the library.

**Needed:**

- `Exceptional.of(Retryable<T>)` — wraps a single attempt
- `Retryable<Exceptional<T>>` — natural composition for "retry until a non-exceptional result"
- `Exceptional.isEphemeral()` / `isPermanent()` — classify the stored exception

### Fix `Connection` to Use `Marshalling`

`Connection` uses `ObjectOutputStream`/`ObjectInputStream` (Java serialization), which is:

- A **remote code execution surface** (gadget chains)
- Fragile across class version changes
- Architecturally inconsistent (the library has its own marshalling framework and ignores it)

Replace with `Marshaller` + a transport format. This is the strongest argument for `base-transport-binary`.

### Fix `Connection`'s `synchronized` for Virtual Threads

`Connection` uses `synchronized` on instance methods while explicitly spawning Virtual Threads. On JDK <24, `synchronized` pins the carrier thread, defeating the purpose of Virtual Threads. Replace with `ReentrantLock`.

### Fix `RetryFrequency.within()` Bug

The guard conditions in `RetryFrequency.within(Duration floor, Duration ceiling)` are inverted:

```java
// Current (broken):
if (!floor.isNegative()) throw ...   // rejects valid positive floors
if (ceiling.compareTo(floor) >= 0) throw ...  // rejects valid ceiling > floor

// Should be:
if (floor.isNegative()) throw ...
if (ceiling.compareTo(floor) <= 0) throw ...
```

Valid inputs throw `IllegalArgumentException`. This is a correctness bug in a key infrastructure abstraction.

### Add Publisher Operators to `base-flow`

There are `MappingSubscriber` and `FilteringSubscriber` on the subscriber side, but no `map`/`filter`/`flatMap` on `Publisher`. This makes the reactive API awkward compared to any standard reactive library. Adding:

```java
publisher.map(f).filter(p).subscribe(subscriber)
```

...as `Publisher` default methods wrapping the existing subscriber decorators would be straightforward.

### Integrate `TelemetryRecorder` with `BlockingRetry`

Retry is currently silent — no diagnostic on each failed attempt, no warning on timeout. Accept an optional `TelemetryRecorder` (via `Configuration`) and emit:

- `diagnostic` on each ephemeral failure with attempt count
- `warn` on timeout with total attempts and elapsed time
- `info` on success after retries

### Add Structured Data to `TelemetryRecorder`

Currently string-only (`format + args`). Adding a key-value tag/payload system would make telemetry machine-processable and enable structured logging, metric extraction, and alerting integration.

### Add Typed Exception Recovery to `Exceptional`

Missing: `recoverFrom(Class<X>, Function<X, T>)` — typed exception handling within the monadic pipeline. Currently callers must cast-check manually.

```java
// Desired API:
Exceptional.call(() -> riskyOperation())
    .recoverFrom(SocketTimeoutException.class, e -> fallbackValue)
    .recoverFrom(IOException.class, e -> otherFallback)
    .orElseThrow();
```

### Add Change Notifications to `Index`

When objects are indexed/unindexed, downstream components have no way to react. An `Index` that is also a `Publisher<IndexEvent>` (leveraging `base-flow`) would enable reactive cache warming and query invalidation.

### Configuration Sourcing

No file, environment variable, or system property sourcing beyond individual `@Default` annotations. A `ConfigurationLoader` that can populate a `ConfigurationBuilder` from:

- `.properties` or `.yaml` files
- Environment variables with prefix mapping
- System properties

...and merge them with a precedence chain would complete the configuration story.

---

## Latent Issues to Fix

| Issue | Module | Severity | Status | Notes |
|---|---|---|---|---|
| ~~`RetryFrequency.within()` inverted guards~~ | ~~base-retryable~~ | ~~**Bug**~~ | ✅ Fixed | ~~Method is broken — valid inputs are rejected~~ |
| ~~`RetryFrequency.never()` calls `maxRetriesOf(0)` which throws on `limit <= 0`~~ | ~~base-retryable~~ | ~~**Bug**~~ | ✅ Fixed | ~~Self-contradictory construction — likely unreachable~~ |
| ~~`TextualRange` null-checks `start` twice, never checks `end`~~ | ~~base-telemetry~~ | ~~**Bug**~~ | ✅ Fixed | ~~Constructor parameter validation is wrong~~ |
| ~~`Table.getRow(int)` off-by-one in bounds check~~ | ~~base-table~~ | ~~**Bug**~~ | ✅ Fixed | ~~`index > rows.size()` should be `>= rows.size()`~~ |
| ~~`getMarshallingSchema` typo in public API~~ | ~~base-marshalling~~ | ~~API quality~~ | ✅ Fixed | ~~Missing 'r' in "Marshalling" — part of the `SchemaFactory` interface~~ |
| ~~`@Unmarshal` on methods silently ignored~~ | ~~base-marshalling~~ | ~~Surprising behavior~~ | ✅ Fixed | ~~Only constructors are scanned despite annotation targeting both~~ |
| ~~`Progress.percentage()` uses integer division~~ | ~~base-telemetry~~ | ~~Precision loss~~ | ✅ Fixed | ~~`current=1, maximum=3` → `33%` not `33.3%`; return type changed to `double`~~ |
| `Marshalling.GLOBAL_SCHEMA_FACTORY` is a static mutable singleton | base-marshalling | Test isolation | 🔲 Open | Cannot be overridden via `Configuration`; shared across all tests |
| `Connection` uses Java serialization | base-network | **Security** | ⏭ Deferred | Remote code execution surface via gadget chains |
| `Connection` uses `synchronized` with Virtual Threads | base-network | **Performance** | ⏭ Deferred | Pins carrier thread on JDK <24 |
| Multiple correctness and threading bugs in telemetry modules | base-telemetry, base-telemetry-foundation, base-telemetry-ansi | **Bug** | 🔲 Open | See [docs/telemetry-bugs.md](telemetry-bugs.md) |

---

## Unused POM Properties

Five version properties in the root POM are pure forward-looking placeholders with zero usage anywhere in the codebase. They were introduced in the initial commit and have never been consumed by any module.

| Property | Version | Intended Future Module |
|---|---|---|
| `jakarta-inject.version` | 2.0.1 | DI container (`base-inject`) |
| `kie-dmn-feel.version` | 9.44.0.Final | DMN/business-rules expression module |
| `mustache-java.version` | 0.9.14 | Template engine (`base-template`) |
| `auto-service.version` | 1.1.1 | Annotation processor module |
| `compile-testing.version` | 0.21.0 | Annotation processor test harness |

None of these have a corresponding `<dependency>` entry in the root `<dependencyManagement>` block — they exist only as `<properties>`. They are safe to keep as intent markers or remove to reduce confusion.

---

## Priority Ranking

Ordered by impact-to-effort ratio:

| Priority | Item | Category | Status | Rationale |
|---|---|---|---|---|
| 1 | ~~Fix `RetryFrequency.within()` and `.never()` bugs~~ | ~~Bug fix~~ | ✅ Fixed | ~~Low effort, high trust — correctness fix for a key abstraction~~ |
| 2 | ~~Fix `TextualRange` null-check and `Table.getRow` off-by-one~~ | ~~Bug fix~~ | ✅ Fixed | ~~One-line fixes each~~ |
| 2 | ~~Fix `getMarshallingSchema` typo, `@Unmarshal` target, `Progress.percentage()` type~~ | ~~Bug fix~~ | ✅ Fixed | ~~Public API quality and precision fix~~ |
| 3 | Fix telemetry bugs (see [docs/telemetry-bugs.md](telemetry-bugs.md)) | Bug fix | 🔲 Open | 20 correctness, threading, and rendering bugs across all three telemetry modules |
| 4 | Fix `Marshalling.GLOBAL_SCHEMA_FACTORY` singleton | Bug fix | 🔲 Open | Breaks test isolation |
| 5 | Bridge `Exceptional` and `Retryable` | Enhancement | 🔲 Open | Removes the biggest architectural seam in the library |
| 6 | Add Publisher operators to `base-flow` | Enhancement | 🔲 Open | Small surface area, big usability improvement |
| 7 | Integrate `TelemetryRecorder` with `BlockingRetry` | Enhancement | 🔲 Open | Makes retry observable — critical for operations |
| 8 | `base-validation` module | New module | 🔲 Open | Fills the most commonly felt gap for downstream consumers |
| 9 | Fix `Connection` (`Marshalling` + `ReentrantLock`) | Security + correctness | ⏭ Deferred | Eliminates RCE surface and thread pinning |
| 10 | `base-transport-binary` module | New module | 🔲 Open | Enables the Connection fix and adds a performance transport option |
| 11 | `base-scheduling` / `AsyncRetry` | New module | 🔲 Open | Completes the concurrency story beyond blocking retry |
| 12 | `base-cache` module | New module | 🔲 Open | Natural extension of `Memoizer` with TTL, eviction, statistics |
| 13 | Configuration sourcing (file/env/sysprop loader) | Enhancement | 🔲 Open | Enables external config files without custom code |
| 14 | Typed exception recovery on `Exceptional` | Enhancement | 🔲 Open | Quality-of-life for monadic error handling |
| 15 | Structured data for `TelemetryRecorder` | Enhancement | 🔲 Open | Enables machine-processable telemetry |
| 16 | Change notifications on `Index` | Enhancement | 🔲 Open | Enables reactive index-driven workflows |
| 17 | `base-inject` module | New module | 🔲 Open | Consolidates DI patterns already scattered across modules |
| 18 | `base-process` module | New module | 🔲 Open | Subprocess management integrated with existing I/O infrastructure |
| 19 | `base-template` module | New module | 🔲 Open | Completes the string-processing story alongside EL expressions |
