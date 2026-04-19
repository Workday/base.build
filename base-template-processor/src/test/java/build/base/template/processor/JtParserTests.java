package build.base.template.processor;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JtParserTests {

    @Test
    void shouldParseMinimalTemplate() {
        final var lines = List.of(
            "package com.example;",
            "",
            "template HtmlOut HelloTemplate(String name) {",
            "<h1>Hello</h1>",
            "}"
        );
        final var result = JtParser.parse(lines, "hello.jt");

        assertThat(result.packageName()).isEqualTo("com.example");
        assertThat(result.outType()).isEqualTo("HtmlOut");
        assertThat(result.className()).isEqualTo("HelloTemplate");
        assertThat(result.params()).isEqualTo("String name");
    }

    @Test
    void shouldParseImports() {
        final var lines = List.of(
            "package com.example;",
            "",
            "import java.util.List;",
            "import com.example.Task;",
            "",
            "template HtmlOut TasksTemplate(List<Task> tasks) {",
            "}");
        final var result = JtParser.parse(lines, "tasks.jt");

        assertThat(result.imports()).containsExactly("import java.util.List", "import com.example.Task");
    }

    @Test
    void shouldParseTextLine() {
        final var body = new java.util.ArrayList<BodyNode>();
        JtParser.parseTextLine("<h1>Hello</h1>\n", body);

        assertThat(body).containsExactly(new BodyNode.RawText("<h1>Hello</h1>\n"));
    }

    @Test
    void shouldParseExpressionInTextLine() {
        final var body = new java.util.ArrayList<BodyNode>();
        JtParser.parseTextLine("<h1>#{name}</h1>\n", body);

        assertThat(body).containsExactly(
            new BodyNode.RawText("<h1>"),
            new BodyNode.Expression("name"),
            new BodyNode.RawText("</h1>\n")
        );
    }

    @Test
    void shouldParseMultipleExpressionsOnOneLine() {
        final var body = new java.util.ArrayList<BodyNode>();
        JtParser.parseTextLine("<li id=\"#{task.id()}\">#{task.title()}</li>\n", body);

        assertThat(body).containsExactly(
            new BodyNode.RawText("<li id=\""),
            new BodyNode.Expression("task.id()"),
            new BodyNode.RawText("\">"),
            new BodyNode.Expression("task.title()"),
            new BodyNode.RawText("</li>\n")
        );
    }

    @Test
    void shouldParseExpressionWithStringLiteralContainingBrace() {
        final var body = new java.util.ArrayList<BodyNode>();
        JtParser.parseTextLine("#{task.done() ? \" done\" : \"\"}\n", body);

        assertThat(body).containsExactly(
            new BodyNode.Expression("task.done() ? \" done\" : \"\""),
            new BodyNode.RawText("\n")
        );
    }

    @Test
    void shouldParseCodeLine() {
        final var lines = List.of(
            "package com.example;",
            "template HtmlOut T(java.util.List<String> items) {",
            "@for (var item : items) {",
            "<li>#{item}</li>",
            "@}",
            "}"
        );
        final var result = JtParser.parse(lines, "t.jt");
        assertThat(result.body()).contains(new BodyNode.CodeLine("for (var item : items) {"));
        assertThat(result.body()).contains(new BodyNode.CodeLine("}"));
    }

    @Test
    void shouldParseInclude() {
        final var lines = List.of(
            "package com.example;",
            "template HtmlOut T(Object item) {",
            "@include new ItemTemplate(item)",
            "}"
        );
        final var result = JtParser.parse(lines, "t.jt");
        assertThat(result.body()).contains(new BodyNode.Include("new ItemTemplate(item)"));
    }

    @Test
    void shouldParseWildcardImport() {
        final var lines = List.of(
            "package com.example;",
            "import java.util.*;",
            "template HtmlOut T() {",
            "}"
        );
        final var result = JtParser.parse(lines, "t.jt");
        assertThat(result.imports()).containsExactly("import java.util.*");
    }

    @Test
    void shouldParseStaticImport() {
        final var lines = List.of(
            "package com.example;",
            "import static java.util.List.of;",
            "template HtmlOut T() {",
            "}"
        );
        final var result = JtParser.parse(lines, "t.jt");
        assertThat(result.imports()).containsExactly("import static java.util.List.of");
    }

    @Test
    void shouldParseStaticWildcardImport() {
        final var lines = List.of(
            "package com.example;",
            "import static java.util.Collections.*;",
            "template HtmlOut T() {",
            "}"
        );
        final var result = JtParser.parse(lines, "t.jt");
        assertThat(result.imports()).containsExactly("import static java.util.Collections.*");
    }

    @Test
    void shouldThrowOnMissingDeclaration() {
        final var lines = List.of("package com.example;");
        assertThatThrownBy(() -> JtParser.parse(lines, "bad.jt"))
            .isInstanceOf(JtParseException.class)
            .hasMessageContaining("missing template declaration");
    }
}
