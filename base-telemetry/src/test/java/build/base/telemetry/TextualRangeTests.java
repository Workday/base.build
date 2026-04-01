package build.base.telemetry;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link TextualRange}.
 */
class TextualRangeTests {

    private static final URI DOC = URI.create("file:///test.txt");

    /**
     * Ensure {@link TextualRange#create} rejects a null {@code end} position.
     */
    @Test
    void createShouldRejectNullEnd() {
        final var start = TextualPosition.create(DOC, 1, 0);

        assertThatNullPointerException()
            .isThrownBy(() -> TextualRange.create(start, null))
            .withMessageContaining("end");
    }
}
