package build.base.parsing;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TemplateParser}.
 *
 * @author tim.berston
 * @since Mar-2025
 */
class TemplateParserTests {

    // Two node types: plain text and interpolated variable references.
    sealed interface Node permits Text, Var {}
    record Text(String value) implements Node {}
    record Var(String name)   implements Node {}

    // Adjacent Text nodes are merged; Var nodes are never merged.
    private static final java.util.function.BiFunction<Node, Node, Optional<Node>> MERGE =
        (a, b) -> (a instanceof Text ta && b instanceof Text tb)
            ? Optional.of(new Text(ta.value() + tb.value()))
            : Optional.empty();

    private static TemplateParser<Node> parser;

    @BeforeAll
    static void init() {
        parser = new TemplateParser<>(MERGE);
        // `[[` is an escape sequence producing a literal `[`
        parser.defineAtom("\\[\\[", _ -> new Text("["));
        // `[name]` is a variable reference
        parser.defineAtom("\\[[^]]+\\]", token -> new Var(token.trimFirstAndLast()));
        // Everything else (non-bracket runs) is plain text
        parser.defineAtom("[^\\[]+", token -> new Text(token.value()));
    }

    @ParameterizedTest
    @MethodSource("cases")
    void shouldParseTemplateCorrectly(final String expression, final List<Node> expected) {
        assertThat(parser.parse(expression)).isEqualTo(expected);
    }

    static Stream<Arguments> cases() {
        return Stream.of(
            // Plain text — no interpolation
            Arguments.of("some text",
                List.of(new Text("some text"))),

            // Escape sequence `[[` → literal `[`, followed by `]`
            Arguments.of("[[]",
                List.of(new Text("[]"))),

            // Text then escape: `:[[ ` → `:[`
            Arguments.of(":[[",
                List.of(new Text(":["))),

            // Multiple escapes and characters
            Arguments.of("[[\\[[\\]]+",
                List.of(new Text("[\\[\\]]+"))),

            // Text, variable, text
            Arguments.of("[[ [variable] ]",
                List.of(new Text("[ "), new Var("variable"), new Text(" ]"))),

            // Just a double-quote character
            Arguments.of("\"",
                List.of(new Text("\""))),

            // Text before escape, variable, text after
            Arguments.of("\"[[[variable]]\"",
                List.of(new Text("\"["), new Var("variable"), new Text("]\""))),

            Arguments.of("'[[[variable]]'",
                List.of(new Text("'["), new Var("variable"), new Text("]'"))),

            // Two variables separated by text
            Arguments.of("[template]:[expression]",
                List.of(new Var("template"), new Text(":"), new Var("expression"))),

            // Escape at start, then variable
            Arguments.of("[[template]:[expression]",
                List.of(new Text("[template]:"), new Var("expression"))),

            // Escape, variable, text, variable
            Arguments.of("[[[template]:[expression]",
                List.of(new Text("["), new Var("template"), new Text(":"), new Var("expression"))),

            // Variable, space+parens text, variable, closing text
            Arguments.of("[instance] ([class])",
                List.of(new Var("instance"), new Text(" ("), new Var("class"), new Text(")"))),

            // Newline between two variables
            Arguments.of("[prev]\n[curr]",
                List.of(new Var("prev"), new Text("\n"), new Var("curr")))
        );
    }
}
