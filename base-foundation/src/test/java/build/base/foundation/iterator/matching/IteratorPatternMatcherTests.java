package build.base.foundation.iterator.matching;

import build.base.foundation.Capture;
import static build.base.foundation.predicate.Predicates.descriptive;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link IteratorPatternMatcher}s.
 *
 * @author brian.oliver
 * @since Jun-2019
 */
class IteratorPatternMatcherTests {

    /**
     * Assert that an {@link IteratorPatternMatcher} matches the specified values.
     *
     * @param <T>     the type of values
     * @param matcher the {@link IteratorPatternMatcher}
     * @param values  the values (may be empty)
     */
    @SafeVarargs
    private static <T> void assertMatches(final IteratorPatternMatcher<T> matcher,
                                          final T... values) {

        final Stream<T> stream = values == null
            ? Stream.empty()
            : Arrays.stream(values);

        assertThat(matcher.test(stream))
            .isTrue();
    }

    /**
     * Assert that an {@link IteratorPatternMatcher} does not match the specified values.
     *
     * @param <T>     the type of values
     * @param matcher the {@link IteratorPatternMatcher}
     * @param values  the values (may be empty)
     */
    @SafeVarargs
    private static <T> void assertFails(final IteratorPatternMatcher<T> matcher,
                                        final T... values) {

        final Stream<T> stream = values == null
            ? Stream.empty()
            : Arrays.stream(values);

        assertThat(matcher.test(stream))
            .isFalse();
    }

    @Test
    void shouldStartMatchingEmptyIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("starts().then().ends()");

        // positive tests
        assertMatches(matcher);

