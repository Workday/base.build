# Telemetry Bug Report

> Discovered: 2026-04-01. All bugs are undocumented and untested.
> Modules affected: `base-telemetry`, `base-telemetry-foundation`, `base-telemetry-ansi`

---

## Summary

| # | File | Lines | Category | Severity |
|---|------|-------|----------|----------|
| 1 | `TextualRange.java` | 38–41 | Wrong Javadoc (`start()` says "end") | Low |
| 2 | `Progress.java` et al. | 88 | NPE on `null` format string (10 factories affected) | **High** |
| 3 | `ProgressBar.java` | 140 | Division-by-zero silent wrong value when `total == 0` | Medium |
| 4 | `ProgressBar.java` | 105 | Negative delta accepted; progress counter goes below zero | **High** |
| 5 | `ProgressBar.java` | 104–111 | Counter diverges from underlying `Meter` — can exceed `total` and show >100% | **High** |
| 6 | `ProgressBar.java` | 130–135 | `close()` never forces counter to `total`; bar may not show 100% | Medium |
| 7 | `Commenced.java` | 40–43, 98–101 | Wrong Javadoc — `create()` docs say `Information`, should say `Commenced` | Low |
| 8 | `Completed.java` | 121–124 | Time-breakdown arithmetic wrong for durations > 1 day | **High** |
| 9 | `AbstractTelemetryRecorder.java` | 143 | Double-formats description then forwards `args` again — breaks on `%` in data | **High** |
| 10 | `AbstractTelemetryRecorder.java` | 203 | Race: `progress()` can record after `complete()` due to non-atomic check | Medium |
| 11 | `TelemetryRecorder.java` | 407 | Stray `null` in `diagnostic(Location, ...)` delegate — always NPEs | **High** |
| 12 | `ObservableTelemetryRecorder.java` | 101–103 | `stream()` and `hasObserved()` unsynchronized vs. synchronized `record()` | Medium |
| 13 | `ObservableTelemetryRecorder.java` | 128–130 | `uri()` returns wrapped recorder's URI; stored `this.uri` is dead code | Medium |
| 14 | `PrintStreamTelemetryRecorder.java` | 69–76 | `Fatal` and `Warning` routed to `outputStream`, not `errorStream` | Medium |
| 15 | `ANSIBasedTelemetryRecorder.java` | 176 | Cursor moved up `N+1` lines but only `N` printed — overwrites line above bars | **High** |
| 16 | `ANSIBasedTelemetryRecorder.java` | 78–81 | `InterruptedException` swallowed without re-interrupting the thread | Low |
| 17 | `ANSIBasedTelemetryRecorder.java` | 148–154 | `render()` called before null-entry `clear()` — blank garbage on last removal | Medium |
| 18 | `ProgressBar.java` | 65, 108–109 | `message` field not volatile; stale reads from render thread | Medium |

---

## Bug Details

### BUG-1 — `TextualRange.start()` Javadoc says "end"
**File:** `base-telemetry/src/main/java/build/base/telemetry/TextualRange.java:38`

The `@return` tag for `start()` reads "the end `TextualPosition`" — identical to `end()`. Copy-paste error; no functional impact but misleading in generated docs.

---

### BUG-2 — NPE on `null` format string in all `Telemetry.create()` factories
**File:** All `create()` factory methods across `Progress`, `Commenced`, `Information`, `Notification`, `Advice`, `Diagnostic`, `Warning`, `Error`, `Fatal`, `Completed`

Every factory calls `String.format(format, args)` unconditionally. The Javadoc marks `format` as `{@code null}able` but the implementation does not honour it.

```java
// In Progress.java:88, and equivalents in 9 other files:
final String message = String.format(format, args);  // NPE when format == null
```

**Reproduction:**
```java
Progress.create(uri, NamedUnit.none(), 0, 10, null);
// → NullPointerException
```

**Fix:** Guard with `format == null ? "" : String.format(format, args)` or remove the nullability claim from Javadoc.

---

### BUG-3 — `ProgressBar` silently shows 0% when `total == 0`
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ProgressBar.java:140`

```java
final var percentage = (int) ((double) progress / this.total * 100);
// When total == 0: NaN cast to int → 0, silently
```

`Progress.create()` explicitly guards `maximum == 0 → 0.0`. `ProgressBar` duplicates the calculation without the guard. No exception, but the display is meaningless.

---

### BUG-4 — Negative delta accepted; `ProgressBar` counter goes below zero
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ProgressBar.java:105`

