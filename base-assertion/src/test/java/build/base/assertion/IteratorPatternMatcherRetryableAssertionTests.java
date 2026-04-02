package build.base.assertion;

import build.base.option.Timeout;
import build.base.retryable.Retryable;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link IteratorPatternMatcherRetryableAssertion}.
 *
 * @author brian.oliver
 * @since Feb-2026
 */
class IteratorPatternMatcherRetryableAssertionTests {

    /**
     * Ensure the pattern eventually matches after the {@link Retryable} source changes.
     */
    @Test
    void shouldEventuallyMatchWhenSourceBecomesCorrect() {
        final var counter = new AtomicInteger(0);

        IteratorPatternMatcherRetryableAssertion.<String>assertThat(() -> {
                if (counter.incrementAndGet() < 3) {
                    return List.of("Fred", "Wilma");
                }
                return List.of("Fred", "Barney", "Rubble");
            })
            .withTimeout(Timeout.of(Duration.ofSeconds(5)))
            .starts()
            .thenLater().matches("Barney")
            .isTrue();

        assertThat(counter.get())
            .isGreaterThanOrEqualTo(3);
    }

    /**
     * Ensure an {@link AssertionError} is thrown when the pattern never matches within the timeout.
     */
    @Test
    void shouldFailWhenPatternNeverMatchesWithinTimeout() {
        assertThatThrownBy(() ->
            IteratorPatternMatcherRetryableAssertion.<String>assertThat(() -> List.of("Fred", "Wilma"))
                .withTimeout(Timeout.of(Duration.ofMillis(200)))
                .starts()
                .thenLater().matches("Barney")
                .isTrue())
            .isInstanceOf(AssertionError.class);
    }

    /**
     * Ensure {@code isFalse()} passes when the {@link Retryable} source never matches the pattern.
     */
    @Test
    void shouldPassIsFalseWhenPatternNeverMatches() {
        IteratorPatternMatcherRetryableAssertion.<String>assertThat(Retryable.of(List.of("Fred", "Wilma")))
            .withTimeout(Timeout.of(Duration.ofSeconds(1)))
            .starts()
            .thenLater().matches("Barney")
            .isFalse();
    }

    /**
     * Ensure a {@link Timeout} can be configured using {@link IteratorPatternMatcherRetryableAssertion#withOption}.
     */
    @Test
    void shouldAcceptOptionViaWithOption() {
        final var timeout = Timeout.of(Duration.ofSeconds(5));

        IteratorPatternMatcherRetryableAssertion.<String>assertThat(Retryable.of(List.of("Barney")))
            .withOption(timeout)
            .starts()
            .thenLater().matches("Barney")
            .isTrue();
    }
}
