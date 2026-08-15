package build.base.telemetry;

/*-
 * #%L
 * base.build Telemetry
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

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link Progress}.
 */
class ProgressTests {

    private static final URI URI = java.net.URI.create("test://uri");

    /**
     * Verify {@link Progress#toString()} formats the percentage to a single decimal place,
     * rather than the (much longer) default {@code double} representation.
     */
    @Test
    void toStringShouldFormatPercentageToOneDecimalPlace() {
        final var progress = Progress.create(URI, 1, 3, "task");

        final var text = progress.toString();

        assertThat(progress.percentage())
            .isEqualTo(33.33333333333333);

        assertThat(text)
            .contains("(33.3%)")
            .doesNotContain("33.33333333333333");
    }

    /**
     * Verify a whole-number percentage still renders with a single trailing decimal digit.
     */
    @Test
    void toStringShouldFormatWholePercentageWithTrailingDecimal() {
        final var progress = Progress.create(URI, 1, 2, "task");

        final var text = progress.toString();

        assertThat(text)
            .contains("(50.0%)");
    }
}
