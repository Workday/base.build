package build.base.table;

import build.base.table.option.CellAlignment;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link CellAlignment}.
 *
 * @author brian.oliver
 * @since Dec-2018
 */
class CellAlignmentTests {

    /**
     * Ensure a {@code null} {@link String} can be aligned left, centered and right.
     */
    @Test
    void shouldAlignNull() {
        assertThat(CellAlignment.LEFT.align(null, 10))
            .isEqualTo("          ");

        assertThat(CellAlignment.CENTERED.align(null, 10))
            .isEqualTo("          ");

        assertThat(CellAlignment.RIGHT.align(null, 10))
            .isEqualTo("          ");
    }

    /**
     * Ensure a {@link String} larger than the specified field width will be truncated as expected and then aligned
     * left, centered and right as required.
     */
    @Test
    void shouldAlignContentWiderThanWidth() {
        assertThat(CellAlignment.LEFT.align("hello world", 5))
            .isEqualTo("hello");

        assertThat(CellAlignment.CENTERED.align("hello world", 5))
            .isEqualTo("hello");

        assertThat(CellAlignment.RIGHT.align("hello world", 5))
            .isEqualTo("hello");
    }

    /**
     * Ensure a {@link String} containing a single character can be aligned left, centered and right.
     */
    @Test
    void shouldAlignSingleCharacter() {
        assertThat(CellAlignment.LEFT.align("x", 5))
            .isEqualTo("x    ");

        assertThat(CellAlignment.CENTERED.align("x", 5))
            .isEqualTo("  x  ");

        assertThat(CellAlignment.RIGHT.align("x", 5))
            .isEqualTo("    x");
    }

    /**
     * Ensure a {@link String} containing multiple words can be aligned left, centered and right.
     */
    @Test
    void shouldAlignWordsCharacter() {
        assertThat(CellAlignment.LEFT.align("hello", 10))
            .isEqualTo("hello     ");

        assertThat(CellAlignment.CENTERED.align("hello", 10))
            .isEqualTo("   hello  ");

        assertThat(CellAlignment.RIGHT.align("hello", 10))
            .isEqualTo("     hello");
    }
}
