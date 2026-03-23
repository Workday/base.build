package build.base.foundation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Memoizer}.
 *
 * @author reed.vonredwitz
 * @since Dec-2024
 */
class MemoizerTests {

    private AtomicInteger callCount;
    private Memoizer<String, Integer> memoizer;

    @BeforeEach
    void setUp() {
        this.callCount = new AtomicInteger(0);

        // simulate an expensive computation
        final Function<String, Integer> expensiveFunction = input -> {
            this.callCount.incrementAndGet();
            // Simulate expensive computation
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return input.length();
        };

        this.memoizer = new Memoizer<>(expensiveFunction);
    }

    /**
     * Ensure memoizer returns correct result for first computation.
     */
    @Test
    void shouldComputeResultForFirstCall() {
        final var result = this.memoizer.compute("hello");

        assertThat(result).isEqualTo(5);
        assertThat(this.callCount.get()).isEqualTo(1);
    }

    /**
     * Ensure memoizer returns cached result for subsequent calls.
     */
    @Test
    void shouldReturnCachedResultForSubsequentCalls() {
        // First call
        final var result1 = this.memoizer.compute("hello");
        assertThat(result1).isEqualTo(5);
        assertThat(this.callCount.get()).isEqualTo(1);

        // Second call with same input
        final var result2 = this.memoizer.compute("hello");
        assertThat(result2).isEqualTo(5);
        assertThat(this.callCount.get()).isEqualTo(1); // Should not increment
    }

    /**
     * Ensure memoizer computes different results for different inputs.
     */
    @Test
    void shouldComputeDifferentResultsForDifferentInputs() {
        final var result1 = this.memoizer.compute("hello");
        final var result2 = this.memoizer.compute("world");

        assertThat(result1).isEqualTo(5);
        assertThat(result2).isEqualTo(5);
        assertThat(this.callCount.get()).isEqualTo(2);
    }

    /**
     * Ensure memoizer handles null input correctly.
     */
    @Test
    void shouldHandleNullInput() {
        final var nullFunction = new Function<String, Integer>() {
            @Override
            public Integer apply(String input) {
                callCount.incrementAndGet();
                return input == null ? 0 : input.length();
            }
        };
        final var nullMemoizer = new Memoizer<>(nullFunction);

        final var result = nullMemoizer.compute(null);
        assertThat(result).isEqualTo(0);
        assertThat(this.callCount.get()).isEqualTo(1);
    }

    /**
     * Ensure memoizer constructor throws exception for null function.
     */
    @Test
    void shouldThrowExceptionForNullFunction() {
        assertThatThrownBy(() -> new Memoizer<>(null))
            .isInstanceOf(NullPointerException.class);
    }

    /**
     * Ensure clear method removes all cached results.
     */
    @Test
    void shouldClearAllCachedResults() {
        // First call
        this.memoizer.compute("hello");
        assertThat(this.callCount.get()).isEqualTo(1);
        assertThat(this.memoizer.size()).isEqualTo(1);

        // Clear cache
        this.memoizer.clear();
        assertThat(this.memoizer.size()).isEqualTo(0);

        // Second call should recompute
        this.memoizer.compute("hello");
        assertThat(this.callCount.get()).isEqualTo(2);
        assertThat(memoizer.size()).isEqualTo(1);
    }

    /**
     * Ensure size method returns correct cache size.
     */
    @Test
    void shouldReturnCorrectCacheSize() {
        assertThat(this.memoizer.size()).isEqualTo(0);

        this.memoizer.compute("hello");
        assertThat(this.memoizer.size()).isEqualTo(1);

        this.memoizer.compute("world");
        assertThat(this.memoizer.size()).isEqualTo(2);

        this.memoizer.compute("hello"); // Should not increase size
        assertThat(this.memoizer.size()).isEqualTo(2);
    }

