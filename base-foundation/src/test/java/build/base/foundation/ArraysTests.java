package build.base.foundation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Arrays}.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
class ArraysTests {

    final static String[] EMPTY = new String[0];
    final static String[] NULL = null;
    final static String[] SINGLE = new String[] { "Hello" };
    final static String[] DUAL = new String[] { "Hello", "World" };
    final static String[] MULTIPLE = new String[] { "Hello", "World", "From", "Downunder" };

    /**
     * Ensure prepending arrays produces the expected result.
     */
    @Test
    void shouldPrependArrays() {

        assertThat(Arrays.prepend(NULL, "Hello"))
            .containsExactly("Hello");

        assertThat(Arrays.prepend(EMPTY, "Hello"))
            .containsExactly("Hello");

        assertThat(Arrays.prepend(SINGLE))
            .containsExactly("Hello");

        assertThat(Arrays.prepend(SINGLE, (Object[]) NULL))
            .containsExactly("Hello");

        assertThat(Arrays.prepend(SINGLE, (Object[]) SINGLE))
            .containsExactly("Hello", "Hello");

        assertThat(Arrays.prepend(SINGLE, (Object[]) DUAL))
            .containsExactly("Hello", "World", "Hello");

        assertThat(Arrays.prepend(DUAL, (Object[]) SINGLE))
            .containsExactly("Hello", "Hello", "World");

        assertThat(Arrays.prepend(MULTIPLE, (Object[]) MULTIPLE))
            .containsExactly("Hello", "World", "From", "Downunder", "Hello", "World", "From", "Downunder");
    }
}
