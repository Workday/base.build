package build.base.foundation.iterator;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Iterators}.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
class IteratorsTests {

    /**
     * Ensure concatenating null iterators returns an empty iterator.
     */
    @Test
    void shouldConcatenateNullIterators() {
        final Iterator<?> result = Iterators.concat((Iterator<?>[]) null);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure concatenating zero iterators returns an empty iterator.
     */
    @Test
    void shouldConcatenateZeroIterators() {
        final Iterator<?> result = Iterators.concat();

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure concatenating empty iterators returns an empty iterator.
     */
    @Test
    void shouldConcatenateEmptyIterators() {
        final Iterator<?> result = Iterators.concat(
            Iterators.empty(),
            Iterators.empty(),
            Iterators.empty()
        );

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure concatenating a single iterator works correctly.
     */
    @Test
    void shouldConcatenateSingleIterator() {
        final Iterator<?> result = Iterators.concat(Iterators.of(1, 2, 3));

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.next())
            .isEqualTo(3);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure concatenating multiple iterators works correctly.
     */
    @Test
    void shouldConcatenateMultipleIterators() {
        final Iterator<?> result = Iterators.concat(
            Iterators.of(1, 2),
            Iterators.of("hello", "world"),
            Iterators.of(3.14, true)
        );

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.next())
            .isEqualTo("hello");

        assertThat(result.next())
            .isEqualTo("world");

        assertThat(result.next())
            .isEqualTo(3.14);

        assertThat(result.next())
            .isEqualTo(true);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure concatenating iterators with null elements works correctly.
     */
    @Test
    void shouldConcatenateIteratorsWithNullIterators() {
        final Iterator<?> result = Iterators.concat(
            Iterators.of(1, 2),
            null,
            Iterators.of(3, 4),
            null,
            Iterators.empty()
        );

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.next())
            .isEqualTo(3);

        assertThat(result.next())
            .isEqualTo(4);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure concatenating mixed empty and non-empty iterators works correctly.
     */
    @Test
    void shouldConcatenateMixedEmptyAndNonEmptyIterators() {
        final Iterator<?> result = Iterators.concat(
            Iterators.empty(),
            Iterators.of("first"),
            Iterators.empty(),
            Iterators.of("second", "third"),
            Iterators.empty()
        );

        assertThat(result.next())
            .isEqualTo("first");

        assertThat(result.next())
            .isEqualTo("second");

        assertThat(result.next())
            .isEqualTo("third");

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure multiple calls to hasNext() don't affect iteration.
     */
    @Test
    void shouldHandleMultipleHasNextCalls() {
        final Iterator<?> result = Iterators.concat(Iterators.of(1, 2));

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.hasNext())
            .isFalse();

        assertThat(result.hasNext())
            .isFalse();
    }
}