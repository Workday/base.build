package build.base.retryable;

import build.base.retryable.option.RetryFrequency;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.Iterator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Tests for {@link RetryFrequency}.
 *
 * @author reed.vonredwitz
 */
class RetryFrequencyTests {

    /**
     * Ensure {@link RetryFrequency#within} accepts a valid non-negative floor.
     */
    @Test
    void withinShouldAcceptNonNegativeFloor() {
        assertThatNoException().isThrownBy(() ->
            RetryFrequency.every(Duration.ofSeconds(10))
                .within(Duration.ZERO, Duration.ofSeconds(10)));
    }

    /**
     * Ensure {@link RetryFrequency#within} accepts a valid ceiling greater than floor.
     */
    @Test
    void withinShouldAcceptCeilingGreaterThanFloor() {
        assertThatNoException().isThrownBy(() ->
            RetryFrequency.every(Duration.ofSeconds(10))
                .within(Duration.ofSeconds(1), Duration.ofSeconds(5)));
    }

    /**
     * Ensure {@link RetryFrequency#within} clamps values above the ceiling down to the ceiling.
     */
    @Test
    void withinShouldClampValuesToCeiling() {
        final var ceiling = Duration.ofSeconds(3);
        final Iterator<Duration> iter = RetryFrequency.every(Duration.ofSeconds(10))
            .within(Duration.ZERO, ceiling)
            .iterator();

        assertThat(iter.next()).isEqualTo(ceiling);
    }

    /**
     * Ensure {@link RetryFrequency#within} clamps values below the floor up to the floor.
     */
    @Test
    void withinShouldClampValuesToFloor() {
        final var floor = Duration.ofSeconds(5);
        final Iterator<Duration> iter = RetryFrequency.every(Duration.ofSeconds(1))
            .within(floor, Duration.ofSeconds(10))
            .iterator();

        assertThat(iter.next()).isEqualTo(floor);
    }

    /**
     * Ensure {@link RetryFrequency#never} does not throw on construction.
     */
    @Test
    void neverShouldNotThrowOnConstruction() {
        assertThatNoException().isThrownBy(RetryFrequency::never);
    }

    /**
     * Ensure {@link RetryFrequency#never} produces an iterator with no elements.
     */
    @Test
    void neverShouldProduceNoElements() {
        assertThat(RetryFrequency.never().iterator().hasNext()).isFalse();
    }
}
