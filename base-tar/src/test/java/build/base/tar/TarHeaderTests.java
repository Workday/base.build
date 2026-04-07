package build.base.tar;

/*-
 * #%L
 * base.build Tar
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

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TarHeader}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class TarHeaderTests {

    @Test
    void shouldProduceExactly512Bytes() {
        final var header = TarHeader.createHeader("file.txt", 1024, 0, false, 0644);
        assertThat(header.toBytes()).hasSize(512);
    }

    @Test
    void shouldWriteEntryName() {
        final var header = TarHeader.createHeader("hello.txt", 0, 0, false, 0644);
        final var bytes = header.toBytes();
        final var name = new String(bytes, 0, 9, StandardCharsets.US_ASCII);
        assertThat(name).isEqualTo("hello.txt");
    }

    @Test
    void shouldWriteFileTypeflag() {
        final var header = TarHeader.createHeader("file.txt", 100, 0, false, 0644);
        assertThat(header.toBytes()[156]).isEqualTo((byte) '0');
    }

    @Test
    void shouldWriteDirectoryTypeflag() {
        final var header = TarHeader.createHeader("dir/", 0, 0, true, 0755);
        assertThat(header.toBytes()[156]).isEqualTo((byte) '5');
    }

    @Test
    void shouldWriteZeroSizeForDirectory() {
        // size passed to createHeader is ignored for directories
        final var header = TarHeader.createHeader("dir/", 999, 0, true, 0755);
        final var bytes = header.toBytes();
        final var sizeField = new String(bytes, 124, 11, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(sizeField, 8)).isEqualTo(0);
    }

    @Test
    void shouldWriteFileSizeInOctal() {
        final var header = TarHeader.createHeader("file.bin", 1024, 0, false, 0644);
        final var bytes = header.toBytes();
        final var sizeField = new String(bytes, 124, 11, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(sizeField, 8)).isEqualTo(1024);
    }

    @Test
    void shouldWriteUstarMagic() {
        final var header = TarHeader.createHeader("file.txt", 0, 0, false, 0644);
        final var bytes = header.toBytes();
        final var magic = new String(bytes, 257, 5, StandardCharsets.US_ASCII);
        assertThat(magic).isEqualTo("ustar");
    }

    @Test
    void shouldWriteUstarVersion() {
        final var header = TarHeader.createHeader("file.txt", 0, 0, false, 0644);
        final var bytes = header.toBytes();
        assertThat(bytes[263]).isEqualTo((byte) '0');
        assertThat(bytes[264]).isEqualTo((byte) '0');
    }

    @Test
    void shouldWriteNonZeroChecksum() {
        final var header = TarHeader.createHeader("file.txt", 1024, 0, false, 0644);
        final var bytes = header.toBytes();
        final var checksumField = new String(bytes, 148, 7, StandardCharsets.US_ASCII).trim();
        assertThat(Long.parseLong(checksumField, 8)).isGreaterThan(0);
    }

    @Test
    void shouldSplitLongNameIntoPrefixAndNameFields() {
        // 150-char name: 50-char prefix + 100-char name
        final var prefix = "a".repeat(50);
        final var name = "b".repeat(100);
        final var longName = prefix + name;

        final var header = TarHeader.createHeader(longName, 0, 0, false, 0644);
        final var bytes = header.toBytes();

        final var nameField = new String(bytes, 0, 100, StandardCharsets.US_ASCII).replace("\0", "");
        final var prefixField = new String(bytes, 345, 155, StandardCharsets.US_ASCII).replace("\0", "");

        assertThat(prefixField).isEqualTo(prefix);
        assertThat(nameField).isEqualTo(name);
    }

    @Test
    void shouldRejectNameThatIsTooLong() {
        // length > 255: splitIndex would exceed 155
        final var tooLong = "a".repeat(256);
        final var header = TarHeader.createHeader(tooLong, 0, 0, false, 0644);
        assertThatThrownBy(header::toBytes)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("too long");
    }
}
