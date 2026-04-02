package build.base.retryable;

import build.base.option.Timeout;
import build.base.retryable.option.MaximumDelay;
import build.base.retryable.option.MinimumDelay;
import build.base.retryable.option.RetryFrequency;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.Stack;

import static build.base.retryable.ConditionallyRetryable.retrying;
import static java.time.Duration.ofMillis;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link BlockingRetry}.
 *
 * @author brian.oliver
 * @since Dec-2017
 */
class BlockingRetryTests {

    /**
     * The timeout for {@link BlockingRetry} tests.
     */
    private static final Duration TIMEOUT = ofMillis(4000);

    /**
     * Ensure a {@link BlockingRetry} created with a {@link Timeout} retries {@link EphemerallyFailingRetryable}s
     * for at least the specified time.
     */
    @Test
    void shouldRetryForASpecificTimeout() {
        assertTimeout(TIMEOUT, () -> {
            final var duration = Duration.ofSeconds(2);
            final var timeout = Timeout.of(duration);

            final var start = Instant.now();

            try {
                final var blockingRetry = BlockingRetry.of(new EphemerallyFailingRetryable<>(), timeout);

                blockingRetry.get();

                fail("Expected a PermanentFailureException");
            }
            catch (final PermanentFailureException e) {
                final Instant stop = Instant.now();

                // ensure at least the specified timeout was used
                final var period = Duration.between(start, stop).toMillis();
                final var minimum = (long) (duration.toMillis() * 0.75);

                assertThat(period)
                    .isGreaterThan(minimum);

                assertThat(e)
                    .hasCauseInstanceOf(EphemeralFailureException.class);
            }
            catch (final Exception e) {
                fail("Failed with " + e);
            }
        });
    }

    /**
     * Ensure a {@link BlockingRetry} fails fast when encountering a {@link PermanentFailureException}.
     */
    @Test
    void shouldFailFast() {
        assertTimeout(TIMEOUT, () -> {
            final var duration = Duration.ofSeconds(2);
            final var timeout = Timeout.of(duration);

            final var start = Instant.now();

            try {
                final var blockingRetry = BlockingRetry.of(new PermanentlyFailingRetryable<>(), timeout);

                blockingRetry.get();
            }
            catch (final PermanentFailureException e) {
                final var stop = Instant.now();

                // ensure the timeout wasn't waited
                assertThat(Duration.between(start, stop))
                    .isLessThan(ofMillis(100));
            }
            catch (final Exception e) {
                fail("Failed with " + e);
            }
        });
    }

    /**
     * Ensure a {@link BlockingRetry} succeeds fast when encountering a non-null constant.
     */
    @Test
    void shouldSucceedFastWithNonNullConstant() {
        assertTimeout(TIMEOUT, () -> {
            final var duration = Duration.ofSeconds(2);
            final var timeout = Timeout.of(duration);

            final var start = Instant.now();

            try {
                final var blockingRetry = BlockingRetry.of(Retryable.of(42), timeout);

                blockingRetry.get();

                final var stop = Instant.now();

                // ensure the timeout wasn't waited
                assertThat(Duration.between(start, stop))
                    .isLessThan(ofMillis(100));
            }
            catch (final Exception e) {
                fail("Failed with " + e);
            }
        });
    }

    /**
     * Ensure a {@link BlockingRetry} succeeds fast when encountering a null.
     */
    @Test
    void shouldSucceedFastWithNullConstant() {
        assertTimeout(TIMEOUT, () -> {
            final var duration = Duration.ofSeconds(2);
            final var timeout = Timeout.of(duration);

            final var start = Instant.now();

            try {
                final var blockingRetry = BlockingRetry.of(Retryable.of((String) null), timeout);

                blockingRetry.get();

                final var stop = Instant.now();

                // ensure the timeout wasn't waited
                assertThat(Duration.between(start, stop))
                    .isLessThan(ofMillis(100));
            }
            catch (final Exception e) {
                fail("Failed with " + e);
            }
        });
    }

