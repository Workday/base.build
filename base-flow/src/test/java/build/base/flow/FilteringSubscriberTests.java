package build.base.flow;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link FilteringSubscriber}s.
 *
 * @author brian.oliver
 * @since Aug-2018
 */
class FilteringSubscriberTests {

    private static final Subscription NO_OP_SUBSCRIPTION = new Subscription() {
        @Override public void request(final long number) {}
        @Override public void cancel() {}
    };

    /**
     * Ensure a {@link FilteringSubscriber} can be created.
     */
    @Test
    void shouldCreateAFilteringSubscriber() {
        assertThat(FilteringSubscriber.<String>of(s -> s.startsWith("Hello"), new RecordingSubscriber<>()))
            .isNotNull();
    }

    /**
     * Ensure a {@link FilteringSubscriber} can filter values into another {@link Subscriber}.
     */
    @Test
    void shouldObserveFilteredValues() {

        final var recordingSubscriber = new RecordingSubscriber<String>();
        final var filteringObserver = FilteringSubscriber.of(s -> s.startsWith("Hello"), recordingSubscriber);

        filteringObserver.onSubscribe(NO_OP_SUBSCRIPTION);

        assertThat(recordingSubscriber.isSubscribed())
            .isTrue();

        filteringObserver.onNext("Hello World");

        assertThat(recordingSubscriber.items())
            .hasSize(1);

        assertThat(recordingSubscriber.items().findFirst())
            .contains("Hello World");

        filteringObserver.onNext("G'day Mate");

        assertThat(recordingSubscriber.items())
            .hasSize(1);

        assertThat(recordingSubscriber.items().findFirst())
            .contains("Hello World");

        filteringObserver.onNext("Hello Mate");

        assertThat(recordingSubscriber.items())
            .hasSize(2);

        assertThat(recordingSubscriber.items().findFirst())
            .contains("Hello World");

        assertThat(recordingSubscriber.items().skip(1).findFirst())
            .contains("Hello Mate");
    }

    /**
     * Ensure a {@link FilteringSubscriber} completes the adapted {@link Subscriber}.
     */
    @Test
    void shouldObserveCompletionWhenFilteringSubscriberObservesCompletion() {

        final var recordingSubscriber = new RecordingSubscriber<String>();
        final var filteringObserver = FilteringSubscriber.of(s -> s.startsWith("Hello"), recordingSubscriber);

        filteringObserver.onSubscribe(NO_OP_SUBSCRIPTION);

        filteringObserver.onComplete();

        assertThat(recordingSubscriber.isCompleted())
            .isTrue();
    }

    /**
     * Ensure a {@link FilteringSubscriber} errors the adapted {@link Subscriber}.
     */
    @Test
    void shouldObserveAnErrorWhenFilteringObserverObservesAnError() {

        final var recordingSubscriber = new RecordingSubscriber<String>();
        final var filteringObserver = FilteringSubscriber.of(s -> s.startsWith("Hello"), recordingSubscriber);

        filteringObserver.onSubscribe(NO_OP_SUBSCRIPTION);

        assertThat(recordingSubscriber.isSubscribed())
            .isTrue();

        final var throwable = new IllegalStateException("Oops!");

        filteringObserver.onError(throwable);

        assertThat(recordingSubscriber.isErrored())
            .isTrue();

        assertThat(recordingSubscriber.throwable())
            .contains(throwable);
    }
}
