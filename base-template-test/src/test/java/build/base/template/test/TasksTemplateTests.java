package build.base.template.test;

import build.base.template.HtmlOut;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TasksTemplateTests {

    @Test
    void shouldRenderTitleAndItems() {
        final var out = new HtmlOut();
        new TasksTemplate("My Tasks", List.of("Buy milk", "Walk dog")).render(out);
        final var html = out.toString();

        assertThat(html).contains("<h1>My Tasks</h1>");
        assertThat(html).contains("<li>Buy milk</li>");
        assertThat(html).contains("<li>Walk dog</li>");
    }

    @Test
    void shouldRenderEmptyList() {
        final var out = new HtmlOut();
        new TasksTemplate("Empty", List.of()).render(out);
        final var html = out.toString();

        assertThat(html).contains("<h1>Empty</h1>");
        assertThat(html).contains("<ul>");
        assertThat(html).contains("</ul>");
        assertThat(html).doesNotContain("<li>");
    }

    @Test
    void shouldEscapeItemsInOutput() {
        final var out = new HtmlOut();
        new TasksTemplate("Tasks", List.of("<b>bold</b>")).render(out);
        final var html = out.toString();

        assertThat(html).contains("&lt;b&gt;bold&lt;/b&gt;");
        assertThat(html).doesNotContain("<b>");
    }

    @Test
    void shouldComposeWithItemTemplate() {
        final var out = new HtmlOut();
        new ItemTemplate("Active item", true).render(out);
        final var html = out.toString();

        assertThat(html).contains("class=\"item active\"");
        assertThat(html).contains("Active item");
    }
}
