package build.base.foundation;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for {@link Lazy}.
 *
 * @author brian.oliver
 * @since Jun-2024
 */
public class LazyTests {

    /**
     * Ensure different {@link Lazy} instances can be compared.
     */
    @Test
    void shouldCompareDifferentLazies() {
        assertThat(Lazy.empty())
            .isEqualTo(Lazy.empty());

        assertThat(Lazy.empty())
            .isNotSameAs(Lazy.empty());

        assertThat(Lazy.empty().hashCode())
            .isNotEqualTo(Lazy.empty().hashCode());

        assertThat(Lazy.empty())
            .isNotEqualTo(Lazy.of(1));

        assertThat(Lazy.of(1))
            .isEqualTo(Lazy.of(1));

        assertThat(Lazy.of(1))
            .isNotEqualTo(Lazy.empty());

        assertThat(Lazy.of(1))
            .isNotSameAs(Lazy.empty());

        assertThat(Lazy.of(1).hashCode())
            .isNotEqualTo(Lazy.of(1).hashCode());
    }

    /**
     * Ensure different {@link Lazy} instances can be compared.
     */
    @Test
    void shouldCompareSameLazies() {
        final var nulled = Lazy.<Integer>ofNullable(null);
        final var empty = Lazy.<Integer>empty();
        final var one = Lazy.of(1);

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
     * Ensure mapping {@link java.util.function.Function} is applied to {@link Lazy} elements
     */
    @Test
    void shouldApplyMappingToLazyElements() {
        final var first = Lazy.<Object>of("foo");

        final var mapped = first
            .map(String.class)
            .map(String::toUpperCase);

        assertThat(mapped.get())
            .isEqualTo("FOO");
    }

    /**
     * Ensure filter returns the element only if it matches the given {@link java.util.function.Predicate}
     */
    @Test
    void shouldFilterElements() {
        final var first = Lazy.of(1);
        final var second = Lazy.of(2);

        assertThat(first.filter(a -> a < 2))
            .isEqualTo(first);

        assertThat(second.filter(a -> a < 2))
            .isEqualTo(Lazy.empty());
    }

    /**
     * Should allow an optional of an empty {@link Lazy}.
     */
    @Test
    void shouldAllowLazyOfOptional() {
        assertThat(Lazy.empty().optional())
            .isEmpty();
    }

    /**
     * Should allow a {@link Lazy} to produce {@link Stream}s.
     */
    @Test
    void shouldProduceStreamFromLazies() {
        assertThat(Lazy.empty().stream())
            .isEmpty();

        assertThat(Lazy.of(42).stream())
            .containsExactly(42);
    }

    /**
     * Should throw {@link NoSuchElementException}.
     */
    @Test
    void shouldThrowNoSuchElementExceptionWhenNothingLazilyInitialized() {
        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> Lazy.empty().get());

        assertThatExceptionOfType(NoSuchElementException.class)
            .isThrownBy(() -> Lazy.empty().orElseThrow());
    }

    /**
     * Ensure a {@link Lazy} can be lazily initialized.
     */
    @Test
    void shouldLazilyInitializeALazy() {
        final var lazy = Lazy.of(() -> 42);

        assertThat(lazy.isEmpty())
            .isFalse();

        assertThat(lazy.isPresent())
            .isTrue();

        assertThat(lazy.get())
            .isEqualTo(42);
    }

    /**
     * Ensure a {@link Lazy#collector()} can collect a single element.
     */
    @Test
    void shouldLazilyCollect() {
        assertThat(Stream.of(1)
            .collect(Lazy.collector()))
            .isEqualTo(1);

        assertThatThrownBy(() -> Stream.empty()
            .collect(Lazy.collector()))
            .isInstanceOf(NoSuchElementException.class);

        assertThatThrownBy(() -> Stream.of(1, 2)
            .collect(Lazy.collector()))
            .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Ensure filtering a {@link Lazy} whose supplier returns {@code null} does not invoke the predicate on {@code null}.
     */
    @Test
    void shouldReturnEmptyWhenFilterAppliedToNullYieldingLazy() {
        final Lazy<String> nullSupplied = Lazy.of(() -> (String) null);
        final Lazy<String> filtered = nullSupplied.filter(String::isEmpty);

        assertThat(filtered.isEmpty()).isTrue();
        assertThat(filtered.isPresent()).isFalse();
        assertThat(filtered.getOrNull()).isNull();
    }

    /**
     * Ensure mapping a {@link Lazy} whose supplier returns {@code null} does not invoke the mapper on {@code null}.
     */
    @Test
    void shouldReturnNullWhenMapperAppliedToNullYieldingLazy() {
        final Lazy<String> nullSupplied = Lazy.of(() -> (String) null);
        final Lazy<String> mapped = nullSupplied.map(String::toUpperCase);

        assertThat(mapped.getOrNull()).isNull();
    }

    /**
     * Ensure a {@link Lazy} established with a {@link java.util.function.Supplier} throwing a {@link RuntimeException}
     * is not invoked.
     */
    @Test
    void voidNotInvokeSupplier() {
        final var exception = new RuntimeException("Should not be invoked");

        final var lazy = Lazy.<String>of(() -> {
            throw exception;
        });

        assertThat(lazy.isPresent())
            .isTrue();

        assertThat(lazy.isEmpty())
            .isFalse();

        assertThat(lazy
            .map(String::toUpperCase)
            .isPresent())
            .isTrue();

        assertThatThrownBy(() ->
            lazy.filter(String::isEmpty)
                .isPresent())
            .isEqualTo(exception);
    }
}
