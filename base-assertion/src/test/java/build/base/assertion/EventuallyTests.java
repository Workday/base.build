package build.base.assertion;

import build.base.option.Timeout;
import build.base.retryable.EphemerallyFailingRetryable;
import build.base.retryable.PermanentFailureException;
import build.base.retryable.PermanentlyFailingRetryable;
import build.base.retryable.Retryable;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Eventually}.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
class EventuallyTests {

    /**
     * Ensure constants can be asserted.
     */
    @Test
    void shouldEventuallyAssertAConstant() {
        Eventually.assertThat(Retryable.of(42))
            .isEqualTo(42);

        Eventually.assertThat(Retryable.of(Long.valueOf(42)))
            .isEqualTo(42L);

        Eventually.assertThat(Retryable.of(42L))
            .isEqualTo(Long.valueOf(42));
    }

    /**
     * Ensure a specified {@link Timeout} can be used for retry.
     */
    @Test
    void shouldRetryForASpecificTimeout() {
        final var duration = Duration.ofSeconds(1);
        final var timeout = Timeout.of(duration);
        final var start = Instant.now();

        try {
            Eventually.assertThat(new EphemerallyFailingRetryable<>())
                .withTimeout(timeout)
                .isEqualTo(42);
        }
        catch (final AssertionError e) {

            // ensure the assertion was due to the supplier failing
            assertThat(e)
                .hasCauseInstanceOf(PermanentFailureException.class);

            final var stop = Instant.now();

            // ensure at least the specified timeout was used
            final var period = Duration.between(start, stop).toMillis();
            final var minimum = (long) (duration.toMillis() * 0.95);

            assertThat(period)
                .isGreaterThan(minimum);
        }
        catch (final Exception e) {
            fail("Failed with " + e);
        }
    }

    /**
     * Ensure an assertion fails fast when encountering a {@link PermanentFailureException}.
     */
    @Test
    void shouldFailFast() {
        final var duration = Duration.ofSeconds(3);
        final var timeout = Timeout.of(duration);
        final var start = Instant.now();

        try {
            Eventually.assertThat(new PermanentlyFailingRetryable<>())
                .withTimeout(timeout)
                .isEqualTo(42);
        }
        catch (final AssertionError e) {

            // ensure the assertion was due to the supplier failing
            assertThat(e)
                .hasCauseInstanceOf(PermanentFailureException.class);

            final var stop = Instant.now();

            // ensure the timeout wasn't waited
            assertThat(Duration.between(start, stop))
                .isLessThan(Duration.ofMillis(500));
        }
        catch (final Exception e) {
            fail("Failed with " + e);
        }
    }

    /**
     * Ensure that a lambda-based {@link Retryable} that throws a {@link RuntimeException}
     * is retried and doesn't fail fast.
     */
    @Test
    void shouldRetryRetryableLambdaThrowingARuntimeException() {

        final var duration = Duration.ofSeconds(1);
        final var timeout = Timeout.of(duration);
        final var start = Instant.now();

        try {
            Eventually.assertThat((Runnable) () -> {
                    throw new RuntimeException("Not Fatal");
                })
                .withTimeout(timeout)
                .isEqualTo(42);
        }
        catch (final AssertionError e) {

            // ensure the assertion was due to the supplier failing
            assertThat(e)
                .hasCauseInstanceOf(PermanentFailureException.class);

            final var stop = Instant.now();

            // ensure the timeout was waited
            assertThat(Duration.between(start, stop))
                .isGreaterThanOrEqualTo(duration);
        }
        catch (final Exception e) {
            fail("Failed with " + e);
        }
    }

    /**
     * Ensure an assertion fails fast when encountering a completed {@link CompletableFuture}.
     */
    @Test
    void shouldFailFastWithAnImmediatelyCompletedCompletableFuture() {
        final var duration = Duration.ofSeconds(3);
        final var timeout = Timeout.of(duration);
        final var start = Instant.now();

        final var completableFuture = CompletableFuture.completedFuture(42);

        Eventually.assertThat(completableFuture)
            .withTimeout(timeout)
            .isCompletedWithValue(42);

        final var stop = Instant.now();

        // ensure the timeout wasn't waited
        assertThat(Duration.between(start, stop))
            .isLessThanOrEqualTo(Duration.ofMillis(500));
    }

    /**
     * Ensure an assertion fails fast when encountering an exceptionally completed {@link CompletableFuture}.
     */
    @Test
    void shouldFailFastWithAnImmediatelyExceptionalCompletableFuture() {
        final var duration = Duration.ofSeconds(3);
        final var timeout = Timeout.of(duration);
        final var start = Instant.now();

        try {
            // establish and exceptionally complete a CompletableFuture
            final var completableFuture = new CompletableFuture<Integer>();
            completableFuture.completeExceptionally(new RuntimeException("Oops"));

            Eventually.assertThat(completableFuture)
                .withTimeout(timeout)
                .isCompletedExceptionally()
                .hasCauseInstanceOf(RuntimeException.class);

            fail("The Eventually.assertThat(...) should not have succeeded");
        }
        catch (final AssertionError e) {

            final var stop = Instant.now();

            // ensure the timeout wasn't waited
            assertThat(Duration.between(start, stop))
                .isLessThan(Duration.ofMillis(500));
        }
    }

