package build.base.flow;

import build.base.foundation.predicate.Predicates;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CompletingSubscriber}s.
 *
 * @author brian.oliver
 * @since Aug-2018
 */
class CompletingSubscriberTests {

    /**
     * Ensure a {@link CompletingSubscriber} can be created.
     */
    @Test
    void shouldCreateACompletingObserver() {
        new CompletingSubscriber<>();
    }

    /**
     * Ensure a {@link CompletableFuture} is completed when an item is observed after the
     * {@link CompletableFuture} was registered.
     */
    @Test
    void shouldCompleteWhenItemObservedAfterCompletableFutureCreation() {
        final var subscriber = new CompletingSubscriber<String>();

        final var future = subscriber.when(Predicates.always());

        subscriber.onNext("Hello World");

        assertThat(future)
            .isCompletedWithValue("Hello World");
    }

    /**
     * Ensure a {@link CompletableFuture} is not completed when an item is observed that doesn't satisfy
     * a {@link Predicate} after the {@link CompletableFuture} was registered.
     */
    @Test
    void shouldNotCompleteWhenItemObservedAfterCompletableFutureCreation() {
        final var subscriber = new CompletingSubscriber<String>();

        final var future = subscriber.when(s -> s.startsWith("G'day"));

        subscriber.onNext("Hello World");

        assertThat(future)
            .isNotDone();
    }

    /**
     * Ensure a {@link CompletableFuture} is not completed when an item is observed before the
     * {@link CompletableFuture} was registered.
     */
    @Test
    void shouldCompleteWhenItemObservedBeforeCompletableFutureCreation() {
        final var subscriber = new CompletingSubscriber<String>();

        subscriber.onNext("Hello World");

        final CompletableFuture<String> future = subscriber.when(Predicates.always());

        assertThat(future)
            .isNotDone();
    }

    /**
     * Ensure a {@link CompletableFuture} is not completed when an item is observed that doesn't satisfy
     * a {@link Predicate} before the {@link CompletableFuture} was registered.
     */
    @Test
    void shouldNotCompleteWhenItemObservedBeforeCompletableFutureCreation() {
        final var subscriber = new CompletingSubscriber<String>();

        subscriber.onNext("Hello World");

        final CompletableFuture<String> future = subscriber.when(s -> s.startsWith("G'day"));

        assertThat(future)
            .isNotDone();

        subscriber.onNext("G'day");

        assertThat(future)
            .isCompletedWithValue("G'day");
    }

    /**
     * Ensure a {@link CompletableFuture} is completed with a mapped value when an item is observed.
     */
    @Test
    void shouldCompleteWithMappedValue() {
        final var subscriber = new CompletingSubscriber<String>();

        final var future = subscriber.when(Predicates.always(), s -> s.toUpperCase());

        subscriber.onNext("Hello World");

        assertThat(future)
            .isCompletedWithValue("HELLO WORLD");
    }

    /**
     * Ensure a {@link CompletableFuture} is cancellable when an item is observed.
     */
    @Test
    void shouldCancelACompletableFuture() {
        final var subscriber = new CompletingSubscriber<String>();

        final var future = subscriber.when(Predicates.always(), (_, f) -> f.cancel(true));

        subscriber.onNext("Hello World");

        assertThat(future)
            .isDone();

        assertThat(future)
            .isCancelled();
    }

    /**
     * Ensure {@link CompletableFuture}s are cancelled when a {@link CompletingSubscriber} is completed.
     */
    @Test
    void shouldCancelACompletableFuturesWhenCompletionObserved() {
        final var subscriber = new CompletingSubscriber<String>();

        final var future1 = subscriber.when("Hello");
        final var future2 = subscriber.when("World");

        subscriber.onComplete();

        assertThat(future1)
            .isCancelled();

        assertThat(future2)
            .isCancelled();
    }

    /**
     * Ensure {@link CompletableFuture}s are completed exceptionally when a {@link CompletingSubscriber} is errored.
     */
    @Test
    void shouldExceptionallyCompletableFuturesWhenErrorObserved() {
        final var subscriber = new CompletingSubscriber<String>();

        final var future1 = subscriber.when("Hello");
        final var future2 = subscriber.when("World");

        final var throwable = new IllegalStateException("Oops!");

        subscriber.onError(throwable);

        assertThat(future1)
            .isCompletedExceptionally();

        future1.whenComplete((_, t) -> assertThat(t).hasCause(throwable));

        assertThat(future2)
            .isCompletedExceptionally();

        future2.whenComplete((_, t) -> assertThat(t).hasCause(throwable));
    }
}
