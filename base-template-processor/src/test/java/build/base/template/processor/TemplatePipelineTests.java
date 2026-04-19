package build.base.template.processor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TemplatePipelineTests {

    private static List<String> load(final String name) throws IOException {
        final var resource = TemplatePipelineTests.class.getClassLoader()
            .getResourceAsStream("templates/" + name);
        assertThat(resource).as("test resource: " + name).isNotNull();
        return new String(resource.readAllBytes()).lines().toList();
    }

    @Test
    void shouldProcessHelloTemplate() throws IOException {
        final var lines = load("hello.jt");
        final var parsed = JtParser.parse(lines, "hello.jt");

        assertThat(parsed.className()).isEqualTo("HelloTemplate");
        assertThat(parsed.outType()).isEqualTo("HtmlOut");
        assertThat(parsed.params()).isEqualTo("String name");

        final var source = CodeGenerator.generate(parsed);

        assertThat(source).contains("public record HelloTemplate(String name) implements Template<HtmlOut>");
        assertThat(source).contains("out.write(name)");
        assertThat(source).contains("<h1>Hello, ");
        assertThat(source).contains("!</h1>\\n");
    }

    @Test
    void shouldProcessTasksTemplate() throws IOException {
        final var lines = load("tasks.jt");
        final var parsed = JtParser.parse(lines, "tasks.jt");

        assertThat(parsed.className()).isEqualTo("TasksTemplate");
        assertThat(parsed.imports()).contains("import java.util.List");

        final var source = CodeGenerator.generate(parsed);

        assertThat(source).contains("import java.util.List;");
        assertThat(source).contains("for (var item : items) {");
        assertThat(source).contains("out.write(item)");
    }

    @Test
    void shouldProcessItemTemplateWithConditionalExpression() throws IOException {
        final var lines = load("item.jt");
        final var parsed = JtParser.parse(lines, "item.jt");

        assertThat(parsed.className()).isEqualTo("ItemTemplate");

        final var source = CodeGenerator.generate(parsed);

        assertThat(source).contains("out.write(active ? \" active\" : \"\")");
        assertThat(source).contains("out.write(label)");
    }
}
