package build.base.flow;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link MappingSubscriber}s.
 *
 * @author brian.oliver
 * @since Aug-2018
 */
class MappingSubscriberTests {

    /**
     * Ensure a {@link MappingSubscriber} can be created.
     */
    @Test
    void shouldCreateAMappingSubscriber() {
        MappingSubscriber.of(String::length, new RecordingSubscriber<>());
    }

    /**
     * Ensure a {@link MappingSubscriber} can map values into another {@link Subscriber}.
     */
    @Test
    void shouldObserveMapValues() {

        final var subscription = Mockito.mock(Subscription.class);
        final var recordingObserver = new RecordingSubscriber<Integer>();
        final var mappingObserver = MappingSubscriber.of(String::length, recordingObserver);

        mappingObserver.onSubscribe(subscription);

        assertThat(recordingObserver.isSubscribed())
            .isTrue();

        mappingObserver.onNext("Hello");

        assertThat(recordingObserver.items())
            .hasSize(1);

        assertThat(recordingObserver.items().findFirst())
            .contains(5);

        mappingObserver.onNext("Mate");

        assertThat(recordingObserver.items())
            .hasSize(2);

        assertThat(recordingObserver.items().skip(1).findFirst())
            .contains(4);
    }

    /**
     * Ensure a {@link MappingSubscriber} completes the adapted {@link Subscriber}.
     */
    @Test
    void shouldObserveCompletionWhenMappingSubscriberObservesCompletion() {

        final var subscription = Mockito.mock(Subscription.class);
        final var recordingObserver = new RecordingSubscriber<Integer>();
        final var mappingObserver = MappingSubscriber.of(String::length, recordingObserver);

        mappingObserver.onSubscribe(subscription);

        mappingObserver.onComplete();

        assertThat(recordingObserver.isCompleted())
            .isTrue();
    }

    /**
     * Ensure a {@link MappingSubscriber} errors the adapted {@link Subscriber}.
     */
    @Test
    void shouldObserveAnErrorWhenMappingSubscriberObservesAnError() {

        final var subscription = Mockito.mock(Subscription.class);
        final var recordingObserver = new RecordingSubscriber<Integer>();
        final var mappingObserver = MappingSubscriber.of(String::length, recordingObserver);

        mappingObserver.onSubscribe(subscription);

        assertThat(recordingObserver.isSubscribed())
            .isTrue();

        final Throwable throwable = new IllegalStateException("Oops!");

        mappingObserver.onError(throwable);

        assertThat(recordingObserver.isErrored())
            .isTrue();

        assertThat(recordingObserver.throwable())
            .contains(throwable);
    }
}
