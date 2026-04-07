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
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TarOutputStream}.
 *
 * @author reed.vonredwitz
 * @since Apr-2026
 */
class TarOutputStreamTests {

    private static final long MOD_TIME = 0L;

    @Test
    void shouldWriteTwoEofZeroBlocksOnClose() throws IOException {
        final var out = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(out)) {
            // empty archive — no entries
        }

        final var bytes = out.toByteArray();
        assertThat(bytes).hasSize(1024);
        for (final byte b : bytes) {
            assertThat(b).isEqualTo((byte) 0);
        }
    }

    @Test
    void shouldPadEntryDataToBlockBoundary() throws IOException {
        // 100 bytes of data: not aligned to 512, so padded to 512
        final var data = new byte[100];
        final var out = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(out)) {
            final var header = TarHeader.createHeader("file.bin", data.length, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(header));
            tar.write(data);
        }

        // 512 (header) + 512 (padded data) + 1024 (eof) = 2048
        assertThat(out.toByteArray()).hasSize(2048);
    }

    @Test
    void shouldNotDoublePadExactBlockSizeEntry() throws IOException {
        // 512 bytes of data: already aligned, no extra padding block needed
        final var data = new byte[512];
        final var out = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(out)) {
            final var header = TarHeader.createHeader("file.bin", data.length, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(header));
            tar.write(data);
        }

        // 512 (header) + 512 (data, already aligned) + 1024 (eof) = 2048
        assertThat(out.toByteArray()).hasSize(2048);
    }

    @Test
    void shouldPadAcrossBlockBoundary() throws IOException {
        // 513 bytes: one full block + 1 byte, padded to 1024
        final var data = new byte[513];
        final var out = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(out)) {
            final var header = TarHeader.createHeader("file.bin", data.length, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(header));
            tar.write(data);
        }

        // 512 (header) + 1024 (padded data) + 1024 (eof) = 2560
        assertThat(out.toByteArray()).hasSize(2560);
    }

    @Test
    void shouldProduceArchiveThatCanBeExtracted(@TempDir final Path tempDir)
        throws IOException, InterruptedException {

        final var content = "hello, tar\n";
        final var archivePath = tempDir.resolve("test.tar");

        try (final var tar = new TarOutputStream(Files.newOutputStream(archivePath))) {
            final var bytes = content.getBytes(StandardCharsets.UTF_8);
            final var header = TarHeader.createHeader("hello.txt", bytes.length, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(header));
            tar.write(bytes);
        }

        final var extractDir = tempDir.resolve("out");
        Files.createDirectories(extractDir);

        final var result = new ProcessBuilder("tar", "-xf", archivePath.toString(), "-C", extractDir.toString())
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);
        assertThat(Files.readString(extractDir.resolve("hello.txt"))).isEqualTo(content);
    }

    @Test
    void shouldProduceArchiveWithDirectoryEntry(@TempDir final Path tempDir)
        throws IOException, InterruptedException {

        final var content = "nested content\n";
        final var archivePath = tempDir.resolve("test.tar");

        try (final var tar = new TarOutputStream(Files.newOutputStream(archivePath))) {
            final var dirHeader = TarHeader.createHeader("mydir/", 0, MOD_TIME, true, 0755);
            tar.putNextEntry(new TarEntry(dirHeader));

            final var bytes = content.getBytes(StandardCharsets.UTF_8);
            final var fileHeader = TarHeader.createHeader("mydir/file.txt", bytes.length, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(fileHeader));
            tar.write(bytes);
        }

        final var extractDir = tempDir.resolve("out");
        Files.createDirectories(extractDir);

        final var result = new ProcessBuilder("tar", "-xf", archivePath.toString(), "-C", extractDir.toString())
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);
        assertThat(Files.isDirectory(extractDir.resolve("mydir"))).isTrue();
        assertThat(Files.readString(extractDir.resolve("mydir/file.txt"))).isEqualTo(content);
    }

    @Test
    void shouldProduceArchiveWithMultipleEntries(@TempDir final Path tempDir)
        throws IOException, InterruptedException {

        final var archivePath = tempDir.resolve("test.tar");

        try (final var tar = new TarOutputStream(Files.newOutputStream(archivePath))) {
            for (int i = 1; i <= 3; i++) {
                final var bytes = ("content" + i).getBytes(StandardCharsets.UTF_8);
                final var header = TarHeader.createHeader("file" + i + ".txt", bytes.length, MOD_TIME, false, 0644);
                tar.putNextEntry(new TarEntry(header));
                tar.write(bytes);
            }
        }

        final var extractDir = tempDir.resolve("out");
        Files.createDirectories(extractDir);

        final var result = new ProcessBuilder("tar", "-xf", archivePath.toString(), "-C", extractDir.toString())
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);
        assertThat(Files.readString(extractDir.resolve("file1.txt"))).isEqualTo("content1");
        assertThat(Files.readString(extractDir.resolve("file2.txt"))).isEqualTo("content2");
        assertThat(Files.readString(extractDir.resolve("file3.txt"))).isEqualTo("content3");
    }
}
