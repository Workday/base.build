package build.base.json;

/*-
 * #%L
 * base.build JSON
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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs the <a href="https://github.com/nst/JSONTestSuite">JSON Test Suite</a> against {@link Json#parse(String)}.
 * <p>
 * File naming convention:
 * <ul>
 *   <li>{@code y_} — must be accepted</li>
 *   <li>{@code n_} — must be rejected</li>
 *   <li>{@code i_} — implementation-defined; we accept either outcome</li>
 * </ul>
 */
class JsonTestSuiteTests {

    static Stream<Path> testFiles() {
        try {
            final var dir = Paths.get(JsonTestSuiteTests.class
                .getClassLoader()
                .getResource("JSONTestSuite/test_parsing")
                .toURI());
            return Files.list(dir).filter(p -> p.toString().endsWith(".json")).sorted();
        }
        catch (final URISyntaxException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("testFiles")
    void testParsing(final Path file) throws IOException {
        final var name  = file.getFileName().toString();
        final var bytes = Files.readAllBytes(file);

        if (name.startsWith("y_")) {
            assertThatCode(() -> Json.parse(bytes))
                .as("expected parse to succeed: %s", name)
                .doesNotThrowAnyException();
        }
        else if (name.startsWith("n_")) {
            assertThatThrownBy(() -> Json.parse(bytes))
                .as("expected parse to fail: %s", name)
                .isInstanceOf(JsonParseException.class);
        }
        // i_ files: no assertion — either outcome is acceptable
    }
}
