package build.base.parsing;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Scanner}.
 *
 * @author brian.oliver
 * @since Aug-2019
 */
class ScannerTests {

    @Test
    void shouldScanEmptyString() {
        final var scanner = new Scanner("");

        assertThat(scanner.hasNext())
            .isFalse();
    }

    @Test
    void shouldScanStringShorterThanPattern() {
        final var scanner = new Scanner("cat");

        assertThat(scanner.follows("catatonic"))
            .isFalse();

        scanner.consume("cat");

        assertThat(scanner.hasNext())
            .isFalse();
    }

    @Test
    void shouldScanStringLongerThanPattern() {
        final var scanner = new Scanner("catatonic");

        assertThat(scanner.follows("cat"))
            .isTrue();

        scanner.consume("cat");
        scanner.consume("atonic");

        assertThat(scanner.hasNext())
            .isFalse();
    }

    @Test
    void shouldScanStringWithWhiteSpace() {
        final var scanner = new Scanner("  module  {  \t  } \t \t \t")
            .register(Filter.WHITESPACE);

        assertThat(scanner.consume("module"))
            .isEqualTo("module");

        scanner.consume("{");
        scanner.consume("}");

        assertThat(scanner.hasNext())
            .isFalse();
    }

    @Test
    void shouldScanStringWithNewLines() {
        final var scanner = new Scanner("module\n{\n}\n\n")
            .register(Filter.WHITESPACE);

        assertThat(scanner.consume("module"))
            .isEqualTo("module");

        scanner.consume("{");
        scanner.consume("}");

        assertThat(scanner.hasNext())
            .isFalse();
    }

    @Test
    void shouldScanPatternsAtStartAndEndOfALine() {
        final var scanner = new Scanner("hello world")
            .register(Filter.WHITESPACE);

        assertThat(scanner.hasNext()).isTrue();

        assertThat(scanner.follows(Pattern.compile("^hello")))
            .isTrue();

        assertThat(scanner.follows(Pattern.compile("hello")))
            .isTrue();

        assertThat(scanner.follows(Pattern.compile("world")))
            .isFalse();

        scanner.consume("hello");

        assertThat(scanner.follows(Pattern.compile("^world$")))
            .isFalse();

        assertThat(scanner.follows(Pattern.compile("world$")))
            .isTrue();

        assertThat(scanner.follows(Pattern.compile("world")))
            .isTrue();

        scanner.consume("world");

        assertThat(scanner.hasNext())
            .isFalse();
    }

    @Test
    void shouldScanUsingSpecifiedEvaluator() {
        final var NUMBER = Evaluator.create("[0-9]+", Integer::parseInt);

        final var scanner = new Scanner("42 is magic");

        assertThat(scanner.follows(NUMBER))
            .isTrue();

        final var value = scanner.consume(NUMBER);
        assertThat(value)
            .isEqualTo(42);

        assertThat(scanner.follows(" is magic"))
            .isTrue();
    }

    @Test
    void shouldScanUsingRegisteredEvaluator() {

        final var scanner = new Scanner("42 is magic");
        scanner.register(Integer.class, Evaluator.create("[0-9]+", Integer::parseInt));

        assertThat(scanner.follows(Integer.class))
            .isTrue();

        final var value = scanner.consume(Integer.class);
        assertThat(value)
            .isEqualTo(42);

        assertThat(scanner.follows(" is magic"))
            .isTrue();
    }

    /**
     * Ensure {@link Scanner#skipUntil(Pattern)} skips characters until the {@link Pattern} matches.
     */
    @Test
    void shouldSkipUntilPattern() {
        final var scanner = new Scanner("hello=world");

        scanner.skipUntil(Pattern.compile("="));

        assertThat(scanner.follows("="))
            .isTrue();

        assertThat(scanner.consume("=world"))
            .isEqualTo("=world");
    }

    /**
     * Ensure {@link Scanner#skipUntil(String)} skips characters until the {@link String} matches.
     */
    @Test
    void shouldSkipUntilString() {
        final var scanner = new Scanner("prefix-->suffix");

        scanner.skipUntil("-->");

        assertThat(scanner.follows("-->"))
            .isTrue();

        assertThat(scanner.consume("-->suffix"))
            .isEqualTo("-->suffix");
    }

    /**
     * Ensure {@link Scanner#skipUntil(Evaluator)} skips characters until the {@link Evaluator} matches.
     */
    @Test
    void shouldSkipUntilEvaluator() {
        final var NUMBER = Evaluator.create("[0-9]+", Integer::parseInt);

        final var scanner = new Scanner("abc123");

        scanner.skipUntil(NUMBER);

        assertThat(scanner.follows(NUMBER))
            .isTrue();

        assertThat(scanner.consume(NUMBER))
            .isEqualTo(123);
    }

    /**
     * Ensure {@link Scanner#skipUntil(Class)} skips characters until the registered {@link Evaluator} matches.
     */
    @Test
    void shouldSkipUntilClass() {
        final var scanner = new Scanner("abc123");
        scanner.register(Integer.class, Evaluator.create("[0-9]+", Integer::parseInt));

        scanner.skipUntil(Integer.class);

        assertThat(scanner.follows(Integer.class))
            .isTrue();

        assertThat(scanner.consume(Integer.class))
            .isEqualTo(123);
    }