        // negative tests
        assertFails(matcher, 42);
        assertFails(matcher, 1, 2);
    }

    @Test
    void shouldStartMatchingAValueAtStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42)");

        // positive tests
        assertMatches(matcher, 42);
        assertMatches(matcher, 42, 1);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 0);
        assertFails(matcher, 1, 42);
    }

    @Test
    void shouldMatchAValueInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42);

        assertThat(matcher.toString())
            .isEqualTo("matches(42)");

        // positive tests
        assertMatches(matcher, 42);
        assertMatches(matcher, 42, 2, 3, 4, 5);
        assertMatches(matcher, 1, 42, 3, 4, 5);
        assertMatches(matcher, 1, 2, 42, 4, 5);
        assertMatches(matcher, 1, 2, 3, 42, 5);
        assertMatches(matcher, 1, 2, 3, 4, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
    }

    @Test
    void shouldMatchASequenceOfValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42)
            .then().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).then().matches(84)");

        // positive tests
        assertMatches(matcher, 42, 84, 3, 4);
        assertMatches(matcher, 1, 42, 84, 4);
        assertMatches(matcher, 1, 2, 42, 84);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);

        assertFails(matcher, 42, 2);
        assertFails(matcher, 1, 42);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 84);

        assertFails(matcher, 1, 84);
        assertFails(matcher, 84, 2);

        assertFails(matcher, 84, 2, 3);
        assertFails(matcher, 1, 84, 3);
        assertFails(matcher, 1, 2, 84);

        assertFails(matcher, 42, 2, 84);
        assertFails(matcher, 84, 2, 42);

        assertFails(matcher, 42, 2, 84, 4);
        assertFails(matcher, 1, 42, 3, 84);
        assertFails(matcher, 42, 2, 3, 84);

        assertFails(matcher, 1, 42, 3, 84, 5);
    }

    @Test
    void shouldMatchValueAtEndOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).then().ends()");

        // positive tests
        assertMatches(matcher, 42);
        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 1, 2, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 42, 2);
    }

    @Test
    void shouldSkipValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42)
            .then().skip(1)
            .then().matches(42);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).then().skip(1).then().matches(42)");

        // positive tests
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 1, 42, 3, 42);
        assertMatches(matcher, 1, 42, 3, 42, 5);
        assertMatches(matcher, 1, 2, 42, 4, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 42);
        assertFails(matcher, 42, 2);
        assertFails(matcher, 42, 42);
    }

    @Test
    void shouldStartSkippingValuesAtTheStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().skip(1)
            .then().matches(42);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().skip(1).then().matches(42)");

        // positive tests
        assertMatches(matcher, 1, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 42);
        assertFails(matcher, 1, 2, 42);
    }

    @Test
    void shouldSkipValuesBeforeTheEndOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42)
            .then().skip(1)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).then().skip(1).then().ends()");

        // positive tests
        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 1, 42, 3);
        assertMatches(matcher, 1, 2, 42, 4);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 42);
        assertFails(matcher, 1, 42);
        assertFails(matcher, 1, 2, 42);
    }

    @Test
    void shouldFailToStartSkippingValuesAtTheStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().skip(1)
            .then().matches(42);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().skip(1).then().matches(42)");

        // positive tests
        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 1, 42, 3);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 42);
        assertFails(matcher, 42, 2);
        assertFails(matcher, 42, 2, 3);
    }

    @Test
    void shouldMatchNumerousValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42)
            .thenLater().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).thenLater().matches(84)");

        // positive tests
        assertMatches(matcher, 42, 84);

        assertMatches(matcher, 42, 84, 3);
        assertMatches(matcher, 42, 2, 84);
        assertMatches(matcher, 1, 42, 84);

        assertMatches(matcher, 42, 84, 3, 4);
        assertMatches(matcher, 42, 2, 84, 4);
        assertMatches(matcher, 42, 2, 3, 84);
        assertMatches(matcher, 1, 42, 84, 4);
        assertMatches(matcher, 1, 42, 3, 84);
        assertMatches(matcher, 1, 2, 42, 84);

        assertMatches(matcher, 42, 84, 3, 4, 5);
        assertMatches(matcher, 42, 2, 84, 4, 5);
        assertMatches(matcher, 42, 2, 3, 84, 5);
        assertMatches(matcher, 42, 2, 3, 4, 84);
        assertMatches(matcher, 1, 42, 84, 4, 5);
        assertMatches(matcher, 1, 42, 3, 84, 5);
        assertMatches(matcher, 1, 42, 3, 4, 84);
        assertMatches(matcher, 1, 2, 42, 84, 5);
        assertMatches(matcher, 1, 2, 42, 4, 84);
        assertMatches(matcher, 1, 2, 3, 42, 84);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
        assertFails(matcher, 1, 2, 3, 4);

        assertFails(matcher, 42);
        assertFails(matcher, 84);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);
        assertFails(matcher, 84, 42);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 1, 84);
        assertFails(matcher, 84, 2);

        assertFails(matcher, 84, 2, 3);
        assertFails(matcher, 1, 84, 3);
        assertFails(matcher, 1, 2, 84);
    }

    @Test
    void shouldStartMatchingNumerousSequentialValuesAtTheStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42)
            .thenLater().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).thenLater().matches(84)");

        // positive tests
        assertMatches(matcher, 42, 84);

        assertMatches(matcher, 42, 84, 3);
        assertMatches(matcher, 42, 2, 84);

        assertMatches(matcher, 42, 84, 3, 4);
        assertMatches(matcher, 42, 2, 84, 4);
        assertMatches(matcher, 42, 2, 3, 84);

        assertMatches(matcher, 42, 84, 3, 4, 5);
        assertMatches(matcher, 42, 2, 84, 4, 5);
        assertMatches(matcher, 42, 2, 3, 84, 5);
        assertMatches(matcher, 42, 2, 3, 4, 84);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
        assertFails(matcher, 1, 2, 3, 4);

        assertFails(matcher, 42);
        assertFails(matcher, 84);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 1, 42, 84);

        assertFails(matcher, 1, 84);
        assertFails(matcher, 84, 2);

        assertFails(matcher, 84, 2, 3);
        assertFails(matcher, 1, 84, 3);
        assertFails(matcher, 1, 2, 84);

        assertFails(matcher, 1, 42, 84, 4);
        assertFails(matcher, 1, 42, 3, 84);
        assertFails(matcher, 1, 2, 42, 84);

        assertFails(matcher, 1, 42, 84, 4, 5);
        assertFails(matcher, 1, 42, 3, 84, 5);
        assertFails(matcher, 1, 42, 3, 4, 84);
        assertFails(matcher, 1, 2, 42, 84, 5);
        assertFails(matcher, 1, 2, 42, 4, 84);
        assertFails(matcher, 1, 2, 3, 42, 84);
    }

    @Test
    void shouldMatchNumerousEndingValuesAtTheEndOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42)
            .thenLater().matches(84)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).thenLater().matches(84).then().ends()");

        // positive tests
        assertMatches(matcher, 42, 84);

        assertMatches(matcher, 42, 2, 84);
        assertMatches(matcher, 1, 42, 84);

        assertMatches(matcher, 42, 2, 3, 84);
        assertMatches(matcher, 1, 42, 3, 84);
        assertMatches(matcher, 1, 2, 42, 84);

        assertMatches(matcher, 42, 2, 3, 4, 84);
        assertMatches(matcher, 1, 42, 3, 4, 84);
        assertMatches(matcher, 1, 2, 42, 4, 84);
        assertMatches(matcher, 1, 2, 3, 42, 84);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
        assertFails(matcher, 1, 2, 3, 4);

        assertFails(matcher, 42);
        assertFails(matcher, 84);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 1, 84);
        assertFails(matcher, 84, 2);

        assertFails(matcher, 84, 2, 3);
        assertFails(matcher, 1, 84, 3);
        assertFails(matcher, 1, 2, 84);

        assertFails(matcher, 42, 84, 3);

        assertFails(matcher, 42, 84, 3, 4);
        assertFails(matcher, 42, 2, 84, 4);
        assertFails(matcher, 1, 42, 84, 4);

        assertFails(matcher, 42, 84, 3, 4, 5);
        assertFails(matcher, 42, 2, 84, 4, 5);
        assertFails(matcher, 42, 2, 3, 84, 5);
        assertFails(matcher, 1, 42, 84, 4, 5);
        assertFails(matcher, 1, 42, 3, 84, 5);
        assertFails(matcher, 1, 2, 42, 84, 5);
    }

    @Test
    void shouldMatchRepeatedValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).times(3);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).times(3)");

        // positive tests
        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 1, 42, 42, 42);
        assertMatches(matcher, 42, 42, 42, 4);
        assertMatches(matcher, 42, 42, 42, 42);

        assertMatches(matcher, 42, 42, 42, 4, 5);
        assertMatches(matcher, 1, 42, 42, 42, 5);
        assertMatches(matcher, 1, 2, 42, 42, 42);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);
        assertFails(matcher, 42, 42);
    }

    @Test
    void shouldStartMatchingRepeatedValuesAtStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42).times(3);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).times(3)");

        // positive tests
        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 42, 42, 42, 4);
        assertMatches(matcher, 42, 42, 42, 42);

        assertMatches(matcher, 42, 42, 42, 4, 5);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);
        assertFails(matcher, 42, 42);

        assertFails(matcher, 1, 42, 42, 42);

        assertFails(matcher, 1, 42, 42, 42, 5);
        assertFails(matcher, 1, 2, 42, 42, 42);
    }

    @Test
    void shouldMatchEndingRepeatedValuesAtTheEndOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).times(3)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).times(3).then().ends()");

        // positive tests
        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 1, 42, 42, 42);
        assertMatches(matcher, 42, 42, 42, 42);

        assertMatches(matcher, 1, 2, 42, 42, 42);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);
        assertFails(matcher, 42, 42);

        assertFails(matcher, 42, 42, 42, 4);

        assertFails(matcher, 42, 42, 42, 4, 5);
        assertFails(matcher, 1, 42, 42, 42, 5);
    }

    @Test
    void shouldMatchAtLeastValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).atLeast(2);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).atLeast(2)");

        // positive tests
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 1, 42, 42);
        assertMatches(matcher, 42, 42, 3);

        assertMatches(matcher, 42, 42, 3, 4);
        assertMatches(matcher, 1, 42, 42, 3);
        assertMatches(matcher, 1, 2, 42, 42);

        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 1, 42, 42, 42);
        assertMatches(matcher, 42, 42, 42, 4);
        assertMatches(matcher, 42, 42, 42, 42);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);
        assertFails(matcher, 42, 2, 42);

        assertFails(matcher, 42, 2, 42, 4);
        assertFails(matcher, 42, 2, 3, 42);
        assertFails(matcher, 1, 42, 3, 42);
    }

    @Test
    void shouldStartMatchingAtLeastValuesAtStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42).atLeast(2);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).atLeast(2)");

        // positive tests
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 42, 42, 3);

        assertMatches(matcher, 42, 42, 3, 4);

        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 42, 42, 42, 4);
        assertMatches(matcher, 42, 42, 42, 42);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);
        assertFails(matcher, 42, 2, 42);

        assertFails(matcher, 1, 42, 42);

        assertFails(matcher, 1, 42, 42, 3);
        assertFails(matcher, 1, 2, 42, 42);

        assertFails(matcher, 42, 2, 42, 4);
        assertFails(matcher, 42, 2, 3, 42);
        assertFails(matcher, 1, 42, 3, 42);

        assertFails(matcher, 1, 42, 42, 42);
    }

    @Test
    void shouldMatchEndingAtLeastValuesAtTheEndOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).atLeast(2)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).atLeast(2).then().ends()");

        // positive tests
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 1, 42, 42);

        assertMatches(matcher, 1, 2, 42, 42);

        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 1, 42, 42, 42);
        assertMatches(matcher, 42, 42, 42, 42);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);
        assertFails(matcher, 42, 2, 42);

        assertFails(matcher, 42, 42, 3);

        assertFails(matcher, 42, 2, 42, 4);
        assertFails(matcher, 42, 2, 3, 42);
        assertFails(matcher, 1, 42, 3, 42);

        assertFails(matcher, 42, 42, 3, 4);
        assertFails(matcher, 1, 42, 42, 3);

        assertFails(matcher, 42, 42, 42, 4);
    }

    @Test
    void shouldMatchAtLeastValuesAtStartOfAStreamThenAnotherValue() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).atLeast(2)
            .then().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).atLeast(2).then().matches(84)");

        // positive tests
        assertMatches(matcher, 42, 42, 84);
        assertMatches(matcher, 1, 42, 42, 84);
        assertMatches(matcher, 1, 2, 42, 42, 84);
        assertMatches(matcher, 42, 42, 84, 4);
        assertMatches(matcher, 42, 42, 84, 4, 5);
        assertMatches(matcher, 1, 42, 42, 84, 5);

        assertMatches(matcher, 42, 42, 42, 84);
        assertMatches(matcher, 1, 42, 42, 42, 84);
        assertMatches(matcher, 1, 2, 42, 42, 42, 84);
        assertMatches(matcher, 42, 42, 42, 84, 5);
        assertMatches(matcher, 42, 42, 42, 84, 5, 6);
        assertMatches(matcher, 1, 2, 42, 42, 42, 84, 7, 8);

        // negative tests
        assertFails(matcher);

        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);
        assertFails(matcher, 42, 42);
        assertFails(matcher, 42, 42, 3);
        assertFails(matcher, 1, 42, 42);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);
        assertFails(matcher, 42, 2, 42);

        assertFails(matcher, 42, 2, 42, 4);
        assertFails(matcher, 42, 2, 3, 42);
        assertFails(matcher, 1, 42, 3, 42);
        assertFails(matcher, 42, 42, 3, 84);
        assertFails(matcher, 42, 42, 4, 84, 5);
        assertFails(matcher, 1, 42, 42, 4, 84);
        assertFails(matcher, 1, 42, 42, 4, 84, 5);
    }

    @Test
    void shouldMatchZeroOrMoreValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).zeroOrMoreTimes();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).zeroOrMoreTimes()");

        // positive tests
        assertMatches(matcher);
        assertMatches(matcher, 1);
        assertMatches(matcher, 1, 2);
        assertMatches(matcher, 1, 2, 3);

        assertMatches(matcher, 42);

        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 42, 2, 3);
        assertMatches(matcher, 1, 42, 3);
        assertMatches(matcher, 1, 2, 42);

        assertMatches(matcher, 42, 42, 3);
        assertMatches(matcher, 1, 42, 42);
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 42, 42, 42);

        // negative tests
        // (none)
    }

    @Test
    void shouldStartMatchingZeroOrMoreValuesAtStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42).zeroOrMoreTimes();

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).zeroOrMoreTimes()");

        // positive tests
        assertMatches(matcher);
        assertMatches(matcher, 1);
        assertMatches(matcher, 1, 2);
        assertMatches(matcher, 1, 2, 3);

        assertMatches(matcher, 42);

        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 42, 2, 3);
        assertMatches(matcher, 1, 42, 3);
        assertMatches(matcher, 1, 2, 42);

        assertMatches(matcher, 42, 42, 3);
        assertMatches(matcher, 1, 42, 42);
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 42, 42, 42);

        // negative tests
        // (none)
    }

    @Test
    void shouldMatchZeroOrMoreValuesAtStartOfAStreamThenAnotherValue() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).zeroOrMoreTimes()
            .then().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).zeroOrMoreTimes().then().matches(84)");

        // positive tests
        assertMatches(matcher, 84);
        assertMatches(matcher, 1, 84);
        assertMatches(matcher, 1, 2, 84);
        assertMatches(matcher, 42, 84);
        assertMatches(matcher, 1, 42, 84);
        assertMatches(matcher, 42, 42, 84);
        assertMatches(matcher, 1, 42, 42, 84);

        assertMatches(matcher, 84, 2);
        assertMatches(matcher, 1, 84, 3);
        assertMatches(matcher, 1, 2, 84, 4);
        assertMatches(matcher, 42, 84, 3);
        assertMatches(matcher, 42, 2, 84);
        assertMatches(matcher, 1, 42, 84, 4);
        assertMatches(matcher, 1, 42, 3, 84, 5);
        assertMatches(matcher, 42, 42, 84, 4);
        assertMatches(matcher, 42, 42, 3, 84);
        assertMatches(matcher, 42, 42, 3, 84, 5);
        assertMatches(matcher, 1, 42, 42, 84, 5);
        assertMatches(matcher, 1, 42, 3, 42, 84, 6);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);
        assertFails(matcher, 42, 42);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 42, 42, 3);
        assertFails(matcher, 1, 42, 42);
        assertFails(matcher, 42, 2, 42);
        assertFails(matcher, 42, 42, 42);
    }

    @Test
    void shouldStartMatchingZeroOrMoreValuesAtStartOfAStreamThenAnotherValue() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42).zeroOrMoreTimes()
            .then().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).zeroOrMoreTimes().then().matches(84)");

        // positive tests
        assertMatches(matcher, 84);
        assertMatches(matcher, 42, 84);
        assertMatches(matcher, 42, 42, 84);

        assertMatches(matcher, 84, 2);
        assertMatches(matcher, 42, 84, 3);
        assertMatches(matcher, 42, 42, 84, 4);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);
        assertFails(matcher, 42, 42);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 42, 42, 3);
        assertFails(matcher, 1, 42, 42);
        assertFails(matcher, 42, 2, 42);
        assertFails(matcher, 42, 42, 42);

        assertFails(matcher, 1, 84);
        assertFails(matcher, 1, 2, 84);
        assertFails(matcher, 1, 42, 84);
        assertFails(matcher, 1, 42, 42, 84);
        assertFails(matcher, 1, 84, 3);
        assertFails(matcher, 1, 2, 84, 4);
        assertFails(matcher, 42, 2, 84);
        assertFails(matcher, 1, 42, 84, 4);
        assertFails(matcher, 1, 42, 3, 84, 5);
        assertFails(matcher, 42, 42, 3, 84);
        assertFails(matcher, 42, 42, 3, 84, 5);
        assertFails(matcher, 1, 42, 42, 84, 5);
        assertFails(matcher, 1, 42, 3, 42, 84, 6);
    }

    @Test
    void shouldMatchOneOrMoreValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).oneOrMoreTimes();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).oneOrMoreTimes()");

        // positive tests
        assertMatches(matcher, 42);

        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 42, 2, 3);
        assertMatches(matcher, 1, 42, 3);
        assertMatches(matcher, 1, 2, 42);

        assertMatches(matcher, 42, 42, 3);
        assertMatches(matcher, 1, 42, 42);
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 42, 42, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
    }

    @Test
    void shouldStartMatchingOneOrMoreValuesAtStartOfAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42).oneOrMoreTimes();

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).oneOrMoreTimes()");

        // positive tests
        assertMatches(matcher, 42);

        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 42, 2, 3);

        assertMatches(matcher, 42, 42, 3);
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 42, 42, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);
        assertFails(matcher, 1, 42, 42);
    }

    @Test
    void shouldMatchOneOrMoreValuesAtStartOfAStreamThenAnotherValue() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).oneOrMoreTimes()
            .then().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).oneOrMoreTimes().then().matches(84)");

        // positive tests
        assertMatches(matcher, 42, 84);
        assertMatches(matcher, 42, 42, 84);

        assertMatches(matcher, 42, 84, 3);
        assertMatches(matcher, 1, 42, 84);

        assertMatches(matcher, 1, 42, 84, 4);
        assertMatches(matcher, 1, 2, 42, 84);

        assertMatches(matcher, 42, 42, 84, 4);
        assertMatches(matcher, 1, 42, 42, 84);

        assertMatches(matcher, 1, 42, 42, 84, 5);
        assertMatches(matcher, 1, 42, 3, 42, 84, 6);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);
        assertFails(matcher, 84);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);
        assertFails(matcher, 42, 42);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 42, 42, 3);
        assertFails(matcher, 1, 42, 42);
        assertFails(matcher, 42, 2, 42);
        assertFails(matcher, 42, 42, 42);

        assertFails(matcher, 1, 84);
        assertFails(matcher, 1, 2, 84);
        assertFails(matcher, 1, 84, 3);
        assertFails(matcher, 1, 2, 84, 4);
        assertFails(matcher, 42, 2, 84);
        assertFails(matcher, 1, 42, 3, 84, 5);
        assertFails(matcher, 42, 42, 3, 84);
        assertFails(matcher, 42, 42, 3, 84, 5);
    }

    @Test
    void shouldStartMatchingOneOrMoreValuesAtStartOfAStreamThenAnotherValue() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42).oneOrMoreTimes()
            .then().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).oneOrMoreTimes().then().matches(84)");

        // positive tests
        assertMatches(matcher, 42, 84);
        assertMatches(matcher, 42, 42, 84);

        assertMatches(matcher, 42, 84, 3);

        assertMatches(matcher, 42, 42, 84, 4);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42);
        assertFails(matcher, 84);

        assertFails(matcher, 1, 42);
        assertFails(matcher, 42, 2);
        assertFails(matcher, 42, 42);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);

        assertFails(matcher, 1, 42, 84);

        assertFails(matcher, 42, 42, 3);
        assertFails(matcher, 1, 42, 42);
        assertFails(matcher, 42, 2, 42);
        assertFails(matcher, 42, 42, 42);

        assertFails(matcher, 1, 42, 84, 4);
        assertFails(matcher, 1, 2, 42, 84);
        assertFails(matcher, 1, 42, 42, 84);
        assertFails(matcher, 1, 42, 42, 84, 5);
        assertFails(matcher, 1, 42, 3, 42, 84, 6);

        assertFails(matcher, 1, 84);
        assertFails(matcher, 1, 2, 84);
        assertFails(matcher, 1, 84, 3);
        assertFails(matcher, 1, 2, 84, 4);
        assertFails(matcher, 42, 2, 84);
        assertFails(matcher, 1, 42, 3, 84, 5);
        assertFails(matcher, 42, 42, 3, 84);
        assertFails(matcher, 42, 42, 3, 84, 5);
    }

    @Test
    void shouldMatchRangeOfValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).times(1, 2);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).times(1, 2)");

        // positive tests
        assertMatches(matcher, 42);

        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 42, 2, 3);
        assertMatches(matcher, 1, 42, 3);
        assertMatches(matcher, 1, 2, 42);

        assertMatches(matcher, 42, 42, 3);
        assertMatches(matcher, 1, 42, 42);
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 42, 2, 3, 4);
        assertMatches(matcher, 1, 42, 3, 4);
        assertMatches(matcher, 1, 2, 42, 4);
        assertMatches(matcher, 1, 2, 3, 42);

        assertMatches(matcher, 42, 42, 3, 4);
        assertMatches(matcher, 1, 42, 42, 4);
        assertMatches(matcher, 1, 2, 42, 42);
        assertMatches(matcher, 42, 2, 42, 4);
        assertMatches(matcher, 42, 2, 3, 42);
        assertMatches(matcher, 1, 42, 3, 42);

        assertMatches(matcher, 42, 42, 42, 4, 5);
        assertMatches(matcher, 1, 42, 42, 42, 5);
        assertMatches(matcher, 1, 2, 42, 42, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
    }

    @Test
    void shouldStartMatchingRangeOfValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .then().matches(42).times(1, 2);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42).times(1, 2)");

        // positive tests
        assertMatches(matcher, 42);

        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 42, 2, 3);

        assertMatches(matcher, 42, 42, 3);
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 42, 2, 3, 4);

        assertMatches(matcher, 42, 42, 3, 4);
        assertMatches(matcher, 42, 2, 42, 4);
        assertMatches(matcher, 42, 2, 3, 42);

        assertMatches(matcher, 42, 42, 42, 4, 5);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 1, 42);

        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 1, 2, 42);
        assertFails(matcher, 1, 42, 42);

        assertFails(matcher, 1, 42, 3, 4);
        assertFails(matcher, 1, 2, 42, 4);
        assertFails(matcher, 1, 2, 3, 42);
        assertFails(matcher, 1, 42, 42, 4);
        assertFails(matcher, 1, 2, 42, 42);
        assertFails(matcher, 1, 42, 3, 42);

        assertFails(matcher, 1, 42, 42, 42, 5);
        assertFails(matcher, 1, 2, 42, 42, 42);
    }

    @Test
    void shouldMatchRangeOfValuesInAnIteratorThenEnd() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).times(1, 2)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).times(1, 2).then().ends()");

        // positive tests
        assertMatches(matcher, 42);

        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 42, 42);

        assertMatches(matcher, 1, 2, 42);

        assertMatches(matcher, 1, 42, 42);
        assertMatches(matcher, 42, 2, 42);
        assertMatches(matcher, 42, 42, 42);

        assertMatches(matcher, 1, 2, 3, 42);

        assertMatches(matcher, 1, 2, 42, 42);
        assertMatches(matcher, 42, 2, 3, 42);
        assertMatches(matcher, 1, 42, 3, 42);

        assertMatches(matcher, 1, 2, 42, 42, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 42, 2);

        assertFails(matcher, 42, 2, 3);
        assertFails(matcher, 1, 42, 3);
        assertFails(matcher, 42, 42, 3);

        assertFails(matcher, 42, 2, 3, 4);
        assertFails(matcher, 1, 42, 3, 4);
        assertFails(matcher, 1, 2, 42, 4);
        assertFails(matcher, 42, 42, 3, 4);
        assertFails(matcher, 1, 42, 42, 4);
        assertFails(matcher, 42, 2, 42, 4);

        assertFails(matcher, 42, 42, 42, 4, 5);
        assertFails(matcher, 1, 42, 42, 42, 5);
    }

    @Test
    void shouldMatchRangeOfValuesInAnIteratorThenAnotherValue() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).times(1, 2)
            .then().matches(84);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).times(1, 2).then().matches(84)");

        // positive tests
        assertMatches(matcher, 42, 84);

        assertMatches(matcher, 1, 42, 84);
        assertMatches(matcher, 42, 42, 84);

        assertMatches(matcher, 1, 42, 84, 4);
        assertMatches(matcher, 1, 2, 42, 84);

        assertMatches(matcher, 42, 42, 84, 4);
        assertMatches(matcher, 1, 42, 42, 84);
        assertMatches(matcher, 42, 42, 42, 84);

        assertMatches(matcher, 42, 84, 3, 4, 5);
        assertMatches(matcher, 1, 42, 84, 4, 5);
        assertMatches(matcher, 1, 2, 42, 84, 5);
        assertMatches(matcher, 1, 2, 3, 42, 84);

        assertMatches(matcher, 42, 42, 84, 4, 5);
        assertMatches(matcher, 1, 42, 42, 84, 5);
        assertMatches(matcher, 1, 2, 42, 42, 84);

        assertMatches(matcher, 42, 42, 42, 84, 5);
        assertMatches(matcher, 1, 42, 42, 42, 84);
        assertMatches(matcher, 42, 42, 42, 42, 84);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);

        assertFails(matcher, 84);
        assertFails(matcher, 1, 84);
        assertFails(matcher, 84, 2);
        assertFails(matcher, 1, 84, 3);
    }

    @Test
    void shouldOptionallyMatchWithInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).optionally();

        assertThat(matcher.toString())
            .isEqualTo("matches(42).optionally()");

        // positive tests
        assertMatches(matcher);
        assertMatches(matcher, 1);
        assertMatches(matcher, 42);

        assertMatches(matcher, 1, 2);
        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 42, 42);

        // negative tests
        // (none)
    }

    @Test
    void shouldOptionallyMatchWithValuesInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42)
            .then().matches(84).optionally()
            .then().matches(42);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).then().matches(84).optionally().then().matches(42)");

        // positive tests
        assertMatches(matcher, 42, 42);
        assertMatches(matcher, 42, 84, 42);

        assertMatches(matcher, 42, 42, 1);
        assertMatches(matcher, 1, 42, 42);

        assertMatches(matcher, 42, 84, 42, 1);
        assertMatches(matcher, 1, 42, 84, 42);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 42);
        assertFails(matcher, 42, 84);
        assertFails(matcher, 42, 1, 42);
        assertFails(matcher, 42, 84, 84, 42);
    }

    @Test
    void shouldMatchSequentialIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42).times(2)
            .thenLater().matches(42).times(2);

        assertThat(matcher.toString())
            .isEqualTo("matches(42).times(2).thenLater().matches(42).times(2)");

        // positive tests
        assertMatches(matcher, 42, 42, 42, 42);

        assertMatches(matcher, 1, 42, 42, 42, 42);
        assertMatches(matcher, 42, 42, 42, 42, 5);

        assertMatches(matcher, 42, 42, 3, 42, 42);
        assertMatches(matcher, 42, 42, 3, 4, 42, 42);

        assertMatches(matcher, 1, 42, 42, 4, 5, 42, 42);
        assertMatches(matcher, 42, 42, 3, 4, 42, 42, 7);

        assertMatches(matcher, 1, 42, 42, 4, 5, 42, 42, 8);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 42);
        assertFails(matcher, 42, 42);
        assertFails(matcher, 42, 42, 42);

    }

    @Test
    void shouldMatchAValueInAnInfiniteIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matches(42);

        assertThat(matcher.toString())
            .isEqualTo("matches(42)");

        final Stream<Integer> infinite = Stream.iterate(0, i -> i + 2);

        // positive tests
        assertThat(matcher.test(infinite))
            .isTrue();

        // negative tests (they'd never terminate)
        // (none)
    }

    @Test
    void shouldMatchAValueAtTheStartOfAnInfiniteIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .<Integer>starts()
            .then().matches(42);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().matches(42)");

        final Stream<Integer> infinite = Stream.iterate(0, i -> i + 2);

        // positive tests (they'd never terminate)
        // (none)

        // negative tests
        assertThat(matcher.test(infinite))
            .isFalse();
    }

    @Test
    void shouldSkipWhileSatisfyingAPredicateInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .<Integer>starts()
            .then().skipWhile(descriptive(v -> v < 5, "< 5"))
            .then().matches(5);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().skipWhile(< 5).then().matches(5)");

        // positive tests
        assertMatches(matcher, 5);
        assertMatches(matcher, 1, 5);
        assertMatches(matcher, 1, 2, 5);
        assertMatches(matcher, 5, 2);
        assertMatches(matcher, 1, 2, 5, 6);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
    }

    @Test
    void shouldSkipUntilAPredicateIsSatisfiedInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .<Integer>starts()
            .then().skipUntil(descriptive(v -> v >= 5, ">= 5"))
            .then().matches(5);

        assertThat(matcher.toString())
            .isEqualTo("starts().then().skipWhile(not(>= 5)).then().matches(5)");

        // positive tests
        assertMatches(matcher, 5);
        assertMatches(matcher, 1, 5);
        assertMatches(matcher, 1, 2, 5);
        assertMatches(matcher, 5, 2);
        assertMatches(matcher, 1, 2, 5, 6);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
    }

    @Test
    void shouldSatisfyANestedPatternInAnIterator() {

        final IteratorPatternMatcher<Integer> nested = IteratorPatternMatchers
            .matches(42)
            .then().matches(84);

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .satisfies(nested);

        assertThat(matcher.toString())
            .isEqualTo("satisfies(matches(42).then().matches(84))");

        // positive tests
        assertMatches(matcher, 42, 84);

        assertMatches(matcher, 42, 84, 3);
        assertMatches(matcher, 1, 42, 84);

        assertMatches(matcher, 42, 84, 3, 4);
        assertMatches(matcher, 1, 42, 84, 4);
        assertMatches(matcher, 1, 2, 42, 84);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 42);
        assertFails(matcher, 84);

        assertFails(matcher, 1, 2);
        assertFails(matcher, 84, 42);
    }

    @Test
    void shouldSatisfyASequenceOfNestedPatternsInAnIterator() {

        final IteratorPatternMatcher<Integer> nested = IteratorPatternMatchers
            .matches(42)
            .then().matches(84);

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .satisfies(nested)
            .then().satisfies(nested);

        assertThat(matcher.toString())
            .isEqualTo("satisfies(matches(42).then().matches(84)).then().satisfies(matches(42).then().matches(84))");

        // positive tests
        assertMatches(matcher, 42, 84, 42, 84);
        assertMatches(matcher, 1, 42, 84, 42, 84);
        assertMatches(matcher, 42, 84, 42, 84, 5);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 5);
        assertFails(matcher, 42);
        assertFails(matcher, 42, 84);
        assertFails(matcher, 1, 42, 84);
        assertFails(matcher, 1, 42, 84, 4);
        assertFails(matcher, 1, 2, 42, 84);
        assertFails(matcher, 42, 84, 3, 42, 84);
    }

    @Test
    void shouldRepetitivelySatisfyANestedPatternInAnIterator() {

        final IteratorPatternMatcher<Integer> nested = IteratorPatternMatchers
            .matches(42)
            .then().matches(84);

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .satisfies(nested).times(2);

        assertThat(matcher.toString())
            .isEqualTo("satisfies(matches(42).then().matches(84)).times(2)");

        // positive tests
        assertMatches(matcher, 42, 84, 42, 84);
        assertMatches(matcher, 1, 42, 84, 42, 84);
        assertMatches(matcher, 42, 84, 42, 84, 5);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 5);
        assertFails(matcher, 42);
        assertFails(matcher, 42, 84);
        assertFails(matcher, 42, 84, 42);
        assertFails(matcher, 1, 42, 84);
        assertFails(matcher, 1, 42, 84, 4);
        assertFails(matcher, 1, 2, 42, 84);
        assertFails(matcher, 42, 84, 3, 42, 84);
    }

    @Test
    void shouldMatchEitherValueInAnIterator() {

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .matchesEither(42, 84);

        assertThat(matcher.toString())
            .isEqualTo("matches(anyOf(42, 84))");

        // positive tests
        assertMatches(matcher, 42);
        assertMatches(matcher, 84);

        assertMatches(matcher, 42, 2);
        assertMatches(matcher, 1, 42);
        assertMatches(matcher, 1, 42, 3);

        assertMatches(matcher, 84);
        assertMatches(matcher, 84, 2);
        assertMatches(matcher, 1, 84);
        assertMatches(matcher, 1, 84, 3);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
    }

    @Test
    void shouldMatchConsecutiveNestedPatternInAnIterator() {

        final IteratorPatternMatcher<Integer> pattern = IteratorPatternMatchers.matches(42);

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers
            .satisfies(pattern).times(3);

        assertThat(matcher.toString())
            .isEqualTo("satisfies(matches(42)).times(3)");

        // positive tests
        assertMatches(matcher, 42, 42, 42);
        assertMatches(matcher, 1, 42, 42, 42);
        assertMatches(matcher, 1, 42, 42, 42, 5);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 42);
        assertFails(matcher, 42, 42);
        assertFails(matcher, 1, 42, 3, 42, 5, 42);
    }

    @Test
    void shouldMatchThenNeverMatch() {

        final IteratorPatternMatcher<Object> matcher = IteratorPatternMatchers.starts()
            .thenLater().matches(String.class)
            .then().matchesNoneOf(Integer.class)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo(
                "starts().thenLater().matches(java.lang.String::isInstance).then().matches(not(java.lang.Integer::isInstance)).then().ends()");

        final IteratorPatternMatcher<Object> matcherOptionally = IteratorPatternMatchers.starts()
            .thenLater().matches(String.class)
            .then().matchesNoneOf(Integer.class).optionally()
            .then().ends();

        assertThat(matcherOptionally.toString())
            .isEqualTo(
                "starts().thenLater().matches(java.lang.String::isInstance).then().matches(not(java.lang.Integer::isInstance)).optionally().then().ends()");

        final IteratorPatternMatcher<Object> matcherTwice = IteratorPatternMatchers.starts()
            .thenLater().matches(String.class)
            .then().matchesNoneOf(Integer.class).times(2)
            .then().ends();

        assertThat(matcherTwice.toString())
            .isEqualTo(
                "starts().thenLater().matches(java.lang.String::isInstance).then().matches(not(java.lang.Integer::isInstance)).times(2).then().ends()");

        // positive tests
        assertMatches(matcher, "foo", "bar");
        assertMatches(matcher, "foo", 5.6);
        assertMatches(matcherOptionally, "foo");
        assertMatches(matcherTwice, "foo", "bar", "biff");
        assertMatches(matcherTwice, "foo", 5.6, "bar");

        // negative tests
        assertFails(matcher, "foo");
        assertFails(matcher, "foo", 4);
        assertFails(matcherTwice, "foo", "bar");
        assertFails(matcherTwice, "foo", 4, "bar");
    }

    @Test
    void shouldCaptureElements() {

        final Capture<Object> first = Capture.empty();
        final Capture<Object> again = Capture.empty();
        final Capture<Object> second = Capture.empty();

        final IteratorPatternMatcher<Object> matcher = IteratorPatternMatchers.starts()
            .thenLater().matches(String.class).capture(first).capture(again)
            .then().matchesNoneOf(Integer.class).capture(second)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo(
                "starts().thenLater().matches(java.lang.String::isInstance).then().matches(not(java.lang.Integer::isInstance)).then().ends()");
        ;

        // positive tests
        assertMatches(matcher, "foo", "bar");

        assertThat(first.optional())
            .isPresent()
            .contains("foo");

        assertThat(second.optional())
            .isPresent()
            .contains("bar");

        assertThat(first)
            .isEqualTo(again);

        assertMatches(matcher, "foo", 5.6);

        assertThat(first.optional())
            .isPresent()
            .contains("foo");

        assertThat(second.optional())
            .isPresent()
            .contains(5.6);

        assertThat(first)
            .isEqualTo(again);

        // negative tests
        assertFails(matcher, "foo");

        assertThat(first.optional())
            .isPresent();

        assertThat(second.optional())
            .isEmpty();

        assertThat(first)
            .isEqualTo(again);
    }

    @Test
    void shouldMatchAnyElement() {

        final Capture<Object> first = Capture.empty();
        final Capture<Object> again = Capture.empty();
        final Capture<Object> second = Capture.empty();

        final IteratorPatternMatcher<Object> matcher = IteratorPatternMatchers.starts()
            .thenLater().matchesAny().capture(first).capture(again)
            .then().matchesAny().capture(second)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo("starts().thenLater().matchesAny().then().matchesAny().then().ends()");

        // positive tests
        assertMatches(matcher, "foo", "bar");

        assertThat(first.optional())
            .isPresent()
            .contains("foo");

        assertThat(second.optional())
            .isPresent()
            .contains("bar");

        assertThat(first)
            .isEqualTo(again);

        assertMatches(matcher, "foo", 5.6);

        assertThat(first.optional())
            .isPresent()
            .contains("foo");

        assertThat(second.optional())
            .isPresent()
            .contains(5.6);

        assertThat(first)
            .isEqualTo(again);

        // negative tests
        assertFails(matcher, "foo");

        assertThat(first.optional())
            .isPresent();

        assertThat(second.optional())
            .isEmpty();

        assertThat(first)
            .isEqualTo(again);
    }

    @Test
    void shouldMapCaptureAndMatchMatchedElements() {

        final Capture<Object> first = Capture.empty();
        final Capture<Object> again = Capture.empty();
        final Capture<Object> second = Capture.empty();

        final IteratorPatternMatcher<Object> matcher = IteratorPatternMatchers.starts()
            .thenLater().matches(String.class).map(String::length).capture(first).capture(again).match(l -> l > 2)
            .then().matchesNoneOf(Integer.class).capture(second)
            .then().ends();

        assertThat(matcher.toString())
            .isEqualTo(
                "starts().thenLater().matches(java.lang.String::isInstance).then().matches(not(java.lang.Integer::isInstance)).then().ends()");

        // positive tests
        assertMatches(matcher, "foo", "garl");

        assertThat(first.optional())
            .isPresent()
            .contains(3);

        assertThat(second.optional())
            .isPresent()
            .contains("garl");

        assertThat(first)
            .isEqualTo(again);

        assertMatches(matcher, "foo", 5.6);

        assertThat(first.optional())
            .isPresent()
            .contains(3);

        assertThat(second.optional())
            .isPresent()
            .contains(5.6);

        assertThat(first)
            .isEqualTo(again);

        // negative tests
        assertFails(matcher, "foo");

        assertThat(first.optional())
            .isPresent();

        assertThat(second.optional())
            .isEmpty();

        assertThat(first)
            .isEqualTo(again);
    }

    @Test
    void shouldMapCaptureAndMatchMultipleMatchedElements() {

        final Capture<List<Integer>> capture = Capture.empty();

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .thenLater().matchesAny().times(4)
            .map(number -> number * 2)
            .filter(number -> number > 10)
            .collect(Collectors.toList())
            .capture(capture)
            .match(list -> list.size() == 4);

        // positive tests
        assertMatches(matcher, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        assertThat(capture.isPresent())
            .isTrue();

        assertThat(capture.get())
            .containsExactly(12, 14, 16, 18);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
        assertFails(matcher, 1, 2, 3, 4);

        assertThat(capture.isPresent())
            .isFalse();
    }

    @Test
    void shouldMapCaptureAndMatchAtLeastMatchedElements() {

        final Capture<List<Integer>> capture = Capture.empty();

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .thenLater().matchesAny().atLeast(4)
            .map(number -> number * 2)
            .filter(number -> number < 10)
            .collect(Collectors.toList())
            .capture(capture)
            .match(list -> list.size() == 4);

        // positive tests
        assertMatches(matcher, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        assertThat(capture.isPresent())
            .isTrue();

        assertThat(capture.get())
            .containsExactly(2, 4, 6, 8);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
        assertFails(matcher, 1, 2, 3, 5);

        assertThat(capture.isPresent())
            .isFalse();
    }

    @Test
    void shouldMapCaptureAndMatchRangeOfMatchedElements() {

        final Capture<List<Integer>> capture = Capture.empty();

        final IteratorPatternMatcher<Integer> matcher = IteratorPatternMatchers.<Integer>starts()
            .thenLater().matchesAny().times(2, 4)
            .map(number -> number * 2)
            .filter(number -> number > 10)
            .collect(Collectors.toList())
            .capture(capture)
            .match(list -> list.size() == 2);

        // positive tests
        assertMatches(matcher, 1, 2, 3, 4, 5, 6, 7, 8, 9);

        assertThat(capture.isPresent())
            .isTrue();

        assertThat(capture.get())
            .containsExactly(12, 14);

        // negative tests
        assertFails(matcher);
        assertFails(matcher, 1);
        assertFails(matcher, 1, 2);
        assertFails(matcher, 1, 2, 3);
        assertFails(matcher, 1, 2, 3, 5);

        assertThat(capture.isPresent())
            .isFalse();
    }
}
