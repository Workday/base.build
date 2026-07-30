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

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TarInputStream}.
 *
 * @author reed.vonredwitz
 * @since Jul-2026
 */
class TarInputStreamTests {

    private static final long MOD_TIME = 0L;

    /**
     * macOS ships bsdtar as {@code tar}, which doesn't support the GNU {@code --sparse}
     * flag this test relies on. Look for a real GNU tar under its usual names, preferring
     * whatever {@code tar} already resolves to before falling back to {@code gtar}
     * (the name Homebrew's {@code gnu-tar} installs under).
     */
    private static String findGnuTarBinary() {
        for (final var candidate : new String[] {"tar", "gtar"}) {
            try {
                final var process = new ProcessBuilder(candidate, "--version").redirectErrorStream(true).start();
                final var output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
                if (process.waitFor() == 0 && output.contains("GNU tar")) {
                    return candidate;
                }
            } catch (IOException | InterruptedException ignored) {
                // candidate not on PATH — try the next one
            }
        }
        return null;
    }

    @Test
    void shouldReturnNullForEmptyArchive() throws IOException {
        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            // empty archive — no entries
        }

        try (final var tar = new TarInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
            assertThat(tar.getNextEntry()).isNull();
        }
    }

    @Test
    void shouldReadSingleFileEntry() throws IOException {
        final var content = "hello, tar\n".getBytes(StandardCharsets.UTF_8);
        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            final var header = TarHeader.createHeader("hello.txt", content.length, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(header));
            tar.write(content);
        }

        try (final var tar = new TarInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
            final var entry = tar.getNextEntry();
            assertThat(entry).isNotNull();
            assertThat(entry.header().name()).isEqualTo("hello.txt");
            assertThat(entry.header().size()).isEqualTo(content.length);
            assertThat(entry.header().directory()).isFalse();
            assertThat(entry.header().permissions()).isEqualTo(0644);

            assertThat(tar.readAllBytes()).isEqualTo(content);
            assertThat(tar.getNextEntry()).isNull();
        }
    }

    @Test
    void shouldReadDirectoryEntry() throws IOException {
        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            final var header = TarHeader.createHeader("mydir/", 0, MOD_TIME, true, 0755);
            tar.putNextEntry(new TarEntry(header));
        }

        try (final var tar = new TarInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
            final var entry = tar.getNextEntry();
            assertThat(entry).isNotNull();
            assertThat(entry.header().name()).isEqualTo("mydir/");
            assertThat(entry.header().directory()).isTrue();
            assertThat(entry.header().size()).isEqualTo(0);
        }
    }

    @Test
    void shouldReadMultipleEntriesSkippingUnreadData() throws IOException {
        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            for (int i = 1; i <= 3; i++) {
                final var bytes = ("content" + i).getBytes(StandardCharsets.UTF_8);
                final var header = TarHeader.createHeader("file" + i + ".txt", bytes.length, MOD_TIME, false, 0644);
                tar.putNextEntry(new TarEntry(header));
                tar.write(bytes);
            }
        }

        try (final var tar = new TarInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
            for (int i = 1; i <= 3; i++) {
                final var entry = tar.getNextEntry();
                assertThat(entry.header().name()).isEqualTo("file" + i + ".txt");
                // deliberately not reading the entry data — next call must skip it
            }
            assertThat(tar.getNextEntry()).isNull();
        }
    }

    @Test
    void shouldReadEntryDataAcrossBlockBoundary() throws IOException {
        final var data = new byte[513];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) (i % 256);
        }

        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            final var header = TarHeader.createHeader("file.bin", data.length, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(header));
            tar.write(data);
        }

        try (final var tar = new TarInputStream(new ByteArrayInputStream(archive.toByteArray()))) {
            final var entry = tar.getNextEntry();
            assertThat(entry.header().size()).isEqualTo(data.length);
            assertThat(tar.readAllBytes()).isEqualTo(data);
        }
    }

    @Test
    void shouldRoundTripArchiveProducedByGnuTar(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        Files.writeString(sourceDir.resolve("a.txt"), "alpha");
        Files.writeString(sourceDir.resolve("b.txt"), "beta");

        final var archivePath = tempDir.resolve("test.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        final var names = new HashMap<String, String>();
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (!entry.header().directory()) {
                    names.put(entry.header().name(), new String(tar.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }

        assertThat(names).containsEntry("./a.txt", "alpha");
        assertThat(names).containsEntry("./b.txt", "beta");
    }

    @Test
    void shouldReadGnuLongNameEntry(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        final var longName = "a".repeat(60) + "/" + "b".repeat(60) + ".txt";
        final var longFile = sourceDir.resolve(longName);
        Files.createDirectories(longFile.getParent());
        Files.writeString(longFile, "long name content");

        final var archivePath = tempDir.resolve("longname.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        final var names = new HashMap<String, String>();
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (!entry.header().directory()) {
                    names.put(entry.header().name(), new String(tar.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }

        assertThat(names).containsEntry("./" + longName, "long name content");
    }

    @Test
    void shouldReadSymlinkTarget(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        Files.writeString(sourceDir.resolve("target.txt"), "target contents");
        Files.createSymbolicLink(sourceDir.resolve("link.txt"), Path.of("target.txt"));

        final var archivePath = tempDir.resolve("symlink.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        String linkTarget = null;
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.header().name().equals("./link.txt")) {
                    linkTarget = entry.header().linkName();
                }
            }
        }

        assertThat(linkTarget).isEqualTo("target.txt");
    }

    @Test
    void shouldReadHardlinkTarget(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        Files.writeString(sourceDir.resolve("target.txt"), "target contents");
        Files.createLink(sourceDir.resolve("link.txt"), sourceDir.resolve("target.txt"));

        final var archivePath = tempDir.resolve("hardlink.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        // GNU tar emits whichever of the two directory entries it visits second as a
        // hardlink pointing back at the first, so which name ends up as the hardlink
        // depends on directory traversal order rather than the names themselves.
        TarHeader hardlinkHeader = null;
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.header().hardlink()) {
                    hardlinkHeader = entry.header();
                }
            }
        }

        assertThat(hardlinkHeader).isNotNull();
        assertThat(hardlinkHeader.linkName()).isIn("./target.txt", "./link.txt");
    }

    @Test
    void shouldReadGnuSparseFile(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var gnuTar = findGnuTarBinary();
        Assumptions.assumeTrue(gnuTar != null, "no GNU tar binary found on PATH (macOS ships bsdtar as 'tar', "
            + "which doesn't support --sparse the way this test needs)");

        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        final var sparsePath = sourceDir.resolve("sparse.bin");
        try (final var raf = new java.io.RandomAccessFile(sparsePath.toFile(), "rw")) {
            raf.setLength(1_000_000);
            raf.seek(10_000);
            raf.write("hello".getBytes(StandardCharsets.UTF_8));
            raf.seek(900_000);
            raf.write("world".getBytes(StandardCharsets.UTF_8));
        }

        final var archivePath = tempDir.resolve("sparse.tar");
        final var result = new ProcessBuilder(gnuTar, "--sparse", "-cf", archivePath.toString(),
            "-C", sourceDir.toString(), "sparse.bin")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        TarHeader header = null;
        byte[] content = null;
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            final var entry = tar.getNextEntry();
            header = entry.header();
            content = tar.readAllBytes();
        }

        assertThat(header.name()).isEqualTo("sparse.bin");
        assertThat(header.size()).isEqualTo(1_000_000);
        assertThat(content).hasSize(1_000_000);
        assertThat(new String(content, 10_000, 5, StandardCharsets.UTF_8)).isEqualTo("hello");
        assertThat(new String(content, 900_000, 5, StandardCharsets.UTF_8)).isEqualTo("world");
        assertThat(content[0]).isZero();
        assertThat(content[999_999]).isZero();
    }

    @Test
    void shouldReadNonAsciiEntryName(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        final var name = "café-ü.txt";
        Files.writeString(sourceDir.resolve(name), "content");

        final var archivePath = tempDir.resolve("nonascii.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        final var names = new HashMap<String, String>();
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (!entry.header().directory()) {
                    names.put(entry.header().name(), new String(tar.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }

        assertThat(names).containsEntry("./" + name, "content");
    }

    @Test
    void shouldReadGnuLongNameEntryWithNonAsciiCharacters(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        // a single path component over 100 bytes with no '/' forces the GNU longname
        // extension (typeflag 'L'), rather than a ustar prefix/name split
        final var longName = "café-" + "a".repeat(150) + ".txt";
        Files.writeString(sourceDir.resolve(longName), "long name content");

        final var archivePath = tempDir.resolve("longname-nonascii.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        final var names = new HashMap<String, String>();
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (!entry.header().directory()) {
                    names.put(entry.header().name(), new String(tar.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }

        assertThat(names).containsEntry("./" + longName, "long name content");
    }

    @Test
    void shouldReadPaxLongNameEntry(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        // single path component over 100 bytes with no '/', forcing a long-name extension;
        // --format=posix makes tar use PAX ('x') headers rather than the GNU 'L' extension
        final var longName = "café-" + "a".repeat(150) + ".txt";
        Files.writeString(sourceDir.resolve(longName), "pax content");

        final var archivePath = tempDir.resolve("pax-longname.tar");
        final var result = new ProcessBuilder("tar", "--format=posix", "-cf", archivePath.toString(),
            "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        final var names = new HashMap<String, String>();
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (!entry.header().directory()) {
                    names.put(entry.header().name(), new String(tar.readAllBytes(), StandardCharsets.UTF_8));
                }
            }
        }

        assertThat(names).containsEntry("./" + longName, "pax content");
    }

    @Test
    void shouldReadGnuLongLinkSymlinkTarget(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        final var longTarget = "b".repeat(150) + ".txt";
        Files.createSymbolicLink(sourceDir.resolve("link.txt"), Path.of(longTarget));

        final var archivePath = tempDir.resolve("longlink.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        String linkTarget = null;
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.header().name().equals("./link.txt")) {
                    linkTarget = entry.header().linkName();
                }
            }
        }

        assertThat(linkTarget).isEqualTo(longTarget);
    }

    @Test
    void shouldReadPaxLongLinkSymlinkTarget(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        final var longTarget = "b".repeat(150) + ".txt";
        Files.createSymbolicLink(sourceDir.resolve("link.txt"), Path.of(longTarget));

        final var archivePath = tempDir.resolve("pax-longlink.tar");
        final var result = new ProcessBuilder("tar", "--format=posix", "-cf", archivePath.toString(),
            "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        String linkTarget = null;
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.header().name().equals("./link.txt")) {
                    linkTarget = entry.header().linkName();
                }
            }
        }

        assertThat(linkTarget).isEqualTo(longTarget);
    }

    @Test
    void shouldReadOwnershipFromRealArchive(@TempDir final Path tempDir) throws IOException, InterruptedException {
        final var sourceDir = tempDir.resolve("src");
        Files.createDirectories(sourceDir);
        Files.writeString(sourceDir.resolve("owned.txt"), "content");

        final var idProcess = new ProcessBuilder("id", "-u").start();
        assertThat(idProcess.waitFor()).isEqualTo(0);
        final var currentUid = Integer.parseInt(new String(idProcess.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim());

        final var archivePath = tempDir.resolve("owned.tar");
        final var result = new ProcessBuilder("tar", "-cf", archivePath.toString(), "-C", sourceDir.toString(), ".")
            .redirectErrorStream(true)
            .start();
        assertThat(result.waitFor()).isEqualTo(0);

        TarHeader ownedHeader = null;
        try (final var tar = new TarInputStream(Files.newInputStream(archivePath))) {
            TarEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                if (entry.header().name().equals("./owned.txt")) {
                    ownedHeader = entry.header();
                }
            }
        }

        assertThat(ownedHeader).isNotNull();
        assertThat(ownedHeader.uid()).isEqualTo(currentUid);
        assertThat(ownedHeader.userName()).isNotEmpty();
    }

    @Test
    void shouldThrowOnCorruptHeaderChecksum() throws IOException {
        final var archive = new ByteArrayOutputStream();
        try (final var tar = new TarOutputStream(archive)) {
            final var header = TarHeader.createHeader("file.txt", 5, MOD_TIME, false, 0644);
            tar.putNextEntry(new TarEntry(header));
            tar.write("hello".getBytes(StandardCharsets.UTF_8));
        }

        final var bytes = archive.toByteArray();
        // corrupt a byte within the name field, invalidating the checksum
        bytes[0] = 'X';

        try (final var tar = new TarInputStream(new ByteArrayInputStream(bytes))) {
            assertThatThrownBy(tar::getNextEntry).isInstanceOf(IllegalArgumentException.class);
        }
    }
}
