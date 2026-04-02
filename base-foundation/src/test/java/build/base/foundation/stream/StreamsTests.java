package build.base.foundation.stream;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Streams}.
 *
 * @author brian.oliver
 * @since Jan-2020
 */
class StreamsTests {

    /**
     * Should reverse {@link java.util.stream.Stream}s.
     */
    @Test
    void shouldReverseStream() {

        assertThat(Streams.reverse(null))
            .isEmpty();

        assertThat(Streams.reverse(Stream.empty()))
            .isEmpty();

        assertThat(Streams.reverse(Stream.of(1, 2, 3, 4)))
            .containsExactly(4, 3, 2, 1);
    }

    /**
     * Should create {@link Stream} from {@link Iterable}.
     */
    @SuppressWarnings("unchecked")
    @Test
    void shouldCreateStreamFromIterable() {

        assertThat(Streams.stream(null))
            .isEmpty();

        assertThat(Streams.stream(Collections.EMPTY_LIST))
            .isEmpty();

        assertThat(Streams.stream(Arrays.asList(1, 2, 3, 4)))
            .containsExactly(1, 2, 3, 4);
    }

    /**
     * Should compare {@link Stream}s using {@link Streams#equals(Stream, Stream)}.
     */
    @Test
    void shouldCompareStreams() {
        assertThat(Streams.equals(Stream.empty(), Stream.empty()))
            .isTrue();

        assertThat(Streams.equals(Stream.of(1), Stream.of(1)))
            .isTrue();

        assertThat(Streams.equals(Stream.of(1, 2, 3), Stream.of(1, 2, 3)))
            .isTrue();

        assertThat(Streams.equals(Stream.empty(), Stream.of(1)))
            .isFalse();

        assertThat(Streams.equals(Stream.of(1), Stream.empty()))
            .isFalse();

        assertThat(Streams.equals(Stream.of(1), Stream.of(2)))
            .isFalse();

        assertThat(Streams.equals(Stream.of(1), Stream.of(1, 2)))
            .isFalse();

        assertThat(Streams.equals(Stream.of(1, 2), Stream.of(1)))
            .isFalse();
    }

    /**
     * Ensure multiple {@link Stream}s can be concatenated.
     */
    @Test
    void shouldConcatenateMultipleStreams() {
        assertThat(Streams.concat(null, null, null))
            .isEmpty();

        assertThat(Streams.concat(Stream.empty(), Stream.empty(), Stream.empty()))
            .isEmpty();

        assertThat(Streams.equals(
            Streams.concat(Stream.of(1, 2, 3), Stream.empty(), Stream.of(4, 5, 6)),
            Stream.of(1, 2, 3, 4, 5, 6)))
            .isTrue();
    }
}
