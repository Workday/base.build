package build.base.foundation;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Unit tests for {@link Capture}.
 *
 * @author brian.oliver
 * @since Jun-2024
 */
public class CaptureTests {

    /**
     * Ensure different {@link Capture} instances can be compared.
     */
    @Test
    void shouldCompareDifferentCaptures() {
        assertThat(Capture.empty())
            .isEqualTo(Capture.empty());

        assertThat(Capture.empty())
            .isNotSameAs(Capture.empty());

        assertThat(Capture.empty().hashCode())
            .isNotEqualTo(Capture.empty().hashCode());

        assertThat(Capture.empty())
            .isNotEqualTo(Capture.of(1));

        assertThat(Capture.of(1))
            .isEqualTo(Capture.of(1));

        assertThat(Capture.of(1))
            .isNotEqualTo(Capture.empty());

        assertThat(Capture.of(1))
            .isNotSameAs(Capture.empty());

        assertThat(Capture.of(1).hashCode())
            .isNotEqualTo(Capture.of(1).hashCode());
    }

    /**
     * Ensure different {@link Capture} instances can be compared.
     */
    @Test
    void shouldCompareSameCaptures() {
        final Capture<Integer> nulled = Capture.ofNullable(null);
        final Capture<Integer> empty = Capture.empty();
        final Capture<Integer> one = Capture.of(1);

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
    void shouldApplyMappingToCapturedElements() {
        final Capture<Object> first = Capture.of("foo");

        final Capture<String> mappedCapture = first
            .map(String.class)
            .map(String::toUpperCase);

        assertThat(mappedCapture.get())
            .isEqualTo("FOO");
    }

    /**
     * Ensure filter returns the element only if it matches the given {@link java.util.function.Predicate}
     */
    @Test
    void shouldFilterElements() {
        final Capture<Integer> first = Capture.of(1);
        final Capture<Integer> second = Capture.of(2);

        assertThat(first.filter(a -> a < 2))
            .isEqualTo(first);

        assertThat(second.filter(a -> a < 2))
            .isEqualTo(Capture.empty());
    }

    /**
     * Should allow an optional of an empty {@link Capture}.
     */
    @Test
    void shouldAllowCaptureOfOptional() {
        assertThat(Capture.empty().optional())
            .isEmpty();
    }

    /**
     * Should allow a {@link Capture} to produce {@link Stream}s.
     */
    @Test
    void shouldProduceStreamFromCaptures() {
        assertThat(Capture.empty().stream())
            .isEmpty();

        assertThat(Capture.of(42).stream())
            .containsExactly(42);
    }

    /**
     * Should throw {@link java.util.NoSuchElementException}.
     */
    @Test
    void shouldThrowNoSuchElementExceptionWhenNothingCaptured() {
        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> Capture.empty().get());

        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> Capture.empty().orElseThrow());
    }

    /**
     * Ensure a {@link Capture} can be lazily initialized.
     */
    @Test
    void shouldLazilyInitializeACapture() {
        final var capture = Capture.of(() -> 42);

        assertThat(capture.isEmpty())
            .isFalse();

        assertThat(capture.isPresent())
            .isTrue();

        assertThat(capture.get())
            .isEqualTo(42);

        capture.set(84);

        assertThat(capture.get())
            .isEqualTo(84);

        capture.clear();

        assertThat(capture.isEmpty())
            .isFalse();

        assertThat(capture.get())
            .isEqualTo(42);
    }
}
