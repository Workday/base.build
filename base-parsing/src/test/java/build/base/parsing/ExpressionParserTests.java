package build.base.parsing;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link ExpressionParser}.
 *
 * @author tim.berston
 * @since Nov-2024
 */
class ExpressionParserTests {

    // Minimal node hierarchy for testing — records give structural equals() for free.
    sealed interface Node permits Num, Var, Neg, Not, Add, Sub, Mul, Div, Mod, Pow, And, Or {}
    record Num(String value) implements Node {}
    record Var(String name)  implements Node {}
    record Neg(Node operand) implements Node {}
    record Not(Node operand) implements Node {}
    record Add(Node left, Node right) implements Node {}
    record Sub(Node left, Node right) implements Node {}
    record Mul(Node left, Node right) implements Node {}
    record Div(Node left, Node right) implements Node {}
    record Mod(Node left, Node right) implements Node {}
    record Pow(Node left, Node right) implements Node {}
    record And(Node left, Node right) implements Node {}
    record Or(Node left, Node right)  implements Node {}

    private ExpressionParser<Node> parser;

    @BeforeEach
    void init() {
        parser = new ExpressionParser<>();
        parser.defineSection("(", ")");
        parser.defineAtom("[0-9]+(?:[.][0-9]+)?", token -> new Num(token.value()));
        parser.defineAtom("\\[[^]]+\\]", token -> new Var(token.trimFirstAndLast()));
        parser.defineUnary("-", 1, Neg::new);
        parser.defineBinary("^", 2, Pow::new);
        parser.defineBinary("*", 3, Mul::new);
        parser.defineBinary("/", 3, Div::new);
        parser.defineBinary("%", 3, Mod::new);
        parser.defineBinary("-", 4, Sub::new);
        parser.defineBinary("+", 4, Add::new);
        parser.defineUnary("not", 11, Not::new);
        parser.defineBinary("and", 12, And::new);
        parser.defineBinary("or", 13, Or::new);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = { "", "      " })
    void shouldReturnNullForEmptyExpression(final String expr) {
        assertThat(parser.parse(expr)).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "1", " 1", "1 " })
    void shouldParseSingleLiteral(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Num("1"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1 + 2", "1+2", "1    +2 " })
    void shouldParseSimpleAddition(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Add(new Num("1"), new Num("2")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1 * 2", "1*2" })
    void shouldParseSimpleMultiplication(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Mul(new Num("1"), new Num("2")));
    }

    // 1 + 2 * 3  →  Add(1, Mul(2, 3))  because * binds tighter than +
    @ParameterizedTest
    @ValueSource(strings = { "1 + 2 * 3", "1+2*3" })
    void shouldRespectPrecedence_addThenMul(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Add(new Num("1"), new Mul(new Num("2"), new Num("3"))));
    }

    // 1 * 2 + 3  →  Add(Mul(1, 2), 3)
    @ParameterizedTest
    @ValueSource(strings = { "1 * 2 + 3", "1*2+3" })
    void shouldRespectPrecedence_mulThenAdd(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Add(new Mul(new Num("1"), new Num("2")), new Num("3")));
    }

    // 1 + 2 - 3  →  Sub(Add(1, 2), 3)  — left-associative at equal precedence
    @ParameterizedTest
    @ValueSource(strings = { "1 + 2 - 3", "1+2-3" })
    void shouldParseEqualPrecedenceLeftAssociatively(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Sub(new Add(new Num("1"), new Num("2")), new Num("3")));
    }

    // 1 + 2 * 3 - 4  →  Sub(Add(1, Mul(2, 3)), 4)
    @ParameterizedTest
    @ValueSource(strings = { "1 + 2 * 3 - 4", "1+2*3-4" })
    void shouldParseMultiOperatorExpressionCorrectly(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(
            new Sub(new Add(new Num("1"), new Mul(new Num("2"), new Num("3"))), new Num("4")));
    }

    @Test
    void shouldParseUnaryNegation() {
        assertThat(parser.parse("-1")).isEqualTo(new Neg(new Num("1")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "-1 + 2", "-1+2" })
    void shouldParseUnaryAtStartOfExpression(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Add(new Neg(new Num("1")), new Num("2")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "1 + -2", "1+-2" })
    void shouldParseUnaryWithinExpression(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Add(new Num("1"), new Neg(new Num("2"))));
    }

    @ParameterizedTest
    @ValueSource(strings = { "--1", "-   -1" })
    void shouldParseDoubleUnary(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Neg(new Neg(new Num("1"))));
    }

    // (1 + 2) * 3  →  Mul(Add(1, 2), 3)
    @ParameterizedTest
    @ValueSource(strings = { "(1 + 2) * 3", "(1+2)*3" })
    void shouldParseSectionAtStart(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Mul(new Add(new Num("1"), new Num("2")), new Num("3")));
    }

    // 4 * (1 + 2) * 3  →  Mul(Mul(4, Add(1, 2)), 3)
    @ParameterizedTest
    @ValueSource(strings = { "4 * (1 + 2) * 3", "4*(1+2)*3" })
    void shouldParseSectionWithinExpression(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(
            new Mul(new Mul(new Num("4"), new Add(new Num("1"), new Num("2"))), new Num("3")));
    }

    // 4 * (1 + (5 - 2)) * 3
    @ParameterizedTest
    @ValueSource(strings = { "4 * (1 + (5 - 2)) * 3", "4*(1+(5-2))*3" })
    void shouldParseNestedSections(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(
            new Mul(
                new Mul(new Num("4"), new Add(new Num("1"), new Sub(new Num("5"), new Num("2")))),
                new Num("3")));
    }

    // (1 + 2) * (3 - 4)
    @ParameterizedTest
    @ValueSource(strings = { "(1 + 2) * (3 - 4)", "(1+2)*(3-4)" })
    void shouldParseMultipleSections(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(
            new Mul(new Add(new Num("1"), new Num("2")), new Sub(new Num("3"), new Num("4"))));
    }

    // (1) - 2  →  Sub(1, 2) — section-end should allow binary to follow
    @Test
    void shouldParseBinaryAfterSectionEnd() {
        assertThat(parser.parse("(1)-2")).isEqualTo(new Sub(new Num("1"), new Num("2")));
    }

    @Test
    void shouldParseDecimalLiteral() {
        assertThat(parser.parse("1.5 + 2.25")).isEqualTo(new Add(new Num("1.5"), new Num("2.25")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "3 % 2", "3%2" })
    void shouldParseModulo(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Mod(new Num("3"), new Num("2")));
    }

    @ParameterizedTest
    @ValueSource(strings = { "3 ^ 2", "3^2" })
    void shouldParseExponent(final String expr) {
        assertThat(parser.parse(expr)).isEqualTo(new Pow(new Num("3"), new Num("2")));
    }

    // 3 * 1 ^ 2  →  Mul(3, Pow(1, 2))  because ^ binds tighter than *
    @Test
    void shouldParseExponentPrecedence() {
        assertThat(parser.parse("3 * 1 ^ 2")).isEqualTo(new Mul(new Num("3"), new Pow(new Num("1"), new Num("2"))));
    }

    @Test
    void shouldParseVariableAtom() {
        assertThat(parser.parse("[bob]")).isEqualTo(new Var("bob"));
    }

    @Test
    void shouldParseVariableInExpression() {
        assertThat(parser.parse("[bob] * 5")).isEqualTo(new Mul(new Var("bob"), new Num("5")));
    }

    @Test
    void shouldParseLogicalNot() {
        assertThat(parser.parse("not [flag]")).isEqualTo(new Not(new Var("flag")));
    }

    @Test
    void shouldParseLogicalAnd() {
        assertThat(parser.parse("[a] and [b]")).isEqualTo(new And(new Var("a"), new Var("b")));
    }

    @Test
    void shouldParseLogicalOr() {
        assertThat(parser.parse("[a] or [b]")).isEqualTo(new Or(new Var("a"), new Var("b")));
    }

    // [a] and not ([b] or [c])
    @Test
    void shouldParseComplexLogicalExpression() {
        assertThat(parser.parse("[a] and not ([b] or [c])")).isEqualTo(
            new And(new Var("a"), new Not(new Or(new Var("b"), new Var("c")))));
    }

    // ([a] or not [b]) or ([c] and [d])
    @Test
    void shouldParseNestedLogicalExpression() {
        assertThat(parser.parse("[a] or [b] or (not [c]) or ([d] and [e])")).isEqualTo(
            new Or(
                new Or(
                    new Or(new Var("a"), new Var("b")),
                    new Not(new Var("c"))),
                new And(new Var("d"), new Var("e"))));
    }

    // Stress test — long expression must parse without error
    @Test
    void shouldParseLongExpressionWithoutError() {
        final var expr = String.join(" and ",
            java.util.Collections.nCopies(50, "([a] or not [b])"));
        assertThat(parser.parse(expr)).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(strings = { "(1 + 2", "1 +", "-" })
    void shouldThrowOnMalformedExpression(final String expr) {
        assertThatThrownBy(() -> parser.parse(expr))
            .isInstanceOf(ExpressionParserException.class)
            .hasMessage("The expression is malformed");
    }

    @ParameterizedTest
    @MethodSource("unknownTokenCases")
    void shouldThrowOnUnknownToken(final String expr, final int column) {
        assertThatThrownBy(() -> parser.parse(expr))
            .isInstanceOf(ExpressionParserException.class)
            .hasMessageContaining("Location 1:" + column);
    }

    static Stream<Arguments> unknownTokenCases() {
        return Stream.of(
            Arguments.of("1 + 2 # 3", 7),
            Arguments.of("{1 + 1}", 1),
            Arguments.of("1 + 2)", 6),
            Arguments.of("(1 + 2))", 8),
            Arguments.of("1 2", 3)
        );
    }
}
