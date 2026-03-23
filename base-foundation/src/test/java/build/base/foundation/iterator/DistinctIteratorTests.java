package build.base.foundation.iterator;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link DistinctIterator}.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
class DistinctIteratorTests {

    /**
     * Ensure {@link DistinctIterator} handles null iterator gracefully.
     */
    @Test
    void shouldHandleNullIterator() {
        final var result = Iterators.distinct(null);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure {@link DistinctIterator} handles empty iterator.
     */
    @Test
    void shouldHandleEmptyIterator() {
        final var result = Iterators.distinct(Iterators.empty());

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure {@link DistinctIterator} returns unique elements only.
     */
    @Test
    void shouldReturnUniqueElementsOnly() {
        final var result = Iterators.distinct(Iterators.of(1, 2, 1, 3, 2, 4, 1));

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
     * Ensure {@link DistinctIterator} handles single element.
     */
    @Test
    void shouldHandleSingleElement() {
        final var result = Iterators.distinct(Iterators.of(42));

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.next())
            .isEqualTo(42);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure {@link DistinctIterator} handles all duplicate elements.
     */
    @Test
    void shouldHandleAllDuplicateElements() {
        final var result = Iterators.distinct(Iterators.of(5, 5, 5, 5));

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.next())
            .isEqualTo(5);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure {@link DistinctIterator} handles all unique elements.
     */
    @Test
    void shouldHandleAllUniqueElements() {
        final var result = Iterators.distinct(Iterators.of(1, 2, 3, 4));

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
     * Ensure {@link DistinctIterator} handles null elements correctly.
     */
    @Test
    void shouldHandleNullElements() {
        final var result = Iterators.distinct(Iterators.of(null, 1, null, 2, null));

        assertThat(result.next())
            .isNull();

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure multiple calls to hasNext() don't affect iteration.
     */
    @Test
    void shouldHandleMultipleHasNextCalls() {
        final var result = Iterators.distinct(Iterators.of(1, 1, 2));

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

    /**
     * Ensure {@link DistinctIterator} works with different types.
     */
    @Test
    void shouldWorkWithDifferentTypes() {
        final var result = Iterators.distinct(
            Iterators.of("hello", "world", "hello", "test", "world")
        );

        assertThat(result.next())
            .isEqualTo("hello");

        assertThat(result.next())
            .isEqualTo("world");

        assertThat(result.next())
            .isEqualTo("test");

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure next() throws NoSuchElementException when no elements available.
     */
    @Test
    void shouldThrowNoSuchElementExceptionWhenNoElementsAvailable() {
        final var result = Iterators.distinct(Iterators.of(1, 1, 1));

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure {@link DistinctIterator} handles duplicates at the beginning.
     */
    @Test
    void shouldHandleDuplicatesAtBeginning() {
        final var result = Iterators.distinct(Iterators.of(1, 1, 1, 2, 3));

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
     * Ensure {@link DistinctIterator} handles duplicates at the end.
     */
    @Test
    void shouldHandleDuplicatesAtEnd() {
        final var result = Iterators.distinct(Iterators.of(1, 2, 3, 3, 3));

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
     * Ensure {@link DistinctIterator} handles complex duplicate patterns.
     */
    @Test
    void shouldHandleComplexDuplicatePatterns() {
        final var result = Iterators.distinct(
            Iterators.of(1, 2, 1, 3, 2, 4, 3, 5, 1, 2, 3, 4, 5)
        );

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.next())
            .isEqualTo(3);

        assertThat(result.next())
            .isEqualTo(4);

        assertThat(result.next())
            .isEqualTo(5);

        assertThat(result.hasNext())
            .isFalse();
    }
}