```java
this.progress.addAndGet(delta);  // no clamping to [0, total]
```

The underlying `Meter` in `AbstractTelemetryRecorder` clamps via `Math.min(current + delta, maximum)`, but `ProgressBar`'s own counter does not. A negative delta makes `this.progress` negative, causing a negative percentage and a negative `filledLength` passed to `Strings.repeat("#", filledLength)`.

---

### BUG-5 — `ProgressBar` counter diverges from underlying `Meter`; can exceed `total`
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ProgressBar.java:104–111`

`ProgressBar.progress` accumulates unclamped while `this.meter.progress()` clamps at `maximum`. After enough deltas, `ProgressBar` shows >100%.

```java
// total = 10
progressBar.progress(8);  // ProgressBar.progress=8, Meter=8
progressBar.progress(5);  // ProgressBar.progress=13, Meter=10 (clamped)
// → bar displays "130%"
```

---

### BUG-6 — `close()` never forces `ProgressBar` counter to `total`
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ProgressBar.java:130–135`

`close()` calls `meter.close()` which internally completes at `maximum`, but `ProgressBar.progress` is never updated. A render that races with `close()` shows the pre-close progress, not 100%.

---

### BUG-7 — `Commenced.create()` Javadoc says it creates `Information` telemetry
**File:** `base-telemetry/src/main/java/build/base/telemetry/Commenced.java:40–43, 98–101`

Both `create()` overloads have `@return new {@link Information} {@link Telemetry}` — they actually return `Commenced`. Copy-paste from `Information.java`.

---

### BUG-8 — `Completed.toString()` time arithmetic is wrong for durations > 1 day
**File:** `base-telemetry/src/main/java/build/base/telemetry/Completed.java:121–124`

```java
final long days    = duration.toDays();
final long hours   = duration.toHours()   - (days * 24);
final long minutes = duration.toMinutes() - (hours * 60);    // BUG: subtracts adjusted hours not total
final long seconds = (duration.toMillis() / 1000) - (minutes * 60); // BUG: same
```

`duration.toMinutes()` returns total minutes (including those from days). Subtracting only `hours * 60` (the adjusted hours, not the full hours-worth of minutes) produces a massively inflated minutes value for any duration >= 1 day.

**Reproduction:** 2 days 1 hour 5 minutes → `minutes` = 2945 − 60 = 2885 instead of 5.

**Fix:**
```java
long hours   = duration.toHours()    % 24;
long minutes = duration.toMinutes()  % 60;
long seconds = duration.getSeconds() % 60;
```

---

### BUG-9 — `AbstractTelemetryRecorder.commence()` double-formats the description
**File:** `base-telemetry-foundation/src/main/java/build/base/telemetry/foundation/AbstractTelemetryRecorder.java:143`

```java
final String description = String.format(format, args);          // formatted once
record(Commenced.create(this.uri, description, args));           // formatted AGAIN inside create()
```

`Commenced.create(uri, format, args)` calls `String.format(format, args)` internally. Passing the already-formatted `description` as `format` then re-applying `args` corrupts the output and throws `MissingFormatArgumentException` if the original data contained `%` characters.

**Fix:** Use `Commenced.create(this.uri, description)` (single-arg overload, no re-formatting).

---

### BUG-10 — Race: `progress()` can record after `complete()` in `AbstractTelemetryRecorder.Meter`
**File:** `base-telemetry-foundation/src/main/java/build/base/telemetry/foundation/AbstractTelemetryRecorder.java:203`

```java
if (completed.compareAndSet(false, false)) {  // non-atomic check-then-act
    // another thread can call complete() here
    record(Progress.create(...));             // spurious post-completion Progress event
}
```

The check and the record are not atomic. A `Progress` telemetry event can be emitted after a `Completed` event, violating the expected ordering.

---

### BUG-11 — `TelemetryRecorder.diagnostic(Location, ...)` always NPEs
**File:** `base-telemetry/src/main/java/build/base/telemetry/TelemetryRecorder.java:406–408`

