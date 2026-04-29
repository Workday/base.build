package build.base.parsing;

import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link Scanner} backtracking support via {@link ScannerInput}.
 */
class ScannerBacktrackingTests {

    @Test
    void scannerFromStringSupportsBacktracking() {
        assertThat(new Scanner("abc").supportsBacktracking()).isTrue();
    }

    @Test
    void scannerFromReaderDoesNotSupportBacktracking() {
        assertThat(new Scanner(new StringReader("abc")).supportsBacktracking()).isFalse();
    }

    @Test
    void saveAndRestoreReturnsToPriorPosition() {
        final var scanner = new Scanner("hello world");

        final int checkpoint = scanner.save();
        scanner.consume("hello");

        assertThat(scanner.follows(" world")).isTrue();

        scanner.restore(checkpoint);

        assertThat(scanner.follows("hello")).isTrue();
    }

    @Test
    void saveOnNonBacktrackingScannerThrows() {
        final var scanner = new Scanner(new StringReader("x"));
        assertThatThrownBy(scanner::save)
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void restoreOnNonBacktrackingScannerThrows() {
        final var scanner = new Scanner(new StringReader("x"));
        assertThatThrownBy(() -> scanner.restore(0))
            .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void saveAndRestoreWorksWithFilters() {
        final var scanner = new Scanner("  hello  world")
            .register(Filter.WHITESPACE);

        final int checkpoint = scanner.save();
        scanner.consume("hello");

        assertThat(scanner.follows("world")).isTrue();

        scanner.restore(checkpoint);

        assertThat(scanner.follows("hello")).isTrue();
    }
}
