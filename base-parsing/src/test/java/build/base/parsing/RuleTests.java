package build.base.parsing;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Rule} combinators.
 */
class RuleTests {

    private static final Rule<String> WORD = scanner -> {
        if (!scanner.follows(c -> Character.isLetter(c))) {
            return Optional.empty();
        }
        return Optional.of(scanner.consumeWhile(c -> Character.isLetter(c)));
    };

    private static final Rule<String> DIGITS = scanner -> {
        if (!scanner.follows(c -> Character.isDigit(c))) {
            return Optional.empty();
        }
        return Optional.of(scanner.consumeWhile(c -> Character.isDigit(c)));
    };

    private static final Rule<String> COMMA = scanner ->
        scanner.optionallyConsume(",").isPresent() ? Optional.of(",") : Optional.empty();

    // ---- tryParse basics ----------------------------------------------------

    @Test
    void returnsValueWhenInputMatches() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.tryParse(scanner)).contains("hello");
    }

    @Test
    void returnsEmptyWhenInputDoesNotMatch() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.tryParse(scanner)).isEmpty();
    }

    @Test
    void doesNotConsumeInputOnEmptyResult() throws ParseException {
        final var scanner = new Scanner("123");
        WORD.tryParse(scanner);
        assertThat(scanner.follows("123")).isTrue();
    }

    // ---- map ----------------------------------------------------------------

    @Test
    void mapTransformsResult() throws ParseException {
        final var scanner = new Scanner("hello");
        final Rule<Integer> length = WORD.map(String::length);
        assertThat(length.tryParse(scanner)).contains(5);
    }

    @Test
    void mapPropagatesEmpty() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.map(String::length).tryParse(scanner)).isEmpty();
    }

    // ---- filter -------------------------------------------------------------

    @Test
    void filterPassesMatchingResult() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.filter(w -> w.length() > 3).tryParse(scanner)).contains("hello");
    }

    @Test
    void filterRejectsNonMatchingResult() throws ParseException {
        final var scanner = new Scanner("hi");
        assertThat(WORD.filter(w -> w.length() > 3).tryParse(scanner)).isEmpty();
    }

    // ---- or -----------------------------------------------------------------

    @Test
    void orReturnsFirstAlternativeWhenItMatches() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.or(DIGITS).tryParse(scanner)).contains("hello");
    }

    @Test
    void orFallsBackToSecondAlternative() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.or(DIGITS).tryParse(scanner)).contains("123");
    }

    @Test
    void orRestoresPositionBeforeTryingSecond() throws ParseException {
        final Rule<String> failAfterConsuming = scanner -> {
            if (!scanner.follows("ab")) return Optional.empty();
            scanner.consume("ab");
            return Optional.empty(); // consumed but returns empty
        };
        final var scanner = new Scanner("abcd");
        final var result = failAfterConsuming.or(WORD).tryParse(scanner);
        assertThat(result).contains("abcd");
    }

    @Test
    void orThrowsOnNonBacktrackingScanner() {
        final var scanner = new Scanner(new java.io.StringReader("hello"));
        assertThatThrownBy(() -> WORD.or(DIGITS).tryParse(scanner))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void orReturnsEmptyWhenBothAlternativesFail() throws ParseException {
        final var scanner = new Scanner("!!");
        assertThat(WORD.or(DIGITS).tryParse(scanner)).isEmpty();
    }

    // ---- then ---------------------------------------------------------------

    @Test
    void thenReturnsSecondResult() throws ParseException {
        final var scanner = new Scanner("hello123");
        assertThat(WORD.then(DIGITS).tryParse(scanner)).contains("123");
    }

    @Test
    void thenConsumesFirstParserInput() throws ParseException {
        final var scanner = new Scanner("hello123");
        WORD.then(DIGITS).tryParse(scanner);
        assertThat(scanner.hasNext()).isFalse();
    }

    // ---- repeated -----------------------------------------------------------

    @Test
    void repeatedCollectsAllMatches() throws ParseException {
        final var scanner = new Scanner("abc");
        final Rule<String> singleChar = s -> {
            if (!s.follows(c -> Character.isLetter(c))) return Optional.empty();
            return Optional.of(String.valueOf(s.consumeChar()));
        };
        assertThat(singleChar.repeated().tryParse(scanner)).contains(List.of("a", "b", "c"));
    }

    @Test
    void repeatedReturnsEmptyListWhenNothingMatches() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.repeated().tryParse(scanner)).contains(List.of());
    }

    @Test
    void repeatedAlwaysSucceeds() throws ParseException {
        final var scanner = new Scanner("");
        assertThat(WORD.repeated().tryParse(scanner)).isPresent();
    }

    // ---- separatedBy --------------------------------------------------------

    @Test
    void separatedByCollectsItems() throws ParseException {
        final var scanner = new Scanner("a,b,c");
        final Rule<String> letter = s -> {
            if (!s.follows(c -> Character.isLetter(c))) return Optional.empty();
            return Optional.of(String.valueOf(s.consumeChar()));
        };
        assertThat(letter.separatedBy(COMMA).tryParse(scanner)).contains(List.of("a", "b", "c"));
    }

    @Test
    void separatedByReturnsSingleItemWithNoSeparator() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.separatedBy(COMMA).tryParse(scanner)).contains(List.of("hello"));
    }

    @Test
    void separatedByReturnsEmptyListWhenFirstItemDoesNotMatch() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.separatedBy(COMMA).tryParse(scanner)).contains(List.of());
    }

    // ---- zip ----------------------------------------------------------------

    @Test
    void zipCombinesBothResults() throws ParseException {
        final var scanner = new Scanner("hello123");
        assertThat(WORD.zip(DIGITS, (w, d) -> w + ":" + d).tryParse(scanner)).contains("hello:123");
    }

    @Test
    void zipReturnsEmptyWhenFirstParserFails() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.zip(DIGITS, (w, d) -> w + d).tryParse(scanner)).isEmpty();
        assertThat(scanner.follows("123")).isTrue();
    }

    @Test
    void zipReturnsEmptyWhenSecondParserFails() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.zip(DIGITS, (w, d) -> w + d).tryParse(scanner)).isEmpty();
    }

    @Test
    void zipConsumesInputFromBothParsers() throws ParseException {
        final var scanner = new Scanner("hello123");
        WORD.zip(DIGITS, (w, d) -> w + d).tryParse(scanner);
        assertThat(scanner.hasNext()).isFalse();
    }

    // ---- optional -----------------------------------------------------------

    @Test
    void optionalReturnsPresentValueWhenParserMatches() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.optional().tryParse(scanner)).contains(Optional.of("hello"));
    }

    @Test
    void optionalReturnsEmptyOptionalWhenParserDoesNotMatch() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.optional().tryParse(scanner)).contains(Optional.empty());
    }

    @Test
    void optionalAlwaysSucceeds() throws ParseException {
        final var scanner = new Scanner("");
        assertThat(WORD.optional().tryParse(scanner)).isPresent();
    }

    @Test
    void optionalDoesNotConsumeInputOnNoMatch() throws ParseException {
        final var scanner = new Scanner("123");
        WORD.optional().tryParse(scanner);
        assertThat(scanner.follows("123")).isTrue();
    }

    // ---- repeated1 ----------------------------------------------------------

    @Test
    void repeated1CollectsAllMatches() throws ParseException {
        final var scanner = new Scanner("abc");
        final Rule<String> singleChar = s -> {
            if (!s.follows(c -> Character.isLetter(c))) return Optional.empty();
            return Optional.of(String.valueOf(s.consumeChar()));
        };
        assertThat(singleChar.repeated1().tryParse(scanner)).contains(List.of("a", "b", "c"));
    }

    @Test
    void repeated1ReturnsSingleElementList() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.repeated1().tryParse(scanner)).contains(List.of("hello"));
    }

    @Test
    void repeated1ReturnsEmptyWhenNothingMatches() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.repeated1().tryParse(scanner)).isEmpty();
    }

    @Test
    void repeated1DoesNotConsumeInputOnNoMatch() throws ParseException {
        final var scanner = new Scanner("123");
        WORD.repeated1().tryParse(scanner);
        assertThat(scanner.follows("123")).isTrue();
    }

    // ---- between ------------------------------------------------------------

    private static final Rule<String> OPEN_PAREN = scanner ->
        scanner.optionallyConsume("(").isPresent() ? Optional.of("(") : Optional.empty();

    private static final Rule<String> CLOSE_PAREN = scanner ->
        scanner.optionallyConsume(")").isPresent() ? Optional.of(")") : Optional.empty();

    @Test
    void betweenExtractsContentBetweenDelimiters() throws ParseException {
        final var scanner = new Scanner("(hello)");
        assertThat(WORD.between(OPEN_PAREN, CLOSE_PAREN).tryParse(scanner)).contains("hello");
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void betweenReturnsEmptyWhenOpenFails() throws ParseException {
        final var scanner = new Scanner("hello)");
        assertThat(WORD.between(OPEN_PAREN, CLOSE_PAREN).tryParse(scanner)).isEmpty();
        assertThat(scanner.follows("hello)")).isTrue();
    }

    @Test
    void betweenConsumesMissingCloseGracefully() throws ParseException {
        final var scanner = new Scanner("(hello");
        assertThat(WORD.between(OPEN_PAREN, CLOSE_PAREN).tryParse(scanner)).contains("hello");
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void betweenWorksWithNestedContent() throws ParseException {
        final var scanner = new Scanner("(hello world)");
        final Rule<String> untilClose = s -> Optional.of(s.consumeUntil(")"));
        assertThat(untilClose.between(OPEN_PAREN, CLOSE_PAREN).tryParse(scanner)).contains("hello world");
        assertThat(scanner.hasNext()).isFalse();
    }

    // ---- times --------------------------------------------------------------

    @Test
    void timesMatchesExactCount() throws ParseException {
        final var scanner = new Scanner("aaa");
        final Rule<String> a = s -> s.follows('a') ? Optional.of(String.valueOf(s.consumeChar())) : Optional.empty();
        assertThat(a.times(3).tryParse(scanner)).contains(List.of("a", "a", "a"));
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void timesReturnsEmptyWhenFirstMatchFails() throws ParseException {
        final var scanner = new Scanner("bbb");
        final Rule<String> a = s -> s.follows('a') ? Optional.of(String.valueOf(s.consumeChar())) : Optional.empty();
        assertThat(a.times(3).tryParse(scanner)).isEmpty();
        assertThat(scanner.follows("bbb")).isTrue();
    }

    @Test
    void timesZeroAlwaysSucceedsWithEmptyList() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.times(0).tryParse(scanner)).contains(List.of());
    }

    @Test
    void timesNegativeThrows() {
        assertThatThrownBy(() -> WORD.times(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- atLeast ------------------------------------------------------------

    @Test
    void atLeastSucceedsWhenMinMet() throws ParseException {
        final var scanner = new Scanner("aaa");
        final Rule<String> a = s -> s.follows('a') ? Optional.of(String.valueOf(s.consumeChar())) : Optional.empty();
        assertThat(a.atLeast(2).tryParse(scanner)).contains(List.of("a", "a", "a"));
    }

    @Test
    void atLeastReturnsEmptyWhenBelowMin() throws ParseException {
        final var scanner = new Scanner("a");
        final Rule<String> a = s -> s.follows('a') ? Optional.of(String.valueOf(s.consumeChar())) : Optional.empty();
        assertThat(a.atLeast(2).tryParse(scanner)).isEmpty();
    }

    @Test
    void atLeastZeroIsSameAsRepeated() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.atLeast(0).tryParse(scanner)).contains(List.of());
    }

    @Test
    void atLeastNegativeThrows() {
        assertThatThrownBy(() -> WORD.atLeast(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- atMost -------------------------------------------------------------

    @Test
    void atMostCollectsUpToMax() throws ParseException {
        final var scanner = new Scanner("aaaaa");
        final Rule<String> a = s -> s.follows('a') ? Optional.of(String.valueOf(s.consumeChar())) : Optional.empty();
        assertThat(a.atMost(3).tryParse(scanner)).contains(List.of("a", "a", "a"));
        assertThat(scanner.follows("aa")).isTrue();
    }

    @Test
    void atMostSucceedsWithFewerThanMax() throws ParseException {
        final var scanner = new Scanner("a");
        final Rule<String> a = s -> s.follows('a') ? Optional.of(String.valueOf(s.consumeChar())) : Optional.empty();
        assertThat(a.atMost(3).tryParse(scanner)).contains(List.of("a"));
    }

    @Test
    void atMostAlwaysSucceedsOnNoMatch() throws ParseException {
        final var scanner = new Scanner("bbb");
        final Rule<String> a = s -> s.follows('a') ? Optional.of(String.valueOf(s.consumeChar())) : Optional.empty();
        assertThat(a.atMost(3).tryParse(scanner)).contains(List.of());
    }

    @Test
    void atMostNegativeThrows() {
        assertThatThrownBy(() -> WORD.atMost(-1))
            .isInstanceOf(IllegalArgumentException.class);
    }

    // ---- not ----------------------------------------------------------------

    @Test
    void notSucceedsWhenParserFails() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.not().tryParse(scanner)).contains(true);
        assertThat(scanner.follows("123")).isTrue();
    }

    @Test
    void notFailsWhenParserSucceeds() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.not().tryParse(scanner)).isEmpty();
        assertThat(scanner.follows("hello")).isTrue();
    }

    @Test
    void notNeverAdvancesScanner() throws ParseException {
        final var scanner = new Scanner("hello");
        WORD.not().tryParse(scanner);
        assertThat(scanner.follows("hello")).isTrue();
    }

    @Test
    void notThrowsOnNonBacktrackingScanner() {
        final var scanner = new Scanner(new java.io.StringReader("hello"));
        assertThatThrownBy(() -> WORD.not().tryParse(scanner))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    // ---- endOfInput ---------------------------------------------------------

    @Test
    void endOfInputSucceedsAtEnd() throws ParseException {
        final var scanner = new Scanner("");
        assertThat(Rule.endOfInput().tryParse(scanner)).contains(true);
    }

    @Test
    void endOfInputFailsWhenInputRemains() throws ParseException {
        final var scanner = new Scanner("x");
        assertThat(Rule.endOfInput().tryParse(scanner)).isEmpty();
    }

    @Test
    void endOfInputSucceedsAfterConsumingAll() throws ParseException {
        final var scanner = new Scanner("hi");
        WORD.tryParse(scanner);
        assertThat(Rule.endOfInput().tryParse(scanner)).contains(true);
    }

    // ---- label --------------------------------------------------------------

    @Test
    void labelRelabelsParseException() {
        final Rule<String> alwaysThrows = scanner -> {
            throw new ParseException(scanner.getLocation(), "original", "x");
        };
        final var scanner = new Scanner("x");
        assertThatThrownBy(() -> alwaysThrows.label("relabelled").tryParse(scanner))
            .isInstanceOf(ParseException.class)
            .satisfies(e -> assertThat(((ParseException) e).getExpected()).isEqualTo("relabelled"));
    }

    @Test
    void labelPassesThroughEmptyResult() throws ParseException {
        final var scanner = new Scanner("123");
        assertThat(WORD.label("word").tryParse(scanner)).isEmpty();
    }

    @Test
    void labelPassesThroughSuccessResult() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.label("word").tryParse(scanner)).contains("hello");
    }

    // ---- require ------------------------------------------------------------

    @Test
    void requireThrowsWhenParserReturnsEmpty() {
        final var scanner = new Scanner("123");
        assertThatThrownBy(() -> WORD.require("identifier").tryParse(scanner))
            .isInstanceOf(ParseException.class)
            .satisfies(e -> assertThat(((ParseException) e).getExpected()).isEqualTo("identifier"));
    }

    @Test
    void requirePassesThroughSuccessResult() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(WORD.require("identifier").tryParse(scanner)).contains("hello");
    }

    @Test
    void requireRelabelsParseException() {
        final Rule<String> alwaysThrows = scanner -> {
            throw new ParseException(scanner.getLocation(), "original", "x");
        };
        final var scanner = new Scanner("x");
        assertThatThrownBy(() -> alwaysThrows.require("identifier").tryParse(scanner))
            .isInstanceOf(ParseException.class)
            .satisfies(e -> assertThat(((ParseException) e).getExpected()).isEqualTo("identifier"));
    }

    // ---- define (recursive grammars) ----------------------------------------

    // Parses a word or a parenthesised group containing more of the same, e.g. "(hello (world))"
    private static final Rule<String> NESTED_PARENS = Rule.define(self -> {
        final Rule<String> open = scanner ->
            scanner.optionallyConsume("(").isPresent() ? Optional.of("(") : Optional.empty();
        final Rule<String> close = scanner ->
            scanner.optionallyConsume(")").isPresent() ? Optional.of(")") : Optional.empty();
        return WORD.or(self).repeated1()
            .map(parts -> String.join(" ", parts))
            .between(open, close);
    });

    @Test
    void defineHandlesFlatContent() throws ParseException {
        final var scanner = new Scanner("(hello world)");
        scanner.register(Filter.WHITESPACE);
        assertThat(NESTED_PARENS.tryParse(scanner)).isPresent();
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void defineHandlesOneNestedLevel() throws ParseException {
        final var scanner = new Scanner("(hello (world))");
        scanner.register(Filter.WHITESPACE);
        assertThat(NESTED_PARENS.tryParse(scanner)).isPresent();
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void defineHandlesDeeplyNested() throws ParseException {
        final var scanner = new Scanner("(a (b (c (d))))");
        scanner.register(Filter.WHITESPACE);
        assertThat(NESTED_PARENS.tryParse(scanner)).isPresent();
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void defineReturnsEmptyWhenNoOpenParen() throws ParseException {
        final var scanner = new Scanner("hello");
        assertThat(NESTED_PARENS.tryParse(scanner)).isEmpty();
        assertThat(scanner.follows("hello")).isTrue();
    }

    // Parses nested block comments: /* ... /* ... */ ... */
    private static Rule<String> nestedBlockComment() {
        final Rule<String> notDelimiter = scanner -> {
            if (scanner.follows("/*") || scanner.follows("*/") || !scanner.hasNext()) {
                return Optional.empty();
            }
            return Optional.of(String.valueOf(scanner.consumeChar()));
        };
        return Rule.define(self -> {
            final Rule<String> open = s ->
                s.optionallyConsume("/*").isPresent() ? Optional.of("/*") : Optional.empty();
            final Rule<String> close = s ->
                s.optionallyConsume("*/").isPresent() ? Optional.of("*/") : Optional.empty();
            return notDelimiter.or(self).repeated()
                .map(parts -> String.join("", parts))
                .between(open, close);
        });
    }

    @Test
    void defineHandlesFlatBlockComment() throws ParseException {
        final var scanner = new Scanner("/* hello */");
        assertThat(nestedBlockComment().tryParse(scanner)).contains(" hello ");
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void defineHandlesNestedBlockComment() throws ParseException {
        final var scanner = new Scanner("/* outer /* inner */ outer */");
        assertThat(nestedBlockComment().tryParse(scanner)).isPresent();
        assertThat(scanner.hasNext()).isFalse();
    }

    @Test
    void defineHandlesDeeplyNestedBlockComment() throws ParseException {
        final var scanner = new Scanner("/* a /* b /* c */ b */ a */");
        assertThat(nestedBlockComment().tryParse(scanner)).isPresent();
        assertThat(scanner.hasNext()).isFalse();
    }
}
