package build.base.parsing;

import org.junit.jupiter.api.Test;

import java.io.Reader;
import java.io.StringReader;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AbstractParserTests {

    private static final class DemoException extends RuntimeException {
        DemoException(final String message, final Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Parses {@code <ident> = <ident>} and returns the pair.
     */
    private static final class AssignmentParser extends AbstractParser<String> {

        private static final Pattern IDENT = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*");

        AssignmentParser(final String input) {
            super(input);
        }

        AssignmentParser(final Reader input) {
            super(input);
        }

        @Override
        protected String parse() {
            final var lhs = expect(IDENT, "identifier");
            expect("=");
            final var rhs = expect(IDENT, "identifier");
            return lhs + "=" + rhs;
        }

        @Override
        protected RuntimeException translate(final ParseException cause) {
            return new DemoException("parse failed: " + cause.getMessage(), cause);
        }
    }

    /**
     * Exercises followsKeyword/consumeKeyword.
     */
    private static final class KeywordParser extends AbstractParser<String> {

        KeywordParser(final String input) {
            super(input);
        }

        @Override
        protected String parse() {
            if (followsKeyword("yes")) {
                consumeKeyword("yes");
                return "Y";
            }
            consumeKeyword("no");
            return "N";
        }

        @Override
        protected RuntimeException translate(final ParseException cause) {
            return new DemoException("kw fail", cause);
        }
    }

    @Test
    void parsesStringInput() {
        assertThat(new AssignmentParser("a=b").run()).isEqualTo("a=b");
    }

    @Test
    void parsesReaderInput() {
        assertThat(new AssignmentParser((Reader) new StringReader("foo = bar")).run()).isEqualTo("foo=bar");
    }

    @Test
    void translatesParseException() {
        assertThatThrownBy(() -> new AssignmentParser("a + b").run())
            .isInstanceOf(DemoException.class)
            .hasMessageContaining("parse failed")
            .hasCauseInstanceOf(ParseException.class);
    }

    @Test
    void requiresFullInputConsumption() {
        // The grammar parses "a=b" successfully but extra "; trailing" remains.
        assertThatThrownBy(() -> new AssignmentParser("a=b; trailing").run())
            .isInstanceOf(DemoException.class)
            .hasCauseInstanceOf(ParseException.class);
    }

    @Test
    void followsKeywordRespectsWordBoundary() {
        assertThat(new KeywordParser("yes").run()).isEqualTo("Y");
        assertThat(new KeywordParser("no").run()).isEqualTo("N");
        // "yesterday" starts with "yes" but is not the keyword
        assertThatThrownBy(() -> new KeywordParser("yesterday").run())
            .isInstanceOf(DemoException.class);
    }

    @Test
    void consumeKeywordFailsOnNonKeyword() {
        assertThatThrownBy(() -> new KeywordParser("yesno").run())
            .isInstanceOf(DemoException.class);
    }

    @Test
    void scannerFieldClearedAfterRun() throws Exception {
        final var p = new AssignmentParser("a=b");
        p.run();
        // package-private field; null after run() returns
        final var f = AbstractParser.class.getDeclaredField("scanner");
        f.setAccessible(true);
        assertThat(f.get(p)).isNull();
    }

    @Test
    void consumeBalancedReturnsBody() throws Exception {
        try (var s = new Scanner("{a + b}")) {
            assertThat(s.consumeBalanced('{', '}')).isEqualTo("a + b");
            assertThat(s.hasNext()).isFalse();
        }
    }

    @Test
    void consumeBalancedHandlesNesting() throws Exception {
        try (var s = new Scanner("{outer {inner} more}rest")) {
            assertThat(s.consumeBalanced('{', '}')).isEqualTo("outer {inner} more");
            assertThat(s.consumeChar()).isEqualTo('r');
        }
    }

    @Test
    void consumeBalancedSkipsQuotedDelimiters() throws Exception {
        try (var s = new Scanner("{\"}}}\" + 'x}'}")) {
            assertThat(s.consumeBalanced('{', '}')).isEqualTo("\"}}}\" + 'x}'");
        }
    }

    @Test
    void consumeBalancedHandlesEscapedQuotes() throws Exception {
        try (var s = new Scanner("{\"a\\\"b\"}")) {
            assertThat(s.consumeBalanced('{', '}')).isEqualTo("\"a\\\"b\"");
        }
    }

    @Test
    void consumeBalancedThrowsOnEofBeforeClose() throws Exception {
        try (var s = new Scanner("{unclosed")) {
            assertThatThrownBy(() -> s.consumeBalanced('{', '}'))
                .isInstanceOf(ParseException.class);
        }
    }

    @Test
    void consumeBalancedThrowsWhenNotPositionedOnOpen() throws Exception {
        try (var s = new Scanner("not-a-brace")) {
            assertThatThrownBy(() -> s.consumeBalanced('{', '}'))
                .isInstanceOf(ParseException.class);
        }
    }
}
