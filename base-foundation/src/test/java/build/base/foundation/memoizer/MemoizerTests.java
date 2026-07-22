package build.base.foundation.memoizer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import build.base.foundation.Lazy;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for the {@link Memoizer} hierarchy: {@link StrongMemoizer}, {@link SoftMemoizer},
 * {@link WeakMemoizer}, {@link StrongConcurrentMemoizer}, {@link SoftConcurrentMemoizer},
 * and {@link WeakConcurrentMemoizer}.
 *
 * @author reed.vonredwitz
 * @since Jul-2025
 */
class MemoizerTests {

    // -------------------------------------------------------------------------
    // Parameterized helpers — one factory per implementation
    // -------------------------------------------------------------------------

    static Stream<Function<Function<String, Integer>, Memoizer<String, Integer>>> allFactories() {
        return Stream.of(
            fn -> Memoizer.of(fn).build(),
            fn -> Memoizer.of(fn).soft().build(),
            fn -> Memoizer.of(fn).weak().build(),
            fn -> Memoizer.of(fn).concurrent().build(),
            fn -> Memoizer.of(fn).soft().concurrent().build(),
            fn -> Memoizer.of(fn).weak().concurrent().build()
        );
    }

    static Stream<Function<Function<String, Integer>, Memoizer<String, Integer>>> concurrentFactories() {
        return Stream.of(
            fn -> Memoizer.of(fn).concurrent().build(),
            fn -> Memoizer.of(fn).soft().concurrent().build(),
            fn -> Memoizer.of(fn).weak().concurrent().build()
        );
    }

