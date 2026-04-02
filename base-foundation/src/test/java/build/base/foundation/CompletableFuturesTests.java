package build.base.foundation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for the {@link CompletableFutures} class.
 *
 * @author mark.falco
 * @since October-2019
 */
class CompletableFuturesTests {

    /**
     * Ensure {@link CompletableFutures#getOrThrow(CompletableFuture)} and
     * {@link CompletableFutures#getOrThrow(CompletableFuture, long, TimeUnit)} return a {@link CompletableFuture}'s
     * value.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldGetViaGetOrThrow(final boolean withTimeout) {

        final var future = new CompletableFuture<>();
        final var completedObject = new Object();

        future.complete(completedObject);

        try {
            final Object futureObject;
            if (withTimeout) {
                futureObject = CompletableFutures.getOrThrow(future, 10, TimeUnit.MILLISECONDS);
            }
            else {
                futureObject = CompletableFutures.getOrThrow(future);
            }
            assertThat(completedObject)
                .isSameAs(futureObject);
        }
        catch (final Exception e) {
            fail(e);
        }
    }

    /**
     * Ensure the {@code getOrThrow} methods unwrap an {@link Exception} when the future is
     * {@link CompletableFuture#completeExceptionally(Throwable) completed exceptionally}.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldUnwrapExceptionViaGetOrThrow(final boolean withTimeout) {

        final var future = new CompletableFuture<>();
        final var exception = new Exception();

        future.completeExceptionally(exception);

        try {
            if (withTimeout) {
                CompletableFutures.getOrThrow(future, 10, TimeUnit.MILLISECONDS);
            }
            else {
                CompletableFutures.getOrThrow(future);
            }
            fail();
        }
        catch (final Exception caughtException) {
            assertThat(caughtException)
                .isSameAs(exception);
        }
    }

    /**
     * Ensure the {@code getOrThrow} methods unwrap an {@link Error} when the future is
     * {@link CompletableFuture#completeExceptionally(Throwable) completed with an error}.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldUnwrapErrorViaGetOrThrow(final boolean withTimeout) {

        final var future = new CompletableFuture<>();
        final var completedError = new AssertionError();

        future.completeExceptionally(completedError);

        try {
            if (withTimeout) {
                CompletableFutures.getOrThrow(future, 10, TimeUnit.MILLISECONDS);
            }
            else {
                CompletableFutures.getOrThrow(future);
            }
            fail();
        }
        catch (final Throwable throwable) {
            assertThat(throwable)
                .isSameAs(completedError);
        }
    }

    /**
     * Ensure the {@code getOrThrow} methods do not unwrap a {@link Throwable} when the future is
     * {@link CompletableFuture#completeExceptionally(Throwable) completed with a throwable}.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldNotUnwrapThrowableViaGetOrThrow(final boolean withTimeout) {

        final var future = new CompletableFuture<>();
        final var completedThrowable = new Throwable();

        future.completeExceptionally(completedThrowable);

        try {
            if (withTimeout) {
                CompletableFutures.getOrThrow(future, 10, TimeUnit.MILLISECONDS);
            }
            else {
                CompletableFutures.getOrThrow(future);
            }
            fail();
        }
        catch (final ExecutionException executionException) {
            assertThat(executionException.getCause())
                .isSameAs(completedThrowable);
        }
        catch (final Exception e) {
            fail(e);
        }
    }

    /**
     * Ensure the {@code getOrThrow} methods throw {@link InterruptedException} when interrupted.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldThrowInterruptedViaGetOrThrow(final boolean withTimeout) {

        final var future = new CompletableFuture<>();

        Thread.currentThread().interrupt();

        try {
            if (withTimeout) {
                CompletableFutures.getOrThrow(future, 10, TimeUnit.MILLISECONDS);
            }
            else {
                CompletableFutures.getOrThrow(future);
            }
            fail();
        }
        catch (final InterruptedException e) {
            // expected
        }
        catch (final Exception e) {
            fail(e);
        }
    }

    /**
     * Ensure the {@code getOrThrow} methods throw {@link CancellationException} when their future is
     * {@link CompletableFuture#cancel(boolean) cancelled}.
     */
    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldThrowCancellationViaGetOrThrow(final boolean withTimeout) {

        final var future = new CompletableFuture<>();

        future.cancel(true);

        try {
            if (withTimeout) {
                CompletableFutures.getOrThrow(future, 10, TimeUnit.MILLISECONDS);
            }
            else {
                CompletableFutures.getOrThrow(future);
            }
            fail();
        }
        catch (final CancellationException e) {
            // expected
        }
        catch (final Exception e) {
            fail(e);
        }
    }

    /**
     * Ensure {@link CompletableFutures#getOrThrow(CompletableFuture, long, TimeUnit)} throws a
     * {@link TimeoutException} if the timeout expires before the future is completed.
     */
    @Test
    void shouldThrowTimeoutViaGetOrThrow() {

        final var future = new CompletableFuture<>();

        try {
            CompletableFutures.getOrThrow(future, 10, TimeUnit.MILLISECONDS);
            fail();
        }
        catch (final TimeoutException e) {
            // expected
        }
        catch (final Exception e) {
            fail(e);
        }
    }

    /**
     * Ensure we can complete a future via {@link CompletableFutures#complete(CompletableFuture, Object, Throwable)}.
     */
    @Test
    void shouldCompleteViaComplete()
        throws Exception {

        final var future = new CompletableFuture<Integer>();

        CompletableFutures.complete(future, 42, (Throwable) null);

        assertThat(future.get())
            .isEqualTo(42);
    }

    /**
     * Ensure we can exceptionally complete a future via
     * {@link CompletableFutures#complete(CompletableFuture, Object, Throwable)}.
     */
    @Test
    void shouldCompleteExceptionallyViaComplete() {

        final var future = new CompletableFuture<>();

        CompletableFutures.complete(future, null, new Exception());

        assertThat(future.isCompletedExceptionally())
            .isTrue();
    }
}