    /**
     * Ensure a {@link BlockingRetry} waits at least a specified {@link MinimumDelay} before retrying.
     */
    @Test
    void shouldDelayRetrying() {
        assertTimeout(TIMEOUT, () -> {
            final var duration = Duration.ofSeconds(2);

            final var start = Instant.now();

            try {
                final var blockingRetry = BlockingRetry.of(Retryable.of("Hello"), MinimumDelay.of(duration));

                blockingRetry.get();

                final var stop = Instant.now();

                // ensure the minimum duration was waited
                assertThat(Duration.between(start, stop))
                    .isGreaterThanOrEqualTo(duration);
            }
            catch (final Exception e) {
                fail("Failed with " + e);
            }
        });
    }

    /**
     * Ensure a {@link BlockingRetry} retries a final time after timing out
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldDelayRetryingFinalTime() {
        assertTimeout(TIMEOUT, () -> {

            final Retryable<Integer> retryable = mock(Retryable.class);
            when(retryable.get())
                .thenThrow(new EphemeralFailureException("First Failure"))
                .thenReturn(123);

            final var value = BlockingRetry.retry(
                retryable,
                Timeout.ofSeconds(2),
                MinimumDelay.of(Duration.ofMillis(100)),
                RetryFrequency.once(Duration.ofMinutes(1)));

            assertEquals(123, value);

            verify(retryable, times(2)).get();
        });
    }

    /**
     * Ensure a {@link BlockingRetry} with a timeout of 0 only executes the {@link Retryable} once
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldRetryOnceWithZeroTimeout() {
        assertTimeout(TIMEOUT, () -> {

            final Retryable<Integer> retryable = mock(Retryable.class);
            when(retryable.get())
                .thenThrow(new EphemeralFailureException("First Failure"));

            assertThrows(PermanentFailureException.class, () -> BlockingRetry.retry(retryable, Timeout.ofMillis(0)));

            verify(retryable, times(1)).get();
        });
    }

    /**
     * Ensure a {@link BlockingRetry} waits at most a specified {@link MinimumDelay} when retrying.
     */
    @Test
    void shouldUseAMaximumDelay() {
        assertTimeout(TIMEOUT, () -> {
            final var maximumDuration = Duration.ofSeconds(1);
            final var timeoutDuration = Duration.ofSeconds(2);

            final var start = Instant.now();

            try {
                final var blockingRetry = BlockingRetry.of(
                    new EphemerallyFailingRetryable<>(),
                    MaximumDelay.of(maximumDuration),
                    Timeout.of(timeoutDuration),
                    RetryFrequency.once(Duration.ofSeconds(5)));

                blockingRetry.get();

                fail("Expected a PermanentFailureException");
            }
            catch (final PermanentFailureException e) {
                final var stop = Instant.now();

                // ensure at least the specified maximum duration was used
                final var period = Duration.between(start, stop).toMillis();
                final var minimum = (long) (maximumDuration.toMillis() * 0.95);

                assertThat(period)
                    .isGreaterThan(minimum);

                // ensure the timeout wasn't used
                assertThat(Duration.between(start, stop)
                    .compareTo(timeoutDuration))
                    .isLessThan(0);
            }
            catch (final Exception e) {
                fail("Failed with " + e);
            }
        });
    }