    /**
     * Ensure {@link Scanner#skipWhile(Pattern)} skips while the {@link Pattern} matches.
     */
    @Test
    void shouldSkipWhilePattern() {
        final var scanner = new Scanner("aaabbb");

        scanner.skipWhile(Pattern.compile("a+"));

        assertThat(scanner.follows("b"))
            .isTrue();

        assertThat(scanner.consume("bbb"))
            .isEqualTo("bbb");
    }

    /**
     * Ensure {@link Scanner#skipWhile(String)} skips while the {@link String} matches.
     */
    @Test
    void shouldSkipWhileString() {
        final var scanner = new Scanner("------end");

        scanner.skipWhile("--");

        assertThat(scanner.follows("end"))
            .isTrue();

        assertThat(scanner.consume("end"))
            .isEqualTo("end");
    }

    /**
     * Ensure {@link Scanner#skipWhile(Evaluator)} skips while the {@link Evaluator} matches.
     */
    @Test
    void shouldSkipWhileEvaluator() {
        final var NUMBER = Evaluator.create("[0-9]+", Integer::parseInt);

        final var scanner = new Scanner("123456rest");

        scanner.skipWhile(NUMBER);

        assertThat(scanner.follows("rest"))
            .isTrue();

        assertThat(scanner.consume("rest"))
            .isEqualTo("rest");
    }

    /**
     * Ensure {@link Scanner#skipWhile(Class)} skips while the registered {@link Evaluator} matches.
     */
    @Test
    void shouldSkipWhileClass() {
        final var scanner = new Scanner("123456rest");
        scanner.register(Integer.class, Evaluator.create("[0-9]+", Integer::parseInt));

        scanner.skipWhile(Integer.class);

        assertThat(scanner.follows("rest"))
            .isTrue();

        assertThat(scanner.consume("rest"))
            .isEqualTo("rest");
    }

    /**
     * Ensure {@link Scanner#consumeUntil(Pattern)} consumes characters until the {@link Pattern} matches.
     */
    @Test
    void shouldConsumeUntilPattern() {
        final var scanner = new Scanner("hello=world");

        assertThat(scanner.consumeUntil(Pattern.compile("=")))
            .isEqualTo("hello");

        assertThat(scanner.follows("="))
            .isTrue();
    }

    /**
     * Ensure {@link Scanner#consumeUntil(String)} consumes characters until the {@link String} matches.
     */
    @Test
    void shouldConsumeUntilString() {
        final var scanner = new Scanner("prefix-->suffix");

        assertThat(scanner.consumeUntil("-->"))
            .isEqualTo("prefix");

        assertThat(scanner.follows("-->"))
            .isTrue();
    }

    /**
     * Ensure {@link Scanner#consumeUntil(Evaluator)} consumes characters until the {@link Evaluator} matches.
     */
    @Test
    void shouldConsumeUntilEvaluator() {
        final var NUMBER = Evaluator.create("[0-9]+", Integer::parseInt);

        final var scanner = new Scanner("abc123");

        assertThat(scanner.consumeUntil(NUMBER))
            .isEqualTo("abc");

        assertThat(scanner.follows(NUMBER))
            .isTrue();
    }

    /**
     * Ensure {@link Scanner#consumeUntil(Class)} consumes characters until the registered {@link Evaluator} matches.
     */
    @Test
    void shouldConsumeUntilClass() {
        final var scanner = new Scanner("abc123");
        scanner.register(Integer.class, Evaluator.create("[0-9]+", Integer::parseInt));

        assertThat(scanner.consumeUntil(Integer.class))
            .isEqualTo("abc");

        assertThat(scanner.follows(Integer.class))
            .isTrue();
    }

    /**
     * Ensure {@link Scanner#consumeWhile(Pattern)} consumes characters while the {@link Pattern} matches.
     */
    @Test
    void shouldConsumeWhilePattern() {
        final var scanner = new Scanner("aaabbb");

        assertThat(scanner.consumeWhile(Pattern.compile("a+")))
            .isEqualTo("aaa");

        assertThat(scanner.follows("b"))
            .isTrue();
    }

    /**
     * Ensure {@link Scanner#consumeWhile(Evaluator)} consumes values while the {@link Evaluator} matches.
     */
    @Test
    void shouldConsumeWhileEvaluator() {
        final var NUMBER = Evaluator.create("[0-9]+", Integer::parseInt);

        final var scanner = new Scanner("1 2 3rest")
            .register(Filter.WHITESPACE);

        assertThat(scanner.consumeWhile(NUMBER))
            .containsExactly(1, 2, 3);

        assertThat(scanner.follows("rest"))
            .isTrue();
    }

    /**
     * Ensure {@link Scanner#consumeWhile(Class)} consumes values while the registered {@link Evaluator} matches.
     */
    @Test
    void shouldConsumeWhileClass() {
        final var scanner = new Scanner("1 2 3rest")
            .register(Filter.WHITESPACE);

        scanner.register(Integer.class, Evaluator.create("[0-9]+", Integer::parseInt));

        assertThat(scanner.consumeWhile(Integer.class))
            .containsExactly(1, 2, 3);

        assertThat(scanner.follows("rest"))
            .isTrue();
    }
}
