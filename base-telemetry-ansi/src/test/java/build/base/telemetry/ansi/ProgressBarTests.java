package build.base.telemetry.ansi;

/*-
 * #%L
 * base.build Telemetry (ANSI)
 * %%
 * Copyright (C) 2025 Workday Inc
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import build.base.telemetry.NamedUnit;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ProgressBar}.
 *
 * @author brian.oliver
 * @since Feb-2026
 */
class ProgressBarTests {

    /**
     * Ensure a long-running {@link ProgressBar} displays the estimated time to completion after three seconds have elapsed.
     */
    @Test
    void shouldShowEstimatedTimeToCompletionAfterThreeSecondsHaveElapsed()
        throws InterruptedException {

        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(100, "Test activity");

            Thread.sleep(Duration.ofSeconds(3));

            progressBar.progress(10);

            assertThat(progressBar.toString())
                .contains("elapsed")
                .contains("about")
                .contains("remaining");
        }
    }

    /**
     * Ensure the estimated time to completion of a long-running {@link ProgressBar} is updated as progress is made.
     */
    @Test
    void shouldUpdateEstimatedTimeToCompletionAsProgressIsMade()
        throws InterruptedException {

        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(100, "Test activity");

            Thread.sleep(Duration.ofSeconds(3));

            progressBar.progress(10);
            final var firstOutput = progressBar.toString();

            progressBar.progress(10);
            final var secondOutput = progressBar.toString();

            assertThat(firstOutput)
                .contains("elapsed")
                .contains("about")
                .contains("remaining");

            assertThat(secondOutput)
                .contains("elapsed")
                .contains("about")
                .contains("remaining");

            assertThat(firstOutput)
                .isNotEqualTo(secondOutput);
        }
    }

    /**
     * Ensure the line of a completed long-running {@link ProgressBar} is completely cleared to prevent rendering artifacts.
     */
    @Test
    void shouldClearLineWhenLongRunningProgressIsComplete()
        throws InterruptedException {

        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(100, "Test activity");

            Thread.sleep(Duration.ofSeconds(3));

            progressBar.progress(20);

            assertThat(progressBar.toString())
                .contains("elapsed")
                .contains("about")
                .contains("remaining");

            progressBar.progress(80);

            assertThat(progressBar.toString())
                .endsWith("\u001b[0K")
                .doesNotContain("elapsed")
                .doesNotContain("about");
        }
    }

    /**
     * Ensure the estimated time to completion is not shown when a {@link ProgressBar} has just started.
     */
    @Test
    void shouldNotShowEstimatedTimeToCompletionWhenJustStarted() {
        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(100, "Test activity");
            progressBar.progress(10);

            assertThat(progressBar.toString())
                .doesNotContain("about");
        }
    }

    /**
     * Ensure a {@link ProgressBar} includes the plural {@link NamedUnit} name in its output when progress is not one.
     */
    @Test
    void shouldIncludePluralUnitNameInOutputWhenProgressIsNotOne() {
        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(10, NamedUnit.of("item", "items"), "Processing");

            progressBar.progress(5);

            assertThat(progressBar.toString())
                .contains("5 items of 10 items completed");
        }
    }

    /**
     * Ensure a {@link ProgressBar} includes the singular {@link NamedUnit} name in its output when progress is one.
     */
    @Test
    void shouldIncludeSingularUnitNameInOutputWhenProgressIsOne() {
        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(10, NamedUnit.of("item", "items"), "Processing");

            progressBar.progress(1);

            assertThat(progressBar.toString())
                .contains("1 item of 10 items completed");
        }
    }

    /**
     * Ensure a {@link ProgressBar} omits the unit in its output when no {@link NamedUnit} is specified.
     */
    @Test
    void shouldOmitUnitInOutputWhenNoUnitIsSpecified() {
        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(10, "Processing");

            progressBar.progress(5);

            assertThat(progressBar.toString())
                .contains("(5 of 10 completed)")
                .doesNotContain("items");
        }
    }

    /**
     * Ensure a {@link ProgressBar} uses the same name for singular and plural when created with {@link NamedUnit#of(String)}.
     */
    @Test
    void shouldUseSameNameForSingularAndPluralWhenCreatedWithSingleName() {
        try (final var recorder = ANSIBasedTelemetryRecorder.create()) {
            final var progressBar = recorder.commence(100, NamedUnit.of("MB"), "Downloading");

            progressBar.progress(50);

            assertThat(progressBar.toString())
                .contains("50 MB of 100 MB completed");
        }
    }
}
