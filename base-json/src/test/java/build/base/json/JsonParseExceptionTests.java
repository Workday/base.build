package build.base.json;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JsonParseExceptionTests {

    @Test
    void messageIncludesLineColumnOffset() {
        final var ex = new JsonParseException("bad input", 3, 7, 42, null);
        assertThat(ex.getMessage())
            .contains("bad input")
            .contains("line 3")
            .contains("column 7")
            .contains("offset 42");
    }

    @Test
    void messageIncludesPathWhenPresent() {
        final var ex = new JsonParseException("oops", 1, 1, 0, "/users/0/name");
        assertThat(ex.getMessage()).contains("/users/0/name");
    }

    @Test
    void messageOmitsPathWhenNull() {
        final var ex = new JsonParseException("oops", 1, 1, 0, null);
        assertThat(ex.getMessage()).doesNotContain("at");
    }

    @Test
    void accessors() {
        final var ex = new JsonParseException("msg", 2, 5, 10, "/x");
        assertThat(ex.line()).isEqualTo(2);
        assertThat(ex.column()).isEqualTo(5);
        assertThat(ex.offset()).isEqualTo(10);
        assertThat(ex.path()).isEqualTo("/x");
    }
}
