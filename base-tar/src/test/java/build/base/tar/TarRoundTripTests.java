package build.base.tar;

/*-
 * #%L
 * base.build Tar
 * %%
 * Copyright (C) 2026 Workday Inc
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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * End-to-end tests that write entries with {@link TarOutputStream} and read them back with
 * {@link TarInputStream}, exercising every case that requires the two to agree on wire format:
 * plain entries, symlinks, oversized names/link targets that force a PAX extended header, and
 * non-ASCII names. These are the tests that would have caught {@link TarOutputStream} throwing
 * on an entry that {@link TarInputStream} can otherwise read from third-party archives.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class TarRoundTripTests {

    private static final long MOD_TIME = 0L;

    @Test
    void shouldRoundTripRegularFile() throws IOException {
        final var content = "hello, tar\n".getBytes(StandardCharsets.UTF_8);
        final var header = TarHeader.createHeader("hello.txt", content.length, MOD_TIME, false, 0644);

        final var entry = roundTrip(header, content);

        assertThat(entry.header().name()).isEqualTo("hello.txt");
        assertThat(entry.header().directory()).isFalse();
        assertThat(entry.content()).isEqualTo(content);
    }

    @Test
    void shouldRoundTripDirectory() throws IOException {
        final var header = TarHeader.createHeader("mydir/", 0, MOD_TIME, true, 0755);

        final var entry = roundTrip(header, new byte[0]);

        assertThat(entry.header().name()).isEqualTo("mydir/");
        assertThat(entry.header().directory()).isTrue();
    }

    @Test
    void shouldRoundTripShortSymlink() throws IOException {
        final var header = TarHeader.createSymlinkHeader("link.txt", "target.txt", MOD_TIME, 0777);

        final var entry = roundTrip(header, new byte[0]);

        assertThat(entry.header().name()).isEqualTo("link.txt");
        assertThat(entry.header().linkName()).isEqualTo("target.txt");
    }

    @Test
    void shouldRoundTripSparseFileWithFewChunks() throws IOException {
        final var chunks = List.of(new TarHeader.SparseChunk(0, 5), new TarHeader.SparseChunk(1000, 6));
        final var header = TarHeader.createSparseHeader("sparse.bin", 2000, chunks, MOD_TIME, 0644);
        final var data = new byte[11];
        System.arraycopy("first".getBytes(StandardCharsets.UTF_8), 0, data, 0, 5);
        System.arraycopy("second".getBytes(StandardCharsets.UTF_8), 0, data, 5, 6);

        final var entry = roundTrip(header, data);

        assertThat(entry.header().name()).isEqualTo("sparse.bin");
        assertThat(entry.header().size()).isEqualTo(2000);
        assertThat(entry.header().sparseChunks()).isEqualTo(chunks);
        assertThat(entry.content()).hasSize(2000);
        assertThat(new String(entry.content(), 0, 5, StandardCharsets.UTF_8)).isEqualTo("first");
        assertThat(new String(entry.content(), 1000, 6, StandardCharsets.UTF_8)).isEqualTo("second");
        // everything outside the declared chunks must reconstruct as zero-filled holes
        assertThat(entry.content()[10]).isZero();
        assertThat(entry.content()[1999]).isZero();
    }

    @Test
    void shouldRoundTripSparseFileWithManyChunksRequiringExtensionBlocks() throws IOException {
        final var chunks = new ArrayList<TarHeader.SparseChunk>();
        final var buffer = new ByteArrayOutputStream();
        for (int i = 0; i < 30; i++) {
            final var chunkData = ("chunk" + i).getBytes(StandardCharsets.UTF_8);
            chunks.add(new TarHeader.SparseChunk(i * 1000L, chunkData.length));
            buffer.writeBytes(chunkData);
        }
        final var header = TarHeader.createSparseHeader("sparse.bin", 30_000, chunks, MOD_TIME, 0644);

        final var entry = roundTrip(header, buffer.toByteArray());

        assertThat(entry.header().sparseChunks()).isEqualTo(chunks);
        assertThat(entry.content()).hasSize(30_000);
        for (int i = 0; i < 30; i++) {
            final var chunkData = ("chunk" + i).getBytes(StandardCharsets.UTF_8);
            final var offset = i * 1000;
            assertThat(new String(entry.content(), offset, chunkData.length, StandardCharsets.UTF_8))
                .isEqualTo("chunk" + i);
        }
        assertThat(entry.content()[9]).isZero();
    }

    @Test
    void shouldProduceGnuSparseArchiveReadableByRealTar(@TempDir final Path tempDir) throws IOException, InterruptedException {
        // GNU tar's own sparse extractor only reliably reconstructs chunks aligned to a
        // filesystem block boundary (as any real sparse-file detector, e.g. SEEK_HOLE/SEEK_DATA,
        // would naturally produce); arbitrary byte-level offsets it did not write itself can
        // extract incorrectly. Use 4096-byte-aligned chunks here to match real-world usage.
        final var blockSize = 4096;
        final var chunks = List.of(new TarHeader.SparseChunk(0, blockSize), new TarHeader.SparseChunk(98_304, blockSize));
        final var header = TarHeader.createSparseHeader("sparse.bin", 200_000, chunks, MOD_TIME, 0644);
        final var data = new byte[2 * blockSize];
        System.arraycopy("first".getBytes(StandardCharsets.UTF_8), 0, data, 0, 5);
        System.arraycopy("second".getBytes(StandardCharsets.UTF_8), 0, data, blockSize + (100_000 - 98_304), 6);

        final var archivePath = tempDir.resolve("sparse.tar");
        try (final var tar = new TarOutputStream(Files.newOutputStream(archivePath))) {
            tar.putNextEntry(new TarEntry(header));
            tar.write(data);
        }

        final var extractDir = tempDir.resolve("extracted");
        Files.createDirectories(extractDir);
        final var result = new ProcessBuilder("tar", "-xf", archivePath.toString(), "-C", extractDir.toString())
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        final var extracted = Files.readAllBytes(extractDir.resolve("sparse.bin"));
        assertThat(extracted).hasSize(200_000);
        assertThat(new String(extracted, 0, 5, StandardCharsets.UTF_8)).isEqualTo("first");
        assertThat(new String(extracted, 100_000, 6, StandardCharsets.UTF_8)).isEqualTo("second");
        assertThat(extracted[199_999]).isZero();
    }

    @Test
    void shouldEmitPaxSizeUidGidRecordsAlongsideLongName() throws IOException {
        final var longName = "a".repeat(150) + ".txt";
        final var header = TarHeader.createHeader(longName, 0, MOD_TIME, false, 0644, 99_999_999, 1, "big", "staff");

        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            tar.putNextEntry(new TarEntry(header));
        }

        // the PAX extended header data block immediately follows the PAX header's own 512-byte block
        final var paxData = new String(archive.toByteArray(), 512, 300, StandardCharsets.UTF_8);
        assertThat(paxData).contains("uid=99999999");

        final var entry = roundTrip(header, new byte[0]);
        assertThat(entry.header().name()).isEqualTo(longName);
        assertThat(entry.header().uid()).isEqualTo(99_999_999);
    }

    @Test
    void shouldRoundTripHardlink() throws IOException {
        final var header = TarHeader.createHardlinkHeader("link.txt", "target.txt", MOD_TIME, 0644);

        final var entry = roundTrip(header, new byte[0]);

        assertThat(entry.header().name()).isEqualTo("link.txt");
        assertThat(entry.header().linkName()).isEqualTo("target.txt");
        assertThat(entry.header().hardlink()).isTrue();
        assertThat(entry.header().directory()).isFalse();
    }

    @Test
    void shouldRoundTripSymlinkWithLongTargetViaPax() throws IOException {
        final var longTarget = "b".repeat(150) + ".txt";
        final var header = TarHeader.createSymlinkHeader("link.txt", longTarget, MOD_TIME, 0777);

        final var entry = roundTrip(header, new byte[0]);

        assertThat(entry.header().name()).isEqualTo("link.txt");
        assertThat(entry.header().linkName()).isEqualTo(longTarget);
    }

    @Test
    void shouldRoundTripUnsplittableLongNameViaPax() throws IOException {
        // a single path component over 100 bytes with no '/' cannot use the ustar prefix
        // field, so this must go out as a PAX 'path' record instead of throwing
        final var longName = "a".repeat(150) + ".txt";
        final var content = "content".getBytes(StandardCharsets.UTF_8);
        final var header = TarHeader.createHeader(longName, content.length, MOD_TIME, false, 0644);

        final var entry = roundTrip(header, content);

        assertThat(entry.header().name()).isEqualTo(longName);
        assertThat(entry.content()).isEqualTo(content);
    }

    @Test
    void shouldRoundTripSplittableLongNameViaUstarPrefix() throws IOException {
        final var longName = "a".repeat(60) + "/" + "b".repeat(60) + ".txt";
        final var content = "content".getBytes(StandardCharsets.UTF_8);
        final var header = TarHeader.createHeader(longName, content.length, MOD_TIME, false, 0644);

        final var entry = roundTrip(header, content);

        assertThat(entry.header().name()).isEqualTo(longName);
        assertThat(entry.content()).isEqualTo(content);
    }

    @Test
    void shouldRoundTripNameUnder100CharsButOver100Utf8Bytes() throws IOException {
        // 90 CJK characters: <= 100 UTF-16 chars, but 270 UTF-8 bytes. Without a byte-length
        // aware PAX-extension check, TarOutputStream silently truncates this to a corrupted
        // 100-byte name instead of promoting it to a PAX extended header.
        final var name = "中".repeat(90);
        final var content = "content".getBytes(StandardCharsets.UTF_8);
        final var header = TarHeader.createHeader(name, content.length, MOD_TIME, false, 0644);

        final var entry = roundTrip(header, content);

        assertThat(entry.header().name()).isEqualTo(name);
        assertThat(entry.content()).isEqualTo(content);
    }

    @Test
    void shouldRoundTripSymlinkTargetUnder100CharsButOver100Utf8Bytes() throws IOException {
        final var target = "中".repeat(90);
        final var header = TarHeader.createSymlinkHeader("link.txt", target, MOD_TIME, 0777);

        final var entry = roundTrip(header, new byte[0]);

        assertThat(entry.header().linkName()).isEqualTo(target);
    }

    @Test
    void shouldRoundTripNonAsciiName() throws IOException {
        final var name = "café-ü.txt";
        final var content = "content".getBytes(StandardCharsets.UTF_8);
        final var header = TarHeader.createHeader(name, content.length, MOD_TIME, false, 0644);

        final var entry = roundTrip(header, content);

        assertThat(entry.header().name()).isEqualTo(name);
        assertThat(entry.content()).isEqualTo(content);
    }

    @Test
    void shouldRoundTripOwnership() throws IOException {
        final var header = TarHeader.createHeader("owned.txt", 0, MOD_TIME, false, 0644, 1001, 1002, "alice", "staff");

        final var entry = roundTrip(header, new byte[0]);

        assertThat(entry.header().uid()).isEqualTo(1001);
        assertThat(entry.header().gid()).isEqualTo(1002);
        assertThat(entry.header().userName()).isEqualTo("alice");
        assertThat(entry.header().groupName()).isEqualTo("staff");
    }

    @Test
    void shouldRoundTripEntryWithBothLongNameAndLongLinkTarget() throws IOException {
        final var longName = "a".repeat(150) + ".txt";
        final var longTarget = "b".repeat(150) + ".txt";
        final var header = TarHeader.createSymlinkHeader(longName, longTarget, MOD_TIME, 0777);

        final var entry = roundTrip(header, new byte[0]);

        assertThat(entry.header().name()).isEqualTo(longName);
        assertThat(entry.header().linkName()).isEqualTo(longTarget);
    }

    @Test
    void shouldRoundTripMultipleEntriesMixingPlainAndPaxNames() throws IOException {
        final var longName = "a".repeat(150) + ".txt";
        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            tar.putNextEntry(new TarEntry(TarHeader.createHeader("plain.txt", 5, MOD_TIME, false, 0644)));
            tar.write("first".getBytes(StandardCharsets.UTF_8));
            tar.putNextEntry(new TarEntry(TarHeader.createHeader(longName, 6, MOD_TIME, false, 0644)));
            tar.write("second".getBytes(StandardCharsets.UTF_8));
            tar.putNextEntry(new TarEntry(TarHeader.createHeader("plain2.txt", 5, MOD_TIME, false, 0644)));
            tar.write("third".getBytes(StandardCharsets.UTF_8));
        }

        try (final var tar = new TarInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
            final var first = tar.getNextEntry();
            assertThat(first.header().name()).isEqualTo("plain.txt");
            assertThat(new String(tar.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("first");

            final var second = tar.getNextEntry();
            assertThat(second.header().name()).isEqualTo(longName);
            assertThat(new String(tar.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("second");

            final var third = tar.getNextEntry();
            assertThat(third.header().name()).isEqualTo("plain2.txt");
            assertThat(new String(tar.readAllBytes(), StandardCharsets.UTF_8)).isEqualTo("third");

            assertThat(tar.getNextEntry()).isNull();
        }
    }

    @Test
    void shouldProducePaxArchiveReadableByRealTar(@TempDir final Path tempDir) throws IOException, InterruptedException {
        // a single path component over 100 bytes with no '/', forcing our writer to emit
        // a PAX extended header rather than a ustar prefix split
        final var longName = "a".repeat(150) + ".txt";
        final var content = "written by our TarOutputStream".getBytes(StandardCharsets.UTF_8);

        final var archivePath = tempDir.resolve("ours.tar");
        try (final var tar = new TarOutputStream(Files.newOutputStream(archivePath))) {
            tar.putNextEntry(new TarEntry(TarHeader.createHeader(longName, content.length, MOD_TIME, false, 0644)));
            tar.write(content);
        }

        final var extractDir = tempDir.resolve("extracted");
        Files.createDirectories(extractDir);
        final var result = new ProcessBuilder("tar", "-xf", archivePath.toString(), "-C", extractDir.toString())
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        assertThat(Files.readString(extractDir.resolve(longName))).isEqualTo(new String(content, StandardCharsets.UTF_8));
    }

    private static RoundTrippedEntry roundTrip(final TarHeader header, final byte[] content) throws IOException {
        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            tar.putNextEntry(new TarEntry(header));
            tar.write(content);
        }

        try (final var tar = new TarInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
            final var entry = tar.getNextEntry();
            assertThat(entry).isNotNull();
            final var readContent = tar.readAllBytes();
            assertThat(tar.getNextEntry()).isNull();
            return new RoundTrippedEntry(entry.header(), readContent);
        }
    }

    private record RoundTrippedEntry(TarHeader header, byte[] content) {
    }
}