    // -------------------------------------------------------------------------
    // Core contract (all six implementations)
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldComputeOnFirstCall(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(s -> s == null ? -1 : s.length());
        assertThat(memoizer.compute("hello")).isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldReturnCachedResultOnSubsequentCalls(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var count = new AtomicInteger();
        final var memoizer = factory.apply(s -> { count.incrementAndGet(); return s.length(); });

        memoizer.compute("hello");
        memoizer.compute("hello");
        memoizer.compute("hello");

        assertThat(count).hasValue(1);
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldComputeSeparateResultsForDifferentInputs(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(String::length);
        assertThat(memoizer.compute("hi")).isEqualTo(2);
        assertThat(memoizer.compute("hello")).isEqualTo(5);
        assertThat(memoizer.size()).isEqualTo(2);
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldSupportNullInput(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(s -> s == null ? -1 : s.length());
        assertThat(memoizer.compute(null)).isEqualTo(-1);
        assertThat(memoizer.contains(null)).isTrue();
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldSupportNullResult(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var count = new AtomicInteger();
        final var memoizer = factory.apply(s -> { count.incrementAndGet(); return null; });

        assertThat(memoizer.compute("key")).isNull();
        assertThat(memoizer.compute("key")).isNull();
        assertThat(count).hasValue(1);
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldClearAllCachedResults(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(String::length);
        memoizer.compute("hello");
        assertThat(memoizer.size()).isGreaterThanOrEqualTo(1);

        memoizer.clear();
        assertThat(memoizer.size()).isEqualTo(0);
        assertThat(memoizer.contains("hello")).isFalse();
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldReportContainsCorrectly(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(String::length);
        assertThat(memoizer.contains("hello")).isFalse();
        memoizer.compute("hello");
        assertThat(memoizer.contains("hello")).isTrue();
        assertThat(memoizer.contains("world")).isFalse();
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldReportCorrectSize(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(String::length);
        assertThat(memoizer.size()).isEqualTo(0);
        memoizer.compute("a");
        memoizer.compute("bb");
        memoizer.compute("a");
        assertThat(memoizer.size()).isEqualTo(2);
    }

    // -------------------------------------------------------------------------
    // computeWith
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldComputeWithSupplierOnFirstCall(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(s -> { throw new AssertionError("function should not be invoked"); });
        assertThat(memoizer.computeWith("hello", () -> 5)).isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldIgnoreSupplierOnCacheHit(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(String::length);
        memoizer.compute("hello");

        assertThat(memoizer.computeWith("hello", () -> { throw new AssertionError("supplier should not be invoked"); }))
            .isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldShareCacheBetweenComputeAndComputeWith(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(String::length);
        memoizer.computeWith("hello", () -> 5);

        assertThat(memoizer.contains("hello")).isTrue();
        assertThat(memoizer.compute("hello")).isEqualTo(5);
    }

    @ParameterizedTest
    @MethodSource("allFactories")
    void shouldRejectNullSupplier(final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) {
        final var memoizer = factory.apply(String::length);
        assertThatThrownBy(() -> memoizer.computeWith("hello", null))
            .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @MethodSource("concurrentFactories")
    void shouldComputeWithAtMostOnceUnderConcurrentLoad(
        final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) throws InterruptedException {
        final var count = new AtomicInteger();
        final var memoizer = factory.apply(s -> { throw new AssertionError("function should not be invoked"); });

        final var threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    memoizer.computeWith("concurrent", () -> { count.incrementAndGet(); sleep10ms(); return 10; });
                }
            });
        }
        for (final var t : threads) t.start();
        for (final var t : threads) t.join();

        assertThat(count).hasValue(1);
        assertThat(memoizer.size()).isEqualTo(1);
    }

    // -------------------------------------------------------------------------
    // Construction
    // -------------------------------------------------------------------------

    @Test
    void shouldRejectNullFunction() {
        assertThatThrownBy(() -> Memoizer.of(null))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAcceptCustomMapSupplier() {
        final var memoizer = Memoizer.<String, Integer>of(String::length).withMap(HashMap::new).build();
        assertThat(memoizer.compute("hello")).isEqualTo(5);
    }

    @Test
    void shouldRejectNonConcurrentMapForConcurrentMemoizer() {
        assertThatThrownBy(() -> Memoizer.of(String::length).withMap(HashMap::new).concurrent().build())
            .isInstanceOf(IllegalArgumentException.class);
    }

    // -------------------------------------------------------------------------
    // Thread safety (concurrent implementations)
    // -------------------------------------------------------------------------

    @ParameterizedTest
    @MethodSource("concurrentFactories")
    void shouldComputeAtMostOnceUnderConcurrentLoad(
        final Function<Function<String, Integer>, Memoizer<String, Integer>> factory) throws InterruptedException {
        final var count = new AtomicInteger();
        final var memoizer = factory.apply(s -> { count.incrementAndGet(); sleep10ms(); return s.length(); });

        final var threads = new Thread[20];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < 50; j++) {
                    memoizer.compute("concurrent");
                }
            });
        }
        for (final var t : threads) t.start();
        for (final var t : threads) t.join();

        assertThat(count).hasValue(1);
        assertThat(memoizer.size()).isEqualTo(1);
    }

    private static void sleep10ms() {
        try { Thread.sleep(10); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // -------------------------------------------------------------------------
    // Re-entrancy (concurrent implementations — different keys, same hash code)
    // -------------------------------------------------------------------------

    /**
     * Guards against regression to {@code ConcurrentHashMap.computeIfAbsent}: verifies that when
     * a memoized function calls back into the same memoizer for a key that shares a hash code with
     * the in-progress key, the computation completes correctly without deadlock.
     * <p>
     * {@code computeIfAbsent} on the cache map would fail here because it holds the map's bin lock
     * during computation; the re-entrant call for a key in the same bin would attempt to re-acquire
     * that bin lock, causing either a deadlock or an {@link IllegalStateException}. The per-key
     * {@link java.util.concurrent.locks.ReentrantLock} approach avoids this: no bin lock is held
     * during computation, so re-entrant calls always succeed.
     */
    @Test
    void shouldSupportReentrantComputationForKeysWithSameHashCode() {
        verifyReentrantComputation(fn -> Memoizer.of(fn).concurrent().build());
        verifyReentrantComputation(fn -> Memoizer.of(fn).soft().concurrent().build());
        verifyReentrantComputation(fn -> Memoizer.of(fn).weak().concurrent().build());
    }

    private void verifyReentrantComputation(
        final Function<Function<TestKey, String>, Memoizer<TestKey, String>> factory)
    {
        final var count = new AtomicInteger();
        final var lazy = Lazy.<Memoizer<TestKey, String>>empty();

        lazy.set(factory.apply((TestKey key) -> {
            count.incrementAndGet();
            return key.value() == 1
                ? lazy.orElseThrow().compute(new TestKey("test", 2))
                : "hello";
        }));

        assertThat(lazy.orElseThrow().compute(new TestKey("test", 3))).isEqualTo("hello");
        assertThat(count).hasValue(1);

        assertThat(lazy.orElseThrow().compute(new TestKey("test", 1))).isEqualTo("hello");
        assertThat(count).hasValue(3);
    }

    // -------------------------------------------------------------------------
    // Test helpers
    // -------------------------------------------------------------------------

    private record TestKey(String name, int value) {

        @Override
        public int hashCode() {
            // All TestKeys with the same name share a hash — exercises same-stripe scenarios.
            return name.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            return obj instanceof TestKey(String n, int v) && this.value == v && this.name.equals(n);
        }
    }
}
