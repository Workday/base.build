package build.base.foundation.unit;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Tests for {@link MemorySize}.
 *
 * @author hadi.itani
 * @since Dec-2018
 */
class MemorySizeTests {

    /**
     * Ensure that an empty String is not accepted.
     */
    @Test
    void shouldNotAcceptEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> MemorySize.parse(""));
    }

    /**
     * Ensures that when a String is passed without a numerical prefix, the default value of the type is returned.
     */
    @Test
    void shouldParseMemorySizesWithoutExplicitValue() {
        for (var memorySize : MemorySize.values()) {
            final var abbreviation = memorySize.abbreviations()
                .findFirst()
                .orElseThrow();

            final var bytes = memorySize.bytes();

            assertThat(MemorySize.parse(abbreviation))
                .isEqualTo(bytes);
        }
    }

    /**
     * Ensures that {@link IllegalArgumentException} is thrown when erroneous input exists.
     */
    @Test
    void shouldNotParseInvalidMemorySizes() {
        final var list = new ArrayList<String>();
        for (var memorySize : MemorySize.values()) {
            final var abbreviation = memorySize.abbreviations()
                .findFirst()
                .orElseThrow();

            list.add(abbreviation + "1");
            list.add(abbreviation + "15");
            list.add("15" + abbreviation + "1");
            list.add("15" + abbreviation + "15");
            list.add("15" + memorySize.description());
        }

        for (var s : list) {
            try {
                MemorySize.parse(s);
                fail("Bad format: " + s);
            }
            catch (final IllegalArgumentException e) {
                // expected
            }
        }
    }

    /**
     * Ensures that generic parsing works as defined. Includes lack-of-suffix case.
     */
    @Test
    void shouldParseMemorySize() {

        assertThat(MemorySize.parse("15"))
            .isEqualTo(15);

        final var memorySizes = new ArrayList<MemorySize>();
        memorySizes.add(MemorySize.KiB);
        memorySizes.add(MemorySize.MiB);
        memorySizes.add(MemorySize.GiB);
        memorySizes.add(MemorySize.TiB);
        memorySizes.add(MemorySize.PiB);

        for (var memorySize : MemorySize.values()) {
            final var abbreviation = memorySize.abbreviations()
                .findFirst()
                .orElseThrow();

            final var bytes = memorySize.bytes();

            if (memorySizes.contains(memorySize)) {
                final var lowerChar = Character.toLowerCase(abbreviation.charAt(0));
                final var upperChar = Character.toUpperCase(abbreviation.charAt(0));

                assertThat(MemorySize.parse("15" + lowerChar))
                    .isEqualTo(15L * bytes);

                assertThat(MemorySize.parse("15" + upperChar))
                    .isEqualTo(15L * bytes);
            }

            assertThat(MemorySize.parse("15" + abbreviation))
                .isEqualTo(15L * bytes);
        }
    }
}
