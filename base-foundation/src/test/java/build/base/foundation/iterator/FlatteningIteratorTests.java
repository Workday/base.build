package build.base.foundation.iterator;

import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link FlatteningIterator}.
 *
 * @author brian.oliver
 * @since Sep-2025
 */
class FlatteningIteratorTests {

    /**Ø
     * Ensure FlattenIterator handles null iterator gracefully.
     */
    @Test
    void shouldHandleNullIterator() {
        assertThatThrownBy(() -> Iterators.flatten(null))
            .isInstanceOf(NullPointerException.class)
            .hasMessage("The iterators must not be null");
    }

    /**
     * Ensure FlattenIterator handles empty iterator of iterators.
     */
    @Test
    void shouldHandleEmptyIteratorOfIterators() {
        final var result = Iterators.flatten(Iterators.empty());

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure FlattenIterator handles single empty iterator.
     */
    @Test
    void shouldHandleSingleEmptyIterator() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(Iterators.empty())
        );

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure FlattenIterator handles multiple empty iterators.
     */
    @Test
    void shouldHandleMultipleEmptyIterators() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(Iterators.empty(), Iterators.empty(), Iterators.empty())
        );

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure FlattenIterator handles single iterator with elements.
     */
    @Test
    void shouldHandleSingleIteratorWithElements() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(Iterators.of(1, 2, 3))
        );

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
     * Ensure FlattenIterator handles multiple iterators with elements.
     */
    @Test
    void shouldHandleMultipleIteratorsWithElements() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1, 2),
                Iterators.of(3, 4),
                Iterators.of(5, 6)
            )
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

        assertThat(result.next())
            .isEqualTo(6);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure FlattenIterator handles mixed empty and non-empty iterators.
     */
    @Test
    void shouldHandleMixedEmptyAndNonEmptyIterators() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.empty(),
                Iterators.of(1, 2),
                Iterators.empty(),
                Iterators.of(3),
                Iterators.empty()
            )
        );

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
     * Ensure FlattenIterator handles null iterators in the sequence.
     */
    @Test
    void shouldHandleNullIteratorsInSequence() {
        final Iterator<Integer> nullIterator = null;
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1, 2),
                nullIterator,
                Iterators.of(3, 4),
                nullIterator
            )
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
     * Ensure FlattenIterator handles iterators with null elements.
     */
    @Test
    void shouldHandleIteratorsWithNullElements() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1, null, 2),
                Iterators.of(null, 3)
            )
        );

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isNull();

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.next())
            .isNull();

        assertThat(result.next())
            .isEqualTo(3);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure multiple calls to hasNext() don't affect iteration.
     */
    @Test
    void shouldHandleMultipleHasNextCalls() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1),
                Iterators.of(2)
            )
        );

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
     * Ensure FlattenIterator works with different types.
     */
    @Test
    void shouldWorkWithDifferentTypes() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<String>>of(
                Iterators.of("hello", "world"),
                Iterators.of("foo", "bar")
            )
        );

        assertThat(result.next())
            .isEqualTo("hello");

        assertThat(result.next())
            .isEqualTo("world");

        assertThat(result.next())
            .isEqualTo("foo");

        assertThat(result.next())
            .isEqualTo("bar");

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure FlattenIterator handles single element iterators.
     */
    @Test
    void shouldHandleSingleElementIterators() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1),
                Iterators.of(2),
                Iterators.of(3)
            )
        );

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
     * Ensure FlattenIterator handles iterators of varying sizes.
     */
    @Test
    void shouldHandleIteratorsOfVaryingSizes() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1),
                Iterators.of(2, 3, 4, 5),
                Iterators.of(6, 7),
                Iterators.of(8)
            )
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

        assertThat(result.next())
            .isEqualTo(6);

        assertThat(result.next())
            .isEqualTo(7);

        assertThat(result.next())
            .isEqualTo(8);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure next() throws NoSuchElementException when no elements available.
     */
    @Test
    void shouldThrowNoSuchElementExceptionWhenNoElementsAvailable() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(Iterators.of(1))
        );

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure FlattenIterator handles complex nesting scenarios.
     */
    @Test
    void shouldHandleComplexNestingScenarios() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.empty(),
                Iterators.of(1),
                Iterators.empty(),
                Iterators.empty(),
                Iterators.of(2, 3),
                Iterators.empty(),
                Iterators.of(4),
                Iterators.empty()
            )
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
     * Ensure FlattenIterator handles single element in single iterator.
     */
    @Test
    void shouldHandleSingleElementInSingleIterator() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(Iterators.of(42))
        );

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.next())
            .isEqualTo(42);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure FlattenIterator handles all empty iterators at beginning.
     */
    @Test
    void shouldHandleEmptyIteratorsAtBeginning() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.empty(),
                Iterators.empty(),
                Iterators.of(1, 2)
            )
        );

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure FlattenIterator handles all empty iterators at end.
     */
    @Test
    void shouldHandleEmptyIteratorsAtEnd() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1, 2),
                Iterators.empty(),
                Iterators.empty()
            )
        );

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure FlattenIterator handles large number of iterators.
     */
    @Test
    void shouldHandleLargeNumberOfIterators() {
        final var result = Iterators.flatten(
            Iterators.<Iterator<Integer>>of(
                Iterators.of(1),
                Iterators.of(2),
                Iterators.of(3),
                Iterators.of(4),
                Iterators.of(5),
                Iterators.of(6),
                Iterators.of(7),
                Iterators.of(8),
                Iterators.of(9),
                Iterators.of(10)
            )
        );

        for (int i = 1; i <= 10; i++) {
            assertThat(result.next())
                .isEqualTo(i);
        }

        assertThat(result.hasNext())
            .isFalse();
    }
}
