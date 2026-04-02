package build.base.io;

import build.base.assertion.Eventually;
import build.base.assertion.IteratorAssert;
import build.base.flow.RecordingSubscriber;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Pipe}s
 *
 * @author brian.oliver
 * @since Jul-2021
 */
class PipeTests {

    /**
     * Ensure a {@link Pipe} completes when using a closed {@link Reader}.
     */
    @Test
    void shouldCreatePipeForClosedReader() {

        // establish and close the Reader immediately
        final var reader = new StringReader("");
        reader.close();

        // attempt to create the Pipe using the closed Reader
        final var pipe = new Pipe(reader, new OutputStreamWriter(System.out));

        // attempt to open the Pipe
        final var future = pipe.open();

        Eventually.assertThat(future)
            .isCompleted();
    }

    /**
     * Ensure a {@link Pipe} completes when using a closed {@link Writer}.
     *
     * @throws IOException should it not be possible to create the {@link Pipe}
     */
    @Test
    void shouldCreatePipeForClosedWriter()
        throws IOException {

        // a simple Reader from which we can read some lines of content
        final var reader = new StringReader("First Line\nSecond Line\nLast Line");

        // establish and close the Writer immediately
        final var writer = new StringWriter();
        writer.close();

        // attempt to create the Pipe using the closed Writer
        final var pipe = new Pipe(reader, writer);

        // attempt to open the Pipe
        final var future = pipe.open();

        Eventually.assertThat(future)
            .isCompleted();
    }

    /**
     * Ensure a {@link Pipe} can pipe from a {@link Reader} to a {@link Writer}.
     */
    @Test
    void shouldPipeFromReaderToWriter() {

        // a simple Reader from which we can read some lines of content
        final var content = "First Line\nSecond Line\nLast Line\n";
        final var reader = new StringReader(content);

        // establish a StringWriter to receive the content
        final var writer = new StringWriter();

        // establish the Pipe
        final var pipe = new Pipe(reader, writer);

        // attempt to open the Pipe
        final var future = pipe.open();

        Eventually.assertThat(future)
            .isCompleted();

        // ensure the content is the same
        assertThat(writer.toString())
            .isEqualTo(content);
    }

    /**
     * Ensure a {@link Pipe} can be observed.
     */
    @Test
    void shouldObserveReadingFromPipe() {

        // a simple Reader from which we can read some lines of content
        final var content = "First Line\nSecond Line\nLast Line\n";
        final var reader = new StringReader(content);

        final var subscriber = new RecordingSubscriber<String>();

        // establish the Pipe
        final var pipe = new Pipe(reader, NullWriter.get())
            .subscribe(subscriber);

        // attempt to open the Pipe
        final var future = pipe.open();

        Eventually.assertThat(future)
            .isCompleted();

        // ensure the content is as expected
        IteratorAssert.assertThat(subscriber.iterator())
            .starts()
            .then().matches("First Line")
            .then().matches("Second Line")
            .then().matches("Last Line")
            .then().ends()
            .isTrue();
    }

    /**
     * Ensure a {@link Pipe} can transform from a {@link Reader} to a {@link Writer}.
     */
    @Test
    void shouldPipeAndTransformFromReaderToWriter() {

        // a simple Reader from which we can read some lines of content
        final var content = "First Line\nSecond Line\nLast Line\n";
        final var reader = new StringReader(content);

        // establish a StringWriter to receive the content
        final var writer = new StringWriter();

        // establish the Pipe
        final var pipe = new Pipe(reader, writer)
            .setTransformer(String::toUpperCase);

        // attempt to open the Pipe
        final var future = pipe.open();

        Eventually.assertThat(future)
            .isCompleted();

        // ensure the content is the same
        assertThat(writer.toString())
            .isEqualTo(content.toUpperCase());
    }

    /**
     * Ensure a {@link Pipe} can observe and transform from a {@link Reader} to a {@link Writer}.
     */
    @Test
    void shouldPipeObserveAndTransformFromReaderToWriter() {
        // a simple Reader from which we can read some lines of content
        final var content = "First Line\nSecond Line\nLast Line\n";
        final var reader = new StringReader(content);

        // establish a StringWriter to receive the content
        final var writer = new StringWriter();

        // establish the Observer to observe untransformed content
        final var recordingSubscriber = new RecordingSubscriber<String>();

        // establish the Pipe
        final var pipe = new Pipe(reader, writer)
            .setTransformer(String::toUpperCase)
            .subscribe(recordingSubscriber);

        // attempt to open the Pipe
        final var future = pipe.open();

        Eventually.assertThat(future)
            .isCompleted();

        // ensure the content is observed as being untransformed
        IteratorAssert.assertThat(recordingSubscriber.iterator())
            .starts()
            .then().matches("First Line")
            .then().matches("Second Line")
            .then().matches("Last Line")
            .then().ends()
            .isTrue();

        // ensure the content was transformed
        assertThat(writer.toString())
            .isEqualTo(content.toUpperCase());
    }
}