    /**
     * Ensure a {@link BlockingRetry} retries once.
     */
    @Test
    void shouldRetryOnce() {
        assertTimeout(TIMEOUT, () -> {
            final var maximumDelay = MaximumDelay.autodetect();

            // we should wait this long (at most), but in reality we won't as we only retry once for the maximum delay
            // (which is usually less than the timeout)
            final var timeoutDuration = Duration.ofSeconds(2);

            // the retry once duration
            final var duration = maximumDelay.get();

            final var start = Instant.now();

            try {
                final var blockingRetry = BlockingRetry.of(
                    new EphemerallyFailingRetryable<>(),
                    Timeout.of(timeoutDuration),
                    RetryFrequency.once(duration));

                blockingRetry.get();

                fail("Expected a PermanentFailureException");
            }
            catch (final PermanentFailureException e) {
                final var stop = Instant.now();

                // ensure at least the specified duration was used
                final var period = Duration.between(start, stop).toMillis();
                final var minimum = (long)
                    ((duration.compareTo(timeoutDuration) < 0
                        ? duration
                        : timeoutDuration)
                        .toMillis() * 0.95);

                assertThat(period)
                    .isGreaterThan(minimum);

                // ensure the timeout wasn't used
                assertThat(Duration.between(start, stop)
                    .compareTo(timeoutDuration))
                    .isLessThan(0);
            }
            catch (final Exception e) {
                fail("Failed with " + e);
            }
        });
    }

    /**
     * Ensure a {@link BlockingRetry} retries when using a {@link ConditionallyRetryable}.
     */
    @Test
    void shouldRetryConditionallyRetryables() {
        assertTimeout(ofMillis(1000), () -> {
            // a stack of values to pop and return from a retryable

            final Stack<Integer> stack = new Stack<Integer>();
            stack.push(1);
            stack.push(43);
            stack.push(3);
            stack.push(42);

            // the retryable popping and returning values from the stack
            final Retryable<Integer> retryable = stack::pop;

            // establish the blocking retry when values < 42 or > 42
            final BlockingRetry<Integer> blockingRetry = BlockingRetry.of(
                retrying(retryable).when(1).when(v -> v < 42).when(v -> v > 42));

            // ensure we only get 42
            assertThat(blockingRetry.get())
                .isEqualTo(42);
        });
    }

    /**
     * Ensure that a {@link UnsupportedOperationException} thrown by a {@link BlockingRetry} is treated as a
     * {@link PermanentFailureException}
     */
    @Test
    void shouldFailFastWithUnsupportedOperationExceptions() {
        final Retryable<Void> retryable = () -> {
            throw new UnsupportedOperationException();
        };

        assertThrows(PermanentFailureException.class, () -> BlockingRetry.retry(retryable));
    }

    /**
     * Ensure that when a {@link EphemeralFailureException} is thrown and the {@link BlockingRetry} is
     * {@link BlockingRetry#isDone()} that it is treated as a {@link PermanentFailureException}
     */
    @Test
    void shouldFailFastWhenEphemeralFailureExceptionRetryableIsDone() {
        final Retryable<Void> retryable = new Retryable<Void>() {
            @Override
            public Void get()
                throws EphemeralFailureException, PermanentFailureException {
                throw new EphemeralFailureException("Test");
            }

            @Override
            public boolean isDone() {
                return true;
            }
        };

        assertThrows(PermanentFailureException.class, () -> BlockingRetry.retry(retryable));
    }

    /**
     * Ensure that {@link AssertionError} and {@link RuntimeException} are retired and that that {@link Error} is
     * laundered
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldRetryWhenErrorAndRuntimeExceptionsAreExperienced() {
        final Retryable<Integer> retryable = mock(Retryable.class);
        when(retryable.isDone()).thenReturn(false);
        when(retryable.get())
            .thenThrow(AssertionError.class)
            .thenThrow(RuntimeException.class)
            .thenThrow(Error.class);

        assertThrows(Error.class, () -> BlockingRetry.retry(retryable));
        verify(retryable, times(3)).get();
    }

    /**
     * Ensure that a {@link Retryable} times out before a {@link MinimumDelay}.
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldTimeoutBeforeMinimumDelay() {
        assertTimeout(TIMEOUT, () -> {

            final Retryable<Integer> retryable = mock(Retryable.class);
            when(retryable.get()).thenReturn(123);

            final int value = BlockingRetry.retry(retryable,
                Timeout.ofMillis(1),
                MinimumDelay.of(Duration.ofMinutes(1)));

            assertEquals(123, value);
        });
    }
}
