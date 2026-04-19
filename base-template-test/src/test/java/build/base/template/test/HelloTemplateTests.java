package build.base.template.test;

import build.base.template.HtmlOut;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelloTemplateTests {

    @Test
    void shouldRenderName() {
        final var out = new HtmlOut();
        new HelloTemplate("World").render(out);
        final var html = out.toString();

        assertThat(html).contains("<h1>Hello, World!</h1>");
    }

    @Test
    void shouldEscapeNameInOutput() {
        final var out = new HtmlOut();
        new HelloTemplate("<script>alert('xss')</script>").render(out);
        final var html = out.toString();

        assertThat(html).contains("&lt;script&gt;");
        assertThat(html).doesNotContain("<script>");
    }
}
