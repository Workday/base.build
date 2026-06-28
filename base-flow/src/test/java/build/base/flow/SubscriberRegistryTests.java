package build.base.flow;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
     * Ensure a {@link Subscriber} can be subscribed to a {@link SubscriberRegistry}.
     */
    @Test
    void shouldSubscribe() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>();
        subscriberRegistry.subscribe(subscriber);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).isEmpty();
        assertThat(subscriber.isCompleted()).isFalse();
        assertThat(subscriber.throwable()).isEmpty();
    }

    /**
     * Ensure a {@link Subscriber} can cancel its own subscription to a {@link SubscriberRegistry}.
     */
    @Test
    void shouldCancelSubscription() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>();
        subscriberRegistry.subscribe(subscriber);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).isEmpty();
        assertThat(subscriber.isCompleted()).isFalse();
        assertThat(subscriber.throwable()).isEmpty();

        subscriber.subscription().cancel();

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).isEmpty();
        assertThat(subscriber.isCompleted()).isTrue();
        assertThat(subscriber.throwable()).isEmpty();
    }

    /**
     * Ensure a {@link Subscriber} can observe a value published by a {@link SubscriberRegistry}.
     */
    @Test
    void shouldObserveAnItem() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>();
        subscriberRegistry.subscribe(subscriber);

        final String message = "G'day Mate";

        subscriberRegistry.publish(message);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).hasSize(1);
        assertThat(subscriber.isCompleted()).isFalse();
        assertThat(subscriber.throwable()).isEmpty();

        assertThat(subscriber.items().getFirst())
            .isEqualTo(message);
    }

    /**
     * Ensure a {@link Subscriber} can observe {@link SubscriberRegistry#complete()}.
     */
    @Test
    void shouldObserveCompletion() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>();
        subscriberRegistry.subscribe(subscriber);

        subscriberRegistry.complete();

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).isEmpty();
        assertThat(subscriber.isCompleted()).isTrue();
        assertThat(subscriber.throwable()).isEmpty();
    }

    /**
     * Ensure a {@link Subscriber} can observe a error published by a {@link SubscriberRegistry}.
     */
    @Test
    void shouldObserveAnError() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>();
        subscriberRegistry.subscribe(subscriber);

        final Throwable throwable = new RuntimeException("Oops!");
        subscriberRegistry.error(throwable);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).isEmpty();
        assertThat(subscriber.isCompleted()).isFalse();
        assertThat(subscriber.throwable()).contains(throwable);
    }

    /**
     * Ensure a {@link Subscriber} does not observe items published by a {@link SubscriberRegistry}
     * when subscribing.
     */
    @Test
    void shouldNotObserveItemDuringSubscription() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>(subscription -> {
            subscription.request(Long.MAX_VALUE);
            subscriberRegistry.publish("Should Never See This!");
        });

        subscriberRegistry.subscribe(subscriber);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).isEmpty();
        assertThat(subscriber.isCompleted()).isFalse();
        assertThat(subscriber.throwable()).isEmpty();
    }

    /**
     * Ensure a {@link Subscriber} can cancel their subscription with a {@link SubscriberRegistry}
     * when subscribing.
     */
    @Test
    void shouldCancelDuringSubscription() {
        final SubscriberRegistry<String> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>(Subscription::cancel);

        subscriberRegistry.subscribe(subscriber);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).isEmpty();
        assertThat(subscriber.isCompleted()).isTrue();
        assertThat(subscriber.throwable()).isEmpty();
    }

    /**
     * Ensure a {@link Subscriber} can observe items published in order by a {@link SubscriberRegistry}.
     */
    @Test
    void shouldObserveItemsInOrder() {
        final SubscriberRegistry<Integer> subscriberRegistry = new SubscriberRegistry<>();

        final TrackingSubscriber<Integer> subscriber = new TrackingSubscriber<>();
        subscriberRegistry.subscribe(subscriber);

        subscriberRegistry.publish(1);
        subscriberRegistry.publish(2);
        subscriberRegistry.publish(3);
        subscriberRegistry.publish(4);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).hasSize(4);
        assertThat(subscriber.isCompleted()).isFalse();
        assertThat(subscriber.throwable()).isEmpty();

        assertThat(subscriber.items())
            .containsExactly(1, 2, 3, 4);
    }

    /**
     * Ensure {@link SubscriberRegistry} allows completion of publishing.
     */
    @Test
    void shouldPublishAndComplete() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>();
        registry.subscribe(subscriber);

        final String message = "G'day Mate";

        registry.publish(message);

        assertThat(registry.onComplete())
            .isNotDone();

        registry.complete();

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).hasSize(1);
        assertThat(subscriber.isCompleted()).isTrue();
        assertThat(subscriber.throwable()).isEmpty();

        assertThat(registry.onComplete())
            .isCompleted();

        assertThat(subscriber.items().getFirst())
            .isEqualTo(message);
    }

    /**
     * Ensure {@link SubscriberRegistry} allows completion of publishing due to an error.
     */
    @Test
    void shouldPublishAndCompleteDueToError() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>();
        registry.subscribe(subscriber);

        final String message = "G'day Mate";
        final Throwable throwable = new RuntimeException("Oops!");

        registry.publish(message);
        registry.error(throwable);

        assertThat(subscriber.subscription()).isNotNull();
        assertThat(subscriber.items()).hasSize(1);
        assertThat(subscriber.isCompleted()).isFalse();

        assertThat(subscriber.items().getFirst())
            .isEqualTo(message);

        assertThat(subscriber.throwable())
            .contains(throwable);
    }

    /**
     * Ensure {@link SubscriberRegistry} prevents subscription once publishing has completed.
     */
    @Test
    void shouldPreventSubscriptionWhenCompleted() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        registry.complete();

        assertThrows(IllegalStateException.class, () -> registry.subscribe(item -> {}));
    }

    /**
     * Ensure {@link SubscriberRegistry} prevents subscription once publishing has completed due to a error.
     */
    @Test
    void shouldPreventSubscriptionWhenError() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();
        final Throwable throwable = new RuntimeException("Oops!");

        registry.error(throwable);

        final Throwable cause = assertThrows(IllegalStateException.class,
            () -> registry.subscribe(item -> {}));

        assertThat(cause.getCause())
            .isEqualTo(throwable);
    }

    /**
     * Ensure the subscriber error observer is notified when a {@link Subscriber} throws from
     * {@link Subscriber#onNext(Object)}, and that the subscriber is subsequently terminated.
     */
    @Test
    void shouldNotifyObserverWhenSubscriberThrows() {
        final SubscriberRegistry<String> registry = new SubscriberRegistry<>();

        final List<Throwable> observed = new ArrayList<>();
        registry.onSubscriberError((_, throwable) -> observed.add(throwable));

        final RuntimeException thrown = new RuntimeException("disk full");
        final TrackingSubscriber<String> subscriber = new TrackingSubscriber<>() {
            @Override
            public void onNext(final String item) {
                throw thrown;
            }
        };
        registry.subscribe(subscriber);
        registry.publish("hello");

        assertThat(observed).containsExactly(thrown);
        assertThat(subscriber.throwable()).contains(thrown);
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

    /**
     * A {@link Subscriber} that records all interactions for assertion in tests.
     */
    private static class TrackingSubscriber<T> implements Subscriber<T> {

        @FunctionalInterface
        interface OnSubscribe {
            void accept(Subscription subscription);
        }

        private final List<T> items = new ArrayList<>();
        private final OnSubscribe onSubscribeAction;
        private Subscription subscription;
        private Throwable throwable;
        private boolean completed;

        TrackingSubscriber() {
            this(s -> s.request(Long.MAX_VALUE));
        }

        TrackingSubscriber(final OnSubscribe onSubscribeAction) {
            this.onSubscribeAction = onSubscribeAction;
        }

        @Override
        public void onSubscribe(final Subscription subscription) {
            this.subscription = subscription;
            this.onSubscribeAction.accept(subscription);
        }

        @Override
        public void onNext(final T item) {
            this.items.add(item);
        }

        @Override
        public void onError(final Throwable throwable) {
            this.throwable = throwable;
        }

        @Override
        public void onComplete() {
            this.completed = true;
        }

        Subscription subscription() {
            return this.subscription;
        }

        List<T> items() {
            return this.items;
        }

        Optional<Throwable> throwable() {
            return Optional.ofNullable(this.throwable);
        }

        boolean isCompleted() {
            return this.completed;
        }
    }
}
