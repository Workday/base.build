package build.base.foundation;

import build.base.foundation.predicate.Predicates;
import org.junit.jupiter.api.Test;

import static build.base.foundation.predicate.Predicates.allOf;
import static build.base.foundation.predicate.Predicates.always;
import static build.base.foundation.predicate.Predicates.anyOf;
import static build.base.foundation.predicate.Predicates.never;
import static build.base.foundation.predicate.Predicates.not;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Predicates}.
 *
 * @author brian.oliver
 * @since Mar-2017
 */
class PredicatesTests {

    /**
     * Ensure the {@link Predicates#always()} fails.
     */
    @Test
    void shouldNeverSucceed() {
        assertThat(never().test(null))
            .isFalse();

        assertThat(never().test(42))
            .isFalse();

        assertThat(never().test(new Object()))
            .isFalse();
    }

    /**
     * Ensure the {@link Predicates#always()} succeeds.
     */
    @Test
    void shouldAlwaysSucceed() {
        assertThat(always().test(null))
            .isTrue();

        assertThat(always().test(42))
            .isTrue();

        assertThat(always().test(new Object()))
            .isTrue();
    }

    /**
     * Ensure the {@code Predicates#anyOf(Predicate[])} succeeds.
     */
    @Test
    void shouldAnyOfSucceeds() {
        assertThat(anyOf().test(null))
            .isFalse();

        assertThat(anyOf().test(42))
            .isFalse();

        assertThat(anyOf().test(new Object()))
            .isFalse();

        assertThat(anyOf(never()).test(null))
            .isFalse();

        assertThat(anyOf(never()).test(42))
            .isFalse();

        assertThat(anyOf(never()).test(new Object()))
            .isFalse();

        assertThat(anyOf(never(), never()).test(null))
            .isFalse();

        assertThat(anyOf(never(), never()).test(42))
            .isFalse();

        assertThat(anyOf(never(), never()).test(new Object()))
            .isFalse();

        assertThat(anyOf(never(), always()).test(null))
            .isTrue();

        assertThat(anyOf(never(), always()).test(42))
            .isTrue();

        assertThat(anyOf(never(), always()).test(new Object()))
            .isTrue();

        assertThat(anyOf(always(), never()).test(null))
            .isTrue();

        assertThat(anyOf(always(), never()).test(42))
            .isTrue();

        assertThat(anyOf(always(), never()).test(new Object()))
            .isTrue();

        assertThat(anyOf(always(), always()).test(null))
            .isTrue();

        assertThat(anyOf(always(), always()).test(42))
            .isTrue();

        assertThat(anyOf(always(), always()).test(new Object()))
            .isTrue();
    }

    /**
     * Ensure the {@code Predicates#allOf(Predicate[])} succeeds.
     */
    @Test
    void shouldAllOfSucceed() {
        assertThat(allOf().test(null))
            .isTrue();

        assertThat(allOf().test(42))
            .isTrue();

        assertThat(allOf().test(new Object()))
            .isTrue();

        assertThat(allOf(never()).test(null))
            .isFalse();

        assertThat(allOf(never()).test(42))
            .isFalse();

        assertThat(allOf(never()).test(new Object()))
            .isFalse();

        assertThat(allOf(never(), never()).test(null))
            .isFalse();

        assertThat(allOf(never(), never()).test(42))
            .isFalse();

        assertThat(allOf(never(), never()).test(new Object()))
            .isFalse();

        assertThat(allOf(never(), always()).test(null))
            .isFalse();

        assertThat(allOf(never(), always()).test(42))
            .isFalse();

        assertThat(allOf(never(), always()).test(new Object()))
            .isFalse();

        assertThat(allOf(always(), never()).test(null))
            .isFalse();

        assertThat(allOf(always(), never()).test(42))
            .isFalse();

        assertThat(allOf(always(), never()).test(new Object()))
            .isFalse();

        assertThat(allOf(always(), always()).test(null))
            .isTrue();

        assertThat(allOf(always(), always()).test(42))
            .isTrue();

        assertThat(allOf(always(), always()).test(new Object()))
            .isTrue();
    }

    /**
     * Ensures that {@link Predicates#not} negates the supplied predicate.
     */
    @Test
    void shouldNotNegate() {
        assertThat(not(always()).test(null))
            .isFalse();

        assertThat(not(always()).test(42))
            .isFalse();

        assertThat(not(always()).test(new Object()))
            .isFalse();

        assertThat(not(never()).test(null))
            .isTrue();

        assertThat(not(never()).test(42))
            .isTrue();

        assertThat(not(never()).test(new Object()))
            .isTrue();
    }
}
