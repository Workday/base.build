package build.base.flow;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link FilteringSubscriber}s.
 *
 * @author brian.oliver
 * @since Aug-2018
 */
class FilteringSubscriberTests {

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

        final var subscription = Mockito.mock(Subscription.class);
        final var recordingSubscriber = new RecordingSubscriber<String>();
        final var filteringObserver = FilteringSubscriber.of(s -> s.startsWith("Hello"), recordingSubscriber);

        filteringObserver.onSubscribe(subscription);

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

        final var subscription = Mockito.mock(Subscription.class);
        final var recordingSubscriber = new RecordingSubscriber<String>();
        final var filteringObserver = FilteringSubscriber.of(s -> s.startsWith("Hello"), recordingSubscriber);

        filteringObserver.onSubscribe(subscription);

        filteringObserver.onComplete();

        assertThat(recordingSubscriber.isCompleted())
            .isTrue();
    }

    /**
     * Ensure a {@link FilteringSubscriber} errors the adapted {@link Subscriber}.
     */
    @Test
    void shouldObserveAnErrorWhenFilteringObserverObservesAnError() {

        final var subscription = Mockito.mock(Subscription.class);

        final var recordingSubscriber = new RecordingSubscriber<String>();
        final var filteringObserver = FilteringSubscriber.of(s -> s.startsWith("Hello"), recordingSubscriber);

        filteringObserver.onSubscribe(subscription);

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