    /**
     * Ensure an assertion fails fast when encountering an asynchronously completed {@link CompletableFuture}.
     *
     * @throws Exception upon a {@link Thread} failure
     */
    @Test
    void shouldFailFastWithAnAsynchronouslyCompletedCompletableFuture()
        throws Exception {
        final var duration = Duration.ofSeconds(5);
        final var timeout = Timeout.of(duration);
        final var start = Instant.now();

        final var completableFuture = new CompletableFuture<Integer>();

        // establish a back-ground thread to complete the CompletableFuture
        final Thread thread = Thread.startVirtualThread(() -> {
            // wait for a second to allow the Eventually to start checking
            try {
                Thread.sleep(1000);
            }
            catch (final Exception e) {

            }

            // now complete the CompletableFuture
            completableFuture.complete(42);
        });

        // (use a specific RetryFrequency to remove randomness of retries)
        Eventually.assertThat(completableFuture)
            .withTimeout(timeout)
            .isCompletedWithValue(42);

        final var stop = Instant.now();

        // ensure the timeout wasn't waited
        assertThat(Duration.between(start, stop))
            .isLessThan(duration.dividedBy(2));

        thread.join();
    }

    /**
     * Ensure an assertion fails fast when encountering an asynchronously exceptionally completed
     * {@link CompletableFuture}.
     *
     * @throws Exception upon a {@link Thread} failure
     */
    @Test
    void shouldFailFastWithAnAsynchronouslyExceptionalCompletableFuture()
        throws Exception {
        final var duration = Duration.ofSeconds(5);
        final var timeout = Timeout.of(duration);
        final var start = Instant.now();

        final var completableFuture = new CompletableFuture<Integer>();

        // establish a back-ground thread to complete the CompletableFuture
        final Thread thread = Thread.startVirtualThread(() -> {
            // wait for a second to allow the Eventually to start checking
            try {
                Thread.sleep(1000);
            }
            catch (final Exception e) {

            }

            // now complete the CompletableFuture
            completableFuture.completeExceptionally(new RuntimeException("Opps"));
        });

        // (use a specific RetryFrequency to remove randomness of retries)
        Eventually.assertThat(completableFuture)
            .withTimeout(timeout)
            .isCompletedExceptionally()
            .hasCauseInstanceOf(RuntimeException.class);

        final Instant stop = Instant.now();

        // ensure the timeout wasn't waited
        assertThat(Duration.between(start, stop))
            .isLessThan(duration.dividedBy(2));

        thread.join();
    }

    /**
     * Ensure an {@link Runnable} is retried when it throws a {@link RuntimeException}.
     */
    @Test
    void shouldRetryARunnableThrowingARuntimeException() {
        assertThrows(AssertionError.class, () -> {
            // establish a Timeout for Eventually.assertThat(...)
            final var duration = Duration.ofSeconds(2);
            final var timeout = Timeout.of(duration);

            // establish a counter to determine the number of retries
            final var counter = new AtomicInteger(0);

            final Instant start = Instant.now();

            // attempt to assert a Runnable that throws a RuntimeException
            Eventually.assertThat((Runnable) () -> {
                    throw new RuntimeException("test");
                })
                .withTimeout(timeout)
                .isCompletedExceptionally()
                .hasCauseInstanceOf(RuntimeException.class);

            final var stop = Instant.now();

            // ensure the Timeout was waited
            assertThat(Duration.between(start, stop))
                .isGreaterThanOrEqualTo(duration);

            // ensure the Runnable was evaluated more than once
            assertThat(counter.get())
                .isGreaterThan(1);
        });
    }

    /**
     * Ensure a {@link Runnable} is retried when it throws an {@link AssertionError}.
     */
    @Test
    void shouldRetryARunnableThrowingAnAssertionError() {
        assertThrows(AssertionError.class, () -> {
            // establish a Timeout for Eventually.assertThat(...)
            final var duration = Duration.ofSeconds(2);
            final var timeout = Timeout.of(duration);

            // establish a counter to determine the number of retries
            final var counter = new AtomicInteger(0);

            final Instant start = Instant.now();

            // attempt to assert a Runnable that throws an AssertionError
            Eventually.assertThat((Runnable) () -> {
                    throw new AssertionError("test");
                })
                .withTimeout(timeout)
                .isCompletedExceptionally();

            final var stop = Instant.now();

            // ensure the Timeout was waited
            assertThat(Duration.between(start, stop))
                .isGreaterThanOrEqualTo(duration);

            // ensure the runnable was evaluated more than once
            assertThat(counter.get())
                .isGreaterThan(1);
        });
    }