```java
default Diagnostic diagnostic(final Location location, final String format, final Object... args) {
    return diagnostic(location == null ? Stream.empty() : Stream.of(location), null, format, args);
    //                                                                          ^^^^ stray null
}
```

The stray `null` is matched as the `format` argument by varargs resolution. The actual `format` and `args` become elements of the `Object[]` args array. `Diagnostic.create()` then calls `String.format(null, ...)` → NPE.

**Reproduction:** Any call to `recorder.diagnostic(someLocation, "msg")` throws NPE.

**Fix:** Remove the `null`:
```java
return diagnostic(location == null ? Stream.empty() : Stream.of(location), format, args);
```

---

### BUG-12 — `ObservableTelemetryRecorder.stream()` unsynchronized vs. synchronized `record()`
**File:** `base-telemetry-foundation/src/main/java/build/base/telemetry/foundation/ObservableTelemetryRecorder.java:88–103`

`record()` synchronizes on `this.telemetry` before adding. `stream()` and `hasObserved()` do not synchronize before reading. Concurrent access produces `ConcurrentModificationException` or stale results.

**Fix:** Synchronize `stream()` and `hasObserved()` on `this.telemetry`, or use a `CopyOnWriteArrayList`.

---

### BUG-13 — `ObservableTelemetryRecorder.uri()` ignores `this.uri`; stored field is dead code
**File:** `base-telemetry-foundation/src/main/java/build/base/telemetry/foundation/ObservableTelemetryRecorder.java:56, 128–130`

```java
private final URI uri;   // validated and stored in constructor...

@Override
public URI uri() {
    return this.telemetryRecorder.uri();  // ...but never returned
}
```

When constructed with a different URI than the wrapped recorder, the passed URI is silently discarded.

---

### BUG-14 — `PrintStreamTelemetryRecorder` routes `Fatal` and `Warning` to `outputStream`
**File:** `base-telemetry-foundation/src/main/java/build/base/telemetry/foundation/PrintStreamTelemetryRecorder.java:69–76`

```java
if (telemetry instanceof Error) {
    this.errorStream.println(telemetry);
} else {
    this.outputStream.println(telemetry);  // Fatal and Warning end up here
}
```

`Fatal` (non-recoverable error) and `Warning` both go to stdout. Only `Error` goes to stderr.

**Fix:** `if (telemetry instanceof Error || telemetry instanceof Fatal || telemetry instanceof Warning)`

---

### BUG-15 — ANSI renderer moves cursor up `N+1` lines but only printed `N`
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ANSIBasedTelemetryRecorder.java:176`

```java
// loop prints activities.size() lines
System.out.print("\u001b[" + (this.activities.size() + 1) + "A");  // moves up N+1
```

The off-by-one causes the cursor to land one line above the progress bar region every render cycle, progressively overwriting content above the bars.

**Fix:** Remove the `+ 1`.

---

### BUG-16 — `InterruptedException` swallowed in render thread
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ANSIBasedTelemetryRecorder.java:78–81`

```java
} catch (final InterruptedException e) {
    // ignore interrupts
}
```

The interrupt flag is cleared. The thread will not respond to shutdown signals until its next `renderFuture.isDone()` check (up to 1 second later).

**Fix:** Add `Thread.currentThread().interrupt()` in the catch block.

---

### BUG-17 — `render()` called before null-entry `clear()` in `remove()`
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ANSIBasedTelemetryRecorder.java:148–154`

```java
this.activities.set(i, null);  // mark as null
render();                       // renders N blank lines (list not empty)
if (this.activities.stream().allMatch(Objects::isNull)) {
    this.activities.clear();   // clear happens after render
}
```

When the last activity is removed, `render()` runs while the list contains only nulls — it is not empty, so the render guard passes and blank lines are printed.

**Fix:** Move `clear()` before `render()`.

---

### BUG-18 — `ProgressBar.message` not volatile; stale reads from render thread
**File:** `base-telemetry-ansi/src/main/java/build/base/telemetry/ansi/ProgressBar.java:65, 108–109`

`message` is a `Capture<String>` field with no visibility guarantee. `progress()` writes it from a caller thread; `toString()` reads it from the render thread. Without `volatile` or synchronization, the render thread may see stale message text indefinitely.
