package build.base.foundation.stream;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Streamable}.
 *
 * @author brian.oliver
 * @since May-2025
 */
class StreamableTests {

    /**
     * Should create a variety of {@link Streamable}s.
     */
    @Test
    void shouldCreateStreamable() {
        // create an empty Streamable
        assertThat(Streamable.empty())
            .isEmpty();

        // create an empty Streamable from an empty Array
        assertThat(Streamable.of())
            .isEmpty();

        // create an empty Streamable from an empty Collection
        assertThat(Streamable.of(List.of()))
            .isEmpty();

        // create an empty Streamable from an empty Stream
        assertThat(Streamable.of(Stream.empty()))
            .isEmpty();

        // create a Streamable from an array
        var streamable = Streamable.of(1, 2, 3, 4, 5);
        assertThat(streamable)
            .containsExactly(1, 2, 3, 4, 5);

        // should iterate the same manner
        assertThat(streamable)
            .containsExactly(1, 2, 3, 4, 5);

        // create a Streamable from a collection
        streamable = Streamable.of(List.of(6, 7, 8));
        assertThat(streamable)
            .containsExactly(6, 7, 8);

        // create a Streamable from a stream
        streamable = Streamable.of(Stream.of(9, 10));
        assertThat(streamable)
            .containsExactly(9, 10);

        // create a Streamable from multiple Streamables
        streamable = Streamable.of(Streamable.of(1, 2), Streamable.of(3, 4));
        assertThat(streamable)
            .containsExactly(1, 2, 3, 4);
    }
}
