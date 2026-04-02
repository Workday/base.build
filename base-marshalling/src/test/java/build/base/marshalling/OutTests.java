package build.base.marshalling;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for {@link Out}s.
 *
 * @author brian.oliver
 * @since Apr-2025
 */
public class OutTests {

    /**
     * Ensure different {@link Out} instances can be compared.
     */
    @Test
    void shouldCompareDifferentOuts() {
        assertThat(Out.empty())
            .isEqualTo(Out.empty());

        assertThat(Out.empty())
            .isNotSameAs(Out.empty());

        assertThat(Out.empty().hashCode())
            .isNotEqualTo(Out.empty().hashCode());

        assertThat(Out.empty())
            .isNotEqualTo(Out.of(1));

        assertThat(Out.of(1))
            .isEqualTo(Out.of(1));

        assertThat(Out.of(1))
            .isNotEqualTo(Out.empty());

        assertThat(Out.of(1))
            .isNotSameAs(Out.empty());

        assertThat(Out.of(1).hashCode())
            .isNotEqualTo(Out.of(1).hashCode());
    }

    /**
     * Ensure same {@link Out} instances can be compared.
     */
    @Test
    void shouldCompareSameOuts() {
        final Out<Integer> nulled = Out.nullValue();
        final Out<Integer> empty = Out.empty();
        final Out<Integer> one = Out.of(1);

        assertThat(nulled)
            .isEqualTo(nulled);

        assertThat(nulled)
            .isNotEqualTo(one);

        assertThat(nulled)
            .isNotSameAs(empty);

        assertThat(one)
            .isEqualTo(one);

        assertThat(one)
            .isNotEqualTo(nulled);

        assertThat(one)
            .isNotEqualTo(empty);

        assertThat(empty)
            .isEqualTo(empty);

        assertThat(empty)
            .isNotEqualTo(one);

        assertThat(empty)
            .isNotSameAs(nulled);
    }

    /**
     * Ensure mapping {@link java.util.function.Function} is applied to captured elements
     */
    @Test
    void shouldApplyMappingToOutdElements() {
        final Out<Object> first = Out.of("foo");

        final Out<String> mappedOut = first
            .map(String.class)
            .map(String::toUpperCase);

        assertThat(mappedOut.get())
            .isEqualTo("FOO");
    }

    /**
     * Ensure filter returns the element only if it matches the given {@link java.util.function.Predicate}
     */
    @Test
    void shouldFilterElements() {
        final Out<Integer> first = Out.of(1);
        final Out<Integer> second = Out.of(2);

        assertThat(first.filter(a -> a < 2))
            .isEqualTo(first);

        assertThat(second.filter(a -> a < 2))
            .isEqualTo(Out.empty());
    }

    /**
     * Should allow an optional of an empty {@link Out}.
     */
    @Test
    void shouldAllowOutOfOptional() {
        assertThat(Out.empty().optional())
            .isEmpty();
    }

    /**
     * Should allow a {@link Out} to produce {@link Stream}s.
     */
    @Test
    void shouldProduceStreamFromOuts() {
        assertThat(Out.empty().stream())
            .isEmpty();

        assertThat(Out.of(42).stream())
            .containsExactly(42);
    }

    /**
     * Should throw {@link java.util.NoSuchElementException}.
     */
    @Test
    void shouldThrowNoSuchElementExceptionWhenNothingOutd() {
        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> Out.empty().get());

        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> Out.empty().orElseThrow());
    }
}
