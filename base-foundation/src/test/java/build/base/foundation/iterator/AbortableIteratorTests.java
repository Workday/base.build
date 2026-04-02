package build.base.foundation.iterator;

import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link AbortableIterator}.
 *
 * @author brian.oliver
 * @since May-2025
 */
class AbortableIteratorTests {

    /**
     * Ensure AbortableIterator handles null iterator gracefully.
     */
    @Test
    void shouldHandleNullIterator() {
        final var result = Iterators.<Integer>abortable(null, x -> x > 5);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure AbortableIterator handles null predicate gracefully.
     */
    @Test
    void shouldHandleNullPredicate() {
        final var result = Iterators.abortable(Iterators.of(1, 2, 3), null);

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
     * Ensure AbortableIterator handles empty iterator.
     */
    @Test
    void shouldHandleEmptyIterator() {
        final var result = Iterators.<Integer>abortable(Iterators.empty(), x -> x > 5);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure AbortableIterator returns all elements when predicate never matches.
     */
    @Test
    void shouldReturnAllElementsWhenPredicateNeverMatches() {
        final var result = Iterators.abortable(Iterators.of(1, 2, 3, 4), x -> x > 10);

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
     * Ensure AbortableIterator stops immediately when first element matches predicate.
     */
    @Test
    void shouldStopImmediatelyWhenFirstElementMatches() {
        final var result = Iterators.abortable(Iterators.of(5, 2, 3, 4), x -> x > 4);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }

    /**
     * Ensure AbortableIterator stops when predicate matches in middle.
     */
    @Test
    void shouldStopWhenPredicateMatchesInMiddle() {
        final var result = Iterators.abortable(Iterators.of(1, 2, 5, 3, 4), x -> x > 4);

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.next())
            .isEqualTo(2);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure AbortableIterator stops when predicate matches at end.
     */
    @Test
    void shouldStopWhenPredicateMatchesAtEnd() {
        final var result = Iterators.abortable(Iterators.of(1, 2, 3, 5), x -> x > 4);

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
     * Ensure multiple calls to hasNext() don't affect iteration.
     */
    @Test
    void shouldHandleMultipleHasNextCalls() {
        final var result = Iterators.abortable(Iterators.of(1, 2, 5), x -> x > 4);

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
     * Ensure AbortableIterator works with different types.
     */
    @Test
    void shouldWorkWithDifferentTypes() {
        final var result = Iterators.abortable(
            Iterators.of("hello", "world", "stop", "here"),
            s -> s.equals("stop"));

        assertThat(result.next())
            .isEqualTo("hello");

        assertThat(result.next())
            .isEqualTo("world");

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure remove() throws UnsupportedOperationException.
     */
    @Test
    void shouldThrowUnsupportedOperationExceptionOnRemove() {
        final var result = Iterators.abortable(Iterators.of(1, 2, 3), x -> false);

        assertThatThrownBy(result::remove)
            .isInstanceOf(UnsupportedOperationException.class)
            .hasMessage("Unable to remove from an AbortableIterator");
    }

    /**
     * Ensure next() throws NoSuchElementException when no elements available.
     */
    @Test
    void shouldThrowNoSuchElementExceptionWhenNoElementsAvailable() {
        final var result = Iterators.abortable(Iterators.of(1), x -> x == 1);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class)
            .hasMessage("No more elements are available");
    }

    /**
     * Ensure AbortableIterator handles single element that doesn't match predicate.
     */
    @Test
    void shouldHandleSingleElementThatDoesNotMatch() {
        final var result = Iterators.abortable(Iterators.of(1), x -> x > 5);

        assertThat(result.hasNext())
            .isTrue();

        assertThat(result.next())
            .isEqualTo(1);

        assertThat(result.hasNext())
            .isFalse();
    }

    /**
     * Ensure AbortableIterator handles single element that matches predicate.
     */
    @Test
    void shouldHandleSingleElementThatMatches() {
        final var result = Iterators.abortable(Iterators.of(10), x -> x > 5);

        assertThat(result.hasNext())
            .isFalse();

        assertThatThrownBy(result::next)
            .isInstanceOf(NoSuchElementException.class);
    }
}