    /**
     * Ensure an {@link Runnable} is invoked only once.
     */
    @Test
    void shouldInvokeARunnableOnceAndOnlyOnce() {
        // establish a Timeout for Eventually.assertThat(...)
        final var duration = Duration.ofSeconds(2);
        final var timeout = Timeout.of(duration);

        // establish a counter to determine the number of retries
        final var counter = new AtomicInteger(0);

        final var start = Instant.now();

        // attempt to assert a Runnable that is successfully invoked
        Eventually.assertThat(() -> {
                counter.incrementAndGet();
            }).withTimeout(timeout)
            .isCompleted();

        final Instant stop = Instant.now();

        // ensure the Timeout was waited
        assertThat(Duration.between(start, stop))
            .isLessThan(duration.dividedBy(2));

        // ensure the runnable was only executed once
        assertThat(counter.get())
            .isEqualTo(1);
    }

    /**
     * Ensure that the {@link AssertionError} thrown by {@link Eventually#assertThat}
     * preserves the underlying exception.
     */
    @Test
    void shouldPreserveUnderlyingCause() {
        final var timeout = Timeout.of(Duration.ofSeconds(2));
        final Function<String, String> normalFunction = s -> s;
        final RuntimeException runtimeException = new RuntimeException("exceptional");
        final Function<String, String> exceptionalFunction = s -> {
            throw runtimeException;
        };

        Eventually.assertThat(() -> normalFunction.apply("hello"))
            .withTimeout(timeout)
            .isEqualTo("hello");

        Eventually.assertThat(() -> exceptionalFunction.apply("hello"))
            .withTimeout(timeout)
            .isCompletedExceptionally()
            .hasCause(runtimeException);
    }

    //    /**
    //     * Ensure that an {@link IteratorPatternMatcher} can be used for matching with an {@link Iterable}.
    //     */
    //    @Test
    //    void shouldEventuallyMatchUsingIterablePatternWithAnIterator() {
    //
    //        final Iterable<Integer> iterable = () -> Stream.of(1, 2, 3, 4).iterator();
    //
    //        Eventually.assertThat(iterable, IteratorPatternMatchers.matches(2));
    //    }
    //
    //    /**
    //     * Ensure that an {@link IteratorPatternMatcher} can be used for matching with a {@link Stream}.
    //     */
    //    @Test
    //    void shouldEventuallyMatchUsingIterablePatternWithAStream() {
    //
    //        Eventually.assertThat(() -> Stream.of(1, 2, 3, 4), IteratorPatternMatchers.matches(2));
    //    }
    //
    //    /**
    //     * Ensure that a message {@link Supplier} works.
    //     */
    //    @Test
    //    void shouldHandleMessageSupplier() {
    //        final String[] message = { "before" };
    //        final Supplier<String> supplier = () -> message[0];
    //
    //        assertThat(Assertions.assertThrows(AssertionError.class, () -> Eventually.assertThat(() -> message[0] = "after",
    //            is(false), Timeout.ofSeconds(2), Message.of(supplier))).getMessage(), startsWith("after"));
    //    }
    //
    //    /**
    //     * Ensure that a message {@link Supplier} works with an Exception.
    //     */
    //    @Test
    //    void shouldHandleMessageSupplierWithException() {
    //        final String[] message = { "before" };
    //        final Supplier<String> supplier = () -> message[0];
    //
    //        assertThat(Assertions.assertThrows(AssertionError.class, () -> Eventually.assertThat(() -> {
    //                message[0] = "after";
    //                throw new RuntimeException("problem");
    //            },
    //            is(false), Timeout.ofSeconds(1), Message.of(supplier))).getMessage(), startsWith("after"));
    //    }
    //
    //    /**
    //     * Ensure that when {@link Eventually#assertThat(Iterable, IteratorPatternMatcher, com.workday.quark.option.Option...)
    //     * throws {@link AssertionError}, the error's {@link AssertionError#getMessage() message} contains a {@link String}
    //     * representation of the last, non-matching value.
    //     */
    //    @Test
    //    void shouldSurfaceFailedValueFromLastIteration() {
    //        final List<String> values = Arrays.asList("foo", "bar", "baz");
    //
    //        try {
    //            Eventually.assertThat(values, IteratorPatternMatchers.matches("quux").then().ends(), Timeout.ofMillis(10));
    //            fail(); // Eventually.assertThat(...) should have thrown AssertionError
    //        }
    //        catch (final AssertionError e) {
    //            // make message single-line (friendlier for regexes)
    //            final String message = e.getMessage().replace(System.lineSeparator(), " ");
    //
    //            // ensure message matches "[anything] [space] foo [space] bar [space] baz [space] [anything]"
    //            assertThat(message, matchesRegex(".*\\sfoo\\s+bar\\s+baz\\s.*"));
    //        }
    //    }
}