    /**
     * Ensure contains method works correctly.
     */
    @Test
    void shouldCheckIfInputIsCached() {
        assertThat(this.memoizer.contains("hello")).isFalse();

        this.memoizer.compute("hello");
        assertThat(this.memoizer.contains("hello")).isTrue();
        assertThat(this.memoizer.contains("world")).isFalse();
    }

    /**
     * Ensure memoizer is thread-safe.
     */
    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        final var threadCount = 10;
        final var iterations = 100;
        final var threads = new Thread[threadCount];

        // Create threads that will call the memoizer concurrently
        for (int i = 0; i < threadCount; i++) {
            threads[i] = new Thread(() -> {
                for (int j = 0; j < iterations; j++) {
                    this.memoizer.compute("concurrent");
                }
            });
        }

        // Start all threads
        for (final var thread : threads) {
            thread.start();
        }

        // Wait for all threads to complete
        for (final var thread : threads) {
            thread.join();
        }

        // Verify that the function was only called once despite multiple threads
        assertThat(this.callCount.get()).isEqualTo(1);
        assertThat(this.memoizer.size()).isEqualTo(1);
        assertThat(this.memoizer.contains("concurrent")).isTrue();
    }

    /**
     * Ensure memoizer works with complex objects.
     */
    @Test
    void shouldWorkWithComplexObjects() {
        final var complexFunction = new Function<TestObject, String>() {
            @Override
            public String apply(TestObject input) {
                callCount.incrementAndGet();
                return input.name + "_" + input.value;
            }
        };
        final var complexMemoizer = new Memoizer<>(complexFunction);

        final var obj1 = new TestObject("test", 42);
        final var obj2 = new TestObject("test", 42); // Same values, different instance

        final var result1 = complexMemoizer.compute(obj1);
        final var result2 = complexMemoizer.compute(obj2);

        assertThat(result1).isEqualTo("test_42");
        assertThat(result2).isEqualTo("test_42");
        // Since obj1 and obj2 have the same equals/hashCode, they will be treated as the same key
        assertThat(this.callCount.get()).isEqualTo(1);
    }

    /**
     * Ensure that a {@link Memoizer} allows re-entrant computation of the same keyed value.
     */
    @Test
    void shouldAllowReentrantUpdatesForSameHashCode() {

        final var first = new TestObject("test", 1);
        final var second = new TestObject("test", 2);
        final var third = new TestObject("test", 3);

        final var count = new AtomicInteger(0);

        // define a Lazy<Memoizer> we can use in our reentrant function
        final var lazyMemoizer = Lazy.<Memoizer<TestObject, String>>empty();

        // define a reentrant function that uses the Lazy<Memoizer>
        final var reentrant = new Function<TestObject, String>() {
            @Override
            public String apply(final TestObject testObject) {
                count.incrementAndGet();

                return testObject.value == 1
                    ? lazyMemoizer.orElseThrow()
                    .compute(second)
                    : "hello";
            }
        };

        // now create the Memoizer using our reentrant function
        lazyMemoizer.set(new Memoizer<>(reentrant));

        // ensure we haven't computed anything yet
        assertThat(count)
            .hasValue(0);

        // ensure we can compute the third value (not re-entrant)
        assertThat(lazyMemoizer.orElseThrow()
            .compute(third))
            .isEqualTo("hello");

        // ensure we computed only one value
        assertThat(count)
            .hasValue(1);

        // ensure we can compute the first value (re-entrant)
        assertThat(lazyMemoizer.orElseThrow()
            .compute(first))
            .isEqualTo("hello");

        // ensure we computed two more values (as the first value computation is re-entrant)
        assertThat(count)
            .hasValue(3);
    }


    /**
     * Test object for complex object testing.
     */
    private record TestObject(String name, int value) {


        @Override
        public int hashCode() {
            return Objects.hash(this.name);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            return obj instanceof TestObject(String thatName, int thatValue) &&
                this.value == thatValue &&
                this.name.equals(thatName);
        }
    }
}
