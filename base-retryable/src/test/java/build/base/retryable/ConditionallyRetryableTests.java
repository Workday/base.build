package build.base.retryable;

import static build.base.retryable.ConditionallyRetryable.retrying;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ConditionallyRetryable}.
 *
 * @author brian.oliver
 * @since Mar-2017
 */
class ConditionallyRetryableTests {

    /**
     * Ensure {@code null} {@link Retryable}s are not accepted.
     */
    @Test
    void shouldNotAcceptNullRetryables() {
        assertThrows(NullPointerException.class, () -> retrying((Retryable<?>) null));
    }

    /**
     * Ensure {@link ConditionallyRetryable}s can be created.
     */
    @Test
    void shouldCreateConditionallyRetryables() {
        final var retryable = retrying(() -> 42);
        assertThat(retryable.get())
            .isEqualTo(42);

        final var supplyable = retrying((Supplier<Integer>) () -> 42);
        assertThat(supplyable.get())
            .isEqualTo(42);
    }

    /**
     * Ensure a {@link ConditionallyRetryable} throws an {@link EphemeralFailureException} when a value-based predicate
     * succeeds.
     */
    @Test
    void shouldFailWhenAValuePredicateSucceeds() {
        assertThrows(EphemeralFailureException.class, () -> {
            final var retryable = retrying(() -> 42).when(42);

            assertThat(retryable.get())
                .isEqualTo(42);
        });
    }

    /**
     * Ensure a {@link ConditionallyRetryable} throws an {@link EphemeralFailureException} when a value-based predicate
     * succeeds.
     */
    @Test
    void shouldFailWhenAPredicateSucceeds() {
        assertThrows(EphemeralFailureException.class, () -> {
            final var retryable = retrying(() -> 42).when(v -> v == 42);

            assertThat(retryable.get())
                .isEqualTo(42);
        });
    }

    /**
     * Ensure a {@link ConditionallyRetryable} returns a value when a {@link java.util.function.Predicate} fails.
     */
    @Test()
    void shouldSucceedWhenAPredicateFails() {
        final var retryable = retrying(() -> 42).when(v -> v < 42);

        assertThat(retryable.get())
            .isEqualTo(42);
    }

    /**
     * Ensure a {@link ConditionallyRetryable} returns a value when multiple {@link java.util.function.Predicate}s fail.
     */
    @Test()
    void shouldSucceedWhenMultiplePredicatesFail() {
        final var retryable = retrying(() -> 42)
            .when(v -> v < 42)
            .when(v -> v > 42);

        assertThat(retryable.get())
            .isEqualTo(42);
    }
}
