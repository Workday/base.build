package build.base.logging;

import build.base.flow.LogRecordPublisher;
import build.base.flow.RecordingSubscriber;
import org.junit.jupiter.api.Test;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Logger}s.
 *
 * @author brian.oliver
 * @since Nov-2024
 */
class LoggerTests {

    /**
     * Ensure a {@link Logger} can be obtained for a {@link Class}.
     */
    @Test
    void shouldObtainLogger() {
        final var logger = Logger.get(LoggerTests.class);

        assertThat(logger)
            .isNotNull();

        assertThat(logger.logger())
            .isNotNull();
    }

    /**
     * Ensure a message can be logged using {@link Logger#info(String)}.
     */
    @Test
    void shouldLogInfoMessage() {
        final var logger = Logger.get(LoggerTests.class);
        logger.setLevel(Level.ALL);

        final var publisher = new LogRecordPublisher();
        logger.addHandler(publisher);

        final var subscriber = new RecordingSubscriber<LogRecord>();
        publisher.subscribe(subscriber);

        logger.info("Hello World");

        assertThat(subscriber.items()
            .map(LogRecord::getMessage)
            .findFirst())
            .contains("Hello World");
    }

    /**
     * Ensure an {@link Throwable} can be logged using {@link Logger#debug(Throwable, String, Object...)}.
     */
    @Test
    void shouldLogDebugThrowable() {
        final var logger = Logger.get(LoggerTests.class);
        logger.setLevel(Level.ALL);

        final var publisher = new LogRecordPublisher();
        logger.addHandler(publisher);

        final var subscriber = new RecordingSubscriber<LogRecord>();
        publisher.subscribe(subscriber);

        final var throwable = new RuntimeException();
        logger.debug(throwable, "Hello World");

        final var logRecord = subscriber.items()
            .findFirst()
            .orElseThrow();

        assertThat(logRecord.getMessage())
            .isEqualTo("Hello World");

        assertThat(logRecord.getThrown())
            .isSameAs(throwable);
    }
}
