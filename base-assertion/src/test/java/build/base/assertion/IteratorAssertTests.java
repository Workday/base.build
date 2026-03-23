package build.base.assertion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IteratorAssert}.
 *
 * @author brian.oliver
 * @since Feb-2026
 */
class IteratorAssertTests {

    /**
     * Ensure a pattern that eventually matches an element is asserted as {@code true}.
     */
    @Test
    void shouldMatchWhenPatternEventuallyFindsElement() {
        IteratorAssert.<String>assertThat(List.of("Fred", "Barney", "Rubble"))
            .starts()
            .thenLater().matches("Barney")
            .then().matches("Rubble")
            .isTrue();
    }

    /**
     * Ensure a pattern that does not match is asserted as {@code false}.
     */
    @Test
    void shouldNotMatchWhenElementIsAbsent() {
        IteratorAssert.<String>assertThat(List.of("Fred", "Wilma"))
            .starts()
            .thenLater().matches("Barney")
            .isFalse();
    }

    /**
     * Ensure {@code isTrue()} throws an {@link AssertionError} when the pattern does not match.
     */
    @Test
    void shouldThrowWhenIsTrueButPatternDoesNotMatch() {
        assertThatThrownBy(() ->
            IteratorAssert.<String>assertThat(List.of("Fred", "Wilma"))
                .starts()
                .thenLater().matches("Barney")
                .isTrue())
            .isInstanceOf(AssertionError.class);
    }

    /**
     * Ensure {@code isFalse()} throws an {@link AssertionError} when the pattern does match.
     */
    @Test
    void shouldThrowWhenIsFalseButPatternMatches() {
        assertThatThrownBy(() ->
            IteratorAssert.<String>assertThat(List.of("Fred", "Barney"))
                .starts()
                .thenLater().matches("Barney")
                .isFalse())
            .isInstanceOf(AssertionError.class);
    }

    /**
     * Ensure a pattern using {@code ends()} asserts correctly against an empty source.
     */
    @Test
    void shouldMatchEmptySourceUsingEnds() {
        IteratorAssert.<String>assertThat(List.of())
            .starts()
            .then().ends()
            .isTrue();
    }

    /**
     * Ensure an {@link java.util.Iterator} source is accepted and evaluated correctly.
     */
    @Test
    void shouldMatchWhenSourceIsIterator() {
        IteratorAssert.assertThat(List.of("Fred", "Barney").iterator())
            .starts()
            .thenLater().matches("Barney")
            .isTrue();
    }

    /**
     * Ensure a {@link Stream} source is accepted and evaluated correctly.
     */
    @Test
    void shouldMatchWhenSourceIsStream() {
        IteratorAssert.assertThat(Stream.of("Fred", "Barney", "Rubble"))
            .starts()
            .thenLater().matches("Rubble")
            .isTrue();
    }

    /**
     * Ensure {@link Eventually#assertThat(Iterable)} returns an {@link IteratorPatternMatcherRetryableAssertion}.
     */
    @Test
    void shouldMatchViaEventuallyAssertThat() {
        Eventually.assertThat(List.of("Fred", "Barney", "Rubble"))
            .starts()
            .thenLater().matches("Barney")
            .then().matches("Rubble")
            .isTrue();
    }

    /**
     * Ensure a pattern using repetition ({@code zeroOrMoreTimes}) is asserted correctly.
     */
    @Test
    void shouldMatchWithZeroOrMoreTimesRepetition() {
        IteratorAssert.<String>assertThat(List.of("Fred", "Barney", "Barney", "Rubble"))
            .starts()
            .thenLater().matches("Barney")
            .zeroOrMoreTimes()
            .thenLater().matches("Rubble")
            .isTrue();
    }

    /**
     * Ensure a pattern using a {@link Class} condition matches correctly.
     */
    @Test
    void shouldMatchWhenClassConditionIsUsed() {
        IteratorAssert.<Object>assertThat(List.of("Fred", 42, "Rubble"))
            .starts()
            .thenLater().matches(Integer.class)
            .isTrue();
    }
}
