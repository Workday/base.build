package build.base.flow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

/**
 * Tests for {@link SubscriberRegistry}s.
 *
 * @author brian.oliver
 * @since Feb-2020
 */
class SubscriberRegistryTests {

    /**
     * Ensure a {@link SubscriberRegistry} can be created.
     */
    @Test
    void shouldCreate() {
        new SubscriberRegistry<>();
    }

    /**
     * Creates an unbounded mocked {@link Subscriber}.
     *
     * @return a {@link Subscriber}
     */
    @SuppressWarnings("unchecked")
    private <T> Subscriber<T> createUnboundedMockSubscriber() {
        final Subscriber<T> subscriber = Mockito.mock(Subscriber.class);
        doCallRealMethod().when(subscriber).onSubscribe(any());
        return subscriber;
    }

    /**
     * Ensure a {@link Subscriber} can be subscribed to a {@link SubscriberRegistry}.
     */
    @Test
    void shouldSubscribe() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();
        subscriberRegistry.subscribe(subscriber);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(0)).onNext(any());
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onError(any());
    }

    /**
     * Ensure a {@link Subscriber} can cancel its own subscription to a {@link SubscriberRegistry}.
     */
    @Test
    void shouldCancelSubscription() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();
        subscriberRegistry.subscribe(subscriber);

        final ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);

        verify(subscriber, times(1)).onSubscribe(captor.capture());
        verify(subscriber, times(0)).onNext(any());
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onError(any());

        captor.getValue().cancel();

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(0)).onNext(any());
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(0)).onError(any());
    }

    /**
     * Ensure a {@link Subscriber} can observe a value published by a {@link SubscriberRegistry}.
     */
    @Test
    void shouldObserveAnItem() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();
        subscriberRegistry.subscribe(subscriber);

        final String message = "G'day Mate";

        subscriberRegistry.publish(message);

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(1)).onNext(captor.capture());
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onError(any());

        assertThat(captor.getValue())
            .isEqualTo(message);
    }

    /**
     * Ensure a {@link Subscriber} can observe {@link SubscriberRegistry#complete()}.
     */
    @Test
    void shouldObserveCompletion() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();
        subscriberRegistry.subscribe(subscriber);

        subscriberRegistry.complete();

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(0)).onNext(any());
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(0)).onError(any());
    }

    /**
     * Ensure a {@link Subscriber} can observe a error published by a {@link SubscriberRegistry}.
     */
    @Test
    void shouldObserveAnError() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();
        subscriberRegistry.subscribe(subscriber);

        final Throwable throwable = new RuntimeException("Oops!");
        subscriberRegistry.error(throwable);

        final ArgumentCaptor<Throwable> captor = ArgumentCaptor.forClass(Throwable.class);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(0)).onNext(any());
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(1)).onError(captor.capture());

        assertThat(captor.getValue())
            .isEqualTo(throwable);
    }

    /**
     * Ensure a {@link Subscriber} does not observe items published by a {@link SubscriberRegistry}
     * when subscribing.
     */
    @Test
    void shouldNotObserveItemDuringSubscription() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();

        doAnswer(invocation -> {
            invocation.getArgument(0, Subscription.class).request(Long.MAX_VALUE);

            subscriberRegistry.publish("Should Never See This!");
            return null;
        }).when(subscriber).onSubscribe(any());

        subscriberRegistry.subscribe(subscriber);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(0)).onNext(any());
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onError(any());
    }

    /**
     * Ensure a {@link Subscriber} can cancel their subscription with a {@link SubscriberRegistry}
     * when subscribing.
     */
    @Test
    void shouldCancelDuringSubscription() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();

        doAnswer(invocation -> {
            final Subscription subscription = invocation.getArgument(0);
            subscription.cancel();
            return null;
        }).when(subscriber).onSubscribe(any());

        subscriberRegistry.subscribe(subscriber);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(0)).onNext(any());
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(0)).onError(any());
    }

    /**
     * Ensure a {@link Subscriber} can observe items published in order by a {@link SubscriberRegistry}.
     */
    @Test
    void shouldObserveItemsInOrder() {
        final SubscriberRegistry<Integer> subscriberRegistry = new SubscriberRegistry<>();

        final Subscriber<Integer> subscriber = createUnboundedMockSubscriber();
        subscriberRegistry.subscribe(subscriber);

        subscriberRegistry.publish(1);
        subscriberRegistry.publish(2);
        subscriberRegistry.publish(3);
        subscriberRegistry.publish(4);

        final ArgumentCaptor<Integer> captor = ArgumentCaptor.forClass(Integer.class);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(4)).onNext(captor.capture());
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(0)).onError(any());

        assertThat(captor.getAllValues())
            .containsExactly(1, 2, 3, 4);
    }

    /**
     * Ensure {@link SubscriberRegistry} allows completion of publishing.
     */
    @Test
    void shouldPublishAndComplete() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();
        registry.subscribe(subscriber);

        final String message = "G'day Mate";

        registry.publish(message);

        assertThat(registry.onComplete())
            .isNotDone();

        registry.complete();

        final ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(1)).onNext(captor.capture());
        verify(subscriber, times(1)).onComplete();
        verify(subscriber, times(0)).onError(any());

        assertThat(registry.onComplete())
            .isCompleted();

        assertThat(captor.getValue())
            .isEqualTo(message);
    }

    /**
     * Ensure {@link SubscriberRegistry} allows completion of publishing due to an error.
     */
    @Test
    void shouldPublishAndCompleteDueToError() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        final Subscriber<String> subscriber = createUnboundedMockSubscriber();
        registry.subscribe(subscriber);

        final String message = "G'day Mate";
        final Throwable throwable = new RuntimeException("Oops!");

        registry.publish(message);
        registry.error(throwable);

        final ArgumentCaptor<String> itemCaptor = ArgumentCaptor.forClass(String.class);
        final ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);

        verify(subscriber, times(1)).onSubscribe(any());
        verify(subscriber, times(1)).onNext(itemCaptor.capture());
        verify(subscriber, times(0)).onComplete();
        verify(subscriber, times(1)).onError(throwableCaptor.capture());

        assertThat(itemCaptor.getValue())
            .isEqualTo(message);

        assertThat(throwableCaptor.getValue())
            .isEqualTo(throwable);
    }

    /**
     * Ensure {@link SubscriberRegistry} prevents subscription once publishing has completed.
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldPreventSubscriptionWhenCompleted() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        registry.complete();

        assertThrows(IllegalStateException.class, () -> registry.subscribe(Mockito.mock(Subscriber.class)));
    }

    /**
     * Ensure {@link SubscriberRegistry} prevents subscription once publishing has completed due to a error.
     */
    @Test
    @SuppressWarnings("unchecked")
    void shouldPreventSubscriptionWhenError() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();
        final Throwable throwable = new RuntimeException("Oops!");

        registry.error(throwable);

        final Throwable cause = assertThrows(IllegalStateException.class,
            () -> registry.subscribe(Mockito.mock(Subscriber.class)));

        assertThat(cause.getCause())
            .isEqualTo(throwable);
    }

    /**
     * Ensure {@link SubscriberRegistry} will complete only once, and subsequent completion and error attempts will
     * have no effect (return false).
     */
    @Test
    void shouldCompletedOnlyOnce() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        final boolean firstTry = registry.complete();
        final boolean secondTry = registry.complete();
        final boolean thirdTry = registry.error(new Exception());

        assertThat(firstTry)
            .isTrue();

        assertThat(secondTry)
            .isFalse();

        assertThat(thirdTry)
            .isFalse();
    }
}
